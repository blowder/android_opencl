package com.shl.checkpin.android.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by sesshoumaru on 23.02.16.
 */
public class WakeUpOnMessageService extends WakefulBroadcastReceiver {
    public static final String TAG = WakeUpOnMessageService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(),
                MyGcmListenerService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
        Log.i(TAG, "App now awake and wait for messages");
    }
}
