package com.gov.culturems.entities;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 传感器的基类
 * Created by peter on 4/6/16.
 */
public class BaseSensor extends BaseObj {

    public static final String SENSOR_TEMPERATURE = "Temperature";
    public static final String SENSOR_HUMIDITY = "Humidity";
    public static final String SENSOR_CONTROL = "Control";

    public static Map<String, String> sensorNameMap;

    static {
        sensorNameMap = new HashMap<>();
        sensorNameMap.put(SENSOR_TEMPERATURE, "温度");
        sensorNameMap.put(SENSOR_HUMIDITY, "湿度");
    }


    private String SensorType;
    private String SensorTypeName;
    private String SensorValue;
    private String SensorUnit;
    private BaseDevice parentDevice;
    private String AlertStatus;
    private String AlertStatusName;
    private String InsertTime;
    private AlertInfo alerts;


    public String getSensorType() {
        return SensorType;
    }

    public void setSensorType(String sensorType) {
        SensorType = sensorType;
    }

    public String getSensorTypeName() {
        return SensorTypeName;
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

    public BaseDevice getParentDevice() {
        return parentDevice;
    }

    public void setParentDevice(BaseDevice parentDevice) {
        this.parentDevice = parentDevice;
    }

    public String getAlertStatus() {
        return AlertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        AlertStatus = alertStatus;
    }

    public String getAlertStatusName() {
        if (TextUtils.isEmpty(AlertStatusName)) {
            return AlertStatus;
        } else {
            return AlertStatusName;
        }
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

    public AlertInfo getAlerts() {
        return alerts;
    }

    public void setAlerts(AlertInfo alerts) {
        this.alerts = alerts;
    }

    public String getSensorUnitName() {
        if (TextUtils.isEmpty(SensorTypeName)) {
            return SensorType.replace("传感器", "");
        }
        return SensorTypeName.replace("传感器", "");
    }

    @Override
    public String toString() {
        return super.toString() + "BaseSensor{" +
                "SensorType='" + SensorType + '\'' +
                ", SensorTypeName='" + SensorTypeName + '\'' +
                ", SensorValue='" + SensorValue + '\'' +
                ", SensorUnit='" + SensorUnit + '\'' +
                ", AlertStatus='" + AlertStatus + '\'' +
                ", AlertStatusName='" + AlertStatusName + '\'' +
                ", InsertTime='" + InsertTime + '\'' +
                '}';
    }

    @Override
    public boolean query(String query) {
        if (this.toString().contains(query)) {
            return true;
        } else {

            return false;
        }
    }
}
