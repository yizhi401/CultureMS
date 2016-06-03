package com.gov.culturems.common.http.response;

import com.gov.culturems.entities.BaseDevice;
import com.gov.culturems.entities.BaseScene;
import com.gov.culturems.entities.BaseSensor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 6/22/15.
 */
public class SensorResp implements Serializable {

    private String SensorId;
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


    public BaseSensor convertToBaseSensor() {
        BaseSensor baseSensor = new BaseSensor();
        baseSensor.setId(SensorId);
        baseSensor.setSensorType(SensorType);
        baseSensor.setSensorTypeName(SensorTypeName);
        baseSensor.setSensorValue(SensorValue);
        baseSensor.setSensorUnit(SensorUnit);

        BaseDevice device = new BaseDevice();
        device.setId(DeviceId);
        device.setName(DeviceName);
        device.setDeviceStatus(DeviceStatus);

        BaseScene scene = new BaseScene();
        scene.setId(SceneId);
        scene.setName(SceneName);
        device.setParentScene(scene);

        baseSensor.setParentDevice(device);
        baseSensor.setAlertStatus(AlertStatus);
        baseSensor.setAlertStatusName(AlertStatusName);
        baseSensor.setInsertTime(InsertTime);

        return baseSensor;
    }

    public static List<BaseSensor> convertToBaseSensorList(List<SensorResp> respList) {
        if (respList == null) {
            return null;
        }
        List<BaseSensor> baseSensors = new ArrayList<>();
        for (SensorResp temp : respList) {
            baseSensors.add(temp.convertToBaseSensor());
        }
        return baseSensors;
    }
}
