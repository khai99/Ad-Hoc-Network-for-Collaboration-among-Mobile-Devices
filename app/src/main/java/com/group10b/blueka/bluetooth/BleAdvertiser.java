package com.group10b.blueka.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.content.ContextCompat;


import com.group10b.blueka.Constants;
import com.group10b.blueka.MainActivity;
import com.group10b.blueka.Operation.OperationManager;
import com.group10b.blueka.Operation.WriteCharacteristicOperation;
import com.group10b.blueka.Sound.TimeOffset;

import static com.group10b.blueka.Constants.TAG;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * An Wrapper for all activity of advertising. The device are expected to be an scanner, if nothing
 * can be scanned, then the device will be an advertiser instead.
 */
public class BleAdvertiser {
    private OperationManager operationManager = new OperationManager();
    private BluetoothLeAdvertiser advertiser;
    private Handler hander = new Handler();
    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer mGattServer;
    private Context context;
    private boolean advertising =  false;
    private static ArrayList<BluetoothDevice> mDevices = new ArrayList();
    private ScanResultsConsumer scan_results_consumer;
    TimeOffset timeOffset = new TimeOffset();

    /**
     * Constructor, to set up Bluetooth Low Energy components.
     * @param context the environment, which refer to the device.
     */
    public BleAdvertiser(Context context){
        this.context = context;
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        mBluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        GattServerCallback gattServerCallback = new GattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(context, gattServerCallback);
        setupServer();
    }

    /**
     * A method to pass the interface to the MainActivity. Any update from the Bluetooth Advertiser
     * will be use this method to update the user interface.
     * @param src
     */
    public void updateResultConsumer(ScanResultsConsumer src){
        scan_results_consumer = src;
    }
    // setupServer() is called when initialise the advertiser.
    // Define the server we want to advertise.

    /**
     * Set up the server for advertiser to advertises.
     * Settings of the server will be set here as well.
     */
    private void setupServer(){
        BluetoothGattService service = new BluetoothGattService(MainActivity.getInstance().numOfConnectWanted(),BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //write
        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                Constants.CHARACTERISTIC_ECHO_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(writeCharacteristic);
        mGattServer.addService(service);
    }

    //startAdvertising() is called when advertiser want to start advertising.

    /**
     * MainActivity can call this method to let the advertiser start advertising the server.
     */
    public void startAdvertising(){
        if(advertising){
            Log.d(Constants.TAG, "Already advertising");
            return;
        }
        if(advertiser == null){
            Log.d(Constants.TAG, "Bluetooth advertiser failed.");
            return;
        }
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(true)
                .build();

        ParcelUuid pUuid = new ParcelUuid(MainActivity.getInstance().numOfConnectWanted());
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( false )
                .addServiceUuid( pUuid )
                .build();


        advertiser.startAdvertising( settings, data, advertisingCallback );
        setAdvertising(true);


    }

    // This define what happen when advertising is failed on success.
    /**
     * This is a callback, when advertising is successful, any result from the advertising
     * will be handled by callbacks.
     */
    private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        /**
         * This method will be call when the advertising is sucessful, here we have some logs to
         * confirm the device is advertising sucessfully.
         * @param settingsInEffect
         */
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(Constants.TAG,"Advertise success");
        }

