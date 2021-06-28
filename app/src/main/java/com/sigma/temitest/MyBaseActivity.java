package com.sigma.temitest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MyBaseActivity extends MyWifiBaseActivity { // 두 번째로 상위인 클래스, Timeout 측정하는 용도 (메인 엑티비티를 제외한 모든 엑티비티에서 상속)
    public final long DISCONNECT_TIMEOUT = 20000;

    private final Handler endOfInteractionHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return true;
        }
    });

    private final Runnable endOfInteractionCallback = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    public void resetDisconnectTimer() {
        endOfInteractionHandler.removeCallbacks(endOfInteractionCallback);
        endOfInteractionHandler.postDelayed(endOfInteractionCallback, DISCONNECT_TIMEOUT);
    }

    public void stopDisconnectTimer() {
        endOfInteractionHandler.removeCallbacks(endOfInteractionCallback);
    }

    @Override
    public void onUserInteraction() {
        Log.d("Test: ", "onUserInteraction on popup");
        resetDisconnectTimer();
    }

    @Override
    protected void onResume() {
        Log.d("Test: ", "onResume on popup");
        super.onResume();
        resetDisconnectTimer();
    }

    @Override
    protected void onPause() {
        Log.d("Test: ", "onPause on popup");
        super.onPause();
        stopDisconnectTimer();
    }
}
