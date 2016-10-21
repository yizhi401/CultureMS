package com.gov.culturems.entities;

import java.util.Map;

/**
 * 根据服务器返回数据，重新组合
 * 同一时刻不同的Sensor上报的数据
 * Created by peter on 4/7/16.
 */

public class DeviceDataForChart {
    public static final String ALERT_STATUS_NORMAL = "正常";

    public String InsertTime;
    public String SensorValueH;
    public String SensorValueT;
    public String AlertStatusT;
    public String AlertStatusH;
    public String SensorId;
    public String DeviceId;
}
