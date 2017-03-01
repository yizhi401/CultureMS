package com.gov.culturems.test;

import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 4/7/16.
 */
public class Test {
    private static class UploadParam {
        String SensorType;
        int ThresholdUp;
        int ThresholdDown;
    }

    static class AlarmReq {
        String SensorType;
        String ThresholdUp;
        String ThresholdDown;
    }

    static class WebsocketRequest {
        String mt;
        String ai;
        String gi;
        String di;
        List<Rule> rules;
        List<AlarmReq> alarms;
    }

    static class WebsocketResponse {
        String ai;
        String iscmded;
    }

    static class Rule {
        String type;
        String upper;
        String lower;

    }

    public static void main(String[] args) {
        WebsocketRequest newMessage = new WebsocketRequest();
        newMessage.gi = "mygi";
        newMessage.di = "mydi";
        newMessage.mt = "mymt";
//        newMessage.ai = AndroidUtil.getMyUUID(this);
        newMessage.ai = "user id";
        Rule temp = new Rule();
        temp.lower = "30";
        temp.upper = "30";
        temp.type = BaseSensor.SENSOR_TEMPERATURE;
        Rule humi = new Rule();
        humi.lower = "30";
        humi.upper = "30";
        humi.type = BaseSensor.SENSOR_HUMIDITY;
        newMessage.rules = new ArrayList<>();
        newMessage.rules.add(temp);
        newMessage.rules.add(humi);

        newMessage.alarms = new ArrayList<>();
        AlarmReq alarmReq = new AlarmReq();
        alarmReq.SensorType = BaseSensor.SENSOR_TEMPERATURE;
        alarmReq.ThresholdUp = "30";
        alarmReq.ThresholdDown = "30";
        newMessage.alarms.add(alarmReq);
        alarmReq = new AlarmReq();
        alarmReq.SensorType = BaseSensor.SENSOR_HUMIDITY;
        alarmReq.ThresholdUp = "30";
        alarmReq.ThresholdDown = "30";
        newMessage.alarms.add(alarmReq);

        String sentMessage = GsonUtils.toJson(newMessage);

        System.out.println(sentMessage);
    }
}
