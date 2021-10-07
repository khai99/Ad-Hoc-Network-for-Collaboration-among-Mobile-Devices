package com.group10b.blueka.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.group10b.blueka.Constants;
import com.group10b.blueka.MainActivity;
import com.group10b.blueka.Operation.DisconnectOperation;
import com.group10b.blueka.Operation.DiscoverOperation;
import com.group10b.blueka.Operation.GattCloseOperation;
import com.group10b.blueka.Operation.OperationManager;
import com.group10b.blueka.Operation.WriteRequestOperation;
import com.group10b.blueka.Sound.TimeOffset;

import static com.group10b.blueka.Constants.TAG;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for all the functionality of scanning. The application will first be a scanner using
 * this class BleScanner to scan. If nothing is scanned, then the application will be a advertiser using
 * BleAdvertiser. The app is expected to actively use one class at a time.
 */
public class BleScanner {
    private BluetoothLeScanner scanner = null;
    private BluetoothAdapter bluetooth_adapter = null;
    private BluetoothGatt mGatt;
    private Handler handler = new Handler();
    //this is to send the data to the main activity to display on the UI
    private ScanResultsConsumer scan_results_consumer;
    private Context context;
    private boolean scanning = false;
    private String device_name_start = "";
    private boolean mConnected = false;
    private OperationManager operationManager;
    private GattClientCallback gattClientCallback;
    private boolean mInitialized = false;
    private BleAdvertiser bleAdvertiser;
    private Boolean checkScan = false;

    TimeOffset timeOffset = new TimeOffset();

    //gatt.close() is needed after gatt.disconnect().
    //However, the onConnectionStateChange is not triggered sometimes to close the gatt.
    //We need a Runnable to run it if the onConnectionStateChange is not triggered.
    //If onConnectionStateChange works, it will cancel this Runnable which also close the gatt.
    /**
     * This is a backup delayed runnable function for gatt closing,
     * in case the close operation is not successful.
     */
    private Runnable GattCloseRun= new Runnable(){
        @Override
        public void run()
        {   operationManager.operationCompleted();
            operationManager.request(new GattCloseOperation(mGatt));
            operationManager.operationCompleted();
        }
    };

    //Constructor.

    /**
     * An constructor for the class, set up all the components needed for Bluetooth scanner.
     * @param context An environment, in this case is the device.
     */
    public BleScanner(Context context){
        this.context = context;
        operationManager = new OperationManager();
        gattClientCallback = new GattClientCallback();
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        bluetooth_adapter = bluetoothManager.getAdapter();

        // check bluetooth is available and on.
        if(bluetooth_adapter == null || !bluetooth_adapter.isEnabled()){
            Log.d(Constants.TAG, "Bluetooth is NOT switched on");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(enableBtIntent);
        }
        Log.d(Constants.TAG, "Bluetooth is switched on");
    }

    //startScanning is call when scanner start scanning.

    /**
     * MainActivity can call this method to let the scanner start scanning.
     * @param scan_results_consumer An interface to trigger methods in MainActivity to control user
     *                              interface.
     * @param stop_after_ms The duration of scanning.
     */
    public void startScanning(final ScanResultsConsumer scan_results_consumer, long stop_after_ms){
        if(scanning){
            Log.d(Constants.TAG, "Already scanning so ignoring startScanning request");
            return;
        }
        if(mConnected){

            disconnectGattServer();
        }
        if(scanner == null){
            scanner = bluetooth_adapter.getBluetoothLeScanner();
            Log.d(Constants.TAG, "Created Bluetooth object");
        }
        handler.postDelayed(new Runnable(){
            @Override
            public void run()
            {
                if(scanning){
                    Log.d(Constants.TAG, "Stopping scanning");
                    scanner.stopScan(scan_callback);
                    setScanning(false);
                    if(checkScan != Boolean.TRUE){
                        bleAdvertiser = new BleAdvertiser(context);
                        bleAdvertiser.updateResultConsumer(scan_results_consumer);
                        bleAdvertiser.startAdvertising();
                    }else{
                        checkScan = false;
                    }
                }
            }
        }, stop_after_ms);

        this.scan_results_consumer = scan_results_consumer;
        Log.d(Constants.TAG,"Scanning");
        List<ScanFilter> filters;
        //Filtering the scan results.
        filters = new ArrayList<ScanFilter>();
        ScanFilter scanFilter =  new ScanFilter.Builder().setServiceUuid(new ParcelUuid(MainActivity.getInstance().numOfConnectWanted())).build();
        filters.add(scanFilter);
        //Scan settings.
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        setScanning(true);
        scanner.startScan(filters, settings, scan_callback);
    }
    //stopScanning is call when scanner stop scanning.

    /**
     * MainActivity can call this method to let the scanner stop scanning.
     */
    public void stopScanning(){
        setScanning(false);
        Log.d(Constants.TAG,"Stopping scanning");
        scanner.stopScan(scan_callback);
    }

