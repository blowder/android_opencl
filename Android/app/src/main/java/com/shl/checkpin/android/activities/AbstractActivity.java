package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.shl.checkpin.android.factories.Injector;

/**
 * Created by sesshoumaru on 11.02.16.
 */
public class AbstractActivity extends Activity {
    protected final String TAG = this.getClass().getSimpleName();
    protected SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
