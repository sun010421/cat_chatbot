package com.sigma.temitest;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class App extends Application { // 엑티비티가 아닌 클래스에서도 앱 Context, Resource 등을 접근할 수 있도록 하는 클래스
    // Manifest 내에서의 추가적인 조작도 필요한 점 참고
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
