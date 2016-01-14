package com.shl.checkpin.android.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;
import com.shl.checkpin.android.activities.HistoryActivity;
import com.shl.checkpin.android.opencv.ImageProcessingService;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * Created by sesshoumaru on 03.01.16.
 */
public class AndroidUtils {
    private static final String TAG = "AndroidUtils.class";

    public static boolean isInetConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = connectivityManager.getActiveNetworkInfo();
        return i != null && i.isConnected() && i.isAvailable();
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneId = tMgr.getLine1Number();
        phoneId = phoneId == null || phoneId.isEmpty() ? tMgr.getSimOperatorName() + "_" + tMgr.getSimSerialNumber() : phoneId;
        if (phoneId.startsWith("+"))
            phoneId = phoneId.replace("+", "");
        return phoneId;
    }

    public static Point getScreenDimension(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int mmInPixels(Activity activity, int mm){
        double InchInSm = 2.54;
        double InchInMm = InchInSm*10;
        double inches = mm*(1/InchInMm);
        return (int)(getScreenDpi(activity)*inches);
    }

    public static int getScreenDpi(Activity activity){
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.densityDpi;
    }


    public static void toast(Context context, String message) {
        toast(context, message, Toast.LENGTH_LONG);
    }

    public static void toast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }
}
