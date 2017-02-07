package com.gov.culturems.common.http.response;

import com.gov.culturems.entities.AlertInfo;
import com.gov.culturems.entities.BaseDevice;
import com.gov.culturems.entities.BaseScene;
import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.entities.DCDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 06/11/2016.
 */

public class DeviceResp {

    public String DeviceId;
    public String DeviceName;
    public String DeviceStatus;
    public String SensorValue;
    public String SceneId;
    public String SceneName;
    public String Status;
    public String DevSn;
    public String MacAddr;
    public String SensorCount;
    public String BaterryValue;
    public String AlertStatus;
    public String DeviceRemark;
    public List<AlertInfo> Alerts;
    public String InsertTime;
    public String DeviceConClose;
    public String ThresholdClose;
    public String DeviceConOpen;
    public String ThresholdOpen;
    public String ActionFeedback;
    public String GateId;
    public String UseType;
    public List<BaseSensor> SensorTypes;
    public List<DCDevice.DeviceRule> Rules;



    public DCDevice convertToDevice(){

        DCDevice device = new DCDevice();
        device.setName(DeviceName);
        device.setId(DeviceId);
        device.setActionFeedback(ActionFeedback);
        device.setAlertStatus(AlertStatus);
        device.setDeviceStatus(DeviceStatus);

        BaseScene parentScene = new BaseScene();
        parentScene.setId(SceneId);
        parentScene.setName(SceneName);
        device.setParentScene(parentScene);

        device.setStatus(Status);
        BaseDevice.DeviceProperty property = new BaseDevice.DeviceProperty();
        property.setBaterryValue(BaterryValue);
        property.setDeviceRemark(DeviceRemark);
        property.setDevSn(DevSn);
        property.setGateId(GateId);
        property.setMacAddr(MacAddr);

        device.setProperties(property);
        device.setSensorTypes(SensorTypes);
        device.setAlertStatus(AlertStatus);
        device.setInsertTime(InsertTime);
        device.setUseType(UseType);
        device.setSensorValue(SensorValue);
        device.setSensorCount(SensorCount);
        device.setAlerts(Alerts);
        device.setRules(Rules);
        device.setDeviceConClose(DeviceConClose);
        device.setDeviceConOpen(DeviceConOpen);
        device.setThresholdClose(ThresholdClose);
        device.setThresholdOpen(ThresholdOpen);
        device.setAlerts(Alerts);

        return device;
    }

    public static List<DCDevice> convertToDeviceList(List<DeviceResp> deviceResps){
        if(deviceResps == null){
            return null;
        }
        List<DCDevice> list = new ArrayList<>();
        for(DeviceResp temp : deviceResps){
            list.add(temp.convertToDevice());
        }
        return list;
    }

}
