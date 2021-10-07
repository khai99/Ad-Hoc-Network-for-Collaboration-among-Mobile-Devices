package com.group10b.blueka;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.ScanFilter;
import android.content.pm.PackageManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import android.bluetooth.le.ScanFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;


import androidx.test.platform.app.InstrumentationRegistry;
import com.group10b.blueka.bluetooth.BleAdvertiser;
import com.group10b.blueka.bluetooth.BleScanner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

public class BLETest {
    private static UUID uuidTest = UUID.fromString("7D2EA28A-F7BD-485A-BD9D-92AD6ECFE933");
    private Handler mHandlerTest;
    private BleAdvertiser bleAdvertiser;
    private BleScanner bleScanner;
    @Test
    public void BLESupportTest(){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null){
            assertFalse(InstrumentationRegistry.getInstrumentation().getContext().getPackageManager().
                    hasSystemFeature(PackageManager.FEATURE_BLUETOOTH));
        }else{
            assertTrue(InstrumentationRegistry.getInstrumentation().getContext().getPackageManager().
                    hasSystemFeature(PackageManager.FEATURE_BLUETOOTH));
        }
    }

    @Test
    public void scanFilterTest(){
        ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(Constants.SERVICE_UUID3)).build();
        assertEquals(uuidTest.toString(),scanFilter.getServiceUuid().toString());
    }

    @Test
    public void advertiseTest(){
        ParcelUuid pUuid = new ParcelUuid(Constants.SERVICE_UUID3);
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName( false )
                .addServiceUuid( pUuid )
                .build();
        String advertiseDataUUID = advertiseData.getServiceUuids().toString().substring(1, advertiseData.getServiceUuids().toString().length() - 1);
        assertEquals(uuidTest.toString(), advertiseDataUUID);
    }

    @Test
    public void scanStateTest(){
        mHandlerTest = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                BleScanner bleScanner = new BleScanner(InstrumentationRegistry.getInstrumentation().getContext());
                bleScanner.stopScanning();
                assertFalse(bleScanner.isScanning());
            }
        };
    }


    @Test
    public void advertisingStateTest(){
        mHandlerTest = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                BleAdvertiser bleAdvertiser = new BleAdvertiser(InstrumentationRegistry.getInstrumentation().getContext());
                bleAdvertiser.stopAdvertising();
                assertFalse(bleAdvertiser.isAdvertising());
            }
        };
    }


}
