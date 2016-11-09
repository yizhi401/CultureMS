package com.gov.culturems.entities;

import java.io.Serializable;

/**
 * Created by peter on 6/22/15.
 */
public class AlertInfo implements Serializable{

    private String AlertId;
    private String Title;
    private String DeviceName;
    private String InsertTime;
    private String SensorValue;
    private String LinkMan;
    private String SceneId;
    private String SceneName;
    private String DeviceId;
    private String SensorTypeName;
    private String AlertSettingId;
    private String SensorType;
    private String AlertType;
    private String AlertTypeName;
    private String Threshold;


    public String getAlertId() {
        return AlertId;
    }

    public void setAlertId(String alertId) {
        AlertId = alertId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getAlertTypeName() {
        return AlertTypeName;
    }

    public void setAlertTypeName(String alertTypeName) {
        AlertTypeName = alertTypeName;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getInsertTime() {
        return InsertTime;
    }

    public void setInsertTime(String insertTime) {
        InsertTime = insertTime;
    }

    public String getSensorValue() {
        return SensorValue;
    }

    public void setSensorValue(String sensorValue) {
        SensorValue = sensorValue;
    }

    public String getThreshold() {
        return Threshold;
    }

    public void setThreshold(String threshold) {
        Threshold = threshold;
    }

    public String getLinkMan() {
        return LinkMan;
    }

    public void setLinkMan(String linkMan) {
        LinkMan = linkMan;
    }

    public String getSceneId() {
        return SceneId;
    }

    public void setSceneId(String sceneId) {
        SceneId = sceneId;
    }

    public String getSceneName() {
        return SceneName;
    }

    public void setSceneName(String sceneName) {
        SceneName = sceneName;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

    public String getSensorTypeName() {
        return SensorTypeName;
    }

    public void setSensorTypeName(String sensorTypeName) {
        SensorTypeName = sensorTypeName;
    }

    public String getAlertSettingId() {
        return AlertSettingId;
    }

    public void setAlertSettingId(String alertSettingId) {
        AlertSettingId = alertSettingId;
    }

    public String getSensorType() {
        return SensorType;
    }

    public void setSensorType(String sensorType) {
        SensorType = sensorType;
    }

    public String getAlertType() {
        return AlertType;
    }

    public void setAlertType(String alertType) {
        AlertType = alertType;
    }
}
