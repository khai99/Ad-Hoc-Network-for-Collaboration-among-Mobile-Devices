package com.group10b.blueka;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.group10b.blueka.Sound.SntpClient;
import com.group10b.blueka.Sound.TimeOffset;
import com.group10b.blueka.bluetooth.BleScanner;
import com.group10b.blueka.bluetooth.ScanResultsConsumer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Main class
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, ScanResultsConsumer
{
    Button connectButton;
    private SeekBar seekBar;
    private TextView textView;
    //ble part
    private int numOfSeek;
    private boolean ble_scanning = false;
    private Handler handler = new Handler();
    private BleScanner ble_scanner;
    private static final long SCAN_TIMEOUT = 5000;
    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSION_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private boolean permission_granted = false;
    private int device_count =0;
    private Toast toast;
    private static MainActivity instance;
    public static String offset;

    // Sound Synchronization
    final SntpClient sntpClient = new SntpClient();
    TimeOffset timeOffset;
    TextView phoneConnected;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);

        //Create a scanner.
        ble_scanner = new BleScanner(this.getApplicationContext());
        phoneConnected = (TextView)findViewById(R.id.phone);

        //---------------------------OFFSET FOR SOUND SYNCHRONIZATION--------------------------
        try {
            new AsyncTask<Void, Integer, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    return sntpClient.requestTime("pool.ntp.org", 3000);
                }
                @Override
                protected void onPostExecute(Boolean result) {
                    if (result) {
                        String offsetString = sntpClient.getOffsetString();
                    }
                }
            }.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        offset = sntpClient.getOffsetString();
        //Toast.makeText(getApplicationContext(),"Offset: "+sntpClient.getOffsetString(),Toast.LENGTH_LONG).show();
        timeOffset = new TimeOffset();
        timeOffset.getOffsetValue();



        //-----------------------------SEEKBARCODE----------------------
        seekBar = findViewById(R.id.seekbarid);
        textView = findViewById(R.id.countid);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int i;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                i =  progress;
                //numOfSeek = i;
                textView.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //-----------------TOOLBARCODE-------------------------
        Toolbar toolbarm = findViewById(R.id.toolbarmain);
        setSupportActionBar(toolbarm);

        // Set title to false AFTER toolbar has been set
        try
        {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        catch (NullPointerException e){}

        //-----------------CONNECT BUTTON----------------------
        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(this);



    }

    // Method to set actions upon clicking connect
    public void onClick(View v) {
        //Toast.makeText(getApplicationContext(),"Clicked",Toast.LENGTH_SHORT).show();
        if (!ble_scanner.isScanning()){
            device_count = 0;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    permission_granted = false;
                    requestLocationPermission();
                } else{
                    Log.i(Constants.TAG, "Location permission has already been granted. Starting scanning");
                    permission_granted = true;
                }
            }else{
                permission_granted = true;
            }
            startScanning();

        }else{
            ble_scanner.stopScanning();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
       getMenuInflater().inflate(R.menu.menu,menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       int id = item.getItemId();

        if (id== R.id.settingsid){
            openSettingspage();
        }
        return true;
    }

    /**
     * Method that is called in onOptionsItemSelected method and opens the settings page
     */
    public void openSettingspage(){
        Intent intent = new Intent(this,Settingspage.class);
        startActivity(intent);
    }

    //ble part
    //set to which to scan and connect
    public UUID numOfConnectWanted(){
        numOfSeek = seekBar.getProgress();
       if(numOfSeek == 2){
           return Constants.SERVICE_UUID2;
       }else if(numOfSeek == 3){
           return Constants.SERVICE_UUID3;
       }else if(numOfSeek == 4){
           return Constants.SERVICE_UUID4;
       }else if(numOfSeek == 5){
           return Constants.SERVICE_UUID5;
       }else if(numOfSeek == 6){
           return Constants.SERVICE_UUID6;
       }else if(numOfSeek == 7){
           return Constants.SERVICE_UUID7;
       }else if(numOfSeek == 8){
           return Constants.SERVICE_UUID8;
       }else if(numOfSeek == 9){
           return Constants.SERVICE_UUID9;
       }else{
           return Constants.SERVICE_UUID10;
       }
    }

    @Override
    public void receiveNumofConnected(String res){
        phoneConnected.setText("Phones connected: " + res);
    }


    public int getMaxConnectedDevice(){
        System.out.println("Seek"+numOfSeek);
        numOfSeek = seekBar.getProgress();
        return numOfSeek;
    }

    public static MainActivity getInstance() {
        return instance;
    }


    //startScanning will be call when onScan call it.
    private void startScanning(){
        if(permission_granted){
            connectButton.setEnabled(false);
            seekBar.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    connectButton.setEnabled(true);
                    seekBar.setEnabled(true);
                }
            },1200);// set time as per your requirement

            simpleToast(Constants.SCANNING,1200);
            //disabled button from scanning
            ble_scanner.startScanning( this, SCAN_TIMEOUT);
        }else{
            Log.i(Constants.TAG, "Permission to perform Bluetooth scanning was not yet granted");
        }
    }

    private void requestLocationPermission(){
        Log.i(Constants.TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            Log.i(Constants.TAG,"Displaying location permission rationale to provide additional context.");
            final androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.d(Constants.TAG,"Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                }
            });
            builder.show();
        } else{
            //this will be calling the onRequestPermissionsResult
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == REQUEST_LOCATION){
            Log.i(Constants.TAG, "Received response for location permission request.");
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i(Constants.TAG, "Location permission has now been granted. Scanning......");
                permission_granted = true;
                if(ble_scanner.isScanning()){
                    startScanning();
                }
            }else{
                Log.i(Constants.TAG, "Location permission was NOT granted.");
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void simpleToast(String message, int duration){
        toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.show();
    }


    /**
     * Method that allows us to compute the sleep time of the device and to play the snippet after sleep operation.
     * The sleep time is obtained using the following formula, Sleep Duration = Timestamp - Current System Time.
     * Once the snippet is played a toast message is seen
     * @param timestamp time at which the snippet is to be played
     */
    public void playMusic(long timestamp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.MILLISECONDS.sleep(timestamp - System.currentTimeMillis());
                            startService(new Intent(MainActivity.this, SoundService.class));
                            Toast.makeText(getApplicationContext(),"PLAYED",Toast.LENGTH_SHORT).show();
                            phoneConnected.setText("");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }
}
