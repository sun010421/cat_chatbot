package com.sigma.temitest;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import androidx.appcompat.app.AppCompatActivity;

public class MyWifiBaseActivity extends AppCompatActivity { // 가장 상위 (부모) 클래스, 와이파이 연결 끊김을 감지하는 용도
    WifiReceiver wifiReceiver;

    @Override
    protected void onResume() {
        super.onResume();
        wifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
        wifiReceiver.closeDialog();
    }
}
