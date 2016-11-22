package com.gov.culturems.entities;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 2015/11/7.
 */
public class DryingRoom extends BaseScene implements Serializable, Comparable<DryingRoom> {

    public static final String STATE_FINISHED = "已完成";
    public static final String STATE_ONGOING = "进行中";

    public static Map<String, String> actionFeedbackMap;

    static {
        actionFeedbackMap = new HashMap<>();
        actionFeedbackMap.put("关", "关");
        actionFeedbackMap.put("开", "开");
        actionFeedbackMap.put("0", "关");
        actionFeedbackMap.put("1", "开");
        actionFeedbackMap.put("", "未知");
    }

    private String GoodsId;
    private String GoodsName;
    private String BeginTime;
    private String EndTime;
    private List<BaseSensor> SensorDatas;
    private String TemperatureValueTxt;
    private String HumidityValueTxt;
    private String MoistureValueTxt;
    private String DeviceDispTxt;


    public String getTempatureTxt() {
        return TemperatureValueTxt == null ? "未知" : TemperatureValueTxt;
    }

    public String getHumidityTxt() {
        return HumidityValueTxt == null ? "未知" : HumidityValueTxt;
    }

    public String getMoistureTxt() {
        return MoistureValueTxt == null ? "未知" : MoistureValueTxt;
    }

    public String getTemperatureValueHTML() {
        return getFormatedHtml("温度", TemperatureValueTxt);
    }

    public void setTemperatureValueHTML(String temperatureValueTxt) {
        TemperatureValueTxt = temperatureValueTxt;
    }

    public String getHumidityValueTxt() {
        return getFormatedHtml("湿度", HumidityValueTxt);
    }

    public void setHumidityValueTxt(String humidityValueTxt) {
        HumidityValueTxt = humidityValueTxt;
    }

    public void setMoistureValueTxt(String moistureValueTxt) {
        MoistureValueTxt = moistureValueTxt;
    }

    public String getDeviceDispTxt() {
        return DeviceDispTxt;
    }

    public void setDeviceDispTxt(String deviceDispTxt) {
        DeviceDispTxt = deviceDispTxt;
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

    public List<BaseSensor> getSensorDatas() {
        return SensorDatas;
    }

    public void setSensorDatas(List<BaseSensor> sensorDatas) {
        SensorDatas = sensorDatas;
    }

    public String getSGI() {
        return SGId;
    }

    public void setSGId(String SGId) {
        this.SGId = SGId;
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

    public void setGoodsName(String goodsName) {
        GoodsName = goodsName;
    }

    @NonNull
    public String getGoodsNameNonNull() {
        if (TextUtils.isEmpty(GoodsName)) {
            return "暂无";
        }
        return GoodsName;
    }

    public String getBeginTime() {
        return BeginTime;
    }

    public void setBeginTime(String beginTime) {
        BeginTime = beginTime;
    }

    @NonNull
    public String getBeginTimeNonNull() {
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

    public boolean query(String query) {
        if (getQuerableStr().toLowerCase().contains(query.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

    private String getQuerableStr() {
        return name + id + getGoodsName() + getState();
    }

    @Override
    public String toString() {
        return super.toString() +
                "DryingRoom{" +
                "GoodsId='" + GoodsId + '\'' +
                ", GoodsName='" + GoodsName + '\'' +
                ", BeginTime='" + BeginTime + '\'' +
                ", EndTime='" + EndTime + '\'' +
                ", TemperatureValueTxt='" + TemperatureValueTxt + '\'' +
                ", HumidityValueTxt='" + HumidityValueTxt + '\'' +
                ", DeviceDispTxt='" + DeviceDispTxt + '\'' +
                '}';
    }

    public boolean hasOnlineDevice() {
        boolean hasOnlineDevice = false;
        for (BaseDevice temp : getDeviceDatas()) {
            if (!BaseDevice.DEVICE_STATUS_OFFLINE.equals(temp.getDeviceStatus())) {
                hasOnlineDevice = true;
            }
        }
        if (!hasOnlineDevice) {
            if (!"未知".equals(TemperatureValueTxt) ||
                    !"未知".equals(HumidityValueTxt) ||
                    !"未知".equals(MoistureValueTxt) ||
                    !"无设备".equals(DeviceDispTxt)) {
                hasOnlineDevice = true;
            }
        }
        return hasOnlineDevice;
    }

    @Override
    public int compareTo(DryingRoom another) {
        if (hasOnlineDevice()) {
            if (another.hasOnlineDevice()) {
                return getName().compareTo(another.getName());
            } else {
                return -1;
            }
        } else {
            if (another.hasOnlineDevice()) {
                return 1;
            } else {
                return getName().compareTo(another.getName());
            }
        }
    }
}
