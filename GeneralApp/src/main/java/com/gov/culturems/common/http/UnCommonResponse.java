package com.gov.culturems.common.http;

/**
 * Created by peter on 2015/4/1.
 */
public class UnCommonResponse<T> {

    public int ResultCode;
    public String ResultMsg;
    private T Data;


    public T getData() {
        return Data;
    }

    public void setData(T data) {
        Data = data;
    }
}
