package com.gov.culturems.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.common.WebSocketHelper;
import com.gov.culturems.common.http.CommonResponse;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.entities.DCDevice;
import com.gov.culturems.entities.DeviceInfo;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.SharePreferUtil;
import com.gov.culturems.views.NumberView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 11/10/15.
 */
public class FanControlActivity extends Activity implements View.OnClickListener {

//    private static final String PREFERENCE_OPEN_THRESHOLD = "sp_open_threshold";
//    private static final String PREFERENCE_CLOSE_THRESHOLD = "sp_close_threshold";

    private NumberView temperatureThresholUp;
    private NumberView temperatureThresholDown;
    private NumberView humidityThresholUp;
    private NumberView humidityThresholDown;

    private Button finishedBtn;
    private DCDevice deviceInfo;

    private DeviceInfo oldDeviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fan_setting_activity);

        deviceInfo = (DCDevice) getIntent().getSerializableExtra("dc_device");
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setTitle(deviceInfo.getName());
        }

        humidityThresholDown = (NumberView) findViewById(R.id.humidity_thresholddown);
        humidityThresholUp = (NumberView) findViewById(R.id.humidty_thresholdup);
        temperatureThresholDown = (NumberView) findViewById(R.id.temperature_thresholddown);
        temperatureThresholUp = (NumberView) findViewById(R.id.temperature_thresholdup);

        finishedBtn = (Button) findViewById(R.id.finish_button);
        finishedBtn.setOnClickListener(this);

        getDeviceInfo();
        getDeviceRules();
//        showErrorLog();
    }

//    private void showErrorLog() {
//        if (VersionController.isDebug) {
//            TextView textView = (TextView) findViewById(R.id.error_log);
//            textView.setVisibility(View.VISIBLE);
//            textView.setText("URL = " + SharePreferUtil.getStringDataFromSharePreference(SharePreferUtil.ERROR_URL) + "\n" +
//                    "TOKEN = " + SharePreferUtil.getStringDataFromSharePreference(SharePreferUtil.ERROR_TOKEN) + "\n" +
//                    "Response = " + SharePreferUtil.getStringDataFromSharePreference(SharePreferUtil.ERROR_RESPONSE));
//        }
//    }

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

    private void getDeviceInfo() {
        RequestParams params = new RequestParams();
        params.put("DeviceId", deviceInfo.getId());
        HttpUtil.jsonRequestGet(this, URLRequest.DEVICE_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                CommonResponse<DeviceInfo> result = GsonUtils.fromJson(response, new TypeToken<CommonResponse<DeviceInfo>>() {
                });
                if (result.getRc() == 200) {
                    oldDeviceInfo = result.getData();
                }
            }

            @Override
            public void onNetError(VolleyError error) {
            }
        });
    }

    class WebsocketMessage {
        String mt;
        String ai;
        String gi;
        String di;
        List<Rule> ruels;
    }

    class Rule {
        String type;
        String upper;
        String lower;
    }

    private void sendMessage(String message) {
        WebsocketMessage newMessage = new WebsocketMessage();
        newMessage.gi =oldDeviceInfo.getGateId();
        newMessage.di =oldDeviceInfo.getDeviceId();
        newMessage.mt = "rule";
        newMessage.ai = "123456";
        Rule temp = new Rule();
        temp.lower = "10.00";
        temp.upper = "20.00";
        temp.type = "Temperature";
        Rule humi = new Rule();
        humi.lower = "30.00";
        humi.upper = "50.00";
        humi.type = "Humidity";
        newMessage.ruels = new ArrayList<>();
        newMessage.ruels.add(temp);
        newMessage.ruels.add(humi);

        String sentMessage = GsonUtils.toJson(newMessage);
        Log.e("mInfo", "send message: " + sentMessage);
        WebSocketHelper.getInstance().sendMessage(sentMessage);
   }


    public void getDeviceRules() {
        RequestParams params = new RequestParams();
        params.put("DeviceId", deviceInfo.getId());
        HttpUtil.jsonRequestGet(this, URLRequest.DEVICE_CK_RULES_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                CommonResponse<DeviceRulesResponse> commonResponse = GsonUtils.fromJson(response, new TypeToken<CommonResponse<DeviceRulesResponse>>() {
                });
                if (commonResponse.getRc() == 200 && commonResponse.getData() != null) {
                    for (DeviceRulesResponse.Rule temp : commonResponse.getData().Rules) {
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

            @Override
            public void onNetError(VolleyError error) {

            }
        });
    }

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

    private void uploadDeviceControl() {
        RequestParams params = new RequestParams();
        params.put("DeviceId", deviceInfo.getId());
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
                    uploadDeviceControl();
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
