package com.shl.checkpin.android.factories;

import com.shl.checkpin.android.activities.HistoryActivity;
import com.shl.checkpin.android.activities.HistoryActivity2;
import com.shl.checkpin.android.activities.MainScreenActivity;
import com.shl.checkpin.android.activities.SelectBillAreaActivity;
import com.shl.checkpin.android.gcm.MyGcmListenerService;
import dagger.Module;

/**
 * Created by sesshoumaru on 11.02.16.
 */
@Module(complete = false,
        injects = {MainScreenActivity.class,
                SelectBillAreaActivity.class,
                HistoryActivity.class,
                HistoryActivity2.class,
                MyGcmListenerService.class})
public class ActivityModule {

}
