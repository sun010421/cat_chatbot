package com.sigma.temitest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsActivity extends MyBaseActivity {
    private Robot robot;
    private boolean settingIsLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings_activity);
        setFinishOnTouchOutside(true);

        robot = Robot.getInstance();
        settingIsLocked = getIntent().getBooleanExtra("settingIsLocked", false);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = - metrics.widthPixels / 2;

        ArrayList<myGroup> DataList = new ArrayList<myGroup>();
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.mySettingsList);
        myGroup temp = new myGroup("이동");
        temp.child = robot.getLocations();
        DataList.add(temp);

        temp = new myGroup("조절");
        temp.child.add("볼륨·조명");
        temp.child.add("재시작");
        temp.child.add("전원 끄기");
        //temp.child.add("이동 속도");
        DataList.add(temp);

        temp = new myGroup("버튼 구성");
        temp.child.add("커스텀 구성");
        temp.child.add("인기 구성");
        temp.child.add("구성 초기화");
        DataList.add(temp);

        temp = new myGroup("테미 설정");
        DataList.add(temp);

        temp = new myGroup("눌러서 잠금 해제");
        DataList.add(temp);

        ExpandAdapter adapter = new ExpandAdapter (
                getApplicationContext(),
                R.layout.group_row,
                R.layout.child_row,
                DataList,
                settingIsLocked);
        listView.setAdapter(adapter);

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("Test: child clicked: ", String.valueOf(childPosition));

                if (groupPosition == 0) {
                    String data = ((TextView) v.findViewById(R.id.childName)).getText().toString();

                    if (data.equals("홈베이스")) data = "home base";
                    robot.goTo(data);
                }

                else if (groupPosition == 1) {
                    if (childPosition == 0)
                        robot.setVolume(2);
                    else if (childPosition == 1)
                        robot.restart();
                    else if (childPosition == 2)
                        robot.shutdown();
                }

                else if (groupPosition == 2) {
                    if (childPosition == 0) {
                        Intent intent = new Intent(SettingsActivity.this, ChangeActivity.class);
                        startActivity(intent);

                        finish();
                    }

                    else if (childPosition == 1) {
                        // 클릭 백업 가져와서 구성 업데이트
                        SharedPreferences prefs = getSharedPreferences(MainActivity.BUTTON_CLICKS, MODE_PRIVATE);
                        int[] button_clicks = MainActivity.stringToIntArray(prefs.getString("indices", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"), ",", 24).clone();
                        setIndicesToPopular(button_clicks);

                        // 버튼 구성 백업 업데이트
                        prefs = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("indices", MainActivity.intArrayToString(MainActivity.indices, ","));
                        editor.apply();

                        finish();
                    }

                    else {
                        // 클릭 백업 초기화
                        SharedPreferences prefs = getSharedPreferences(MainActivity.BUTTON_CLICKS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("indices", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
                        editor.apply();

                        // 버튼 구성 백업 초기화
                        MainActivity.indices = new int[]{0, 1, 2, 3, 4, 5, 6, 7};

                        prefs = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                        editor = prefs.edit();
                        editor.putString("indices", MainActivity.intArrayToString(MainActivity.indices, ","));
                        editor.apply();

                        finish();
                    }
                }

                return true;
            }
        });

        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                Log.d("Test: group clicked: ", String.valueOf(groupPosition));

                if (groupPosition == 3) {
                    robot.showAppList();
                }

                if (groupPosition == 4) {
                    settingIsLocked = !settingIsLocked;
                    adapter.changeLock();
                }

                return false;
            }
        });
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putBoolean("settingIsLocked", settingIsLocked);
        intent.putExtras(bundle);

        setResult(0, intent);
        super.finish();
        overridePendingTransition(0, R.anim.slide_in);
    }

    public void setIndicesToPopular(int[] arr) {
        int[] temp = arr.clone();

        int[] arg = new int[temp.length];
        for (int i = 0; i < arg.length; i++)
            arg[i] = i;

        for (int i = 1; i < temp.length; i++) {
            int standard = temp[i];
            int aux = i - 1;

            while (aux >= 0 && standard <= temp[aux]) {
                temp[aux + 1] = temp[aux];
                arg[aux + 1] = arg[aux];
                aux--;
            }

            temp[aux + 1] = standard;
            arg[aux + 1] = i;
        }

        for (int i = 0; i < MainActivity.indices.length; i++)
            MainActivity.indices[i] = arg[arg.length - 1 - i];
    }
}
