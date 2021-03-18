package com.sigma.temitest;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentThird extends Fragment {

    private MainActivity mainActivity = (MainActivity)getActivity();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MainActivity){
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_3p, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Button dormitory = (Button) getView().findViewById(R.id.dormitory_btn);
        dormitory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mainActivity.chatInitialize("기숙사");
            }
        });

        final Button ssai = (Button) getView().findViewById(R.id.ssai_btn);
        ssai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mainActivity.chatInitialize("연합전공");
            }
        });

        final Button doublemajor = (Button) getView().findViewById(R.id.doublemajor_btn);
        doublemajor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mainActivity.chatInitialize("복수전공, 부전공");
            }
        });

        final Button inout = (Button) getView().findViewById(R.id.inout_btn);
        inout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mainActivity.chatInitialize("전입, 전출");
            }
        });

        final Button club = (Button) getView().findViewById(R.id.club_btn);
//        club.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mainActivity.chatInitialize("동아리");
//            }
//        });

        final Button center = (Button) getView().findViewById(R.id.center_btn);
        center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mainActivity.chatInitialize("학생 센터 위치");
            }
        });

        final Button mentoring = (Button) getView().findViewById(R.id.mentoring_btn);
        mentoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mainActivity.chatInitialize("튜터링, 멘토링");
            }
        });

        final Button counseling = (Button) getView().findViewById(R.id.counseling_btn);
        counseling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mainActivity.chatInitialize("심리상담, 검사");
            }
        });
    }
}