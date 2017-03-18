package com.gov.culturems.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gov.culturems.R;
import com.gov.culturems.entities.BaseSensor;

/**
 * 通过左右的滑动，可以加减一周或者一天，返回选中的当天时间
 * Created by peter on 5/20/15.
 */
public class RuleView extends RelativeLayout {

    public static final int TYPE_WARNING = 0;
    public static final int TYPE_CONTROL = 1;

    private int viewType;
    private Context context;
    private String sensorType;
    private TextView titleView;
    public NumberView thresholdUpView;
    public NumberView thresholdDownView;

    public RuleView(Context context, int viewType) {
        super(context);
        this.context = context;
        this.viewType = viewType;
        init();
    }

    private void init() {
        if (viewType == TYPE_CONTROL) {
            LayoutInflater.from(context).inflate(R.layout.control_view, this);
        } else {
            LayoutInflater.from(context).inflate(R.layout.warning_view, this);
        }
        titleView = (TextView) findViewById(R.id.title);
        thresholdUpView = (NumberView) findViewById(R.id.thresholdup);
        thresholdDownView = (NumberView) findViewById(R.id.thresholddown);
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
        titleView.setText(BaseSensor.sensorNameMap.get(sensorType));
        thresholdUpView.setUnit(BaseSensor.sensorUnitMap.get(sensorType));
        thresholdDownView.setUnit(BaseSensor.sensorUnitMap.get(sensorType));
    }

    public void setThresholdUp(String thresholdUp) {
        this.thresholdUpView.setNumberText(Float.parseFloat(thresholdUp));
    }

    public void setThresholdDown(String thresholDown) {
        this.thresholdDownView.setNumberText(Float.parseFloat(thresholDown));
    }

    public String getThresholdUp() {
        return this.thresholdUpView.getCurrentNumStr();
    }

    public String getThresholdDown() {
        return this.thresholdDownView.getCurrentNumStr();
    }

    public String getSensorType() {
        return sensorType;
    }

}
