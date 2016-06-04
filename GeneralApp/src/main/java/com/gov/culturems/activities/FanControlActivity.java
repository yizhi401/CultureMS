package com.gov.culturems.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.WebsocketService;
import com.gov.culturems.common.http.CommonResponse;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.entities.DCDevice;
import com.gov.culturems.entities.DeviceInfo;
import com.gov.culturems.entities.DeviceRule;
import com.gov.culturems.utils.AndroidUtil;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.views.NumberView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 11/10/15.
 */
public class FanControlActivity extends Activity implements View.OnClickListener {

    private static final String TAG = FanControlActivity.class.getName();

    private NumberView temperatureThresholUp;
    private NumberView temperatureThresholDown;
    private NumberView humidityThresholUp;
    private NumberView humidityThresholDown;

    private NumberView warningTemperatureThresholUp;
    private NumberView warningTemperatureThresholDown;
    private NumberView warningHumidityThresholUp;
    private NumberView warningHumidityThresholDown;

    private Button finishedBtn;

    //新版的device接口，为了获取全部的device相关数据
    private DeviceInfo fullDeviceInfo;

    private BroadcastReceiver websocketResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(WebsocketService.WEBSOCKET_MESSAGE);
            WebsocketResponse response = GsonUtils.fromJson(message, WebsocketResponse.class);
            Log.i(TAG, "intent received! message = " + message);
            if (response != null) {
                if (AndroidUtil.getMyUUID(FanControlActivity.this).equals(response.ai)) {
                    //确保是我发的信息
                    if ("1".equals(response.iscmded)) {
                        //请求成功
                        Toast.makeText(FanControlActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        Log.e(TAG, "websocket request failed");
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

        DCDevice deviceInfo = (DCDevice) getIntent().getSerializableExtra("dc_device");
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setTitle(deviceInfo.getName());
        }

        humidityThresholDown = (NumberView) findViewById(R.id.humidity_thresholddown);
        humidityThresholUp = (NumberView) findViewById(R.id.humidty_thresholdup);
        temperatureThresholDown = (NumberView) findViewById(R.id.temperature_thresholddown);
        temperatureThresholUp = (NumberView) findViewById(R.id.temperature_thresholdup);

        warningHumidityThresholDown = (NumberView) findViewById(R.id.warning_humidity_thresholddown);
        warningHumidityThresholUp = (NumberView) findViewById(R.id.warning_humidty_thresholdup);
        warningTemperatureThresholDown = (NumberView) findViewById(R.id.warning_temperature_thresholddown);
        warningTemperatureThresholUp = (NumberView) findViewById(R.id.warning_temperature_thresholdup);


        finishedBtn = (Button) findViewById(R.id.finish_button);
        finishedBtn.setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WebsocketService.WEBSOCKET_SERVICE_RESPONSE);
        registerReceiver(websocketResponseReceiver, filter);

        getDeviceInfo(deviceInfo.getId());
//        getDeviceRules();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(websocketResponseReceiver);
        websocketResponseReceiver = null;
        super.onDestroy();
    }

    private class DeviceRulesResponse {
        String DeviceId;
        String DeviceName;
        List<Rule> Rules;

        class Rule {
            String DeviceId;
            String SensorType;
            String SensorTypeName;
            String ThresholdUp;
            String ThresholdDown;
        }
    }

    private void getDeviceInfo(String deviceId) {
        RequestParams params = new RequestParams();
        params.put("DeviceId", deviceId);
        HttpUtil.jsonRequestGet(this, URLRequest.DEVICE_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                CommonResponse<DeviceInfo> result = GsonUtils.fromJson(response, new TypeToken<CommonResponse<DeviceInfo>>() {
                });
                if (result.getRc() == 200) {
                    fullDeviceInfo = result.getData();
                    if (fullDeviceInfo != null && fullDeviceInfo.getRules() != null) {
                        for (DeviceRule temp : fullDeviceInfo.getRules()) {
                            if (BaseSensor.SENSOR_TEMPERATURE.equals(temp.SensorType)) {
                                temperatureThresholDown.setNumberText(Float.parseFloat(temp.ThresholdDown));
                                temperatureThresholUp.setNumberText(Float.parseFloat(temp.ThresholdUp));
                            }
                            if (BaseSensor.SENSOR_HUMIDITY.equals(temp.SensorType)) {
                                humidityThresholDown.setNumberText(Float.parseFloat(temp.ThresholdDown));
                                humidityThresholUp.setNumberText(Float.parseFloat(temp.ThresholdUp));
                            }
                        }
                    }
                }
            }

            @Override
            public void onNetError(VolleyError error) {
            }
        });
    }

    class WebsocketRequest {
        String mt;
        String ai;
        String gi;
        String di;
        List<Rule> ruels;
    }

    class WebsocketResponse {
        String ai;
        String iscmded;
    }

    class Rule {
        String type;
        String upper;
        String lower;
    }

    /**
     * 新的向服务器更新请求的方式
     */
    private void uploadDeviceDataUsingWebsocket() {
        WebsocketRequest newMessage = new WebsocketRequest();
        newMessage.gi = fullDeviceInfo.getGateId();
        newMessage.di = fullDeviceInfo.getDeviceId();
        newMessage.mt = "rule";
        newMessage.ai = AndroidUtil.getMyUUID(this);
        Rule temp = new Rule();
        temp.lower = temperatureThresholDown.getCurrentNumStr();
        temp.upper = temperatureThresholUp.getCurrentNumStr();
        temp.type = BaseSensor.SENSOR_TEMPERATURE;
        Rule humi = new Rule();
        humi.lower = humidityThresholDown.getCurrentNumStr();
        humi.upper = humidityThresholUp.getCurrentNumStr();
        humi.type = BaseSensor.SENSOR_HUMIDITY;
        newMessage.ruels = new ArrayList<>();
        newMessage.ruels.add(temp);
        newMessage.ruels.add(humi);
        String sentMessage = GsonUtils.toJson(newMessage);
        Log.e("mInfo", "send message: " + sentMessage);
        Intent intent = new Intent();
        intent.setAction(WebsocketService.WEBSOCKET_SERVICE_REQUEST);
        intent.putExtra(WebsocketService.WEBSOCKET_MESSAGE, sentMessage);
        sendBroadcast(intent);
    }

    private List<UploadParam> getUploadData() {
        List<UploadParam> list = new ArrayList<>();
        UploadParam temperature = new UploadParam();
        temperature.SensorType = BaseSensor.SENSOR_TEMPERATURE;
        temperature.ThresholdUp = temperatureThresholUp.getCurrentNumStr();
        temperature.ThresholdDown = temperatureThresholDown.getCurrentNumStr();

        UploadParam humidity = new UploadParam();
        humidity.SensorType = BaseSensor.SENSOR_HUMIDITY;
        humidity.ThresholdUp = humidityThresholUp.getCurrentNumStr();
        humidity.ThresholdDown = humidityThresholDown.getCurrentNumStr();
        list.add(temperature);
        list.add(humidity);
        return list;
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
        if (temperatureThresholUp.getCurrentNum() <= 0 ||
                temperatureThresholUp.getCurrentNum() > 100 ||
                temperatureThresholDown.getCurrentNum() <= 0 ||
                temperatureThresholDown.getCurrentNum() > 100 ||
                humidityThresholUp.getCurrentNum() <= 0 ||
                humidityThresholUp.getCurrentNum() > 100 ||
                humidityThresholDown.getCurrentNum() <= 0 ||
                humidityThresholDown.getCurrentNum() > 100) {
            Toast.makeText(this, "请输入0-100之间的值", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (temperatureThresholDown.getCurrentNum() >= temperatureThresholUp.getCurrentNum()
                || humidityThresholDown.getCurrentNum() >= humidityThresholUp.getCurrentNum()) {
            Toast.makeText(this, "最低值必须小于最高值！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish_button:
                if (checkDataValidity()) {
//                    uploadDeviceControl();
                    uploadDeviceDataUsingWebsocket();
                    startMainActivity();
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