    //Define what happens after scanner have some scan result, also where the scan result store.
    //Connect the device when a device is found.
    /**
     * Bluetooth LE scan callbacks. Scan results are reported using these callbacks.
     */
    private ScanCallback scan_callback = new ScanCallback() {
        /**
         * Callback when a BLE advertisement has been found.
         * @param callbackType int: Determines how this callback was triggered. Could be one of ScanSettings.CALLBACK_TYPE_ALL_MATCHES, ScanSettings#CALLBACK_TYPE_FIRST_MATCH or ScanSettings#CALLBACK_TYPE_MATCH_LOST
         * @param result ScanResult: A Bluetooth LE scan result.
         */
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            checkScan = true;
            if(!scanning){
                return;
            }
            scanner.stopScan(scan_callback);
            BluetoothDevice bluetoothDevice = result.getDevice();
            connectDevice(bluetoothDevice);
        }

    };

    /**
     * A method for scanner to connected the scanned device.
     * @param device The device to connect.
     */
    private void connectDevice(BluetoothDevice device){
        mGatt = device.connectGatt(context, false, gattClientCallback);
        Log.i(TAG,"connected inside");
    }

    /**
     * A method to check the scanning status of the scanner.
     * @return true if the scanner is scanning.
     */
    public boolean isScanning(){
        return scanning;
    }

    // setScanning is called when it start scanning or stop scanning
    // To adjust the UI, and variable.

    /**
     * A method to set boolean value of scanning
     * @param scanning the boolean value representing the status of scanning. Will be set the true when scanning.
     */
    private void setScanning(boolean scanning){
        this.scanning = scanning;
    }

    // GattClientCallback is very important thing to define.
    // Whatever happen during the connection is conducted here.
    // onConnectionStateChange is called when device is connected or disconnected.
    // onServiceDiscovered is called when service of connected device is found.
    // onCharacteristicChanged is called when the advertiser notify.
    // onCharacteristicWrite is called when writeRequest is successfully sent.

    /**
     *This abstract class is used to implement BluetoothGatt callbacks.
     */
    private class GattClientCallback extends BluetoothGattCallback{
        /**
         * Callback indicating when GATT client has connected/disconnected to/from a remote GATT server.
         * @param gatt BluetoothGatt: GATT client
         * @param status int: Status of the connect or disconnect operation. BluetoothGatt.GATT_SUCCESS if the operation succeeds.
         * @param newState int: Returns the new connection state. Can be one of BluetoothProfile.STATE_DISCONNECTED or BluetoothProfile#STATE_CONNECTED
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_FAILURE){
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS){
                disconnectGattServer();
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED){
                mConnected = true;
                //write
                operationManager.request(new DiscoverOperation(gatt));
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                handler.removeCallbacks(GattCloseRun);
                operationManager.operationCompleted();
                operationManager.request(new GattCloseOperation(mGatt));
                operationManager.operationCompleted();
                mConnected = false;
                Log.i(TAG,"CLOSED GATT");
            }
        }
        //write

        /**
         * Callback invoked when the list of remote services, characteristics and descriptors for the remote device have been updated, ie new services have been discovered.
         * @param gatt BluetoothGatt: GATT client invoked BluetoothGatt#discoverServices
         * @param status int: BluetoothGatt#GATT_SUCCESS if the remote device has been explored successfully.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            super.onServicesDiscovered(gatt, status);
            operationManager.operationCompleted();
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            String message = "hello";
            operationManager.request(new WriteRequestOperation(gatt, message));
        }

        /**
         * Callback triggered as a result of a remote characteristic notification.
         * @param gatt BluetoothGatt: GATT client the characteristic is associated with
         * @param characteristic BluetoothGattCharacteristic: Characteristic that has been updated as a result of a remote notification event.
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "entered into notified");

            byte[] messageBytes = characteristic.getValue();
            String messageString = null;
            try {
                messageString = new String(messageBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unable to convert message bytes to string");
            }
            //receiving the message, will be reversed because the advertiser reverse it just to see the difference
            Log.d("Receive message", messageString);
            final String result = messageString;

            long timestamp = Long.parseLong(result);
            long clientMusicTime = getClientMusicTime(timestamp);

            if (result.length() > 2){
                Log.d("Client Offset: ", String.valueOf(timeOffset.getOffsetValue()));
                scan_results_consumer.receiveNumofConnected(Integer.toString(MainActivity.getInstance().getMaxConnectedDevice()));
                MainActivity.getInstance().playMusic(clientMusicTime);
                disconnectGattServer();
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        scan_results_consumer.receiveNumofConnected(result);
                    }
                });
            }

        }

        /**
         * Callback indicating the result of a characteristic write operation.
         *
         * If this callback is invoked while a reliable write transaction is in progress, the value
         * of the characteristic represents the value reported by the remote device. An application
         * should compare this value to the desired value to be written. If the values don't match,
         * the application must abort the reliable write transaction.
         * @param gatt BluetoothGatt: GATT client invoked BluetoothGatt#writeCharacteristic
         * @param characteristic BluetoothGattCharacteristic: Characteristic that was written to the associated remote device.
         * @param status int: The result of the write operation BluetoothGatt#GATT_SUCCESS if the operation succeeds.
         */
        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic,
                                           int status){
            super.onCharacteristicWrite(gatt, characteristic, status);
            operationManager.operationCompleted();
            Log.i(TAG,"YES, another queue works");
        }
    }

    /**
     * A method to disconnect the gatt server connected.
     */
    public void disconnectGattServer(){
        mConnected = false;
        mInitialized = false;
        if(mGatt != null){
            operationManager.request(new DisconnectOperation(mGatt));
            handler.postDelayed(GattCloseRun,2000);
        }
    }

    /**
     * This method takes as parameter the atomic time at which the server shall play the snippet and uses that timestamp to compute the time at which the client should play the snippet.
     * @param timestamp Atomic time at which the server device is set to play the music
     * @return The system time at which the client device shall play the music
     */
    public long getClientMusicTime(long timestamp){
        Boolean offsetSign = timeOffset.getOffsetSign();
        long offsetValue = timeOffset.getOffsetValue();
        Log.d("Client Offset: " ,String.valueOf(offsetValue));
        long clientMusicTime;
        if (offsetSign){
            clientMusicTime = timestamp + offsetValue;
        } else {
            clientMusicTime = timestamp - offsetValue;
        }
        return clientMusicTime;

    }
}
