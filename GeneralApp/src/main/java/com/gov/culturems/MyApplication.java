package com.gov.culturems;

import android.app.Application;
import android.content.Intent;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.gov.culturems.common.UserManager;
import com.gov.culturems.utils.SharePreferUtil;

/**
 * Created by peter on 6/20/15.
 */
public class MyApplication extends Application {

    private RequestQueue mRequestQueue;
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        UserManager.init(getApplicationContext());
        VersionController.setVersion(SharePreferUtil.getIntDataFromSharePreference(VersionController.VERSION_KEY));
        startWebSocketService();
        //this added by
    }

    public static MyApplication getInstance() {
        return instance;
    }

    private void startWebSocketService(){
        Intent intent = new Intent();
        intent.setClass(this,WebsocketService.class);
        startService(intent);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }


}
