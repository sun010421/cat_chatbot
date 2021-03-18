package com.sigma.temitest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PopupActivity2 extends AppCompatActivity {

    private static final String TAG = "PopupActivity";
    TextView popupText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity2);

        //UI 객체생성
        popupText2 = (TextView) findViewById(R.id.popupText2);

        //데이터 가져오기
        String text = getIntent().getStringExtra("text");
        popupText2.setText(text);
    }

    //확인 버튼 클릭
    public void Ok(View v) {
        Log.d(TAG, "No: Press No Btn");
        finish();
    }
}