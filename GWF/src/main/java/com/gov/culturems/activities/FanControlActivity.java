package com.gov.culturems.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.WebsocketService;
import com.gov.culturems.common.CommonConstant;
import com.gov.culturems.common.UserManager;
import com.gov.culturems.common.http.CommonResponse;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.BaseDevice;
import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.UIUtil;
import com.gov.culturems.views.NumberView;

import java.util.ArrayList;
import java.util.List;

/**
 * 风扇类，控制规则和告警规则查看以及修改
 * Created by peter on 11/10/15.
 */
public class FanControlActivity extends Activity implements View.OnClickListener {

    private static final String TAG = FanControlActivity.class.getName();

    private NumberView warningTemperatureThresholUp;
    private NumberView warningTemperatureThresholDown;
    private NumberView warningHumidityThresholUp;
    private NumberView warningHumidityThresholDown;

    private DryingRoom scene;

    private Button finishedBtn;

    //新版的device接口，为了获取全部的device相关数据
//    private DeviceInfo fullDeviceInfo;
    private List<DeviceRulesResponse> deviceRulesResponseList = new ArrayList<>();

    private Handler waitingDialogHandler;
    private Runnable handlerCallback = new Runnable() {
        @Override
        public void run() {
            UIUtil.dismissTipDialog(FanControlActivity.this);
            Toast.makeText(FanControlActivity.this, "修改失败，请重试！", Toast.LENGTH_SHORT).show();
        }
    };

