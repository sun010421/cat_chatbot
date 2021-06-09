package com.sigma.temitest;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.robotemi.sdk.Robot;

import static android.content.Context.WIFI_SERVICE;
import static android.net.wifi.WifiManager.EXTRA_NETWORK_INFO;

public class WifiReceiver extends BroadcastReceiver {
    private Robot robot;

    String prev_state = null;
    AlertDialog wifiDialog;

    public void openDialog(Context context) {
        if (wifiDialog != null && wifiDialog.isShowing())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.wifi_activity, null);
        builder.setView(view);

        wifiDialog = builder.create();
        wifiDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        wifiDialog.setCanceledOnTouchOutside(false);
        wifiDialog.setCancelable(false);

        wifiDialog.show();

        robot = Robot.getInstance();
        robot.showTopBar();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(wifiDialog.getWindow().getAttributes());
        lp.width = 1500;
        lp.height = 600;
        wifiDialog.getWindow().setAttributes(lp);

        // Wifi enable 의 문제인지 확인
        try {
        //WifiConfiguration wifiConfig = new WifiConfiguration();
        //wifiConfig.SSID = String.format("\"%s\"", App.getRes().getString(R.string.wifi_ssid));
        //wifiConfig.preSharedKey = String.format("\"%s\"", App.getRes().getString(R.string.wifi_key));

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        //int netId = wifiManager.addNetwork(wifiConfig);
        //wifiManager.disconnect();
        //wifiManager.enableNetwork(netId, true);
        //wifiManager.reconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeDialog() {
        if (wifiDialog != null && wifiDialog.isShowing())
            wifiDialog.dismiss();
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        // Log.d("Test: ", context.toString());

        NetworkInfo info = intent.getParcelableExtra(EXTRA_NETWORK_INFO);
        if (info == null) return;
        String state = info.getState().toString();
        Log.d("Test: ", "wifi was " + prev_state);
        Log.d("Test: ", "wifi is " + state);

        if (prev_state == null) {
            prev_state = state;
            if (state.equals("DISCONNECTED")) // Resume 시에 애초에 연결이 안되어있는 경우
                openDialog(context);
            return;
        }

        // Wifi가 끊긴 경우
        if (prev_state.equals("CONNECTED") &&
                state.equals("DISCONNECTED"))
            openDialog(context);

        // Wifi가 연결된 경우
        else if (prev_state.equals("CONNECTING") &&
                state.equals("CONNECTED"))
            closeDialog();

        prev_state = state;
    }
};
