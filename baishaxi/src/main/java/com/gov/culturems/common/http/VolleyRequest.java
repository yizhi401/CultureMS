package com.gov.culturems.common.http;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.LogUtil;
import com.gov.culturems.utils.SharePreferUtil;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * volley所用到的request
 * 目前只支持返回JSONObject类型,并且不提供get方法
 * 该类不需要使用者调用，只要HttpUtil调用
 * Created by peter on 14-8-6.
 */
public class VolleyRequest extends com.android.volley.Request<String> {

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";

    private static final String PROTOCOL_CHARSET = "utf-8";
    /**
     * Content type for request.
     */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private final Response.Listener<String> mListener;
    private Context context;
    private RequestParams parameters;
    private String mRequestBody;

    /**
     * @param context
     * @param url
     * @param params
     * @param listener
     */
    public <T> VolleyRequest(int method, Context context, String url, RequestParams params, final VolleyRequestListener listener) {
        //目前只允许使用post方式传输
        super(method, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                listener.onNetError(volleyError);
            }
        });

        Log.i("http", "URL = " + url);
        this.context = context;
        this.parameters = params;
        generateParams();
        mListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonObject) {
                //尚未测试volley返回的JSONObject是何种类型
                LogUtil.i("ResponseBody = " + jsonObject.toString());
                listener.onSuccess(jsonObject);
            }
        };

    }

    private void generateParams() {

        Map<String, Object> paramsMap = new HashMap<String, Object>();
//        paramsMap.putAll(HttpHeader.getInstance(context).getHeaderMap());
        paramsMap.putAll(parameters.getUrlParams());
        mRequestBody = GsonUtils.toJson(paramsMap);

        LogUtil.i("http", "requestBody = " + mRequestBody);
    }


    /**
     * @throws AuthFailureError
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null
                || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }
        try {
            headers.put(COOKIE_KEY, SharePreferUtil.getCookie());
            Log.i("mInfo", "cookie =" + SharePreferUtil.getCookie());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return headers;
    }


    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return null;
    }


    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);

    }


    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            //if we have header cookie, save it
            String setCookie = response.headers.get("Set-Cookie");
            if (!TextUtils.isEmpty(setCookie) && setCookie.contains("utoken")) {
                int index = setCookie.indexOf(";");
                SharePreferUtil.saveCookie(setCookie.substring(0, index));
                Log.e("mInfo", "cookie refreshde!" + "set cookie = " + setCookie);
            }
            parsed =
                    new String(response.data, PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
        return Response.success(parsed,
                HttpHeaderParser.parseCacheHeaders(response));
    }


    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }
}

