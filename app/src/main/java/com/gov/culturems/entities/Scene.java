package com.gov.culturems.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by peter on 6/22/15.
 */
public class Scene implements Serializable {

    private String SceneId;
    private String ParentSceneId;
    private String SceneName;
    private String SceneRemark;
    private String SceneLocation;
    private String ParentSceneName;
    private String AlertStatus;
    private String DeviceStatus;
    private String DeviceCount;
    private List<DeviceInfo> Devices;
    private String SubSceneCount;
    private int Status;

    public String getSubSceneCount() {
        return SubSceneCount;
    }

    public void setSubSceneCount(String subSceneCount) {
        SubSceneCount = subSceneCount;
    }

    public String getSceneName() {
        return SceneName;
    }

    public void setSceneName(String sceneName) {
        SceneName = sceneName;
    }

    public String getSceneId() {
        return SceneId;
    }

    public void setSceneId(String sceneId) {
        SceneId = sceneId;
    }

    public String getParentSceneId() {
        return ParentSceneId;
    }

    public void setParentSceneId(String parentSceneId) {
        ParentSceneId = parentSceneId;
    }

    public String getSceneRemark() {
        return SceneRemark;
    }

    public void setSceneRemark(String sceneRemark) {
        SceneRemark = sceneRemark;
    }

    public String getSceneLocation() {
        return SceneLocation;
    }

    public void setSceneLocation(String sceneLocation) {
        SceneLocation = sceneLocation;
    }

    public String getParentSceneName() {
        return ParentSceneName;
    }

    public void setParentSceneName(String parentSceneName) {
        ParentSceneName = parentSceneName;
    }

    public String getAlertStatus() {
        return AlertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        AlertStatus = alertStatus;
    }

    public String getDeviceStatus() {
        return DeviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        DeviceStatus = deviceStatus;
    }

    public String getDeviceCount() {
        return DeviceCount;
    }

    public void setDeviceCount(String deviceCount) {
        DeviceCount = deviceCount;
    }

    public List<DeviceInfo> getDevices() {
        return Devices;
    }

    public void setDevices(List<DeviceInfo> devices) {
        Devices = devices;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }
}
