package com.sigma.temitest;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyListAdapter extends BaseAdapter { // ListActivity 관리하는 클래스
    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<Integer> sample;

    public MyListAdapter(Context context, ArrayList<Integer> data) {
        mContext = context;
        sample = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return sample.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Integer getItem(int position) {
        return sample.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.list_custom, null);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        textView.setText(App.getRes().getString(ButtonNumber.getButtonText(sample.get(position))));

        imageView.setImageResource(ButtonNumber.getButtonDrawable(sample.get(position)));
        if (position % 2 == 1) // 홀수 자리의 프리뷰는 색깔을 교차
            imageView.setColorFilter(Color.parseColor("#BC80C6"));

        return view;
    }
}