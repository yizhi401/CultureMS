package com.gov.culturems.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * gson工具类，配合gson使用
 *
 * @author wuch
 */
public class GsonUtils {

    private static final String TAG = "JSON ERROR!";

    public static String EMPTY_JSON_OBJECT = "{}";

    public static String EMPTY_JSON_ARRAY = "[]";

    private static GsonBuilder builder;

    private static Gson gson;

    static {

        builder = new GsonBuilder();

        gson = builder.serializeNulls().create();

    }

    /**
     * json序列化
     *
     * @param target 对象
     * @return 失败会返回{}或者[]
     */
    public static String toJson(Object target) {
        String result = EMPTY_JSON_OBJECT;
        try {
            result = gson.toJson(target);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (target instanceof Collection<?>
                    || target instanceof Iterator<?>
                    || target instanceof Enumeration<?>
                    || target.getClass().isArray()) {
                result = EMPTY_JSON_ARRAY;
            } else {
                result = EMPTY_JSON_OBJECT;
            }

        }
        return result;
    }

    /**
     * json反序列化
     *
     * @param source
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String source, Class<T> cls) {
        T result = null;

        try {
            result = gson.fromJson(source, cls);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    /**
     * json反序列化
     *
     * @param source
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String source, TypeToken<T> type) {
        T result = null;

        try {
            result = gson.fromJson(source, type.getType());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    /**
     * json反序列化
     *
     * @param reader
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromJson(Reader reader, TypeToken<T> type) {
        T result = null;

        try {
            result = gson.fromJson(reader, type.getType());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public static GsonBuilder getBuilder() {
        return builder;
    }

}
