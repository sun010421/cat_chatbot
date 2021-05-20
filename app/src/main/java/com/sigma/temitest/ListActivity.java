package com.sigma.temitest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListActivity extends MyBaseActivity {
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
                if (myAdapter.getItem(position) == currentItem) // 자기 자신은 누르지 못하도록.
                    return;

                Intent intent = new Intent();
                intent.putExtra("toChangeIndex", myAdapter.getItem(position)); // data is the value you need in parent
                setResult(0, intent);
                finish();
            }
        });
    }

    public void initializeData() {
        DataList = new ArrayList<Integer>();
        for (int i = 1; i <= ButtonNumber.num; i++)
            DataList.add(i);
    }
}