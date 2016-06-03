package com.gov.culturems.common.http;

import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用Request Parameter类
 * 借鉴自AndroidAsyncHttp，版权属于原作者
 *
 * @author peter 修改
 *         使用方法:
 *         RequestParams params = new RequestParams();
 *         params.put(&quot;username&quot;, &quot;james&quot;);
 *         params.put(&quot;password&quot;, &quot;123456&quot;);
 *         params.put(&quot;email&quot;, &quot;my@email.com&quot;);
 *         params.put(&quot;profile_picture&quot;, new File(&quot;pic.jpg&quot;)); // Upload a File
 *         params.put(&quot;profile_picture2&quot;, someInputStream); // Upload an InputStream
 *         params.put(&quot;profile_picture3&quot;, new ByteArrayInputStream(someBytes)); // Upload
 *         // some
 *         // bytes
 *         <p/>
 *         Map&lt;String, String&gt; map = new HashMap&lt;String, String&gt;();
 *         map.put(&quot;first_name&quot;, &quot;James&quot;);
 *         map.put(&quot;last_name&quot;, &quot;Smith&quot;);
 *         params.put(&quot;user&quot;, map); // url params:
 *         // &quot;user[first_name]=James&amp;user[last_name]=Smith&quot;
 *         <p/>
 *         </pre>
 */
public class RequestParams {

    protected final static String LOG_TAG = "RequestParams";
    protected final ConcurrentHashMap<String, Object> urlParams = new ConcurrentHashMap<String, Object>();

    /**
     * Constructs a new empty {@code RequestParams} instance.
     */
    public RequestParams() {
        this((Map<String, Object>) null);
    }

    /**
     * Constructs a new RequestParams instance containing the key/value string
     * params from the specified map.
     *
     * @param source the source key/value string map to add.
     */
    public RequestParams(Map<String, Object> source) {
        if (source != null) {
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * Constructs a new RequestParams instance and populate it with a single
     * initial key/value string param.
     *
     * @param key   the key name for the intial param.
     * @param value the value string for the initial param.
     */
    public RequestParams(final String key, final String value) {
        this(new HashMap<String, Object>() {
            {
                put(key, value);
            }
        });
    }


    /**
     * Adds a key/value string pair to the request.
     * <p/>
     * 这个方法会自动过滤掉一些特殊字符，如果不要过滤特殊字符，调用{@link #putWithoutFilter(String, String)}
     *
     * @param key   the key name for the new param.
     * @param value the value string for the new param.
     */
    public void put(String key, String value) {
        if (key != null && value != null) {
            // 防止注入攻击过滤掉特殊字符
            value = value.replace("<", "").replace(">", "").replace("'", "")
                    .replace("/", "").replace("%", "");
            urlParams.put(key, value);
        }
    }


    public void put(String key, Object value) {
        if (value != null)
            urlParams.put(key, value);
    }

    /**
     * Adds a key/value string pair to the request.
     * <p/>
     * 不过滤特殊字符
     *
     * @param key   the key name for the new param.
     * @param value the value string for the new param.
     */
    public void putWithoutFilter(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }


    /**
     * Adds a int value to the request.
     *
     * @param key   the key name for the new param.
     * @param value the value int for the new param.
     */
    public void put(String key, int value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    /**
     * Adds a long value to the request.
     *
     * @param key   the key name for the new param.
     * @param value the value long for the new param.
     */
    public void put(String key, long value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    /**
     * Add a double value to the request
     *
     * @param key
     * @param value
     */
    public void put(String key, double value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    /**
     * Removes a parameter from the request.
     *
     * @param key the key name for the parameter to remove.
     */
    public void remove(String key) {
        urlParams.remove(key);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, Object> entry : urlParams
                .entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        return result.toString();
    }


    private List<BasicNameValuePair> getParamsList(String key, Object value) {
        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        if (value instanceof String) {
            params.add(new BasicNameValuePair(key, (String) value));
        }
        return params;
    }


    public ConcurrentHashMap<String, Object> getUrlParams() {
        return urlParams;
    }

}


