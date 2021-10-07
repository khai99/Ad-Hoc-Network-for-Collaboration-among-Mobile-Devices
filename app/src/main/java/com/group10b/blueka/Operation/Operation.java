package com.group10b.blueka.Operation;
import android.bluetooth.BluetoothGatt;

// All this operation is BLE operation, we just arrange the operations as queue to avoid any
// operation is overwritten. Stabilize the performance.

/**
 * The superclass of all operations. Have the abstract method and gatt, to enforce the children class have the same method.
 */
public abstract class Operation {
    protected BluetoothGatt gatt;
    public Operation(){

    }
    public Operation(BluetoothGatt gatt){
        this.gatt = gatt;
    }
    public abstract void performOperation();
}
