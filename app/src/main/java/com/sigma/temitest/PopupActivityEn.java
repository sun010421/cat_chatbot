package com.sigma.temitest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PopupActivityEn extends AppCompatActivity {

    private static final String TAG = "PopupActivity";
    TextView popupText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity_en);

        //UI 객체생성
        popupText = (TextView)findViewById(R.id.popupText);

        //데이터 가져오기
        String text = getIntent().getStringExtra("text");
        popupText.setText(text);
    }

    //확인 버튼 클릭
    public void Yes(View v){
        //데이터 전달하기
        Log.d(TAG, "Yes: Press Yes Btn");
        String teacher = getIntent().getStringExtra("teacher");
        Log.d(TAG, "Yes: teacher - " + teacher);
        Intent intent = new Intent();
        intent.putExtra("teacher", teacher);
        intent.putExtra("result", "Yes");
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    public void No(View v){
        Log.d(TAG, "No: Press No Btn");
        finish();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //바깥레이어 클릭시 안닫히게
//        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
//            return false;
//        }
//        return true;
//    }

//    @Override
//    public void onBackPressed() {
//        //안드로이드 백버튼 막기
//        return;
//    }
}