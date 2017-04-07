package com.gov.culturems.common.http.response;

import com.gov.culturems.entities.BaseDevice;
import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.entities.ControlSensor;
import com.gov.culturems.entities.DCDevice;
import com.gov.culturems.entities.DeviceFactory;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.entities.MonitorDevice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 4/6/16.
 */
public class DryingRoomResp implements Comparable<DryingRoomResp> {

    private String SGId;
    private String GoodsId;
    private String GoodsName;
    private String SceneId;
    private String SceneName;
    private String SceneUseType;
    private String SceneUseTypeDisplayTxt;
    private String Memo;
    private String BeginTime;
    private String EndTime;
    private String State;
    private String AlertStatus;
    private String IsAlert;

    private String TemperatureValueTxt;
    private String HumidityValueTxt;
    private String DeviceDispTxt;

    private List<DeviceData> DeviceDatas;
    private List<SensorData> SensorDatas;
    private List<DeviceCKData> DeviceCKDatas;
    private List<DeviceMonitorData> DeviceMonitorDatas;

    private List<DeviceMonitor> devMonitors;
    private List<DeviceControl> devContrls;

    @Override
    public int compareTo(DryingRoomResp another) {
        return convertToDryingRoom().compareTo(another.convertToDryingRoom());
    }

    public class SensorData implements Serializable {
        private String SensorType;
        private String SensorValue;
        private String DeviceId;
        private String SensorUnit;

        private BaseSensor convertToBaseSensor() {
            BaseSensor baseSensor = new BaseSensor();
            baseSensor.setSensorType(SensorType);
            baseSensor.setSensorValue(SensorValue);
            baseSensor.setSensorUnit(SensorUnit);
            BaseDevice parentDevice = new BaseDevice();
            parentDevice.setId(DeviceId);
            baseSensor.setParentDevice(parentDevice);
            return baseSensor;
        }
    }


    public class DeviceData implements Serializable {
        private String DeviceName;
        private String ActionFeedback;
        private String UseType;
        private String DeviceId;
        private String DeviceStatus;

        private BaseDevice convertToBaseDevice() {
            BaseDevice baseDevice = DeviceFactory.createDevice(UseType);
            baseDevice.setName(DeviceName);
            baseDevice.setId(DeviceId);
            baseDevice.setUseType(UseType);
            baseDevice.setDeviceStatus(DeviceStatus);
            return baseDevice;
        }
    }

    public static class DeviceMonitor implements Serializable {
        public String DeviceId;
        public String DeviceName;
    }

    public static class DeviceControl implements Serializable {
        public String DeviceId;
        public String DeviceName;
        public String CtrlDirection;
    }

    public class DeviceMonitorData implements Serializable {
        public String DeviceName;
        public String DeviceId;
        public String DeviceStatus;
        private List<Rule> Rules;

        private class Rule {
            public String DeviceId;
            public String SensorType;
            public String SensorTypeName;
            public String ThresholdUp;
            public String ThresholdDown;
        }

        public MonitorDevice convertToMonitorDevice() {
            MonitorDevice mDevice = new MonitorDevice();
            mDevice.setId(DeviceId);
            mDevice.setName(DeviceName);
            mDevice.setDeviceStatus(DeviceStatus);
            mDevice.setUseType(BaseDevice.USE_TYPE_MONITOR);
            if (Rules != null) {
                List<BaseSensor> baseSensors = new ArrayList<>();
                for (Rule temp : Rules) {
                    ControlSensor sensor = new ControlSensor();
                    sensor.setParentDevice(mDevice);
                    sensor.setSensorType(temp.SensorType);
                    sensor.setSensorTypeName(temp.SensorTypeName);
                    sensor.setThresholdDown(temp.ThresholdDown);
                    sensor.setThresholdUp(temp.ThresholdUp);
                    baseSensors.add(sensor);
                }
                mDevice.setSensorTypes(baseSensors);
            }
            return mDevice;
        }
    }

    public class DeviceCKData implements Serializable {
        private String DeviceName;
        private String DeviceId;
        private String DeviceStatus;
        private List<Rule> Rules;

        private class Rule {
            public String DeviceId;
            public String SensorType;
            public String SensorTypeName;
            public String ThresholdUp;
            public String ThresholdDown;
        }

        public DCDevice convertToDCDevice() {
            DCDevice dcDevice = new DCDevice();
            dcDevice.setId(DeviceId);
            dcDevice.setName(DeviceName);
            dcDevice.setDeviceStatus(DeviceStatus);
            dcDevice.setUseType(BaseDevice.USE_TYPE_CK);
            if (Rules != null) {
                List<BaseSensor> baseSensors = new ArrayList<>();
                for (Rule temp : Rules) {
                    ControlSensor sensor = new ControlSensor();
                    sensor.setParentDevice(dcDevice);
                    sensor.setSensorType(temp.SensorType);
                    sensor.setSensorTypeName(temp.SensorTypeName);
                    sensor.setThresholdDown(temp.ThresholdDown);
                    sensor.setThresholdUp(temp.ThresholdUp);
                    baseSensors.add(sensor);
                }
                dcDevice.setSensorTypes(baseSensors);
            }
            return dcDevice;
        }

    }

    public DryingRoom convertToDryingRoom() {
        DryingRoom room = new DryingRoom();
        room.setSGId(SGId);
        room.setGoodsId(GoodsId);
        room.setGoodsName(GoodsName);
        room.setId(SceneId);
        room.setName(SceneName);
        room.setBeginTime(BeginTime);
        room.setEndTime(EndTime);
        room.setMemo(Memo);
        room.setState(State);
        room.setSceneUseType(SceneUseType);
        room.setAlertStatus(AlertStatus);
        room.setIsAlert(IsAlert);
        room.setSceneUseTypeDisplayTxt(SceneUseTypeDisplayTxt);
        try {
            List<BaseDevice> deviceList = new ArrayList<>();
//            for (DeviceData temp : DeviceDatas) {
//                if (BaseDevice.USE_TYPE_MONITOR.equals(temp.UseType))
//                    deviceList.add(temp.convertToBaseDevice().setParentScene(room));
//            }
            for (DeviceCKData temp : DeviceCKDatas) {
                deviceList.add(temp.convertToDCDevice().setParentScene(room));
            }
            for (DeviceMonitorData temp : DeviceMonitorDatas) {
                deviceList.add(temp.convertToMonitorDevice().setParentScene(room));
            }
            room.setDeviceDatas(deviceList);
            List<BaseSensor> sensorList = new ArrayList<>();
            for (SensorData temp : SensorDatas) {
                sensorList.add(temp.convertToBaseSensor());
            }
            room.setSensorDatas(sensorList);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        room.setTemperatureValueHTML(TemperatureValueTxt);
        room.setHumidityValueTxt(HumidityValueTxt);
        room.setDeviceDispTxt(DeviceDispTxt);
        room.setDevContrls(devContrls);
        room.setDevMonitors(devMonitors);
        return room;
    }

    public static List<DryingRoom> convertToDryingRoomList(List<DryingRoomResp> resps) {
        if (resps == null) {
            return null;
        }
        List<DryingRoom> dryingRoomList = new ArrayList<>();
        for (DryingRoomResp temp : resps) {
            dryingRoomList.add(temp.convertToDryingRoom());
        }
        return dryingRoomList;
    }

}
