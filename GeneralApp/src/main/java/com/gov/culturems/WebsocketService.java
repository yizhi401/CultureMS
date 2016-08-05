package com.gov.culturems;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by peter on 2016/5/21.
 */
public class WebsocketService extends Service {

    private static final String TAG = WebsocketService.class.getName();
    private static final String SERVER_IP = "ws://121.41.230.252:8016/CmdTran";

    public static final String WEBSOCKET_SERVICE_REQUEST = "com.sean.action.websocket.request";
    public static final String WEBSOCKET_SERVICE_RESPONSE = "com.sean.action.websocket.response";
    public static final String WEBSOCKET_MESSAGE = "websocket_message";


    private WebSocketClient client;// 连接客户端
    private DraftInfo selectDraft;// 连接协议

    private boolean isConnected = false;

    private BroadcastReceiver websocketRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(WEBSOCKET_MESSAGE);
            sendMessage(message);
        }
    };

    public WebsocketService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isConnected) {
            init();
            connetToServer();
        }
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Websocket destroyed");
        client.close();
        unregisterReceiver(websocketRequestReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Websocket Created");
        IntentFilter filter = new IntentFilter();
        filter.addAction(WEBSOCKET_SERVICE_REQUEST);
        registerReceiver(websocketRequestReceiver, filter);
    }

    private void connetToServer() {
        Log.i(TAG, "connection address ：" + SERVER_IP);
        try {
            client = new WebSocketClient(new URI(SERVER_IP), selectDraft.draft) {
                @Override
                public void onOpen(final ServerHandshake serverHandshakeData) {
                    Log.i(TAG, "connected to server :【" + getURI() + "】");
                    isConnected = true;
                }

                @Override
                public void onMessage(final String message) {
                    Log.i(TAG, "received message from server【" + message + "】");
                    Intent intent = new Intent();
                    intent.setAction(WEBSOCKET_SERVICE_RESPONSE);
                    intent.putExtra(WEBSOCKET_MESSAGE, message);
                    sendBroadcast(intent);
                }

                @Override
                public void onClose(final int code, final String reason, final boolean remote) {
                    Log.i(TAG, "disconnected from server【" + getURI() + "，status code ： " + code + "，reason ：" + reason + "】");
                    isConnected = false;
                }

                @Override
                public void onError(final Exception e) {
                    Log.e(TAG, "connection error【 error ：" + e + "】");
                    isConnected = false;
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        DraftInfo[] draftInfos = {new DraftInfo("WebSocket协议Draft_17", new Draft_17()), new DraftInfo
                ("WebSocket协议Draft_10", new Draft_10()), new DraftInfo("WebSocket协议Draft_76", new Draft_76()), new
                DraftInfo("WebSocket协议Draft_75", new Draft_75())};// 所有连接协议
        selectDraft = draftInfos[0];// 默认选择第一个连接协议

        WebSocketImpl.DEBUG = true;
        System.setProperty("java.net.preferIPv6Addresses", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");

    }

    private class DraftInfo {

        private final String draftName;
        private final Draft draft;

        public DraftInfo(String draftName, Draft draft) {
            this.draftName = draftName;
            this.draft = draft;
        }

        @Override
        public String toString() {
            return draftName;
        }
    }

    public void sendMessage(String message) {
        if(!isConnected){
            connetToServer();
        }
        try {
            if (client != null) {
                client.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
