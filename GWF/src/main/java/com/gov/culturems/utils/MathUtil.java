package com.gov.culturems.utils;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

/**
 * Created by peter on 2015/11/7.
 */
public class MathUtil {

    //平均值
    public static double getAverage(ArrayList<Entry> values) {
        double sum = 0;
        for (int i = 0; i < values.size(); i++) {
            sum += values.get(i).getVal();
        }
        return (sum / values.size());
    }

    //标准差
    public static double getStandardDevition(ArrayList<Entry> values) {
        double sum = 0;
        double average = getAverage(values);
        for (int i = 0; i < values.size(); i++) {
            sum += ((double) values.get(i).getVal() - average) * (values.get(i).getVal() - average);
        }
        return Math.sqrt(sum / (values.size()));
    }
}
