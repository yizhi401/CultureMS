package com.gov.culturems.entities;

import java.util.List;

/**
 * Created by peter on 4/6/16.
 */

public class BaseScene extends BaseObj {

    protected String SGI;
    protected String Memo;
    protected String State;
    protected List<BaseDevice> DeviceDatas;


    public String getSGI() {
        return SGI;
    }

    public void setSGI(String SGI) {
        this.SGI = SGI;
    }

    public String getMemo() {
        return Memo;
    }

    public void setMemo(String memo) {
        Memo = memo;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public List<BaseDevice> getDeviceDatas() {
        return DeviceDatas;
    }

    public void setDeviceDatas(List<BaseDevice> deviceDatas) {
        DeviceDatas = deviceDatas;
    }

    @Override
    public String toString() {
        return super.toString() + "BaseScene{" +
                "SGI='" + SGI + '\'' +
                ", Memo='" + Memo + '\'' +
                ", State='" + State + '\'' +
                '}';
    }


    @Override
    public boolean query(String query) {
        if (this.toString().contains(query)) {
            return true;
        } else {
            return false;
        }
    }
}
