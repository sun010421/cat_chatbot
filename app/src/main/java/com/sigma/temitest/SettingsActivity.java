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

        // 화면의 왼쪽에 디스플레이기 되기 위한 코드
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
        temp.child.add("위치 재조정"); // 테미가 자리를 잘 찾아가지 못할 때 영점 조절해주는 버튼
        temp.child.add("재시작");
        temp.child.add("전원 끄기");
        DataList.add(temp);

        temp = new myGroup("버튼 구성");
        temp.child.add("커스텀 구성");
        temp.child.add("인기 구성");
        temp.child.add("구성 초기화");
        DataList.add(temp);

        temp = new myGroup("테미 설정"); // 테미 내부 설정으로 들어가는 버튼
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
                    if (data.equals("홈베이스"))
                        data = "home base";
                    robot.goTo(data);
                }

                else if (groupPosition == 1) {
                    if (childPosition == 0)
                        robot.setVolume(2);
                    else if (childPosition == 1)
                        robot.repose();
                    else if (childPosition == 2)
                        robot.restart();
                    else if (childPosition == 3)
                        robot.shutdown();
                }

                else if (groupPosition == 2) {
                    if (childPosition == 0) {
                        Intent intent = new Intent(SettingsActivity.this, ChangeActivity.class);
                        startActivity(intent);

                        finish();
                    }

                    else if (childPosition == 1) { // 인기 구성 버튼이 눌린 경우
                        // 클릭 백업 변수를 가져와서 버튼 구성 업데이트
                        SharedPreferences prefs = getSharedPreferences(MainActivity.BUTTON_CLICKS, MODE_PRIVATE);
                        int[] button_clicks = MainActivity.stringToIntArray(prefs.getString("indices", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"), ",", 24).clone();
                        setIndicesToPopular(button_clicks);

                        // 위에 맞게 버튼 구성 백업 변수 업데이트
                        prefs = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("indices", MainActivity.intArrayToString(MainActivity.indices, ","));
                        editor.apply();

                        finish();
                    }

                    else { // 구성 초기화 버튼이 눌린 경우
                        // 클릭 백업 변수 초기화
                        SharedPreferences prefs = getSharedPreferences(MainActivity.BUTTON_CLICKS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("indices", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
                        editor.apply();

                        // 버튼 구성 백업 변수 초기화
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

                if (groupPosition == 4) { // 설정 창의 잠김 상태 변환
                    settingIsLocked = !settingIsLocked;
                    adapter.changeLock();
                }

                return false;
            }
        });
    }

    @Override
    public void finish() { // Timeout 으로 인한 종료도 이 함수가 호출된다는 점 참고
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

        // Sorting 코드
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
