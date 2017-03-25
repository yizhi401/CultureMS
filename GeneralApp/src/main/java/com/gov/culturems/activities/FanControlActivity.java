package com.gov.culturems.activities;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.WebsocketService;
import com.gov.culturems.common.CommonConstant;
import com.gov.culturems.common.UserManager;
import com.gov.culturems.common.base.BaseActivity;
import com.gov.culturems.common.http.CommonResponse;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.UnCommonResponse;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.DCDevice;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.UIUtil;
import com.gov.culturems.views.RuleView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 风扇类，控制规则和告警规则查看以及修改
 * Created by peter on 11/10/15.
 */
public class FanControlActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = FanControlActivity.class.getName();

    private static final int TAB_CONTROL = 0;
    private static final int TAB_WARNING = 1;
    private static final int PAGE_SIZE = 20;


    private LinearLayout titleLayout;
    private LinearLayout controlLayout;
    private LinearLayout warningLayout;
    private RelativeLayout controlTitle;
    private RelativeLayout warningTitle;

    private int currentTab;

    private List<RuleView> controlViewList = new ArrayList<>();
    private List<RuleView> warningViewList = new ArrayList<>();


    private Button finishedBtn;

    //新版的device接口，为了获取全部的device相关数据
//    private DeviceInfo fullDeviceInfo;
    private DeviceRulesResponseNew deviceRulesResponseNew;

    private DeviceRulesResponse deviceRulesResponse;

    private DryingRoom dryingRoom;
    private DCDevice deviceInfo;

    private boolean warningUploadSucceeded = false;
    private boolean controlRuleUploadSucceeded = false;

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
                if (checkResponeValid(response)) {
                    //确保是我发的信息
                    if ("1".equals(response.iscmded)) {
                        //请求成功
                        //需要等待控制和报警都成功才算成功
                        controlRuleUploadSucceeded = true;
                        if (warningUploadSucceeded) {
                            Toast.makeText(FanControlActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
                            startMainActivity();
                        }
                    } else {
                        controlRuleUploadSucceeded = false;
                        Log.e(TAG, "websocket request failed");
                        Toast.makeText(FanControlActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    controlRuleUploadSucceeded = false;
                    Log.e(TAG, "Check ai failed");
                }
            } else {
                controlRuleUploadSucceeded = false;
                Log.e(TAG, "websocket request failed");
            }

        }
    };

    private boolean checkResponeValid(WebsocketResponse response) {
        try {
            if (dryingRoom != null) {
                return response.ai.equals(deviceRulesResponseNew.ai);
            } else {
                return UserManager.getInstance().getUserId().equals(response.ai);
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fan_setting_activity);

        deviceInfo = (DCDevice) getIntent().getSerializableExtra("dc_device");
        dryingRoom = (DryingRoom) getIntent().getSerializableExtra("scene");
        if (deviceInfo == null && dryingRoom == null) {
            Toast.makeText(this, "内部错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setTitle(deviceInfo == null ? dryingRoom.getName() : deviceInfo.getName());
        }

        currentTab = TAB_CONTROL;

        initView();


        IntentFilter filter = new IntentFilter();
        filter.addAction(WebsocketService.WEBSOCKET_SERVICE_RESPONSE);
        registerReceiver(websocketResponseReceiver, filter);
        if (deviceInfo == null) {
            getDeviceRulesNew(dryingRoom.getId());
        } else {
            getDeviceRules(deviceInfo.getId());
        }
    }

    private void initView() {

        findViewById(R.id.outer_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        finishedBtn = (Button) findViewById(R.id.finish_button);
        finishedBtn.setOnClickListener(this);

        controlLayout = (LinearLayout) findViewById(R.id.control_layout);
        warningLayout = (LinearLayout) findViewById(R.id.warning_layout);
        controlTitle = (RelativeLayout) findViewById(R.id.control_title);
        controlTitle.setBackgroundResource(VersionController.getDrawable(VersionController.TAB_SELECTOR));
        controlTitle.setOnClickListener(this);
        warningTitle = (RelativeLayout) findViewById(R.id.warning_title);
        warningTitle.setOnClickListener(this);
        warningTitle.setBackgroundResource(VersionController.getDrawable(VersionController.TAB_SELECTOR));

        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        if (dryingRoom != null && DryingRoom.ROOM_TYPE_MONITOR.equals(dryingRoom.getSceneUseType())) {
            titleLayout.setVisibility(View.GONE);
            controlRuleUploadSucceeded = true;
            currentTab = TAB_WARNING;
        }
        refreshTabLayout();
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


    class RuleRsp {
        String ThresholdUp;
        String ThresholdDown;
        String SensorType;
        List<DevMonitor> DevMonitors;
        List<DevControl> DevContrls;

        void ensureFloat() {
            if (TextUtils.isEmpty(ThresholdDown)) {
                ThresholdDown = "0";
            }
            if (TextUtils.isEmpty(ThresholdUp)) {
                ThresholdUp = "0";
            }
        }
    }

    class DevMonitor {
        String DeviceName;
        String DeviceId;
    }

    class DevControl {
        String DeviceName;
        String DeviceId;
        String CtrlDirection;

    }

    private class DeviceRulesResponseNew {
        String ai;
        String mt;
        String gi;
        String di;
        String SceneId;
        List<RuleRsp> Rules;
        List<AlarmRsp> alarms;
    }

    private class DeviceRulesResponse {
        String ai;
        String mt;
        String gi;
        String di;
        String SceneId;
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
//      }
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
        WebsocketRequest newMessage = new WebsocketRequest();
        newMessage.gi = deviceRulesResponse.gi;
        newMessage.di = deviceRulesResponse.di;
        newMessage.mt = deviceRulesResponse.mt;
//        newMessage.ai = AndroidUtil.getMyUUID(this);
        newMessage.ai = UserManager.getInstance().getUserId();

        newMessage.rules = new ArrayList<>();
        for (RuleView r : controlViewList) {
            Rule temp = new Rule();
            temp.lower = r.thresholdDownView.getCurrentNumStr();
            temp.upper = r.thresholdUpView.getCurrentNumStr();
            temp.type = r.getSensorType();
            newMessage.rules.add(temp);
        }

        newMessage.alarms = new ArrayList<>();
        for (RuleView r : warningViewList) {

            AlarmReq alarmReq = new AlarmReq();
            alarmReq.SensorType = r.getSensorType();
            alarmReq.ThresholdUp = r.thresholdUpView.getCurrentNumStr();
            alarmReq.ThresholdDown = r.thresholdDownView.getCurrentNumStr();
            newMessage.alarms.add(alarmReq);
        }

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

    /**
     * 新的向服务器更新请求的方式
     */
    private void uploadDeviceDataUsingWebsocketNew() {
        for (RuleRsp temp : deviceRulesResponseNew.Rules) {
            for (RuleView r : controlViewList) {
                if (temp.SensorType.equals(r.getSensorType())) {
                    temp.ThresholdDown = r.thresholdDownView.getCurrentNumStr();
                    temp.ThresholdUp = r.thresholdUpView.getCurrentNumStr();
                }
            }
        }
        for (AlarmRsp temp : deviceRulesResponseNew.alarms) {
            for (RuleView r : warningViewList) {
                if (temp.SensorType.equals(r.getSensorType())) {
                    temp.ThresholdDown = r.thresholdDownView.getCurrentNumStr();
                    temp.ThresholdUp = r.thresholdUpView.getCurrentNumStr();
                }
            }
        }

        String sentMessage = GsonUtils.toJson(deviceRulesResponseNew);
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


    private List<UploadParam> getWarningUploadData() {
        List<UploadParam> list = new ArrayList<>();
        for (RuleView r : warningViewList) {
            UploadParam param = new UploadParam();
            param.SensorType = r.getSensorType();
            param.ThresholdDown = r.getThresholdDown();
            param.ThresholdUp = r.getThresholdUp();
            list.add(param);
        }
        return list;
    }

    public void getDeviceRulesNew(String sceneId) {
        RequestParams params = new RequestParams();
        params.put("SceneId", sceneId);
        HttpUtil.jsonRequestGet(this, URLRequest.DEVICE_RULES_GET_BY_SCENE, params, new VolleyRequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        UnCommonResponse<DeviceRulesResponseNew> commonResponse = GsonUtils.fromJson(response, new TypeToken<UnCommonResponse<DeviceRulesResponseNew>>() {
                        });
                        deviceRulesResponseNew = commonResponse.getData();
                        if (commonResponse.ResultCode == 200 && deviceRulesResponseNew != null) {
                            if (commonResponse.getData().Rules != null) {
                                Collections.sort(commonResponse.getData().Rules, new Comparator<RuleRsp>() {
                                    @Override
                                    public int compare(RuleRsp lhs, RuleRsp rhs) {
                                        return lhs.SensorType.compareTo(rhs.SensorType);
                                    }
                                });
                                for (RuleRsp temp : commonResponse.getData().Rules) {
                                    temp.ensureFloat();
                                    RuleView r = new RuleView(FanControlActivity.this, RuleView.TYPE_CONTROL);
                                    r.setSensorType(temp.SensorType);
                                    r.setThresholdDown(temp.ThresholdDown);
                                    r.setThresholdUp(temp.ThresholdUp);
                                    controlViewList.add(r);
                                    controlLayout.addView(r);
                                }
                            }
                            if (commonResponse.getData().alarms != null) {
                                Collections.sort(commonResponse.getData().alarms, new Comparator<AlarmRsp>() {
                                    @Override
                                    public int compare(AlarmRsp lhs, AlarmRsp rhs) {
                                        return lhs.SensorType.compareTo(rhs.SensorType);
                                    }
                                });
                                for (AlarmRsp temp : commonResponse.getData().alarms) {
                                    temp.ensureFloat();
                                    RuleView r = new RuleView(FanControlActivity.this, RuleView.TYPE_WARNING);
                                    r.setThresholdDown(temp.ThresholdDown);
                                    r.setThresholdUp(temp.ThresholdUp);
                                    r.setSensorType(temp.SensorType);
                                    warningViewList.add(r);
                                    warningLayout.addView(r);
                                }
                            }
                        }
                        controlLayout.requestLayout();
                        warningLayout.requestLayout();
                    }

                    @Override
                    public void onNetError(VolleyError error) {

                    }
                }

        );
    }

    public void getDeviceRules(String deviceId) {
        RequestParams params = new RequestParams();
        params.put("DeviceId", deviceId);
        HttpUtil.jsonRequestGet(this, URLRequest.DEVICE_RULES_GET, params, new VolleyRequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        CommonResponse<DeviceRulesResponse> commonResponse = GsonUtils.fromJson(response, new TypeToken<CommonResponse<DeviceRulesResponse>>() {
                        });
                        deviceRulesResponse = commonResponse.getData();
                        if (commonResponse.getRc() == 200 && deviceRulesResponse != null) {
                            if (commonResponse.getData().rules != null) {
                                Collections.sort(commonResponse.getData().rules, new Comparator<Rule>() {
                                    @Override
                                    public int compare(Rule lhs, Rule rhs) {
                                        return lhs.type.compareTo(rhs.type);
                                    }
                                });
                                for (Rule temp : commonResponse.getData().rules) {
                                    temp.ensureFloat();
                                    RuleView r = new RuleView(FanControlActivity.this, RuleView.TYPE_CONTROL);
                                    r.setSensorType(temp.type);
                                    r.setThresholdDown(temp.lower);
                                    r.setThresholdUp(temp.upper);
                                    controlViewList.add(r);
                                    controlLayout.addView(r);

                                }
                            }
                            if (commonResponse.getData().alarms != null) {
                                Collections.sort(commonResponse.getData().alarms, new Comparator<AlarmRsp>() {
                                    @Override
                                    public int compare(AlarmRsp lhs, AlarmRsp rhs) {
                                        return lhs.SensorType.compareTo(rhs.SensorType);
                                    }
                                });
                                for (AlarmRsp temp : commonResponse.getData().alarms) {
                                    temp.ensureFloat();
                                    RuleView r = new RuleView(FanControlActivity.this, RuleView.TYPE_WARNING);
                                    r.setThresholdDown(temp.ThresholdDown);
                                    r.setThresholdUp(temp.ThresholdUp);
                                    r.setSensorType(temp.SensorType);
                                    warningViewList.add(r);
                                    warningLayout.addView(r);
                                }
                            }
                        }
                        warningLayout.requestLayout();
                        controlLayout.requestLayout();
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


    private void uploadDeviceControl() {
        RequestParams params = new RequestParams();
        if (dryingRoom != null) {
            params.put("SceneId", dryingRoom.getId());
        } else {
            //集中式报警无需通过服务器上传
            warningUploadSucceeded = true;
            return;
//            params.put("SceneId", "");
        }
//        if (deviceInfo != null) {
//            params.put("DeviceId", deviceInfo.getId());
//        } else {
//            params.put("DeviceId", "");
//        }
        params.put("AlertSettings", "");
        params.put("Alarms", getWarningUploadData());
        params.put("LinkMans", "");
        params.put("AlertControl", "0");
        HttpUtil.jsonRequest(this, URLRequest.ALERT_SETTING_ADD_BATCH, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                CommonResponse commonResponse = GsonUtils.fromJson(response, CommonResponse.class);
                if (commonResponse.getRc() == 200) {
                    Log.i(TAG, "Upload Warning Data Succeeded");
                    //需要等待控制和报警都成功才算成功
                    warningUploadSucceeded = true;
                    if (controlRuleUploadSucceeded) {
                        Toast.makeText(FanControlActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    }
//                    Toast.makeText(FanControlActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
//                    finish();
                } else {
                    warningUploadSucceeded = false;
                    Toast.makeText(FanControlActivity.this, commonResponse.getRm(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNetError(VolleyError error) {
            }
        });
    }

    public boolean checkDataValidity() {
        for (RuleView r : controlViewList) {
            if (!permissible(r.thresholdUpView.getCurrentNum()) ||
                    !permissible(r.thresholdDownView.getCurrentNum())) {
                Toast.makeText(this, "请输入0-100之间的值", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (r.thresholdDownView.getCurrentNum() > r.thresholdUpView.getCurrentNum()) {
                Toast.makeText(this, "最低值必须小于最高值！", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        for (RuleView r : warningViewList) {
            if (!permissible(r.thresholdUpView.getCurrentNum()) ||
                    !permissible(r.thresholdDownView.getCurrentNum())) {
                Toast.makeText(this, "请输入0-100之间的值", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (r.thresholdDownView.getCurrentNum() > r.thresholdUpView.getCurrentNum()) {
                Toast.makeText(this, "最低值必须小于最高值！", Toast.LENGTH_SHORT).show();
                return false;
            }
        }


        return true;
    }

    private boolean permissible(float num) {
        return num >= 0 && num <= 100;
    }

    private void refreshTabLayout() {
        switch (currentTab) {
            case TAB_CONTROL:
                controlTitle.setSelected(true);
                warningTitle.setSelected(false);
                controlLayout.setVisibility(View.VISIBLE);
                warningLayout.setVisibility(View.GONE);
                break;
            case TAB_WARNING:
                controlTitle.setSelected(false);
                warningTitle.setSelected(true);
                controlLayout.setVisibility(View.GONE);
                warningLayout.setVisibility(View.VISIBLE);
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish_button:
                if (checkDataValidity()) {
                    if (dryingRoom != null) {
                        uploadDeviceDataUsingWebsocketNew();
                    } else {
                        uploadDeviceDataUsingWebsocket();
                    }
                    uploadDeviceControl();
                }
                break;
            case R.id.control_title:
                if (currentTab != TAB_CONTROL) {
                    currentTab = TAB_CONTROL;
                    refreshTabLayout();
                }
                break;
            case R.id.warning_title:
                if (currentTab != TAB_WARNING) {
                    currentTab = TAB_WARNING;
                    refreshTabLayout();
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
