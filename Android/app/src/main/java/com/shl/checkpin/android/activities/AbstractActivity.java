package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.os.Bundle;
import com.shl.checkpin.android.factories.Injector;

/**
 * Created by sesshoumaru on 11.02.16.
 */
public class AbstractActivity extends Activity {
    protected final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }
}
