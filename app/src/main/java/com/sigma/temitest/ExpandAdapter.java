package com.sigma.temitest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class ExpandAdapter extends BaseExpandableListAdapter {
    private Context context;
    private int groupLayout = 0;
    private int childLayout = 0;
    private ArrayList<myGroup> DataList;
    private LayoutInflater inflater = null;
    private boolean isLocked;

    private static final int MOVE = 0;
    private static final int LEVELS = 1;
    private static final int CHANGE = 2;
    private static final int LOCK = 3;

    private int lastExpandedGroupPosition;

    public ExpandAdapter(Context context, int groupLay, int childLay, ArrayList<myGroup> DataList, boolean isLocked){
        this.DataList = DataList;
        this.groupLayout = groupLay;
        this.childLayout = childLay;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isLocked = isLocked;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(this.groupLayout, parent, false);

        ImageView arrow =  (ImageView) convertView.findViewById(R.id.arrow);
        if (DataList.get(groupPosition).child.size() > 0) {
            if (isExpanded)
                arrow.setImageResource(R.drawable.upload);
            else arrow.setImageResource(R.drawable.download);
        }

        ImageView groupImage = (ImageView) convertView.findViewById(R.id.groupImage);
        groupImage.setColorFilter(App.getRes().getColor(R.color.colorSecondary, null));

        int drawable = 0;
        switch (groupPosition) {
            case MOVE:
                drawable = R.drawable.move_top;
                break;
            case LEVELS:
                drawable = R.drawable.levels_top;
                break;
            case CHANGE:
                drawable = R.drawable.change_top;
                break;
            case LOCK:
                drawable = R.drawable.locked_top;
                break;
        }
        groupImage.setImageDrawable(App.getRes().getDrawable(drawable, null));

        TextView groupName = (TextView) convertView.findViewById(R.id.groupName);
        groupName.setText(DataList.get(groupPosition).groupName);

        if (groupPosition == LOCK) {
            groupName.setTextColor(App.getRes().getColor(R.color.red, null));

            if (!isLocked) {
                groupImage.setImageDrawable(App.getRes().getDrawable(R.drawable.unlocked_top, null));
                groupName.setText("눌러서 잠금");
            }
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = inflater.inflate(this.childLayout, parent, false);

        TextView childName = (TextView) convertView.findViewById(R.id.childName);
        String data = DataList.get(groupPosition).child.get(childPosition);
        if (data.equals("home base"))
            childName.setText("홈베이스");
        else childName.setText(data);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return DataList.get(groupPosition).child.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return DataList.get(groupPosition).child.size();
    }

    @Override
    public myGroup getGroup(int groupPosition) {
        return DataList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return DataList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public void changeLock() {
        isLocked = !isLocked;
    }
}
