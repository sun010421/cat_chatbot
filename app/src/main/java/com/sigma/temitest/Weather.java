package com.sigma.temitest;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Weather extends MyBaseActivity { // 날씨 웹 페이지 클래스
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web);

        final Button toMain = (Button) findViewById(R.id.main_btn);
        toMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://weather.naver.com/today/09620735");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClientClass());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // 뒤로 가기 버튼 이벤트
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) { // 뒤로 가기 버튼을 누르면 메인 엑티비티로 돌아가도록 설정
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
