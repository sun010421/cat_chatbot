package com.sigma.temitest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ChangeActivity extends MyBaseActivity { // 커스텀 구성 버튼으로 불리는 클래스
    int[] currentIndices;
    Button checkedButton;

    ArrayList<Button> buttons;
    Button save;
    Button close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_activity);

        buttons = new ArrayList<Button>();
        buttons.add(findViewById(R.id.button1));
        buttons.add(findViewById(R.id.button2));
        buttons.add(findViewById(R.id.button3));
        buttons.add(findViewById(R.id.button4));
        buttons.add(findViewById(R.id.button5));
        buttons.add(findViewById(R.id.button6));
        buttons.add(findViewById(R.id.button7));
        buttons.add(findViewById(R.id.button8));

        currentIndices = MainActivity.indices.clone();

        for (int i = 0; i < buttons.size(); i++)
            setButtonView(buttons.get(i), currentIndices[i]); // 메인 화면과 동일하게 버튼 배치

        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.indices = currentIndices.clone(); // 메인 엑티비티 변수에 반영

                // 버튼 구성 백업 변수를 업데이트
                SharedPreferences prefs = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("indices", MainActivity.intArrayToString(currentIndices, ","));
                editor.apply();
            }
        });

        close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void setButtonViewDriver(Button button, int index) {
        for (int i = 0; i < currentIndices.length; i++) {
            if (currentIndices[i] == index) { // 바꾸려는 버튼이 중복된다면, 위치 바꿔치기
                int viewNum = currentIndices[buttons.indexOf(button)];
                setButtonView(button, index);
                setButtonView(buttons.get(i), viewNum);
                return;
            }
        }

        setButtonView(button, index);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setButtonView(Button button, int index) {
        // Change DrawableTop
        int drawableTop = ButtonNumber.getButtonDrawable(index);
        button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(drawableTop, null), null, null);

        // Change Text
        int text = ButtonNumber.getButtonText(index);
        button.setText(getString(text));

        // Change onClick
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkedButton = button;
                Intent intent = new Intent(ChangeActivity.this, ListActivity.class);
                intent.putExtra("currentItem", currentIndices[buttons.indexOf(button)]); // 자기 자신은 이후에 못 선택하도록 해야 하기 때문에 그 정보를 전송
                startActivityForResult(intent,0);
            }
        });

        currentIndices[buttons.indexOf(button)] = index; // 바뀐대로 변수 업데이트
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) // Timeout 되어 돌아온 경우
            return;

        Bundle b = data.getExtras();
        int index = b.getInt("toChangeIndex");
        setButtonViewDriver(checkedButton, index);
    }
}