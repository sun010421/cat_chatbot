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
                mainActivity.chatInitialize("Press scholarship button");
                Intent intent = new Intent(getActivity(), PopupActivity2.class);
                intent.putExtra("text", getString(R.string.scholarship));
                startActivity(intent);
            }
        });

        final Button graduation = (Button) getView().findViewById(R.id.graduation_btn);
        graduation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("Press graduation button");
                Intent intent = new Intent(getActivity(), PopupActivity.class);
                intent.putExtra("teacher", getString(R.string.sonsy));
                intent.putExtra("text", getString(R.string.graduation));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button locker = (Button) getView().findViewById(R.id.locker_btn);
        locker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("Press locker button");
                Intent intent = new Intent(getActivity(), PopupActivity2.class);
                intent.putExtra("text", getString(R.string.locker));
                startActivity(intent);
            }
        });

        final Button grade = (Button) getView().findViewById(R.id.grade_btn);
        grade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("Press grade button");
                Intent intent = new Intent(getActivity(), PopupActivity.class);
                intent.putExtra("teacher", getString(R.string.yooey));
                intent.putExtra("text", getString(R.string.grade));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button apply = (Button) getView().findViewById(R.id.apply_btn);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("Press apply button");
                Intent intent = new Intent(getActivity(), PopupActivity.class);
                intent.putExtra("teacher", getString(R.string.yooey));
                intent.putExtra("text", getString(R.string.apply));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button curriculum = (Button) getView().findViewById(R.id.curriculum_btn);
        curriculum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("Press curriculum button");
                Intent intent = new Intent(getActivity(), PopupActivity.class);
                intent.putExtra("teacher", getString(R.string.yooey));
                intent.putExtra("text", getString(R.string.curriculum));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button lab = (Button) getView().findViewById(R.id.lab_btn);
        lab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("Press lab button");
                Intent intent = new Intent(getActivity(), PopupActivity.class);
                intent.putExtra("teacher", getString(R.string.kimma));
                intent.putExtra("text", getString(R.string.lab));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button certificate = (Button) getView().findViewById(R.id.certificate_btn);
        certificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.chatInitialize("Press certificate button");
                Intent intent = new Intent(getActivity(), PopupActivity2.class);
                intent.putExtra("text", "증명서 발급은 행정실 입구 오른쪽에 있는 노트북을 사용해주세요.\n마이스누 - 학사정보 - 증명/확인서 탭에서 발급 받으실 수 있습니다.");
                startActivity(intent);
            }
        });
    }
}