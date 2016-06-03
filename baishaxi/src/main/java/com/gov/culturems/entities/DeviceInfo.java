package com.gov.culturems.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 6/22/15.
 */
public class DeviceInfo implements Serializable {

    public static final String TEMPERATURE_HIGHER = "温度高于";
    public static final String TEMPERATURE_LOWER = "温度低于";
    public static final String HUMIDITY_HIGHER = "湿度高于";
    public static final String HUMIDITY_LOWER = "湿度低于";

    public static final String USE_TYPE_WATCH = "0";//该检测器是监视检测器
    public static final String USE_TYPE_CONTROL = "1";//该检测器是控制检测器

    public static final String DEVICE_STATUS_ONLINE = "online";
    public static final String DEVICE_STATUS_OFFLINE = "offline";

    public static Map<String, String> conditionMap;

    static {
        conditionMap = new HashMap<>();
        conditionMap.put(DeviceInfo.TEMPERATURE_HIGHER, "0");
        conditionMap.put(DeviceInfo.TEMPERATURE_LOWER, "1");
        conditionMap.put(DeviceInfo.HUMIDITY_HIGHER, "2");
        conditionMap.put(DeviceInfo.HUMIDITY_LOWER, "3");
        conditionMap.put("0", DeviceInfo.TEMPERATURE_HIGHER);
        conditionMap.put("1", DeviceInfo.TEMPERATURE_LOWER);
        conditionMap.put("2", DeviceInfo.HUMIDITY_HIGHER);
        conditionMap.put("3", DeviceInfo.HUMIDITY_LOWER);
    }

    private String DeviceId;
    private String DeviceName;
    private String DeviceStatus;
    private String SensorValue;
    private String SceneId;
    private String SceneName;
    private String Status;
    private String DevSn;
    private String MacAddr;
    private String SensorCount;
    private String BaterryValue;
    private String AlertStatus;
    private String DeviceRemark;
    private List<Sensor> SensorTypes;
    private List<AlertInfo> Alerts;
    private String InsertTime;
    private String DeviceConClose;
    private String ThresholdClose;
    private String DeviceConOpen;
    private String ThresholdOpen;
    private String ActionFeedback;
    private String GateId;
    private String UseType;

    public String getSceneName() {
        return SceneName;
    }

    public void setSceneName(String sceneName) {
        SceneName = sceneName;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getDeviceStatus() {
        return DeviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        DeviceStatus = deviceStatus;
    }

    public String getSensorValue() {
        return SensorValue;
    }

    public void setSensorValue(String sensorValue) {
        SensorValue = sensorValue;
    }

    public String getInsertTime() {
        return InsertTime;
    }

    public void setInsertTime(String insertTime) {
        InsertTime = insertTime;
    }

    public String getDeviceId() {
        return DeviceId.replace(" ", "");
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getSceneId() {
        return SceneId;
    }

    public void setSceneId(String sceneId) {
        SceneId = sceneId;
    }

    public String getDevSn() {
        return DevSn;
    }

    public void setDevSn(String devSn) {
        DevSn = devSn;
    }

    public String getMacAddr() {
        return MacAddr;
    }

    public void setMacAddr(String macAddr) {
        MacAddr = macAddr;
    }

    public String getSensorCount() {
        return SensorCount;
    }

    public void setSensorCount(String sensorCount) {
        SensorCount = sensorCount;
    }

    public String getBaterryValue() {
        return BaterryValue;
    }

    public void setBaterryValue(String baterryValue) {
        BaterryValue = baterryValue;
    }

    public String getAlertStatus() {
        return AlertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        AlertStatus = alertStatus;
    }

    public String getDeviceRemark() {
        return DeviceRemark;
    }

    public void setDeviceRemark(String deviceRemark) {
        DeviceRemark = deviceRemark;
    }

    public List<Sensor> getSensorTypes() {
        return SensorTypes;
    }

    public void setSensorTypes(List<Sensor> sensorTypes) {
        SensorTypes = sensorTypes;
    }

    public List<AlertInfo> getAlerts() {
        return Alerts;
    }

    public void setAlerts(List<AlertInfo> alerts) {
        Alerts = alerts;
    }

    public String getDeviceConClose() {
        return DeviceConClose;
    }

    public void setDeviceConClose(String deviceConClose) {
        DeviceConClose = deviceConClose;
    }

    public String getThresholdClose() {
        return ThresholdClose;
    }

    public void setThresholdClose(String thresholdClose) {
        ThresholdClose = thresholdClose;
    }

    public String getDeviceConOpen() {
        return DeviceConOpen;
    }

    public void setDeviceConOpen(String deviceConOpen) {
        DeviceConOpen = deviceConOpen;
    }

    public String getThresholdOpen() {
        return ThresholdOpen;
    }

    public void setThresholdOpen(String thresholdOpen) {
        ThresholdOpen = thresholdOpen;
    }

    public String getActionFeedback() {
        return ActionFeedback;
    }

    public void setActionFeedback(String actionFeedback) {
        ActionFeedback = actionFeedback;
    }

    public String getGateId() {
        return GateId;
    }

    public void setGateId(String gateId) {
        GateId = gateId;
    }

    public String getUseType() {
        return UseType;
    }

    public void setUseType(String useType) {
        UseType = useType;
    }
}
