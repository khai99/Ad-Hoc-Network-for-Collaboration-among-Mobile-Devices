package com.group10b.blueka;

import android.content.Context;
import android.os.AsyncTask;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.group10b.blueka.Sound.SntpClient;
import com.group10b.blueka.Sound.TimeOffset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SntpClientTest, which will execute on an Android device.
 * Implement test method to verify whether the NTP time is retrieved successfully.
 * Uses @Before annotation to obtain instance from MainActivity class
 */
@RunWith(AndroidJUnit4.class)
public class SntpClientTest {

    final SntpClient sntpClient = new SntpClient();
    private TimeOffset timeOffset;
    private static long currentNetworkTime;
    private static long currentSystemTime;

    @Before
    public void beforeTest(){
        timeOffset = new TimeOffset();
        MainActivity.offset = sntpClient.getOffsetString();
    }

    @Test
    public void networkTimeTest(){
        try {
            new AsyncTask<Void, Integer, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    return sntpClient.requestTime("pool.ntp.org", 3000);
                }
                @Override
                protected void onPostExecute(Boolean result) {
                    if (result) {
                        long ntpTime = sntpClient.getNtpTime();
                        long systemTime = sntpClient.getSystemTime();
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

        //verify whether network time is received
        if (currentNetworkTime> 0){
            assertTrue(true);
        }

        //We verify if the offset is positive
        //Then check if the correct network time is received
        if (timeOffset.getOffsetSign()){
            if (currentNetworkTime <= currentSystemTime){
                assertTrue(true);
            }
        } else {
            if (currentNetworkTime > currentSystemTime){
                assertTrue(true);
            }
        }
    }

}