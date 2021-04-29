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

import java.util.ArrayList;

import static com.sigma.temitest.ButtonNumber.ACCESS;
import static com.sigma.temitest.ButtonNumber.APPLY;
import static com.sigma.temitest.ButtonNumber.CENTER;
import static com.sigma.temitest.ButtonNumber.CERTIFICATE;
import static com.sigma.temitest.ButtonNumber.CLUB;
import static com.sigma.temitest.ButtonNumber.COMMON;
import static com.sigma.temitest.ButtonNumber.COMPUTER;
import static com.sigma.temitest.ButtonNumber.COUNSELING;
import static com.sigma.temitest.ButtonNumber.CURRICULUM;
import static com.sigma.temitest.ButtonNumber.DEAN;
import static com.sigma.temitest.ButtonNumber.DORMITORY;
import static com.sigma.temitest.ButtonNumber.DOUBLEMAJOR;
import static com.sigma.temitest.ButtonNumber.ENTRANCE;
import static com.sigma.temitest.ButtonNumber.GRADE;
import static com.sigma.temitest.ButtonNumber.GRADUATION;
import static com.sigma.temitest.ButtonNumber.INOUT;
import static com.sigma.temitest.ButtonNumber.LAB;
import static com.sigma.temitest.ButtonNumber.LIBRARY;
import static com.sigma.temitest.ButtonNumber.LOCKER;
import static com.sigma.temitest.ButtonNumber.MANAGER;
import static com.sigma.temitest.ButtonNumber.MENTORING;
import static com.sigma.temitest.ButtonNumber.MILITARY;
import static com.sigma.temitest.ButtonNumber.SCHOLARSHIP;
import static com.sigma.temitest.ButtonNumber.SSAI;

public class FragmentSecondEn extends Fragment {
    ArrayList<Button> buttons;

