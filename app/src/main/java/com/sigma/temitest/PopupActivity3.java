package com.sigma.temitest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PopupActivity3 extends AppCompatActivity {

    private static final String TAG = "PopupActivity";
    ImageView popupImage3;
    TextView popupText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity3);

        //UI 객체생성
        popupImage3 = (ImageView) findViewById(R.id.imageView);
        popupText = (TextView) findViewById(R.id.popupText);

        //데이터 가져오기
        String locker = getIntent().getStringExtra("locker");
        String access = getIntent().getStringExtra("access");
        String mysnu = getIntent().getStringExtra("mysnu");

        if (locker != null) {
            popupText.setText(locker);
            popupImage3.setImageDrawable(getResources().getDrawable(R.drawable.locker_apply, null));
        }
        else if (mysnu != null) {
            popupText.setText(mysnu);
            popupImage3.setImageDrawable(getResources().getDrawable(R.drawable.mysnu, null));
        }
        else if (access != null) {
            popupText.setText(access);
            popupImage3.setImageDrawable(getResources().getDrawable(R.drawable.access, null));
        }
    }

    //확인 버튼 클릭
    public void Ok(View v) {
        Log.d(TAG, "No: Press No Btn");
        finish();
    }
}