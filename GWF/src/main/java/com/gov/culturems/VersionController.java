package com.gov.culturems;

import java.util.HashMap;
import java.util.Map;

/**
 * Notice: when changing the version, remembers to change:
 * 1. App name
 * 2. main color
 * 3. ic launcher
 * Created by peter on 10/17/15.
 */
public class VersionController {

    //恭王府接口
    public static final int GONGWANGFU = 0;

    //蓝色通用版
    public static final int GENERAL = 1;

    //茶厂版本
    public static final int TEACORP = 2;


    public static final boolean isDebug = true;

    /**
     * if you want to change the version, change this value
     * Please remembers to change:
     * 1. app name (string.xml)
     * 2. colors (color.xml)
     * 3. ic launcher name(AndroidManifest.xml)
     * 4. title_bar background (style.xml)
     */
    public static int CURRENT_VERSION = GONGWANGFU;

    //--------------------------------------------------------------------------------------
    public static final String ICON_SCENE = "icon_scene";
    public static final String LOGIN_BG = "login_bg";
    public static final String LOGIN_USERNAME = "login_username";
    public static final String LOGIN_PASSWORD = "login_password";
    public static final String SCENE_DATA_IC = "scene_data_ic";
    public static final String SCENE_WARNING_IC = "scene_warning_ic";
    public static final String WELCOME_PAGE = "welcome_page";
    public static final String EDIT_ICON = "edit_icon";
    //--------------------------------------------------------------------------------------

    public static Map<String, Integer> redResourceMap;
    public static Map<String, Integer> greenResourceMap;
    public static Map<String, Integer> blueResourceMap;


    static {
        redResourceMap = new HashMap<>();
        redResourceMap.put(ICON_SCENE, R.drawable.icon_scene_red);
        redResourceMap.put(LOGIN_BG, R.drawable.login_bg_red);
        redResourceMap.put(LOGIN_USERNAME, R.drawable.login_username_red);
        redResourceMap.put(LOGIN_PASSWORD, R.drawable.login_password_red);
        redResourceMap.put(EDIT_ICON, R.drawable.edit_icon_red);
        redResourceMap.put(SCENE_DATA_IC, R.drawable.scene_data_ic_red);
        redResourceMap.put(SCENE_WARNING_IC, R.drawable.scene_warning_ic_red);
        redResourceMap.put(WELCOME_PAGE, R.drawable.welcome_page_red);


        blueResourceMap = new HashMap<>();
        blueResourceMap.put(ICON_SCENE, R.drawable.icon_scene_blue);
        blueResourceMap.put(LOGIN_BG, R.drawable.login_bg_blue);
        blueResourceMap.put(LOGIN_USERNAME, R.drawable.login_username_blue);
        blueResourceMap.put(LOGIN_PASSWORD, R.drawable.login_password_blue);
        blueResourceMap.put(EDIT_ICON, R.drawable.edit_icon_blue);
        blueResourceMap.put(SCENE_DATA_IC, R.drawable.scene_data_ic_blue);
        blueResourceMap.put(SCENE_WARNING_IC, R.drawable.scene_warning_ic_blue);
        blueResourceMap.put(WELCOME_PAGE, R.drawable.welcome_page_blue);


        greenResourceMap = new HashMap<>();
        greenResourceMap.put(ICON_SCENE, R.drawable.icon_scene_green);
        greenResourceMap.put(LOGIN_BG, R.drawable.login_bg_green);
        greenResourceMap.put(LOGIN_USERNAME, R.drawable.login_username_green);
        greenResourceMap.put(LOGIN_PASSWORD, R.drawable.login_password_green);
        greenResourceMap.put(EDIT_ICON, R.drawable.edit_icon_green);
        greenResourceMap.put(SCENE_DATA_IC, R.drawable.scene_data_ic_green);
        greenResourceMap.put(SCENE_WARNING_IC, R.drawable.scene_warning_ic_green);
        greenResourceMap.put(WELCOME_PAGE, R.drawable.welcome_page_green);
    }

    public static void setVersion(int version) {
        CURRENT_VERSION = version;
    }

    public static int getMainColor() {
        if (CURRENT_VERSION == GONGWANGFU) {
            return R.color.main_red;
        } else if (CURRENT_VERSION == GENERAL) {
            return R.color.main_blue;
        } else if (CURRENT_VERSION == TEACORP) {
            return R.color.main_green;
        }
        return R.color.main_blue;
    }

    public static int getDrawable(String resourceKey) {
        if (CURRENT_VERSION == GONGWANGFU) {
            return redResourceMap.get(resourceKey);
        } else if (CURRENT_VERSION == GENERAL) {
            return blueResourceMap.get(resourceKey);
        } else if (CURRENT_VERSION == TEACORP) {
            return greenResourceMap.get(resourceKey);
        }
        return blueResourceMap.get(resourceKey);
    }


    /**
     * 详见 URLConstant
     *
     * @return
     */
    public static String getURLServer() {
        if (CURRENT_VERSION == GONGWANGFU) {
            return "http://121.41.230.252:8018/";
        } else if (CURRENT_VERSION == GENERAL) {
            return "http://121.41.230.252:8019/";
        } else if (CURRENT_VERSION == TEACORP) {
            return "http://121.41.230.252:8019/";
        }
        return "http://121.41.230.252:8019/";
    }

    /**
     * 详见 URLConstant
     *
     * @return
     */
    public static String getURLServerWithoutPort() {
        return "http://121.41.230.252";
    }


}
