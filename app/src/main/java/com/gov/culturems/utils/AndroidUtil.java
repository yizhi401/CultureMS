package com.gov.culturems.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gov.culturems.MyApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class AndroidUtil {
    /**
     * whether the mobile phone network is Connecting
     *
     * @param context
     * @return
     */
    public static boolean isConnectInternet(Context context) {

        ConnectivityManager conManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    public static String getUniqueId(Context context) {
        if (!TextUtils.isEmpty(getDiviceId(context))) {
            return getDiviceId(context);
        } else if (!TextUtils.isEmpty(getAndroidId(context))) {
            return getAndroidId(context);
        } else {
            return "000000000";
        }
    }

    public static String getAndroidId(Context context) {
        return Secure
                .getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    public static String getDiviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /*
获取系统版本
 */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Create shortcut
     *
     * @param context
     */
    public static void addShortcut(Context context, int nameSourceId,
                                   int iconRecourceId) {
        String appName = context.getApplicationContext().getResources()
                .getString(nameSourceId);
        addShortcut(context, appName, iconRecourceId);
    }


    public static void addShortcut(Context context, String shortName,
                                   int iconRecourceId) {
        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        // SHORTCUT_NAME
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortName);
        shortcut.putExtra("duplicate", false); // not allow Repeat
        // current Activity shortcuts to launch objects:such as
        // //com.everest.video.VideoPlayer
        // Note: the ComponentName second parameters must be coupled with a dot
        // (. ), or a shortcut to start the corresponding procedures
        ComponentName comp = new ComponentName(context.getPackageName(),
                "com.tixa.lxcenter.login.PreLogin");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(
                Intent.ACTION_MAIN).setComponent(comp));
        // SHORTCUT_ICON
        ShortcutIconResource iconRes = ShortcutIconResource.fromContext(
                context, iconRecourceId);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        context.sendBroadcast(shortcut);
    }

    public static void addShortcut(Context context, String appName,
                                   int appLogoId, String targetClassName) {
        try {
            Intent shortcut = new Intent(
                    "com.android.launcher.action.INSTALL_SHORTCUT");
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
            shortcut.putExtra("duplicate", false); // not allow Repeat
            Intent extraIntent = new Intent(context,
                    Class.forName(targetClassName));
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, extraIntent);
            ShortcutIconResource iconRes = ShortcutIconResource
                    .fromContext(context, appLogoId);
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
            context.sendBroadcast(shortcut);
        } catch (ClassNotFoundException e) {
        }
    }

    // add someone's shortcut
    public static void addShortcut(Context context, String name, String mobile,
                                   int iconRecourceId) {
        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        // SHORTCUT_NAME
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        shortcut.putExtra("duplicate", false);
        Intent extraIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel://"
                + mobile));
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, extraIntent);
        // SHORTCUT_icon
        ShortcutIconResource iconRes = ShortcutIconResource.fromContext(
                context, iconRecourceId);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        context.sendBroadcast(shortcut);
    }

    public static String getUserAgent() {
        String user_agent = "";
        try {
            String model = Build.MODEL;
            String sdkNum = Build.VERSION.SDK;
            String frameNum = Build.VERSION.RELEASE;
            // user_agent = "android_" + Build.MODEL + "_" + frameNum + "_"
            // + versonNum + ",SDKNum ";
            user_agent = "Mobile Model-->" + model + "\n SDK Model-->" + sdkNum
                    + "\n System Model-->" + frameNum
                    + "\n";
            return user_agent;
        } catch (Exception e) {
        }
        return user_agent;
    }

    public static boolean hasLocalChina() {
        boolean has = false;
        Locale locale[] = Locale.getAvailableLocales();
        for (int i = 0; i < locale.length; i++) {
            if (locale[i].equals(Locale.CHINA)) {
                has = true;
                break;
            }
        }
        return has;
    }

    public static int ScreenOrient(Activity activity) {
        int orient = activity.getRequestedOrientation();
        if (orient != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                && orient != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            WindowManager wm = activity.getWindowManager();
            Display display = wm.getDefaultDisplay();
            int screenWidth = display.getWidth();
            int screenHeight = display.getHeight();
            // height>width ? Vertical screen ��Horizontal screen
            orient = screenWidth < screenHeight ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        return orient;
    }

    public static void AutoBackGround(Activity activity, View view,
                                      int backGround_v, int backGround_h) {
        int orient = ScreenOrient(activity);
        if (orient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {// Vertical
            view.setBackgroundResource(backGround_v);
        } else {// Horizontal
            view.setBackgroundResource(backGround_h);
        }
    }

    public static int getSDKVersionNumber() {
        int sdkVersion = 0;
        try {

            sdkVersion = Integer.valueOf(Build.VERSION.SDK);

        } catch (NumberFormatException e) {

            sdkVersion = 0;
        }
        return sdkVersion;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        // get ListView's Adapter
        if (listView == null) {
            return;
        }
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) { // listAdapter.getCount()������������Ŀ
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0); // Calculate childView's height and width
            totalHeight += listItem.getMeasuredHeight(); // Calculate all
            // childView's
            // height
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight() get divder's height
        // params.height get the final height that ListView can show completely
        listView.setLayoutParams(params);
    }

    /**
     * collapseSoftInput
     *
     * @param context
     * @param view
     */
    public static void collapseSoftInputMethod(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * getVersion
     */
    public static String getVersion(Context context) {
        String version = "0.0.0";

        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (NameNotFoundException e) {
        }

        return version;
    }

    /**
     * 获取包信息
     *
     * @param context
     * @return
     */
    public static String getPakageName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            // int versionCode = info.versionCode;
            // String versionName = info.versionName;
            String packageName = info.packageName;
            return packageName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "获取包名失败";
        }
    }

    /**
     * 获取包信息
     *
     * @param context
     * @return
     */
    public static String getPakageInfo(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            int versionCode = info.versionCode;
            String versionName = info.versionName;
            String packageName = info.packageName;
            return "程序包名称:" + packageName + ",程序版本号:" + versionCode
                    + ",程序版本名称:" + versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "获取包名失败";
        }
    }

    public static void CopyToClipboard(Context context, String str) {
        ClipboardManager c = (ClipboardManager) context
                .getSystemService(Activity.CLIPBOARD_SERVICE);
        c.setText(str);
        Toast.makeText(context, "已复制到剪切板", Toast.LENGTH_LONG).show();
    }

    public static String getFromClipboard(Context context) {
        ClipboardManager c = (ClipboardManager) context
                .getSystemService(Activity.CLIPBOARD_SERVICE);
        return c.getText().toString();
    }

    /**
     * 获取应用程序名称
     *
     * @param context
     * @return
     */
    public static String getApplicationName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);

            String ret = (String) info.applicationInfo.loadLabel(context.getPackageManager());
            return ret;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "未定义";
        }
    }

    /**
     * 获取程序Icon
     *
     * @param context
     * @return
     */
    public static Drawable getApplicationIcon(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);

            Drawable ret = (Drawable) info.applicationInfo.loadIcon(context.getPackageManager());
            return ret;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取指定的APP是否安装在手机中
     *
     * @param appPackageName
     * @return
     */
    public static boolean getSpecifiedAppWhetherInstalled(Context context, String appPackageName) {
        boolean isInstalled = false;
        List<ApplicationInfo> appInfoList = new ArrayList<ApplicationInfo>();
        PackageManager pm = context.getPackageManager();
        appInfoList = pm.getInstalledApplications(0);
        for (int index = 0; index < appInfoList.size(); index++) {
            if (appInfoList.get(index).packageName.equals(appPackageName)) {
                isInstalled = true;
                break;
            }
        }
        return isInstalled;
    }

    /**
     * 显示SD卡不存在或空间不足的警告
     */
    public static void showSDCardUnavailableWarning() {
        Toast.makeText(MyApplication.getInstance().getApplicationContext(), "SD卡不存在或空间不足", Toast.LENGTH_SHORT).show();
    }

    /**
     * 存在并且容量大于10MB
     *
     * @return
     */
    public static boolean isSDCardExistAndNotFull() {
        return isSDCardExistAndNotFull(10);
    }

    /**
     * 存在并且容量大于指定MB
     *
     * @param minMB
     * @return
     */
    public static boolean isSDCardExistAndNotFull(long minMB) {
        if (!isSDCardExist()) {
            return false;
        }

        long leftMB = getSDFreeSize();
        return leftMB >= minMB;
    }

    /**
     * SD卡是否存在
     *
     * @return
     */
    public static boolean isSDCardExist() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取SD卡可用空间 MB
     *
     * @return
     */
    public static long getSDFreeSize() {
        try {
            // 取得SD卡文件路径
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());
            // 获取单个数据块的大小(Byte)
            long blockSize = sf.getBlockSize();
            // 空闲的数据块的数量
            long freeBlocks = sf.getAvailableBlocks();
            // 返回SD卡空闲大小
            // return freeBlocks * blockSize; //单位Byte
            // return (freeBlocks * blockSize)/1024; //单位KB
            return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static long getSDAllSize() {
        try {
            // 取得SD卡文件路径
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());
            // 获取单个数据块的大小(Byte)
            long blockSize = sf.getBlockSize();
            // 获取所有数据块数
            long allBlocks = sf.getBlockCount();
            // 返回SD卡大小
            // return allBlocks * blockSize; //单位Byte
            // return (allBlocks * blockSize)/1024; //单位KB
            return (allBlocks * blockSize) / 1024 / 1024; // 单位MB
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
