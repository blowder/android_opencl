package com.shl.checkpin.android.gcm;

import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import com.shl.checkpin.android.factories.Injector;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.model.ImageDocService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vfedin on 19.11.2015.
 */
public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = MyGcmListenerService.class.getSimpleName();

    enum MessageKey {
        amount,
        retailer,
        check,
        url
    }

    @Inject
    ImageDocService imageDocService;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Injector.inject(this);
        Map<String, String> result = new HashMap<String, String>();
        try {
            JSONArray array = new JSONArray(data.getString("entry"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.getJSONObject(i);
                result.put(entry.getString("key"), entry.getString("value"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred during message parse", e);
        }
        fillBillData(result);
    }

    private void fillBillData(Map<String, String> result) {
        String checkName = result.get(MessageKey.check.toString());
        if (checkName != null) {
            String retailer = result.get(MessageKey.retailer.toString());
            Double amount = 0.0;
            try {
                amount = Double.parseDouble(result.get(MessageKey.amount.toString()));
            } catch (NumberFormatException e) {
                //skip this
            }
            String url = result.get(MessageKey.url.toString());
            ImageDoc imageDoc = imageDocService.findByName(checkName);
            ImageDoc.Status status = "UNKN".equals(retailer)
                    ? ImageDoc.Status.UNRECOGNIZED
                    : ImageDoc.Status.RECOGNIZED;
            imageDoc.setStatus(status);
            imageDoc.setAmount(amount);
            imageDoc.setUrl(url);
            imageDocService.update(imageDoc);
        }
    }

/*    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.GCM_MESSAGE, message);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build());
    }*/
}
