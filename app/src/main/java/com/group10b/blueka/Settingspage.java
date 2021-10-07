package com.group10b.blueka;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class Settingspage extends AppCompatActivity {
    ToggleButton btnToggleDark;
    ToggleButton bluetoothbutton;
    ToggleButton locationbutton;
    ToggleButton internetbutton;
    BluetoothAdapter myBluetoothAdapter;
    LocationManager locationManager;
    ConnectivityManager conMgr ;
    boolean locationStatus;
    //Creating an object of intent
    Intent btEnablingIntent;
    int requestCodeForEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingspage);

        //--------------------------BluetoothButton-----------------
        bluetoothbutton = (ToggleButton) findViewById(R.id.bluetooth_button);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable = 1;
        setBluetoothButtonStatus();
        bluetoothbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    //The toggle is enabled
                    bluetoothONMethod();

                } else {
                    // The toggle is disabled
                    bluetoothOFFMethod();
                }
            }
        });

        //------------------------LocationButton--------------------
        locationbutton = (ToggleButton) findViewById(R.id.location_button);
        checkLocationButtonState();

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        locationStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        locationbutton.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    locationON();
                } else {
                    locationOFF();

                }
            }
        }));

        //------------------------InternetButton---------------------------
        internetbutton = (ToggleButton) findViewById(R.id.internet_button);
        checkNetworkStatus();

        conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        internetbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    internetON();
                } else {
                    internetOFF();
                }
            }
        });
        //----------------Toolbarcode---------------------------------------
        Toolbar toolbars = findViewById(R.id.toolbarsettings);
        setSupportActionBar(toolbars);

        // Set title to false AFTER toolbar has been set
        try
        {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        catch (NullPointerException e){}

        ////////////////////////////////////////For Dark Mode////////////////////////////////////////////
        btnToggleDark
                = findViewById(R.id.btnToggleDark);

        // Saving state of our app
        // using SharedPreferences
        SharedPreferences sharedPreferences
                = getSharedPreferences(
                "sharedPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor editor
                = sharedPreferences.edit();
        final boolean isDarkModeOn
                = sharedPreferences
                .getBoolean(
                        "isDarkModeOn", false);

        // When user reopens the app
        // after applying dark/light mode
        if (isDarkModeOn) {
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_YES);
            btnToggleDark.setChecked(true);
        }
        else {
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_NO);
            btnToggleDark.setChecked(false);
        }

        btnToggleDark.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view)
                    {
                        // When user taps the enable/disable
                        // dark mode button
                        if (isDarkModeOn) {

                            // if dark mode is on it
                            // will turn it off
                            AppCompatDelegate
                                    .setDefaultNightMode(
                                            AppCompatDelegate
                                                    .MODE_NIGHT_NO);
                            // it will set isDarkModeOn
                            // boolean to false
                            editor.putBoolean(
                                    "isDarkModeOn", false);
                            editor.apply();

                            // change text of Button
                            btnToggleDark.setChecked(false);
                        }
                        else {

                            // if dark mode is off
                            // it will turn it on
                            AppCompatDelegate
                                    .setDefaultNightMode(
                                            AppCompatDelegate
                                                    .MODE_NIGHT_YES);

                            // it will set isDarkModeOn
                            // boolean to true
                            editor.putBoolean(
                                    "isDarkModeOn", true);
                            editor.apply();

                            // change text of Button
                            btnToggleDark.setChecked(true);

                        }
                    }
                });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu settingsmenu){
        getMenuInflater().inflate(R.menu.settingsmenu,settingsmenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id== R.id.backid){
            openMainpage();
        }
        return true;
    }
    public void openMainpage() {
        Intent intent = new Intent(this, com.group10b.blueka.MainActivity.class);
        startActivity(intent);

    }


    /**
     * Method to set the status of the Bluetooth button in settings page
     */
    public void setBluetoothButtonStatus(){
        if (myBluetoothAdapter.isEnabled()){
            bluetoothbutton.setChecked(true);
        } else if (!myBluetoothAdapter.isEnabled()){
            bluetoothbutton.setChecked(false);
        }
    }

    /**
     * Method to switch off bluetooth and display a message
     */
    private void bluetoothOFFMethod(){
        bluetoothbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBluetoothAdapter.isEnabled()){
                    myBluetoothAdapter.disable();
                    bluetoothbutton.setChecked(false);
                    Toast.makeText(getApplicationContext(), "Bluetooth is disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Method to switch ON bluetooth
     */
    private void bluetoothONMethod(){
        bluetoothbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBluetoothAdapter == null){
                    Toast.makeText(getApplicationContext(),"Bluetooth is not supported in this device", Toast.LENGTH_SHORT).show();
                }else if (!myBluetoothAdapter.isEnabled()){
                    //startActivityForResult(btEnablingIntent,REQUEST_ENABLE_BT);
                    myBluetoothAdapter.enable();
                    bluetoothbutton.setChecked(true);
                    Toast.makeText(getApplicationContext(),"Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void checkLocationButtonState(){
        if (isLocationEnabled(getApplicationContext())){
            locationbutton.setChecked(true);
        } else {
            locationbutton.setChecked(false);
        }
    }


    public void openLocationSettings(){
        locationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open location settings
                //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                checkLocationButtonState();

            }
        });
    }

    public void locationON(){
        openLocationSettings();

        if (!isLocationEnabled(getApplicationContext())){
            locationbutton.setChecked(true);
            refresh();
            //toast
        }
    }

    public void locationOFF(){
        openLocationSettings();
        if (isLocationEnabled(getApplicationContext())){
            locationbutton.setChecked(false);

            refresh();
            //toast
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public void openNetworkSettings(){
        internetbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), 0);
                checkNetworkStatus();
            }
        });
    }

    public void internetON(){
        openNetworkSettings();
        if (isConnectionEnable() == false){
            internetbutton.setChecked(true);
            refresh();
        }
    }

    public void internetOFF(){
        openNetworkSettings();
        if (isConnectionEnable()){
            internetbutton.setChecked(false);
            refresh();
        }
    }

    public void checkNetworkStatus(){
        if (isConnectionEnable()){
            internetbutton.setChecked(true);
        } else {
            internetbutton.setChecked(false);
        }
    }
    public boolean isConnectionEnable(){
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            // notify user you are online
            return true;
        } else {
            // notify user you are not online
            return false;
        }
    }

    /**
     * Refresh page
     */
    public void refresh(){
        openMainpage();
        Intent intent = new Intent(this, com.group10b.blueka.Settingspage.class);
        startActivity(intent);
    }


}