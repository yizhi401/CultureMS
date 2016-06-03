package com.gov.culturems.utils;

import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by peter on 2015/8/26.
 */
public class FloatValueFormatter implements ValueFormatter {
    @Override
    public String getFormattedValue(float value) {

        DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p = decimalFormat.format(value);//format 返回的是字符串
        return p;
    }
}
