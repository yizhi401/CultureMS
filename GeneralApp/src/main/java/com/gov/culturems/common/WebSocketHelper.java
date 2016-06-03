package com.gov.culturems.common;

import android.util.Log;

import com.gov.culturems.utils.GsonUtils;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by peter on 2016/5/21.
 */
public class WebSocketHelper {

    private static final String TAG = "WebSocketHelper";

    private WebSocketClient client;// 连接客户端
    private DraftInfo selectDraft;// 连接协议
    private boolean isConnected = false;
    public static WebSocketHelper instance;

    public static WebSocketHelper getInstance() {
        if (instance == null) {
            synchronized (WebSocketHelper.class) {
                if (instance == null)
                    instance = new WebSocketHelper();
            }
        }
        return instance;
    }

    private WebSocketHelper() {
        init();
    }

    private void init() {
        DraftInfo[] draftInfos = {new DraftInfo("WebSocket协议Draft_17", new Draft_17()), new DraftInfo
                ("WebSocket协议Draft_10", new Draft_10()), new DraftInfo("WebSocket协议Draft_76", new Draft_76()), new
                DraftInfo("WebSocket协议Draft_75", new Draft_75())};// 所有连接协议
        selectDraft = draftInfos[0];// 默认选择第一个连接协议

        WebSocketImpl.DEBUG = true;
        System.setProperty("java.net.preferIPv6Addresses", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");

        connetToServer();
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
       try {
            if (client != null) {
                client.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connetToServer() {
        String serverIp = "ws://42.120.48.207:8016/CmdTran";
        Log.e(TAG, "连接地址：" + serverIp);
        try {
            client = new WebSocketClient(new URI(serverIp), selectDraft.draft) {
                @Override
                public void onOpen(final ServerHandshake serverHandshakeData) {
                    Log.e(TAG, "已经连接到服务器【" + getURI() + "】");
                }

                @Override
                public void onMessage(final String message) {
                    Log.e(TAG, "获取到服务器信息【" + message + "】");
                }

                @Override
                public void onClose(final int code, final String reason, final boolean remote) {
                    Log.e(TAG, "断开服务器连接【" + getURI() + "，状态码： " + code + "，断开原因：" + reason + "】");
                }

                @Override
                public void onError(final Exception e) {
                    Log.e(TAG, "连接发生了异常【异常原因：" + e + "】");
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.close();
    }

}
