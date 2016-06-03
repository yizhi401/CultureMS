package com.gov.culturems.common.http;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.gov.culturems.MyApplication;
import com.gov.culturems.VersionController;


/**
 * Created by peter on 14-8-6.
 */
public class HttpUtil {

    private static final boolean SHOULD_CACHE = false;
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;

    public static void jsonRequest(final Context context, String url, RequestParams params, VolleyRequestListener listener) {
        VolleyRequest request = new VolleyRequest(Request.Method.POST, context, VersionController.getURLServer() + url + URLConstant.APPENDIX, params, listener);
        request.setShouldCache(SHOULD_CACHE);
        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyApplication.getInstance().getRequestQueue().add(request);
    }

    public static void jsonRequestGet(final Context context, String url, RequestParams params, VolleyRequestListener listener) {
        VolleyRequest request = new VolleyRequest(Request.Method.GET, context, VersionController.getURLServer() + url + URLConstant.APPENDIX + "?" + params.toString(), params, listener);
        request.setShouldCache(SHOULD_CACHE);
        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MyApplication.getInstance().getRequestQueue().add(request);
    }

//    public static void jsonRequestWithPort(final Context context, String port, String url, RequestParams params, VolleyRequestListener listener) {
//        VolleyRequest request = new VolleyRequest(Request.Method.POST, context, VersionController.getURLServerWithoutPort() + ":" + port + "/" + url + URLConstant.APPENDIX, params, listener);
//        request.setShouldCache(SHOULD_CACHE);
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                MY_SOCKET_TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        MyApplication.getInstance().getRequestQueue().add(request);
//    }

//    public static void jsonRequestGetWithPort(final Context context, String port, String url, RequestParams params, VolleyRequestListener listener) {
//        VolleyRequest request = new VolleyRequest(Request.Method.GET, context, VersionController.getURLServerWithoutPort() + ":" + port + "/" + url + URLConstant.APPENDIX + "?" + params.toString(), params, listener);
//        request.setShouldCache(SHOULD_CACHE);
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                MY_SOCKET_TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        MyApplication.getInstance().getRequestQueue().add(request);
//    }

}
