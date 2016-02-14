package com.shl.checkpin.android.factories;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Created by sesshoumaru on 12.02.16.
 */
@Module(library = true)
public class ApplicationModule {
    private Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    public Application getApplication() {
        return application;
    }

    @Provides
    public Context getContext() {
        return application.getApplicationContext();
    }

    @Provides
    public SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());
    }

}
