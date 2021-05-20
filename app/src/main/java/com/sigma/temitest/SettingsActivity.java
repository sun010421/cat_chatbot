package com.sigma.temitest;

import android.content.Intent;
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
        temp.child.add("이동 속도");
        DataList.add(temp);

        temp = new myGroup("편집");
        temp.child.add("버튼 구성");
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
                }

                else if (groupPosition == 2) {
                    Intent intent = new Intent(SettingsActivity.this, ChangeActivity.class);
                    startActivity(intent);
                    finish();
                }

                return true;
            }
        });

        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                Log.d("Test: group clicked: ", String.valueOf(groupPosition));

                if (groupPosition == 3) {
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
}
