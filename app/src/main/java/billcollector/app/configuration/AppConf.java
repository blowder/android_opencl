package billcollector.app.configuration;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by vfedin on 11.11.2015.
 */
public class AppConf {
    private final static String fileName = "billcollector.app.configuration.AppConf";

    enum PreferenceKey {
        HOST_NAME
    }

    public static String get(Context context, PreferenceKey key) {
        return getSharedPreferences(context).getString(key.toString(), null);
    }

    public static void put(Context context, PreferenceKey key, String value) {
        getSharedPreferences(context).edit()
                .putString(key.toString(), value)
                .commit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(fileName, 0);
    }

}
