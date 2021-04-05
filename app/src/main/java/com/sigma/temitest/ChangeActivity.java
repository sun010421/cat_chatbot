package com.sigma.temitest;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ChangeActivity extends AppCompatActivity {
    ArrayList<Button> buttons;
    Button checkedButton;

    int[] currentIndices;
    int[] savedIndices;

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

        Bundle bundle = getIntent().getExtras();
        currentIndices = bundle.getIntArray("currentItems"); // list 초기화 (0인 것은 없음).
        savedIndices = currentIndices.clone();

        for (int i = 0; i < buttons.size(); i++)
            setButtonView(buttons.get(i), currentIndices[i]); // Parent 화면과 동일하게 초기화.

        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 눌렀을 때의 상황을, 나중에 종료할 때 업데이트 하도록.
                savedIndices = currentIndices.clone();
            }
        });

        close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putIntArray("updates", savedIndices);
                intent.putExtras(bundle);

                setResult(0, intent);
                finish();
            }
        });
    }

    // 중복되는 버튼이 있는 경우, 원래 것을 바꿔치기 하도록.
    public void setButtonViewDriver(Button button, int index) {
        for (int i = 0; i < currentIndices.length; i++) {
            if (currentIndices[i] == index) {
                int viewNum = currentIndices[buttons.indexOf(button)];
                setButtonView(button, index);
                setButtonView(buttons.get(i), viewNum);
                return;
            }
        }

        setButtonView(button, index);
    }

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
                intent.putExtra("currentItem", currentIndices[buttons.indexOf(button)]);
                startActivityForResult(intent,0);
            }
        });

        currentIndices[buttons.indexOf(button)] = index; // list 조정.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle b = data.getExtras();
        int index = b.getInt("toChangeIndex");

        setButtonViewDriver(checkedButton, index);
    }
}

