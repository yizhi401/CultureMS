package com.gov.culturems.entities;

import java.util.List;

/**
 * 设备的基类
 * Created by peter on 4/6/16.
 */
public class BaseDevice extends BaseObj {


    public static final String USE_TYPE_DETECTION = "0";//该检测器是监视检测器
    public static final String USE_TYPE_CONTROL = "1";//该检测器是控制检测器
    public static final String USE_TYPE_CK = "2";//该检测器是测控检测器

    public static final String DEVICE_STATUS_ONLINE = "online";
    public static final String DEVICE_STATUS_OFFLINE = "offline";
    public static final String DEVICE_STATUS_BATTERY_ALARM = "battery_alarm";


    private String DeviceStatus;
    private String Status;
    private String AlertStatus;
    private List<BaseSensor> SensorTypes;
    private String InsertTime;
    private String UseType;
    private BaseScene parentScene;
    private DeviceProperty properties;

    public static class DeviceProperty {
        private String DevSn;
        private String MacAddr;
        private String BaterryValue;
        private String DeviceRemark;
        private String GateId;

        public DeviceProperty(){}

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

        public String getBaterryValue() {
            return BaterryValue;
        }

        public void setBaterryValue(String baterryValue) {
            BaterryValue = baterryValue;
        }

        public String getDeviceRemark() {
            return DeviceRemark;
        }

        public void setDeviceRemark(String deviceRemark) {
            DeviceRemark = deviceRemark;
        }

        public String getGateId() {
            return GateId;
        }

        public void setGateId(String gateId) {

            GateId = gateId;
        }

        @Override
        public String toString() {
            return "DeviceProperty{" +
                    "DevSn='" + DevSn + '\'' +
                    ", MacAddr='" + MacAddr + '\'' +
                    ", BaterryValue='" + BaterryValue + '\'' +
                    ", DeviceRemark='" + DeviceRemark + '\'' +
                    ", GateId='" + GateId + '\'' +
                    '}';
        }
    }

    public String getDeviceStatus() {
        return DeviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        DeviceStatus = deviceStatus;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getAlertStatus() {
        return AlertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        AlertStatus = alertStatus;
    }

    public List<BaseSensor> getSensorTypes() {
        return SensorTypes;
    }

    public void setSensorTypes(List<BaseSensor> sensorTypes) {
        SensorTypes = sensorTypes;
    }

    public String getInsertTime() {
        return InsertTime;
    }

    public void setInsertTime(String insertTime) {
        InsertTime = insertTime;
    }

    public String getUseType() {
        return UseType;
    }

    public void setUseType(String useType) {
        UseType = useType;
    }

    public BaseScene getParentScene() {
        return parentScene;
    }

    public BaseDevice setParentScene(BaseScene parentScene) {
        this.parentScene = parentScene;
        return this;
    }

    public DeviceProperty getProperties() {
        return properties;
    }

    public void setProperties(DeviceProperty properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return super.toString() + "BaseDevice{" +
                "DeviceStatus='" + DeviceStatus + '\'' +
                ", Status='" + Status + '\'' +
                ", AlertStatus='" + AlertStatus + '\'' +
                ", InsertTime='" + InsertTime + '\'' +
                ", UseType='" + UseType + '\'' +
                ", properties=" + properties.toString() +
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
