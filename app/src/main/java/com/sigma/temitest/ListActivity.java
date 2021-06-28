package com.sigma.temitest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListActivity extends MyBaseActivity { // SettingsActivity 에서 호출되는 클래스
    ArrayList<Integer> DataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setFinishOnTouchOutside(false);
        setContentView(R.layout.list_activity);

        initializeData();
        int currentItem = getIntent().getIntExtra("currentItem", 0);

        ListView listView = (ListView) findViewById(R.id.listView);
        final MyListAdapter myAdapter = new MyListAdapter(this, DataList);

        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                if (myAdapter.getItem(position) == currentItem) // 자기 자신은 못 누르도록 설정
                    return;

                Intent intent = new Intent();
                intent.putExtra("toChangeIndex", myAdapter.getItem(position));
                setResult(0, intent);
                finish();
            }
        });
    }

    public void initializeData() { // (현재) 24가지 버튼 선택지의 초기화
        DataList = new ArrayList<Integer>();
        for (int i = 0; i < ButtonNumber.num; i++)
            DataList.add(i);
    }
}
