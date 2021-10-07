package com.group10b.blueka.Operation;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Operation for the server to write its local characteristic.
 */
public class WriteCharacteristicOperation extends Operation {
    private String message;
    private BluetoothGattServer mGattServer;
    private BluetoothDevice device;
    private BluetoothGattCharacteristic characteristic;

    public WriteCharacteristicOperation(BluetoothGattServer mGattServer, BluetoothGattCharacteristic characteristic, BluetoothDevice device){
        this.mGattServer = mGattServer;
        this.device = device;
        this.characteristic = characteristic;

    }

    @Override
    public void performOperation() {
        mGattServer.notifyCharacteristicChanged(device, characteristic, false);
    }
}