    private MainActivity mainActivity = (MainActivity)getActivity();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_2p_en, container, false);

        buttons = new ArrayList<Button>();
        buttons.add((Button) rootView.findViewById(R.id.button1));
        buttons.add((Button) rootView.findViewById(R.id.button2));
        buttons.add((Button) rootView.findViewById(R.id.button3));
        buttons.add((Button) rootView.findViewById(R.id.button4));
        buttons.add((Button) rootView.findViewById(R.id.button5));
        buttons.add((Button) rootView.findViewById(R.id.button6));
        buttons.add((Button) rootView.findViewById(R.id.button7));
        buttons.add((Button) rootView.findViewById(R.id.button8));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int[] indicesOfButton = ((MainActivity) getActivity()).indices;
        for (int i = 0; i < indicesOfButton.length; i++)
            setButtonView(buttons.get(i), indicesOfButton[i]);
    }

    public void setButtonView(Button button, int index) {
        // Change DrawableTop
        int drawableTop = ButtonNumber.getButtonDrawable(index);
        button.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(drawableTop, null), null, null);

        // Change Text
        int text = ButtonNumber.getButtonText_En(index);
        button.setText(getString(text));

        // Change onClick
        setButtonOnClickListener(button, index);
    }

    public void setButtonOnClickListener(Button button, int index) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                switch (index) {
                    case SCHOLARSHIP:
                        mainActivity.sendRequest("Press scholarship button");
                        intent = new Intent(getActivity(), PopupActivity2En.class);
                        intent.putExtra("text", getString(R.string.scholarship_en));
                        startActivity(intent);
                        break;
                    case LOCKER:
                        mainActivity.sendRequest("Press locker button");
                        intent = new Intent(getActivity(), PopupActivity3En.class);
                        intent.putExtra("locker", getString(R.string.locker_en));
                        startActivity(intent);
                        break;
                    case APPLY:
                        mainActivity.sendRequest("Press apply button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.yooey));
                        intent.putExtra("text", getString(R.string.apply_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case GRADUATION:
                        mainActivity.sendRequest("Press graduation button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.sonsy));
                        intent.putExtra("text", getString(R.string.graduation_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case CURRICULUM:
                        mainActivity.sendRequest("Press curriculum button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.yooey));
                        intent.putExtra("text", getString(R.string.curriculum_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case LAB:
                        mainActivity.sendRequest("Press lab button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.kimma));
                        intent.putExtra("text", getString(R.string.lab_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case CERTIFICATE:
                        mainActivity.sendRequest("Press certificate button");
                        intent = new Intent(getActivity(), PopupActivity3En.class);
                        intent.putExtra("mysnu", getString(R.string.certificate_en));
                        startActivity(intent);
                        break;
                    case GRADE:
                        mainActivity.sendRequest("Press grade button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.yooey));
                        intent.putExtra("text", getString(R.string.grade_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case CENTER:
                        mainActivity.sendRequest("Press center button");
                        intent = new Intent(getActivity(), PopupActivity2En.class);
                        intent.putExtra("text", getString(R.string.center_en));
                        startActivity(intent);
                        break;
                    case CLUB:
                        mainActivity.sendRequest("Press club button");
                        intent = new Intent(getActivity(), PopupActivity2En.class);
                        intent.putExtra("text", getString(R.string.club_en));
                        startActivity(intent);
                        break;
                    case COUNSELING:
                        mainActivity.sendRequest("Press counseling button");
                        intent = new Intent(getActivity(), PopupActivity2En.class);
                        intent.putExtra("text", getString(R.string.counseling_en));
                        startActivity(intent);
                        break;
                    case DORMITORY:
                        mainActivity.sendRequest("Press dormitory button");
                        intent = new Intent(getActivity(), PopupActivity2En.class);
                        intent.putExtra("text", getString(R.string.dormitory_en));
                        startActivity(intent);
                        break;
                    case DOUBLEMAJOR:
                        mainActivity.sendRequest("Press doublemajor button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.sonsy));
                        intent.putExtra("text", getString(R.string.doublemajor_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case INOUT:
                        mainActivity.sendRequest("Press inout button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.sonsy));
                        intent.putExtra("text", getString(R.string.inout_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case MENTORING:
                        mainActivity.sendRequest("Press mentoring button");
                        intent = new Intent(getActivity(), PopupActivity2En.class);
                        intent.putExtra("text", getString(R.string.mentoring_en));
                        startActivity(intent);
                        break;
                    case SSAI:
                        mainActivity.sendRequest("Press ssai button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.imes));
                        intent.putExtra("text", getString(R.string.ssai_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case ACCESS:
                        mainActivity.sendRequest("Press access button");
                        intent = new Intent(getActivity(), PopupActivity3En.class);
                        intent.putExtra("access", getString(R.string.access_en));
                        startActivity(intent);
                        break;
                    case LIBRARY:
                        mainActivity.sendRequest("Press library button");
                        intent = new Intent(getActivity(), PopupActivity2En.class);
                        intent.putExtra("text", getString(R.string.library_en));
                        startActivity(intent);
                        break;
                    case MILITARY:
                        mainActivity.sendRequest("Press military button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.shimms));
                        intent.putExtra("text", getString(R.string.military_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case DEAN:
                        mainActivity.sendRequest("Press dean button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.leear));
                        intent.putExtra("text", getString(R.string.dean_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case MANAGER:
                        mainActivity.sendRequest("Press manager button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.shinhc));
                        intent.putExtra("text", getString(R.string.manager_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case ENTRANCE:
                        mainActivity.sendRequest("Press entrance button");
                        intent = new Intent(getActivity(), PopupActivityEn.class);
                        intent.putExtra("teacher", getString(R.string.jangb));
                        intent.putExtra("text", getString(R.string.entrance_en));
                        getActivity().startActivityForResult(intent, 1);
                        break;
                    case COMPUTER:
                        mainActivity.sendRequest("Press computer button");
                        intent = new Intent(getActivity(), PopupActivity2En.class);
                        intent.putExtra("text", getString(R.string.computer_en));
                        startActivity(intent);
                        break;
                    case COMMON:
                        mainActivity.sendRequest("Press common button");
                        intent = new Intent(getActivity(), PopupActivity2En.class);
                        intent.putExtra("text", getString(R.string.common_en));
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
    }
}