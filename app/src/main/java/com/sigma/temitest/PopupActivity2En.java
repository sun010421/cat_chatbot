package com.sigma.temitest;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PopupActivity2En extends MyBaseActivity { // 긴 답변 포함하는 팝업 클래스 (영어)

    private static final String TAG = "PopupActivity";
    TextView popupText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity2_en);

        // UI 객체생성
        popupText2 = (TextView)findViewById(R.id.popupText2);

        // 데이터 가져오기
        String text = getIntent().getStringExtra("text");
        SpannableString ss = new SpannableString(text);

        int startIndex = text.indexOf("http");
        while (startIndex != -1) { // 돌아간다는 것은 링크가 있다는 의미
            final int finalStartIndex = startIndex;
            int temp = text.indexOf("\n", finalStartIndex);
            final int finalEndIndex = temp == -1? text.length(): temp;

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent intent = new Intent(PopupActivity2En.this, Web.class);
                    intent.putExtra("url", text.substring(finalStartIndex, finalEndIndex));
                    startActivity(intent);
                }
            };

            ss.setSpan(clickableSpan, finalStartIndex, finalEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            startIndex = text.indexOf("http", finalEndIndex);
        }

        popupText2.setText(ss);
        popupText2.setMovementMethod(new ScrollingMovementMethod());
        popupText2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void Ok(View v){
        Log.d(TAG, "No: Press No Btn");
        finish();
    }
}