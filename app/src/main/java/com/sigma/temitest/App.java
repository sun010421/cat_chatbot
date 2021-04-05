package com.sigma.temitest;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class App extends Application {
    private static Context mContext;
    private static Resources res;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        res = getResources();
    }

    public static Context getContext(){
        return mContext;
    }

    public static Resources getRes() {
        return res;
    }
}
