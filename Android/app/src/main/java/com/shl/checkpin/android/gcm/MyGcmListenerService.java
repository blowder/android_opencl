package com.shl.checkpin.android.gcm;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import com.shl.checkpin.android.services.JobHolder;
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
        String message = data.getString("message");
        Log.i(TAG, "Message: " + message);

        if (from.startsWith("/topics/upload")
                && "OK".equals(data.getString("status"))) {
            String filename = data.getString("file");
            JobHolder.removeJob(appFileLocator.locate(Environment.DIRECTORY_PICTURES, filename));
        }
    }

}
