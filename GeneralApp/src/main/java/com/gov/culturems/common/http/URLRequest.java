package com.gov.culturems.common.http;

/**
 * Created by peter on 6/22/15.
 */
public class URLRequest {

    //恭王府项目端口
//    public static String SERVER = VersionController.getURLServer();

    //茶厂1
//    public static final String SERVER = "http://42.120.48.207:8020/";
    //茶厂2
//    public static final String SERVER = "http://42.120.48.207:8021/";
    //茶厂3
//    public static final String SERVER = "http://42.120.48.207:8022/";

    //蓝色通用版
//    public static final String SERVER = "http://42.120.48.207:8019/";

    //白沙溪茶厂
//    public static final String SERVER = "http://42.120.48.207:8021/";

    /**
     * 请求后缀
     */
    public static final String APPENDIX = ".ashx";

    /**
     * 用户登录
     */
    public static final String LOGIN = "mgr/login";

    /**
     * 场景列表获取
     */
    public static final String SCENES_GET = "app/ScenesGet";

    /**
     * 数据列表获取
     */
//    public static final String DATAS_GET = "mgr/DatasGet";
    /**
     * 温湿度数据列表获取
     */
    public static final String DATAS_HT_GET = "mgr/DatasHtGet";

    /**
     * 报警信心获取
     */
    public static final String ALERTS_GET = "mgr/AlertsGet";

    /**
     * 单个设备信息列表获取
     */
    public static final String DEVICE_GET = "mgr/DeviceGet";

    /**
     * 场景设备信息获取
     */
    public static final String DEVICES_GET = "mgr/DevicesGet";

    /**
     * 一天数据列表获取
     */
    public static final String DAY_DATAS_GET = "app/DayDatasGet";

    /**
     * 周数据获取
     */
    public static final String WEEK_DATAS_GET = "app/WeekDatasGet";

    //-------------------------------------------------------------------------------------------
    //  货物管理
    //
    //---------------------------------------------------------------------------------------------
    /**
     * 场景下货品列表获取
     */
    public static final String SCENE_GOODS_LIST_GET = "mgr/sceneslistgetwithgoods";

    /**
     * 控制设备
     */
    public static final String DEVICE_CTRL = "mgr/devicectrl";

    /**
     * 测控设备上报
     */
    public static final String DEVICE_CK = "mgr/devicectrlrule";

    /**
     * 场景简单数据获取，主要是温度湿度
     */
    public static final String SCENE_DATAS_GET = "mgr/scenedatasget";

    /**
     * 获取设备上下限信息
     */
    public static final String DEVICE_CK_RULES_GET = "mgr/deviceckrulesget";

}
