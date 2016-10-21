package com.gov.culturems.utils;

import com.github.mikephil.charting.utils.ValueFormatter;

/**
 * Created by peter on 2015/8/26.
 */
public class IntValueFormatter implements ValueFormatter {
    @Override
    public String getFormattedValue(float value) {


        return (int) value + "";
    }
}
