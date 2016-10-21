package com.gov.culturems.test;

import com.gov.culturems.utils.GsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 4/7/16.
 */
public class Test {
    private static class UploadParam {
        String SensorType;
        int ThresholdUp;
        int ThresholdDown;
    }

    public static void main(String[] args) {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
//        paramsMap.putAll(HttpHeader.getInstance(context).getHeaderMap());
        paramsMap.put("akey", "avalue");
        List<UploadParam> list = new ArrayList<>();
        UploadParam temp = new UploadParam();
        temp.SensorType = "sensor1";
        temp.ThresholdDown = 10;
        temp.ThresholdUp = 11;
        list.add(temp);
        temp = new UploadParam();
        temp.SensorType = "sensor2";
        temp.ThresholdDown = 10;
        temp.ThresholdUp = 11;
        list.add(temp);
        paramsMap.put("rule", list);
        String body = GsonUtils.toJson(paramsMap);

        System.out.println(body);
    }
}
