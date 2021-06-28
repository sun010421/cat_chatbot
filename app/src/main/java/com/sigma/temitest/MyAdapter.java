package com.sigma.temitest;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyAdapter extends FragmentStateAdapter { // Fragment 관리하는 클래스 (한국어)
    public int mCount;

    public MyAdapter(FragmentActivity fa, int count) {
        super(fa);
        mCount = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("Test: ", "createFragment on MyAdapter.");

        int index = getRealPosition(position);
        if(index == 0) return new FragmentSecond();
        else return new FragmentFirst();
    }

    @Override
    public int getItemCount() {
        return 2; // 페이지 수는 2개
    }

    public int getRealPosition(int position) { return position % mCount; }
}