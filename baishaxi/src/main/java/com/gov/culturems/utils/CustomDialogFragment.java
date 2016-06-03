package com.gov.culturems.utils;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.gov.culturems.R;
import com.gov.culturems.common.CommonConstant;

import java.util.Timer;
import java.util.TimerTask;

public class CustomDialogFragment extends DialogFragment {

    private ImageView mProgressImage;
    private Timer mTimer = null;

    public static CustomDialogFragment newInstance(int type, String strSrc) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putString("strSrc", strSrc);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int style = DialogFragment.STYLE_NO_FRAME, theme = 0;
        setStyle(style, theme);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.dialog_fragment, null);

        TextView tipText = (TextView) view.findViewById(R.id.tip_text);
        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.icon_layout);
        int type = getArguments().getInt("type");
        switch (type) {
            case CommonConstant.DIALOG_TYPE_SUCCESS:
                relativeLayout.setVisibility(View.GONE);
                break;
            case CommonConstant.DIALOG_TYPE_FAIL:
                relativeLayout.setVisibility(View.GONE);
                break;
            case CommonConstant.DIALOG_TYPE_WAITING:
                relativeLayout.setVisibility(View.VISIBLE);
                RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
                LinearInterpolator lin = new LinearInterpolator();
                animation.setInterpolator(lin);
                animation.setDuration(2000);
                animation.setRepeatCount(-1);
                mProgressImage = (ImageView) view.findViewById(R.id.progress_iamge);
                mProgressImage.setVisibility(View.VISIBLE);
                mProgressImage.startAnimation(animation);
        }
        tipText.setText(getArguments().getString("strSrc"));

        // 设置定时器关闭自己
        int showTime = 2;
        if (type == CommonConstant.DIALOG_TYPE_WAITING) {
            showTime = 30;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    dismissAllowingStateLoss();
                } catch (Exception e) {}
            }
        }, showTime * 1000);

        return view;
    }

    @Override
    public void onDestroyView() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mProgressImage != null) {
            mProgressImage.clearAnimation();
            mProgressImage.setVisibility(View.GONE);
        }
        if (getDialog() != null && getRetainInstance()) getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

}
