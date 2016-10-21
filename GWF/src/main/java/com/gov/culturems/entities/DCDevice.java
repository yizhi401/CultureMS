package com.gov.culturems.entities;

import java.util.HashMap;
import java.util.Map;

/**
 * Detection And Control
 * 测控设备
 * Created by peter on 4/6/16.
 */
public class DCDevice extends BaseDevice {
    public static final String TEMPERATURE_HIGHER = "温度高于";
    public static final String TEMPERATURE_LOWER = "温度低于";
    public static final String HUMIDITY_HIGHER = "湿度高于";
    public static final String HUMIDITY_LOWER = "湿度低于";
    public static Map<String, String> conditionMap;

    static {
        conditionMap = new HashMap<>();
        conditionMap.put(DCDevice.TEMPERATURE_HIGHER, "0");
        conditionMap.put(DCDevice.TEMPERATURE_LOWER, "1");
        conditionMap.put(DCDevice.HUMIDITY_HIGHER, "2");
        conditionMap.put(DCDevice.HUMIDITY_LOWER, "3");
        conditionMap.put("0", DCDevice.TEMPERATURE_HIGHER);
        conditionMap.put("1", DCDevice.TEMPERATURE_LOWER);
        conditionMap.put("2", DCDevice.HUMIDITY_HIGHER);
        conditionMap.put("3", DCDevice.HUMIDITY_LOWER);
    }


    private String ActionFeedback;

    public String getActionFeedback() {
        return ActionFeedback;
    }

    public void setActionFeedback(String actionFeedback) {
        ActionFeedback = actionFeedback;
    }
}
