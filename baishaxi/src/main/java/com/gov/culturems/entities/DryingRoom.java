package com.gov.culturems.entities;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 2015/11/7.
 */
public class DryingRoom implements Serializable {


    public static final String STATE_FINISHED = "已完成";
    public static final String STATE_ONGOING = "进行中";

    public static final String DEVICE_STATUS_ONLINE = "online";
    public static final String DEVICE_STATUS_OFFLINE = "offline";

    public static Map<String, String> actionFeedbackMap;

    static {
        actionFeedbackMap = new HashMap<>();
        actionFeedbackMap.put("关", "关");
        actionFeedbackMap.put("开", "开");
        actionFeedbackMap.put("0", "关");
        actionFeedbackMap.put("1", "开");
        actionFeedbackMap.put("", "未知");
    }


    private String SGI;
    private String GoodsId;
    private String GoodsName;
    private String SceneId;
    private String SceneName;
    private String Memo;
    private String BeginTime;
    private String EndTime;
    private String State;
    private List<DeviceData> DeviceDatas;
    private List<SensorData> SensorDatas;
    private String fullText;

    public class SensorData implements Serializable {
        private String SensorType;
        private String SensorValue;
        private String DeviceId;
        private String SensorUnit;

        public String getDeviceId() {
            return DeviceId;
        }

        public void setDeviceId(String deviceId) {
            DeviceId = deviceId;
        }

        public String getSensorUnit() {
            return SensorUnit;
        }

        public void setSensorUnit(String sensorUnit) {
            SensorUnit = sensorUnit;
        }

        public String getSensorUnitName() {
            return SensorType.replace("传感器", "");
        }

        @Override
        public String toString() {
            return SensorType + SensorValue + DeviceId;
        }

        public String getSensorType() {
            return SensorType;
        }

        public void setSensorType(String sensorType) {
            SensorType = sensorType;
        }

        public String getSensorValue() {
            return SensorValue;
        }

        public void setSensorValue(String sensorValue) {
            SensorValue = sensorValue;
        }
    }

    public class DeviceData implements Serializable {
        private String DeviceName;
        private String ActionFeedback;
        private String UseType;
        private String DeviceId;
        private String DeviceStatus;

        public String getUseType() {
            return UseType;
        }

        public void setUseType(String useType) {
            UseType = useType;
        }

        public String getDeviceId() {
            return DeviceId;
        }

        public void setDeviceId(String deviceId) {
            DeviceId = deviceId;
        }

        public String getDeviceStatus() {
            return DeviceStatus;
        }

        public void setDeviceStatus(String deviceStatus) {
            DeviceStatus = deviceStatus;
        }

        @Override
        public String toString() {
            return DeviceName + ActionFeedback + UseType;
        }

        public String getDeviceName() {
            return DeviceName;
        }

        public void setDeviceName(String deviceName) {
            DeviceName = deviceName;
        }

        public String getActionFeedback() {
            return ActionFeedback;
        }

        public void setActionFeedback(String actionFeedback) {
            ActionFeedback = actionFeedback;
        }
    }

    /**
     * 假定每一个场景只有一个控制器
     *
     * @return
     */
    public List<DeviceData> getControlDevice() {
        List<DeviceData> controlDeviceList = new ArrayList<>();
        for (DeviceData temp : DeviceDatas) {
            if (temp.UseType.equals(DeviceInfo.USE_TYPE_CONTROL)) {
                controlDeviceList.add(temp);
            }
        }
        return controlDeviceList;
    }

