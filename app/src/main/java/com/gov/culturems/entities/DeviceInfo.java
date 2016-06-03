package com.gov.culturems.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by peter on 6/22/15.
 */
public class DeviceInfo implements Serializable {
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
}
