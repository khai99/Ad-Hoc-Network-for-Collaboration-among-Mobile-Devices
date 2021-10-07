package com.group10b.blueka.Sound;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.group10b.blueka.MainActivity;

import java.util.concurrent.ExecutionException;

/**
 * TimeOffset class that implements methods obtain the offset value.
 * And also allows us to verify whether the time difference obtained is positive or negative.
 */
public class TimeOffset {

    /**
     * Retrieves the offset String from AsynTask in MainActivity class and extract the value only
     * @return offset value of type Long
     */
    public long getOffsetValue(){
        String str = MainActivity.offset;
        Log.d("Phone Offset: ", str);
        int length = str.length();
        String substr = str.substring(1,length);
        return Long.parseLong(substr);
    }

    /**
     * Retrieves the offset String from Asynctak in MainActivity class and extrac the sign only
     * @return true if the offset is positive and false if the offset is negative
     */
    public Boolean getOffsetSign(){
        String str = MainActivity.offset;
        Boolean value;
        char sign = str.charAt(0);
        if (sign == '+'){
            value = true;
        } else {
            value = false;
        }
        return value;
    }


}