    private DeviceData getOnlineSensorDevice() {
        List<DeviceData> controlDeviceList = getControlDevice();
        for (DeviceData temp : DeviceDatas) {
            //该device在线
            if (temp.getDeviceStatus().equals(DEVICE_STATUS_ONLINE)) {
                for (SensorData tempSensorData : SensorDatas) {
                    //该device的数值出现在sensorDatas中
                    if (temp.getDeviceId().equals(tempSensorData.getDeviceId())) {
                        boolean isControlDevice = false;
                        for (DeviceData controlDevice : controlDeviceList) {
                            if (controlDevice.getDeviceId().equals(temp.getDeviceId())) {
                                isControlDevice = true;
                            }
                        }
                        if (!isControlDevice) {
                            //该device不是控制器
                            return temp;
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getControlSensorText(int sequence) {
        List<DeviceData> controlDevice = getControlDevice();
        if (controlDevice.size() <= 0) {
            return "数据错误";
        }
        if (controlDevice.get(sequence).getDeviceStatus().equals(DEVICE_STATUS_ONLINE)) {
            return getFormatedHtml(controlDevice.get(sequence).DeviceName, actionFeedbackMap.get(controlDevice.get(sequence).ActionFeedback));
        } else {
            return getFormatedHtml(controlDevice.get(sequence).DeviceName, "离线");
        }
    }

    /**
     * 传入显示类型,假定sensordata中最多出现三个值,分别是控制传感器，温度传感器，湿度传感器
     *
     * @return
     */
    public String getSensorText(int sequence) {
        //TODO 这里根据服务器返回的数据，并不能区分出来是温度还是湿度
        try {
            List<DeviceData> controlDeviceList = getControlDevice();
            List<SensorData> pureSensorDataList = new ArrayList<>();
            for (SensorData temp : SensorDatas) {
                boolean isControlDevice = false;
                for (DeviceData ctrDevice : controlDeviceList) {
                    if (temp.getDeviceId().equals(ctrDevice.getDeviceId())) {
                        isControlDevice = true;
                    }
                }
                if (!isControlDevice) {
                    pureSensorDataList.add(temp);
                }
            }
            DeviceData sensorDevice = getOnlineSensorDevice();
            if (sensorDevice == null || !sensorDevice.getDeviceId().equals(pureSensorDataList.get(0).getDeviceId())) {
                return getFormatedHtml(pureSensorDataList.get(sequence).getSensorType(), "未知");
            } else {
                return getFormatedHtml(pureSensorDataList.get(sequence).getSensorType(), pureSensorDataList.get(sequence).getSensorValue());
            }
        } catch (Exception e) {
            return "数据错误";
        }
    }

    private String getFormatedHtml(String name, String value) {
        String HTMLPREFIX = "<font color=\"";
        String HTMLSUFIX = "</font>";
        String HTML_COLOR_RED = "red\">";
        String HTML_COLOR_BLACK = "black\">";
        String HTML_COLOR_GREEN = "green\">";

        return HTMLPREFIX + HTML_COLOR_BLACK + name + ": " + HTMLSUFIX
                + HTMLPREFIX + HTML_COLOR_GREEN + value + HTMLSUFIX;
    }


    public String getSGI() {
        return SGI;
    }

    public void setSGI(String SGI) {
        this.SGI = SGI;
    }

    public String getGoodsId() {
        return GoodsId;
    }

    public void setGoodsId(String goodsId) {
        GoodsId = goodsId;
    }

    public String getGoodsName() {
        return GoodsName;
    }

    public String getGoodsNameWithoutNullString() {
        if (TextUtils.isEmpty(GoodsName)) {
            return "暂无";
        }
        return GoodsName;
    }

    public void setGoodsName(String goodsName) {
        GoodsName = goodsName;
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

    public String getMemo() {
        return Memo;
    }

    public void setMemo(String memo) {
        Memo = memo;
    }

    public String getBeginTime() {
        return BeginTime;
    }

    public String getBeginTimeWithoutNullString() {
        if (TextUtils.isEmpty(BeginTime)) {
            return "未知";
        }
        int dividerIndex = BeginTime.indexOf("-");
        int spaceIndex = BeginTime.indexOf(" ");
        return BeginTime.substring(dividerIndex + 1, spaceIndex + 6);
    }

    public String getEndTimeWithoutNullString() {
        if (TextUtils.isEmpty(EndTime)) {
            return "未知";
        }
        int dividerIndex = EndTime.indexOf("-");
        int spaceIndex = EndTime.indexOf(" ");
        return EndTime.substring(dividerIndex + 1, spaceIndex + 6);
    }

    public void setBeginTime(String beginTime) {
        BeginTime = beginTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public List<DeviceData> getDeviceDatas() {
        return DeviceDatas;
    }

    public void setDeviceDatas(List<DeviceData> deviceDatas) {
        DeviceDatas = deviceDatas;
    }

    public List<SensorData> getSensorDatas() {
        return SensorDatas;
    }

    public void setSensorDatas(List<SensorData> sensorDatas) {

        SensorDatas = sensorDatas;
    }


    /**
     * 把该drying room 所包含的数据转化成一个deviceInfo列表返回
     *
     * @return
     */
    public List<DeviceInfo> getDeviceInfoList() {
        return null;
    }

    public boolean hasQueryCondition(String query) {
        if (TextUtils.isEmpty(fullText)) {
            generateFullText();
        }
        if (fullText.contains(query.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

    private void generateFullText() {
        StringBuilder builder = new StringBuilder();
        builder.append(SGI).append(GoodsId).append(GoodsName).append(SceneId).append(SceneName).append(Memo)
                .append(BeginTime).append(EndTime).append(State);
        if (TextUtils.isEmpty(State)) {
            builder.append(STATE_FINISHED);
        }
        if (DeviceDatas != null)
            for (DeviceData temp : DeviceDatas) {
                builder.append(temp.toString());
            }
        if (SensorDatas != null) {
            for (SensorData temp : SensorDatas) {
                builder.append(temp.toString());
            }
        }
        fullText = builder.toString().toLowerCase();
    }
}
