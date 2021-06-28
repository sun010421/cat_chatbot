package com.sigma.temitest;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyAdapterEn extends FragmentStateAdapter { // Fragment 관리하는 클래스 (영어)
    public int mCount;

    public MyAdapterEn(FragmentActivity fa, int count) {
        super(fa);
        mCount = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int index = getRealPosition(position);
        if(index==0) return new FragmentSecondEn();
        else return new FragmentFirstEn();
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public int getRealPosition(int position) { return position % mCount; }
}