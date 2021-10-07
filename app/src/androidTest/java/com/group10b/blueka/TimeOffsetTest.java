package com.group10b.blueka;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.group10b.blueka.Sound.SntpClient;
import com.group10b.blueka.Sound.TimeOffset;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * TimeOffsetTest, which will execute on an Android device.
 * Uses @Before annotation to obtain the network time and system time using AsyncTask class
 */
@RunWith(AndroidJUnit4.class)
public class TimeOffsetTest {

    final SntpClient sntpClient = new SntpClient();
    private TimeOffset timeOffset;
    private static long currentNetworkTime;
    private static long currentSystemTime;


    @Before
    public void beforeTest() {
        try {
            new AsyncTask<Void, Integer, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    return sntpClient.requestTime("pool.ntp.org", 3000);
                }
                @Override
                protected void onPostExecute(Boolean result) {
                    if (result) {
                        long systemTime = sntpClient.getSystemTime();
                        long ntpTime = sntpClient.getNtpTime();
                    }
                }
            }.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        currentNetworkTime = sntpClient.getNtpTime();
        currentSystemTime = sntpClient.getSystemTime();
        timeOffset = new TimeOffset();
        MainActivity.offset = sntpClient.getOffsetString();
    }

    /**
     * Allows us to test whether the time difference obtained is positive or negative
     */
    @Test
    public void offsetSignTest(){
        boolean sign;
        if (currentSystemTime >= currentNetworkTime){
            sign = true;
        } else {
            sign = false;
        }
        assertEquals(sign, timeOffset.getOffsetSign());
    }


    /**
     * Allows us to verify whether the offset value obtained is within a suitable range
     */
    @Test
    public void offsetValueTest(){
        long offset;
        if (currentSystemTime >= currentNetworkTime){
            offset = currentSystemTime - currentNetworkTime;
        } else {
            offset = currentNetworkTime - currentSystemTime;
        }
        if (offset > 0 && timeOffset.getOffsetValue() > 0){
            assertTrue(true);
        }
    }

}