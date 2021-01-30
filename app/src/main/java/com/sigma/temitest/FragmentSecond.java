package com.sigma.temitest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentSecond extends Fragment {

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
                R.layout.fragment_2p, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Button scholarship = (Button) getView().findViewById(R.id.scholarship_btn);
        scholarship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("장학금, 학자금");
//                팝업 사용할 시
//                Intent intent = new Intent(getActivity(), PopupActivity.class);
//                intent.putExtra("data", "TEST");
//                startActivityForResult(intent, 1);
            }
        });

        final Button graduation = (Button) getView().findViewById(R.id.graduation_btn);
        graduation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("휴학, 복학, 졸업");
            }
        });

        final Button locker = (Button) getView().findViewById(R.id.locker_btn);
        locker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("출입등록, 사물함");
            }
        });

        final Button grade = (Button) getView().findViewById(R.id.grade_btn);
        grade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("성적, 강의평가");
            }
        });

        final Button apply = (Button) getView().findViewById(R.id.apply_btn);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("수강신청, 수강취소");
            }
        });

        final Button doublemajor = (Button) getView().findViewById(R.id.doublemajor_btn);
        doublemajor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("복수전공, 부전공");
            }
        });

        final Button inout = (Button) getView().findViewById(R.id.inout_btn);
        inout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("전입, 전출");
            }
        });

        final Button certificate = (Button) getView().findViewById(R.id.certificate_btn);
        certificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("증명서 발급");
            }
        });
    }
}