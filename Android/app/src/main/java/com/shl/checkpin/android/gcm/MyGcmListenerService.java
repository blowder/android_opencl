package com.shl.checkpin.android.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.activities.MainScreenActivity;
import com.shl.checkpin.android.utils.Constants;
import com.shl.checkpin.android.utils.FSFileLocator;
import com.shl.checkpin.android.utils.FileLocator;

/**
 * Created by vfedin on 19.11.2015.
 */
public class MyGcmListenerService extends GcmListenerService {
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    String TAG = "MyGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
  /*      String message = data.getString("message");*/
        Log.i(TAG, "Message: " + data);
        sendNotification(data.toString());
/*
        if (from.startsWith("/topics/upload")
                && "OK".equals(data.getString("status"))) {
            String filename = data.getString("file");
            JobHolder.removeJob(appFileLocator.locate(Environment.DIRECTORY_PICTURES, filename));
        }*/
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.GCM_MESSAGE, message);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
