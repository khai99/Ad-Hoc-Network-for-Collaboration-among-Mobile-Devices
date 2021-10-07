package com.group10b.blueka;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GetUUIDTest {
    @Test
    public void getUUID(){
        String uuid4 = "7D2EA28A-F7BD-485A-BD9D-92AD6ECFE934";
        UUID convert = UUID.fromString(uuid4);
        UUID testUUID =  Constants.SERVICE_UUID4;
        assertEquals(convert,testUUID);
    }
}
