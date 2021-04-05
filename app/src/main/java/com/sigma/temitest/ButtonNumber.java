package com.sigma.temitest;

import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ButtonNumber extends AppCompatActivity {
    public static final int num = 16;

    public static final int SCHOLARSHIP = 1;
    public static final int LOCKER = 2;
    public static final int APPLY = 3;
    public static final int GRADUATION = 4;
    public static final int CURRICULUM = 5;
    public static final int LAB = 6;
    public static final int CERTIFICATE = 7;
    public static final int GRADE = 8;
    public static final int CENTER = 9;
    public static final int CLUB = 10;
    public static final int COUNSELING = 11;
    public static final int DORMITORY = 12;
    public static final int DOUBLEMAJOR = 13;
    public static final int INOUT = 14;
    public static final int MENTORING = 15;
    public static final int SSAI = 16;

    public static int getButtonText(int index) {
        switch (index) {
            case SCHOLARSHIP:
                return R.string.scholarship_btn;
            case LOCKER:
                return R.string.locker_btn;
            case APPLY:
                return R.string.apply_btn;
            case GRADUATION:
                return R.string.graduation_btn;
            case CURRICULUM:
                return R.string.curriculum_btn;
            case LAB:
                return R.string.lab_btn;
            case CERTIFICATE:
                return R.string.certificate_btn;
            case GRADE:
                return R.string.grade_btn;
            case CENTER:
                return R.string.center_btn;
            case CLUB:
                return R.string.club_btn;
            case COUNSELING:
                return R.string.counseling_btn;
            case DORMITORY:
                return R.string.dormitory_btn;
            case DOUBLEMAJOR:
                return R.string.doublemajor_btn;
            case INOUT:
                return R.string.inout_btn;
            case MENTORING:
                return R.string.mentoring_btn;
            case SSAI:
                return R.string.ssai_btn;
            default:
                return 0;
        }
    }

    public static int getButtonText_En(int index) {
        switch (index) {
            case SCHOLARSHIP:
                return R.string.scholarship_btn_en;
            case LOCKER:
                return R.string.locker_btn_en;
            case APPLY:
                return R.string.apply_btn_en;
            case GRADUATION:
                return R.string.graduation_btn_en;
            case CURRICULUM:
                return R.string.curriculum_btn_en;
            case LAB:
                return R.string.lab_btn_en;
            case CERTIFICATE:
                return R.string.certificate_btn_en;
            case GRADE:
                return R.string.grade_btn_en;
            case CENTER:
                return R.string.center_btn_en;
            case CLUB:
                return R.string.club_btn_en;
            case COUNSELING:
                return R.string.counseling_btn_en;
            case DORMITORY:
                return R.string.dormitory_btn_en;
            case DOUBLEMAJOR:
                return R.string.doublemajor_btn_en;
            case INOUT:
                return R.string.inout_btn_en;
            case MENTORING:
                return R.string.mentoring_btn_en;
            case SSAI:
                return R.string.ssai_btn_en;
            default:
                return 0;
        }
    }

    public static int getButtonDrawable(int index) {
        switch (index) {
            case SCHOLARSHIP:
                return R.drawable.scholarship_top;
            case LOCKER:
                return R.drawable.locker_top;
            case APPLY:
                return R.drawable.apply_top;
            case GRADUATION:
                return R.drawable.graduation_top;
            case CURRICULUM:
                return R.drawable.curriculum_top;
            case LAB:
                return R.drawable.lab_top;
            case CERTIFICATE:
                return R.drawable.certificate_top;
            case GRADE:
                return R.drawable.grade_top;
            case CENTER:
                return R.drawable.center_top;
            case CLUB:
                return R.drawable.club_top;
            case COUNSELING:
                return R.drawable.counseling_top;
            case DORMITORY:
                return R.drawable.dormitory_top;
            case DOUBLEMAJOR:
                return R.drawable.doublemajor_top;
            case INOUT:
                return R.drawable.inout_top;
            case MENTORING:
                return R.drawable.mentoring_top;
            case SSAI:
                return R.drawable.ssai_top;
            default:
                return 0;
        }
    }
}
