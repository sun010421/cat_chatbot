package com.sigma.temitest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MyBaseActivity extends AppCompatActivity {
    public final long DISCONNECT_TIMEOUT = 15000;

    private final Handler endOfInteractionHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return true;
        }
    });

    private final Runnable endOfInteractionCallback = new Runnable() {
        @Override
        public void run() {
            finish(); // activity 끝내기
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
