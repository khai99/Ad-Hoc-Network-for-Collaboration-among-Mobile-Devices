package com.group10b.blueka;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Service class that allows us to implement methods to play the snippet in background.
 */
public class SoundService extends Service {
    //getting the sound snippet from resources folder
    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    private int originalVolume;
    static boolean destroyed;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Once an instance of the service class is created mediaPlayer, audioManager, and originalVolume are initialised.
     * mediaPlayer allows us to obtain the sound file from the resources folder.
     * audioManger is used for handling management of volume, ringer modes and audio routing.
     * originalVolume is used to store the current volume of the device.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.merdeka);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Once the service activity is started, we verify whether the phone is on ringer mode.
     * If the phone is muted, then the snippet shall not be played.
     * Once the snippet is completed, the original volume is restored.
     * @param intent
     * @param flags
     * @param startId
     * @return constant
     */
    @Override
    public int onStartCommand (Intent intent,int flags, int startId){

        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();

        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
            }
        });
        return START_STICKY;
    }


    /**
     * Used to destroy the instance of the service class created.
     * Also used for testing purposes in SoundServiceTest class.
     */
    @Override
    public void onDestroy(){
        destroyed = true;
        mediaPlayer.stop();
    }
}