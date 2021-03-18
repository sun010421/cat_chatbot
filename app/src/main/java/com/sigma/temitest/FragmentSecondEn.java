package com.sigma.temitest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentSecondEn extends Fragment {

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
                R.layout.fragment_2p_en, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Button scholarship = (Button) getView().findViewById(R.id.scholarship_btn);
        scholarship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendRequest("Press scholarship button");
                Intent intent = new Intent(getActivity(), PopupActivity2En.class);
                intent.putExtra("text", getString(R.string.scholarship_en));
                startActivity(intent);
            }
        });

        final Button graduation = (Button) getView().findViewById(R.id.graduation_btn);
        graduation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendRequest("Press graduation button");
                Intent intent = new Intent(getActivity(), PopupActivityEn.class);
                intent.putExtra("teacher", getString(R.string.sonsy));
                intent.putExtra("text", getString(R.string.graduation_en));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button locker = (Button) getView().findViewById(R.id.locker_btn);
        locker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendRequest("Press locker button");
                Intent intent = new Intent(getActivity(), PopupActivity2En.class);
                intent.putExtra("text", getString(R.string.locker_en));
                startActivity(intent);
            }
        });

        final Button grade = (Button) getView().findViewById(R.id.grade_btn);
        grade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendRequest("Press grade button");
                Intent intent = new Intent(getActivity(), PopupActivityEn.class);
                intent.putExtra("teacher", getString(R.string.yooey));
                intent.putExtra("text", getString(R.string.grade_en));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button apply = (Button) getView().findViewById(R.id.apply_btn);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendRequest("Press apply button");
                Intent intent = new Intent(getActivity(), PopupActivityEn.class);
                intent.putExtra("teacher", getString(R.string.yooey));
                intent.putExtra("text", getString(R.string.apply_en));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button curriculum = (Button) getView().findViewById(R.id.curriculum_btn);
        curriculum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendRequest("Press curriculum button");
                Intent intent = new Intent(getActivity(), PopupActivityEn.class);
                intent.putExtra("teacher", getString(R.string.yooey));
                intent.putExtra("text", getString(R.string.curriculum_en));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button lab = (Button) getView().findViewById(R.id.lab_btn);
        lab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendRequest("Press lab button");
                Intent intent = new Intent(getActivity(), PopupActivityEn.class);
                intent.putExtra("teacher", getString(R.string.kimma));
                intent.putExtra("text", getString(R.string.lab_en));
                getActivity().startActivityForResult(intent, 1);
            }
        });

        final Button certificate = (Button) getView().findViewById(R.id.certificate_btn);
        certificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.sendRequest("Press certificate button");
                Intent intent = new Intent(getActivity(), PopupActivity2En.class);
                intent.putExtra("text", getString(R.string.certificate_en));
                startActivity(intent);
            }
        });
    }
}