        /**
         * This method will be called when the advertising is failed. We print the error code so we
         * know the reason why it failed to advertise.
         * @param errorCode
         */
        @Override
        public void onStartFailure(int errorCode) {
            Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
            super.onStartFailure(errorCode);
        }
    };

    /**
     * This method can be called by MainActivity to make the advertiser stop advertise.
     */
    public void stopAdvertising(){
        setAdvertising(false);
        advertiser.stopAdvertising(advertisingCallback);
    }

    /**
     * A boolean method to check if the advertiser advertising.
     * @return true if the advertiser advertising, else false.
     */
    public boolean isAdvertising(){return advertising;}
    void setAdvertising(boolean advertising){
        this.advertising = advertising;
    }

    // GattClientCallback is very important thing to define.
    // Whatever happen during the connection is conducted here.
    // onCharacteristicWriteRequest() is called when we receive write request from scanner.
    // onConnectionStateChange is called when device is connected or disconnected.
    // onNotificationSent is called when a notification is send to one device.

    /**
     * An object to handled all the events in Gatt connection after the advertiser connected by a scanner.
     */
    private class GattServerCallback extends BluetoothGattServerCallback{
        //write

        /**
         * A remote client has requested to write a local characteristic.
         * @param device The remote device that has requested the write operation
         * @param requestId The Id of the request
         * @param characteristic Characteristic to be written to.
         * @param preparedWrite  true, if this write operation should be queued for later execution.
         * @param responseNeeded  true, if the remote device requires a response
         * @param offset The offset given for the value
         * @param value The value the client wants to assign to the characteristic
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                 int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite,
                                                 boolean responseNeeded,
                                                 int offset,
                                                 byte[] value) {
            super.onCharacteristicWriteRequest(device,
                    requestId,
                    characteristic,
                    preparedWrite,
                    responseNeeded,
                    offset,
                    value);
            //advertiser will see the number of connected device and notify every phone.
            Log.i(TAG,"inside on characterisitic write request in advertiser");

            if (characteristic.getUuid().equals(Constants.CHARACTERISTIC_ECHO_UUID)) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                int num_connected = mDevices.size() + 1;
                String message = Integer.toString(num_connected);

                long serverMusicTime = getServerMusicTime(System.currentTimeMillis());
                long timestamp = getTimestamp(serverMusicTime);
                String msg = String.valueOf(timestamp);

                byte[] reply = new byte[0];

                if ((num_connected == MainActivity.getInstance().getMaxConnectedDevice())){
                    try {
                        reply = msg.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Failed to convert message string to byte array");
                    }
                    stopAdvertising();
                    scan_results_consumer.receiveNumofConnected(Integer.toString(MainActivity.getInstance().getMaxConnectedDevice()));
                    characteristic.setValue(reply);
                    mGattServer.notifyCharacteristicChanged(device, characteristic, false);
                    MainActivity.getInstance().playMusic(serverMusicTime);
                } else {
                    try {
                        reply = message.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Failed to convert message string to byte array");
                    }
                    characteristic.setValue(reply);
                    scan_results_consumer.receiveNumofConnected(Integer.toString(num_connected));
                }


                for(BluetoothDevice dev : mDevices) {
                    operationManager.request(new WriteCharacteristicOperation(mGattServer, characteristic, dev));
                }
            }
        }

        /**
         * Callback indicating when a remote device has been connected or disconnected.
         * @param device BluetoothDevice: Remote device that has been connected or disconnected.
         * @param status int: Status of the connect or disconnect operation.
         * @param newState int: Returns the new connection state. Can be one of BluetoothProfile.STATE_DISCONNECTED or BluetoothProfile#STATE_CONNECTED
         */
        @Override
        public void onConnectionStateChange (BluetoothDevice device,
                                             int status,
                                             int newState){
            super.onConnectionStateChange(device, status, newState);
            Log.i(TAG,"HEREWEGO");
            if (newState == BluetoothProfile.STATE_CONNECTED){
                if(!mDevices.contains(device)) {
                    mDevices.add(device);

                    Log.i(TAG,"Yes, device added");
                }
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                mDevices.remove(device);
                Log.i(TAG,"Yes, device removed");
            }
        }

        /**
         * Callback invoked when a notification or indication has been sent to a remote device.
         *
         * When multiple notifications are to be sent, an application must wait for this callback to be received before sending additional notifications.
         * Here we use to inform the queueManager that the operation has completed.
         * @param device BluetoothDevice: The remote device the notification has been sent to
         * @param status int: BluetoothGatt#GATT_SUCCESS if the operation was successful
         */
        @Override
        public void onNotificationSent (BluetoothDevice device,
                                        int status){
            super.onNotificationSent(device,status);
            operationManager.operationCompleted();

        }
    }

    /**
     * A method to close the server.
     */
    private void stopServer(){
        if(mGattServer != null){
            mGattServer.close();
        }
    }


    /**
     * This method captures the current time on the device and adds a certain value (e.g 6000 ms) to it in order to obtain a future timestamp to play the snippet.
     * @param currentSystemTime the current time on the device
     * @return a future time to play the music
     */
    public long getServerMusicTime(long currentSystemTime){
        return (currentSystemTime + 8000);
    }

    /**
     * This method takes the timestamp at which the server device is set to play the music and adjust the time according to the offset value
     * @param serverMusicTime system time at which the server device shall play the snippet
     * @return atomic time at which the server shall play the music
     */
    public long getTimestamp(long serverMusicTime) {
        long timestamp;
        Boolean offsetSign = timeOffset.getOffsetSign();
        long offsetValue = timeOffset.getOffsetValue();
        if (offsetSign){
            timestamp = serverMusicTime - offsetValue;
        } else {
            timestamp = serverMusicTime + offsetValue;
        }
        return timestamp;
    }
}
