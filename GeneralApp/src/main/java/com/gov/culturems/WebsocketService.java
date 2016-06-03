package com.gov.culturems;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by peter on 2016/5/21.
 */
public class WebsocketService extends IntentService {

    private static final String TAG = WebsocketService.class.getName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WebsocketService(String name) {
        super(name);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Websocket destroyed");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Websocket Created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "new Intent comes!");
    }
}
