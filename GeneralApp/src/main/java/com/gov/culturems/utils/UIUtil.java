package com.gov.culturems.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;

import com.gov.culturems.MyApplication;

import java.util.List;


public class UIUtil {


    public synchronized static void showTipDialog(Context context, int type, int tipStringResID) {
        showTipDialog(context, type, tipStringResID, 2, null);
    }

    public synchronized static void showTipDialog(Context context, int type, int tipStringResID, TimeoutInterface handler) {
        showTipDialog(context, type, tipStringResID, 2, handler);
    }

    public synchronized static void showTipDialog(Context context, int type, String tipString) {
        showTipDialog(context, type, tipString, 2, null);
    }

    public synchronized static void showTipDialog(Context context, int type, String tipString, TimeoutInterface handler) {
        showTipDialog(context, type, tipString, 2, handler);
    }

    public synchronized static void showTipDialog(final Context context, int type, int tipStringResID, int duration,
                                                  final TimeoutInterface timeroutHandler) {

        String text = context.getResources().getString(tipStringResID);
        showTipDialog(context, type, text, duration, timeroutHandler);
    }

    private static Integer mPreDialogLock = 0;
    private static Fragment mPreDialogFragment;

    public synchronized static void showTipDialog(final Context context, int type, String text, int duration, final TimeoutInterface timeroutHandler) {
        if (context == null) return;

        // Activity不在最前端，不显示TipDialog
//        if (!isTopActivity((Activity) context)) {
//            return;
//        }

        synchronized (mPreDialogLock) {
            // 假如已经有一个dialog在显示，那么关闭它
            FragmentManager fm = ((Activity) context).getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            try {
                if (mPreDialogFragment != null) {
                    ft.remove(mPreDialogFragment);
                }
            } catch (Exception e) {
                // 虽然判断了是否为null，但是，还是有可能在remove的时候已经被timer dismiss掉了
            }

            // 显示当前需要显示的dialog
            CustomDialogFragment newFragment = CustomDialogFragment.newInstance(type, text);
            newFragment.show(ft, "dialog");
            mPreDialogFragment = newFragment;
        }
    }

    public static void showMessageDialog(Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT);
        builder.setMessage(message);
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        builder.create().show();
    }

    /**
     * Dismiss tip dialog.
     *
     * @param context the context
     */
    @SuppressLint("Recycle")
    public synchronized static void dismissTipDialog(Context context) {
        if (context != null) {
            synchronized (mPreDialogLock) {
                // 关闭dialog视图
                FragmentManager fm = ((Activity) context).getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                try {
                    if (mPreDialogFragment != null) {
                        ft.remove(mPreDialogFragment).commit();
                        mPreDialogFragment = null;
                    }
                } catch (Exception e) {
                    // 虽然判断了是否为null，但是，还是有可能在remove的时候已经被timer dismiss掉了
                }
            }
        }
    }

    public static interface TimeoutInterface {
        void timeoutHandler();
    }

    /**
     * 根据手机的分辨率从 dp的单位转成为 px(像素)
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素)的单位转成为 dp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int getScreenWidth(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

}