    private BroadcastReceiver websocketResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UIUtil.dismissTipDialog(FanControlActivity.this);
            String message = intent.getStringExtra(WebsocketService.WEBSOCKET_MESSAGE);
            WebsocketResponse response = GsonUtils.fromJson(message, WebsocketResponse.class);
            Log.i(TAG, "intent received! message = " + message);
            if (response != null) {
                if (UserManager.getInstance().getUserId().equals(response.ai)) {
                    //确保是我发的信息
                    if ("1".equals(response.iscmded)) {
                        //请求成功
                        Toast.makeText(FanControlActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        Log.e(TAG, "websocket request failed");
                        Toast.makeText(FanControlActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Log.e(TAG, "websocket request failed");
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fan_setting_activity);

        scene = (DryingRoom) getIntent().getSerializableExtra("scene");
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setTitle(scene.getName());
        }

        warningHumidityThresholDown = (NumberView) findViewById(R.id.warning_humidity_thresholddown);
        warningHumidityThresholUp = (NumberView) findViewById(R.id.warning_humidty_thresholdup);
        warningTemperatureThresholDown = (NumberView) findViewById(R.id.warning_temperature_thresholddown);
        warningTemperatureThresholUp = (NumberView) findViewById(R.id.warning_temperature_thresholdup);
        findViewById(R.id.outer_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        finishedBtn = (Button) findViewById(R.id.finish_button);
        finishedBtn.setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WebsocketService.WEBSOCKET_SERVICE_RESPONSE);
        registerReceiver(websocketResponseReceiver, filter);

        if (scene.getDeviceDatas() == null || scene.getDeviceDatas().size() == 0) {
            Toast.makeText(this, "没有找到设备！", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            for (BaseDevice device : scene.getDeviceDatas()) {
                getDeviceRules(device.getId());
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(websocketResponseReceiver);
        websocketResponseReceiver = null;
        if (waitingDialogHandler != null) {
            waitingDialogHandler.removeCallbacks(handlerCallback);
            waitingDialogHandler = null;
            handlerCallback = null;
        }
        super.onDestroy();
    }

    private class DeviceRulesResponse {
        String mt;
        String gi;
        String di;
        List<Rule> rules;
        List<AlarmRsp> alarms;
    }

    class AlarmRsp {
        String DeviceId;
        String SensorType;
        String SensorTypeName;
        String ThresholdUp;
        String ThresholdDown;
        String SceneId;

        void ensureFloat() {
            if (TextUtils.isEmpty(ThresholdDown)) {
                ThresholdDown = "0";
            }
            if (TextUtils.isEmpty(ThresholdUp)) {
                ThresholdUp = "0";
            }
        }
    }

    class AlarmReq {
        String SensorType;
        String ThresholdUp;
        String ThresholdDown;
    }

//    private void getDeviceInfo(String deviceId) {
//        RequestParams params = new RequestParams();
//        params.put("DeviceId", deviceId);
//        HttpUtil.jsonRequestGet(this, URLRequest.DEVICE_GET, params, new VolleyRequestListener() {
//            @Override
//            public void onSuccess(String response) {
//                CommonResponse<DeviceInfo> result = GsonUtils.fromJson(response, new TypeToken<CommonResponse<DeviceInfo>>() {
//                });
//                if (result.getRc() == 200) {
//                    fullDeviceInfo = result.getData();
//                    if (fullDeviceInfo != null && fullDeviceInfo.getRules() != null) {
//                        for (DeviceRule temp : fullDeviceInfo.getRules()) {
//                            if (BaseSensor.SENSOR_TEMPERATURE.equals(temp.SensorType)) {
//                                temperatureThresholDown.setNumberText(Float.parseFloat(temp.ThresholdDown));
//                                temperatureThresholUp.setNumberText(Float.parseFloat(temp.ThresholdUp));
//                            }
//                            if (BaseSensor.SENSOR_HUMIDITY.equals(temp.SensorType)) {
//                                humidityThresholDown.setNumberText(Float.parseFloat(temp.ThresholdDown));
//                                humidityThresholUp.setNumberText(Float.parseFloat(temp.ThresholdUp));
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onNetError(VolleyError error) {
//            }
//        });
//    }

    class WebsocketRequest {
        String mt;
        String ai;
        String gi;
        String di;
        List<Rule> rules;
        List<AlarmReq> alarms;
    }

    class WebsocketResponse {
        String ai;
        String iscmded;
    }

    class Rule {
        String type;
        String upper;
        String lower;

        void ensureFloat() {
            if (TextUtils.isEmpty(upper)) {
                upper = "0";
            }
            if (TextUtils.isEmpty(lower)) {
                lower = "0";
            }
        }
    }

    /**
     * 新的向服务器更新请求的方式
     */
    private void uploadDeviceDataUsingWebsocket() {
        for (DeviceRulesResponse tempDevice : deviceRulesResponseList) {

            WebsocketRequest newMessage = new WebsocketRequest();
            newMessage.gi = tempDevice.gi;
            newMessage.di = tempDevice.di;
            newMessage.mt = tempDevice.mt;
//        newMessage.ai = AndroidUtil.getMyUUID(this);
            newMessage.ai = UserManager.getInstance().getUserId();
            Rule temp = new Rule();
            temp.type = BaseSensor.SENSOR_TEMPERATURE;
            temp.lower = "0.0";
            temp.upper = "100.0";
            Rule humi = new Rule();
            humi.type = BaseSensor.SENSOR_HUMIDITY;
            humi.lower = "0.0";
            humi.upper = "100.0";
            newMessage.rules = new ArrayList<>();
            newMessage.rules.add(temp);
            newMessage.rules.add(humi);

            newMessage.alarms = new ArrayList<>();
            AlarmReq alarmReq = new AlarmReq();
            alarmReq.SensorType = BaseSensor.SENSOR_TEMPERATURE;
            alarmReq.ThresholdUp = warningTemperatureThresholUp.getCurrentNumStr();
            alarmReq.ThresholdDown = warningTemperatureThresholDown.getCurrentNumStr();
            newMessage.alarms.add(alarmReq);
            alarmReq = new AlarmReq();
            alarmReq.SensorType = BaseSensor.SENSOR_HUMIDITY;
            alarmReq.ThresholdUp = warningHumidityThresholUp.getCurrentNumStr();
            alarmReq.ThresholdDown = warningHumidityThresholDown.getCurrentNumStr();
            newMessage.alarms.add(alarmReq);

            String sentMessage = GsonUtils.toJson(newMessage);
            Log.e("mInfo", "send message: " + sentMessage);
            Intent intent = new Intent();
            intent.setAction(WebsocketService.WEBSOCKET_SERVICE_REQUEST);
            intent.putExtra(WebsocketService.WEBSOCKET_MESSAGE, sentMessage);
            sendBroadcast(intent);
            UIUtil.showTipDialog(FanControlActivity.this, CommonConstant.DIALOG_TYPE_WAITING, "正在修改规则...");
            if (waitingDialogHandler == null) {
                waitingDialogHandler = new Handler();
            }
            waitingDialogHandler.postDelayed(handlerCallback, 10000);
        }
    }

    private List<UploadParam> getUploadData() {
        List<UploadParam> list = new ArrayList<>();
        UploadParam temperature = new UploadParam();
        temperature.SensorType = BaseSensor.SENSOR_TEMPERATURE;

        UploadParam humidity = new UploadParam();
        humidity.SensorType = BaseSensor.SENSOR_HUMIDITY;
        list.add(temperature);
        list.add(humidity);
        return list;
    }

    public void getDeviceRules(String deviceId) {
        RequestParams params = new RequestParams();
        params.put("DeviceId", deviceId);
        HttpUtil.jsonRequestGet(this, URLRequest.DEVICE_RULES_GET, params, new VolleyRequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        CommonResponse<DeviceRulesResponse> commonResponse = GsonUtils.fromJson(response, new TypeToken<CommonResponse<DeviceRulesResponse>>() {
                        });
                        deviceRulesResponseList.add(commonResponse.getData());

                        if (commonResponse.getRc() == 200 && commonResponse.getData() != null) {
                            if (commonResponse.getData().rules != null)
                                for (Rule temp : commonResponse.getData().rules) {
                                    temp.ensureFloat();
                                    if (BaseSensor.SENSOR_TEMPERATURE.equals(temp.type)) {
                                    }
                                    if (BaseSensor.SENSOR_HUMIDITY.equals(temp.type)) {
                                    }
                                }
                            if (commonResponse.getData().alarms != null)
                                for (AlarmRsp temp : commonResponse.getData().alarms) {
                                    temp.ensureFloat();
                                    if (BaseSensor.SENSOR_TEMPERATURE.equals(temp.SensorType)) {
                                        warningTemperatureThresholDown.setNumberText(Float.parseFloat(temp.ThresholdDown));
                                        warningTemperatureThresholUp.setNumberText(Float.parseFloat(temp.ThresholdUp));
                                    }
                                    if (BaseSensor.SENSOR_HUMIDITY.equals(temp.SensorType)) {
                                        warningHumidityThresholDown.setNumberText(Float.parseFloat(temp.ThresholdDown));
                                        warningHumidityThresholUp.setNumberText(Float.parseFloat(temp.ThresholdUp));
                                    }
                                }
                        }
                    }

                    @Override
                    public void onNetError(VolleyError error) {

                    }
                }

        );
    }

//    public void getDeviceRules() {
//        RequestParams params = new RequestParams();
//        params.put("DeviceId", deviceInfo.getId());
//        HttpUtil.jsonRequestGet(this, URLRequest.DEVICE_CK_RULES_GET, params, new VolleyRequestListener() {
//            @Override
//            public void onSuccess(String response) {
//                CommonResponse<DeviceRulesResponse> commonResponse = GsonUtils.fromJson(response, new TypeToken<CommonResponse<DeviceRulesResponse>>() {
//                });
//                if (commonResponse.getRc() == 200 && commonResponse.getData() != null) {
//                    for (DeviceRulesResponse.Rule temp : commonResponse.getData().Rules) {
//                        if (BaseSensor.SENSOR_TEMPERATURE.equals(temp.SensorType)) {
//                            temperatureThresholDown.setNumberText(Float.parseFloat(temp.ThresholdDown));
//                            temperatureThresholUp.setNumberText(Float.parseFloat(temp.ThresholdUp));
//                        }
//                        if (BaseSensor.SENSOR_HUMIDITY.equals(temp.SensorType)) {
//                            humidityThresholDown.setNumberText(Float.parseFloat(temp.ThresholdDown));
//                            humidityThresholUp.setNumberText(Float.parseFloat(temp.ThresholdUp));
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onNetError(VolleyError error) {
//
//            }
//        });
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class UploadParam {
        String SensorType;
        String ThresholdUp;
        String ThresholdDown;
    }


    private void uploadDeviceControl(String deviceId) {
        RequestParams params = new RequestParams();
        params.put("DeviceId", deviceId);
        params.put("Rules", getUploadData());
        HttpUtil.jsonRequest(this, URLRequest.DEVICE_CK, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                CommonResponse commonResponse = GsonUtils.fromJson(response, CommonResponse.class);
                if (commonResponse.getRc() == 200) {
                    Toast.makeText(FanControlActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(FanControlActivity.this, commonResponse.getRm(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNetError(VolleyError error) {
            }
        });
    }

    public boolean checkDataValidity() {
        if (!permissible(warningTemperatureThresholUp.getCurrentNum()) ||
                !permissible(warningTemperatureThresholDown.getCurrentNum()) ||
                !permissible(warningHumidityThresholUp.getCurrentNum()) ||
                !permissible(warningHumidityThresholDown.getCurrentNum())) {
            Toast.makeText(this, "请输入0-100之间的值", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (warningTemperatureThresholDown.getCurrentNum() > warningTemperatureThresholUp.getCurrentNum()
                || warningHumidityThresholDown.getCurrentNum() > warningHumidityThresholUp.getCurrentNum()) {
            Toast.makeText(this, "最低值必须小于最高值！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean permissible(float num) {
        return num >= 0 && num <= 100;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish_button:
                if (checkDataValidity()) {
//                    uploadDeviceControl();
                    uploadDeviceDataUsingWebsocket();
                }
                break;
            default:
                break;
        }

    }

    private void startMainActivity() {
        Intent intent = new Intent(this, DryingRoomActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("Refresh", true);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }


}
