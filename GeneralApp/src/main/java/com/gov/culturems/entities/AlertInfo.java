package com.gov.culturems.entities;

import java.io.Serializable;

/**
 * Created by peter on 6/22/15.
 */
public class AlertInfo implements Serializable{

    private String AlertId;
    private String Title;
    private String AlertTypeName;
    private String DeviceName;
    private String InsertTime;
    private String SensorValue;
    private String Threshold;
    private String LinkMan;


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
}
