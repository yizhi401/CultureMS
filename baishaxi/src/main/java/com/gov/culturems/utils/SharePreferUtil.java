package com.gov.culturems.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.gov.culturems.MyApplication;
import com.gov.culturems.common.CommonConstant;

public class SharePreferUtil {

    public static void saveStringToSharePrefer(String key, String value) {
        SharedPreferences preferences = MyApplication.getInstance().getApplicationContext().getSharedPreferences(CommonConstant.SHAREPREFERENCE_NAME, Activity.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static String getStringDataFromSharePreference(String key) {
        return MyApplication.getInstance().getApplicationContext().getSharedPreferences(CommonConstant.SHAREPREFERENCE_NAME, Activity.MODE_PRIVATE).getString(key, null);
    }

    public static void saveIntToSharePrefer(String key, int value) {
        SharedPreferences preferences = MyApplication.getInstance().getApplicationContext().getSharedPreferences(CommonConstant.SHAREPREFERENCE_NAME, Activity.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getIntDataFromSharePreference(String key) {
        return MyApplication.getInstance().getApplicationContext().getSharedPreferences(CommonConstant.SHAREPREFERENCE_NAME, Activity.MODE_PRIVATE).getInt(key, 0);
    }

    public static int getIntDataFromSharePreference(String key, int defualtNum) {
        return MyApplication.getInstance().getApplicationContext().getSharedPreferences(CommonConstant.SHAREPREFERENCE_NAME, Activity.MODE_PRIVATE).getInt(key, defualtNum);
    }

    public static void saveBooleanToSharePrefer(String key, boolean value) {
        SharedPreferences preferences = MyApplication.getInstance().getApplicationContext().getSharedPreferences(CommonConstant.SHAREPREFERENCE_NAME, Activity.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBooleanDataFromSharePreference(String key) {
        return MyApplication.getInstance().getApplicationContext().getSharedPreferences(CommonConstant.SHAREPREFERENCE_NAME, Activity.MODE_PRIVATE).getBoolean(key, false);
    }

    public static void saveCookie(String cookie) {
        saveStringToSharePrefer("cookie", cookie);
    }

    public static String getCookie() {
        return getStringDataFromSharePreference("cookie");
    }

    public static void saveIsApproveDataChang(boolean isChange) {
        saveBooleanToSharePrefer("is_approve_change", isChange);
    }

    public static boolean getIsApproveDataChange() {
        return getBooleanDataFromSharePreference("is_approve_change");
    }

    public static void saveIsNoticeDataChang(boolean isChange) {
        saveBooleanToSharePrefer("is_notice_change", isChange);
    }

    public static boolean getIsNoticeDataChange() {
        return getBooleanDataFromSharePreference("is_notice_change");
    }

    public static void saveIsColumnDataChang(boolean isChange) {
        saveBooleanToSharePrefer("is_column_change", isChange);
    }

    public static boolean getIsColumnDataChange() {
        return getBooleanDataFromSharePreference("is_column_change");
    }

    public static void saveIsAvatarChang(boolean isChange) {
        saveBooleanToSharePrefer("is_avatar_change", isChange);
    }

    public static boolean getAvatarDataChange() {
        return getBooleanDataFromSharePreference("is_avatar_change");
    }

}
