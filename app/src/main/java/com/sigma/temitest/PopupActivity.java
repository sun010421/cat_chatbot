package com.sigma.temitest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PopupActivity extends MyBaseActivity { // 선생님 자리로 이동할지 물어보는 팝업 클래스 (한국어)
    private static final String TAG = "PopupActivity";
    TextView popupText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity);

        // UI 객체생성
        popupText = (TextView)findViewById(R.id.popupText);

        // 데이터 가져오기
        String text = getIntent().getStringExtra("text");
        popupText.setText(text);
    }

    // 확인 버튼 클릭한 경우의 함수
    public void Yes(View v) {
        Log.d(TAG, "Yes: Press Yes Btn");
        String teacher = getIntent().getStringExtra("teacher");
        Log.d(TAG, "Yes: teacher - " + teacher);
        Intent intent = new Intent();
        intent.putExtra("teacher", teacher);
        intent.putExtra("result", "Yes");
        setResult(RESULT_OK, intent); // 데이터 전달

        finish();
    }

    public void No(View v) {
        Log.d(TAG, "No: Press No Btn");
        finish();
    }
}