package com.haoyu.app.base;

import android.app.Application;

import com.uuzuche.lib_zxing.activity.ZXingLibrary;

public class LingNanApplication extends Application {


    private static LingNanApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        ZXingLibrary.initDisplayOpinion(this);
        instance = this;
    }


    public static Application getInstance() {
        return instance;
    }
}
