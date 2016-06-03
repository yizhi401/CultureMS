package com.gov.culturems.common.http;
import com.android.volley.VolleyError;

/**
 * Created by peter on 14-8-6.
 */
public interface VolleyRequestListener {
    /**
     * 请求成功
     *
     * @param response
     */
    public void onSuccess(String response);

    /**
     * 网络错误，得到VolleyError
     *
     * @param error
     */
    public void onNetError(VolleyError error);

}
