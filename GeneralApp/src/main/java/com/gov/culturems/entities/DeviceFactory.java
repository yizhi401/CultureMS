package com.gov.culturems.entities;

/**
 * Created by peter on 4/6/16.
 */
public class DeviceFactory {
    public static BaseDevice createDevice(String UseType) {
        if (BaseDevice.USE_TYPE_CK.equals(UseType)) {
            return new DCDevice();
        } else if (BaseDevice.USE_TYPE_DETECTION.equals(UseType)) {
            return new DetectionDevice();
        } else if (BaseDevice.USE_TYPE_CONTROL.equals(UseType)) {
            return new ControlDevice();
        } else {
            return new BaseDevice();
        }
    }
}
