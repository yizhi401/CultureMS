package com.gov.culturems.entities;

import java.util.Map;


/**
 * 根据服务器返回数据，重新组合
 * 同一时刻不同的Sensor上报的数据
 * Created by peter on 10/29/15.
 */
public class DeviceDataForChart {
    public String insertTime;
    public Map<String, Sensor> sensorMap;

}
