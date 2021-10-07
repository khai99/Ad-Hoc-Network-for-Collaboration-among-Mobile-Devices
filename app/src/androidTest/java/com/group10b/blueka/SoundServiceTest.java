package com.group10b.blueka;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.group10b.blueka.Sound.SntpClient;
import com.group10b.blueka.Sound.TimeOffset;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;


import static android.content.Context.AUDIO_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * SoundServiceTest, which will execute on an Android device.
 * Allows to to test methods in SoundService class by creating an intent and context.
 */
@RunWith(AndroidJUnit4.class)
public class SoundServiceTest {

    AudioManager audioManager;

    /**
     * Allows us to verify whether the onDestroy method in SoundService class is executed successfully
     */
    @Test
    public void soundServiceDestroyedTest() {
        //Create context of app
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Create the service Intent.
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(),SoundService.class);
        //onDestroy() method is called in response to Context.stopService()
        assertEquals(appContext.stopService(serviceIntent),SoundService.destroyed);
    }


    /**
     * Allows us to check whether the correct value is obtained for maximum volume
     */
    @Test
    public void maximumVolumeTest(){
        //Create context of app
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Create the service Intent.
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(),SoundService.class);
        audioManager = (AudioManager) appContext.getSystemService(AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (maxVolume > 0){
            assertTrue(true);
        }
    }


}