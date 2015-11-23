package com.shl.checkpin.android.gcm;

import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by vfedin on 19.11.2015.
 */
public class MyGcmListenerService extends GcmListenerService {
    String TAG = "MyGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.i(TAG, "From: " + from);
        Log.i(TAG, "Message: " + message);

        /*if (from.startsWith("/topics/global")) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
            // message received from some topic.
        } else {
            // normal downstream message.
        }*/
    }

}
