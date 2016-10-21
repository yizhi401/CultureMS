package com.gov.culturems.entities;

import java.util.List;

/**
 * Created by peter on 4/6/16.
 */

public class BaseScene extends BaseObj {

    protected String SGId;
    protected String Memo;
    protected String State;
    protected List<BaseDevice> DeviceDatas;


    public String getSGId() {
        return SGId;
    }

    public void setSGId(String SGId) {
        this.SGId = SGId;
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
                "SGId='" + SGId + '\'' +
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
