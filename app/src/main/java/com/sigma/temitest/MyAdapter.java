package com.sigma.temitest;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyAdapter extends FragmentStateAdapter {
    public int mCount;

    public MyAdapter(FragmentActivity fa, int count) {
        super(fa);
        mCount = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("Test: ", "createFragment on MyAdapter.");

        // 여기도 바껴야 함.
        int index = getRealPosition(position);
        if(index == 0) return new FragmentSecond();
        else return new FragmentFirst();
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public int getRealPosition(int position) { return position % mCount; }
}