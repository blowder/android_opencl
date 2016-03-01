package com.shl.checkpin.android;

import android.support.multidex.MultiDexApplication;
import com.shl.checkpin.android.factories.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sesshoumaru on 10.02.16.
 */
public class CheckPinApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Injector.init(getModules().toArray());
        //Injector.inject(this);
    }

    private List<Object> getModules() {
        return Arrays.asList(new ApplicationModule(this),
                new LocatorModule(this),
                new ViewModule(),
                new ActivityModule(),
                new AsynkTaskModule(),
                new ImageDocServiceModule(),
                new UploadServiceModule());
    }
}
