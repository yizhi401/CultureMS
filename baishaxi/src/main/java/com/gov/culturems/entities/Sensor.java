package com.gov.culturems.entities;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by peter on 6/22/15.
 */
public class Sensor implements Serializable {

    public static final String SENSOR_TEMPERATURE = "Temperature";
    public static final String SENSOR_HUMIDITY = "Humidity";
    public static final String SENSOR_CONTROL = "Control";

    private String SensorType;//*
    private String SensorTypeName; //*
    private String SensorValue;//*
    private String SensorUnit;//*
    private String DeviceId;  //*
    private String DeviceName;
    private String DeviceStatus;
    private String AlertStatus;
    private String AlertStatusName;
    private String SceneId;
    private String InsertTime;
    private String SceneName;

    public String getAlertStatusName() {
        if (TextUtils.isEmpty(AlertStatusName)) {
            return AlertStatus;
        }
        return AlertStatusName;
    }

    public void setAlertStatusName(String alertStatusName) {
        AlertStatusName = alertStatusName;
    }

    public String getInsertTime() {
        return InsertTime;
    }

    public void setInsertTime(String insertTime) {
        InsertTime = insertTime;
    }

    public String getDeviceId() {
        return DeviceId;
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

    public String getDeviceStatus() {
        return DeviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        DeviceStatus = deviceStatus;
    }

    public String getAlertStatus() {
        if (TextUtils.isEmpty(AlertStatus)) {
            return AlertStatusName;
        }
        return AlertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        AlertStatus = alertStatus;
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

    public String getSensorType() {
        if (TextUtils.isEmpty(SensorType)) {
            return SensorTypeName;
        }
        return SensorType;
    }

    public void setSensorType(String sensorType) {
        SensorType = sensorType;
    }

    public String getSensorTypeName() {
        if (TextUtils.isEmpty(SensorTypeName)) {
            return SensorType;
        }
        return SensorTypeName;
    }

    public String getSensorUnitName() {
        if (TextUtils.isEmpty(SensorTypeName)) {
            return SensorType.replace("传感器", "");
        }
        return SensorTypeName.replace("传感器", "");
    }

    public void setSensorTypeName(String sensorTypeName) {
        SensorTypeName = sensorTypeName;
    }

    public String getSensorValue() {
        return SensorValue;
    }

    public void setSensorValue(String sensorValue) {
        SensorValue = sensorValue;
    }

    public String getSensorUnit() {
        return SensorUnit;
    }

    public void setSensorUnit(String sensorUnit) {
        SensorUnit = sensorUnit;
    }
}
