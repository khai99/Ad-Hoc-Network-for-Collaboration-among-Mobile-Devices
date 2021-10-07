package com.group10b.blueka.Operation;
import android.bluetooth.BluetoothGatt;

/**
 * An operation for the scanner to discover the service provided by the server.
 */
public class DiscoverOperation extends Operation {
    public DiscoverOperation(BluetoothGatt gatt){
        super(gatt);
    }

    @Override
    public void performOperation() {
        gatt.discoverServices();
    }
}
