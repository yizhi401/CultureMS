package com.gov.culturems.entities;

/**
 * Created by peter on 4/6/16.
 */
public class ControlSensor extends BaseSensor {
    private String ThresholdUp;
    private String ThresholdDown;


    public String getThresholdUp() {
        return ThresholdUp;
    }

    public void setThresholdUp(String thresholdUp) {
        ThresholdUp = thresholdUp;
    }

    public String getThresholdDown() {
        return ThresholdDown;
    }

    public void setThresholdDown(String thresholdDown) {
        ThresholdDown = thresholdDown;
    }
}
