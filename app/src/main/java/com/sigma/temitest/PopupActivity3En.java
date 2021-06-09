package com.sigma.temitest;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PopupActivity3En extends MyBaseActivity {

    private static final String TAG = "PopupActivity";
    ImageView popupImage3;
    TextView popupText;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity3_en);

        // UI 객체생성
        popupImage3 = (ImageView) findViewById(R.id.imageView);
        popupText = (TextView) findViewById(R.id.popupText);

        // 데이터 가져오기
        final String text;
        String locker = getIntent().getStringExtra("locker");
        if (locker != null) {
            text = locker;
            popupImage3.setImageDrawable(getResources().getDrawable(R.drawable.picture_locker, null));
        } else {
            String mysnu = getIntent().getStringExtra("mysnu");
            if (mysnu != null) {
                text = mysnu;
                popupImage3.setImageDrawable(getResources().getDrawable(R.drawable.picture_certificate, null));
            } else {
                String access = getIntent().getStringExtra("access");
                if (access != null) {
                    text = access;
                    popupImage3.setImageDrawable(getResources().getDrawable(R.drawable.picture_access, null));
                } else {
                    text = null;
                }
            }
        }

        SpannableString ss = new SpannableString(text);

        int startIndex = text.indexOf("http");
        while (startIndex != -1) { // 돌아간다는 것은 링크가 있다는 것
            final int finalStartIndex = startIndex;
            int temp = text.indexOf("\n", finalStartIndex);
            final int finalEndIndex = temp == -1? text.length(): temp;

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent intent = new Intent(PopupActivity3En.this, Web.class);
                    intent.putExtra("url", text.substring(finalStartIndex, finalEndIndex));
                    startActivity(intent);
                }
            };

            ss.setSpan(clickableSpan, finalStartIndex, finalEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            startIndex = text.indexOf("http", finalEndIndex);
        }

        popupText.setText(ss);
        popupText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // 확인 버튼 클릭
    public void Ok(View v) {
        Log.d(TAG, "No: Press No Btn");
        finish();
    }
}