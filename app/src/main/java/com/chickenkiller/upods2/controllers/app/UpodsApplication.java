package com.chickenkiller.upods2.controllers.app;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.chickenkiller.upods2.models.Category;
import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by alonzilberman on 7/31/15.
 */
public class UpodsApplication extends Application {

    private static Context applicationContext;

    @Override
    public void onCreate() {
        applicationContext = getApplicationContext();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        Category.initCatrgories();
        super.onCreate();
    }

    public static Context getContext() {
        return applicationContext;
    }
}