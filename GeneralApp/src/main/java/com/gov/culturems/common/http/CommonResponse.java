package com.gov.culturems.common.http;
/**
 * Created by peter on 2015/4/1.
 */
public class CommonResponse<T> {
    private int rc;
    private String rm;
    private T Data;

    public int getRc() {
        return rc;
    }

    public void setRc(int rc) {
        this.rc = rc;
    }

    public String getRm() {
        return rm;
    }

    public void setRm(String rm) {
        this.rm = rm;
    }

    public T getData() {
        return Data;
    }

    public void setData(T data) {
        Data = data;
    }
}
