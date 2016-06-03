package com.gov.culturems.common.http;
import android.content.Context;
import android.util.Log;


import com.gov.culturems.utils.AndroidUtil;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.HashMap;
import java.util.Map;

/**
 * 名字超级清楚的啦！
 * 提供两种header方式：
 * 1. Header[]
 * 2. Map<String,String>
 *
 * @author peter
 */
public class HttpHeader {

    private static HttpHeader instance;

    private Header[] headers;

    private Map<String, String> headerMap;

    private Context context;

    private HttpHeader(Context context) {
        this.context = context;
        initHeaders();
    }

    public static HttpHeader getInstance(Context context) {
        if (instance == null) {
            synchronized (HttpHeader.class) {
                if (instance == null) {
                    instance = new HttpHeader(context);
                }
            }
        }
        return instance;
    }

    /**
     */
    private void initHeaders() {
        headers = new Header[6];
        headers[0] = new BasicHeader("appVersion", AndroidUtil.getVersion(context));
        headers[1] = new BasicHeader("phoneModel", "google");
        headers[2] = new BasicHeader("platformType", AndroidUtil.getSystemVersion());
        headers[3] = new BasicHeader("accessToken", "1111");
        headers[4] = new BasicHeader("serviceProvider", "未知");
        headers[5] = new BasicHeader("networkType", "wifi");
        headerMap = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].getName(), headers[i].getValue());
        }
        printHeaders();
    }

    private void refreshHeaders() {
        // 有一些可能会变化的东西，每次调用Header就重新获取一下吧
//        headers[3] = new BasicHeader("accessToken", AccountManager.getInstance().getAccessToken());
        headerMap.put(headers[3].getName(), headers[3].getValue());
    }

    public Header[] getHeaders() {
        // 有一些可能会变化的东西，每次调用Header就重新获取一下吧
        refreshHeaders();
        return headers;
    }

    public Map<String, String> getHeaderMap() {
        refreshHeaders();
        return headerMap;
    }

    private void printHeaders() {
        for (int i = 0; i < headers.length; i++) {
            Log.i("http", headers[i].getName() + " " + headers[i].getValue());
        }
    }
}
