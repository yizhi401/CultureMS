package com.gov.culturems.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.common.http.CommonResponse;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLConstant;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.DeviceInfo;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.views.DeviceItemView;

/**
 * 显示了该设备的相关信息
 * Created by peter on 6/9/15.
 */
public class DeviceInfoActivity extends Activity {

    private DeviceItemView nameItem;
    private DeviceItemView idItem;
    private DeviceItemView snItem;
    private DeviceItemView macItem;
    private DeviceItemView timeItem;
    private DeviceItemView onlineItem;
    private DeviceItemView voltageItem;
    private DeviceItemView alertItem;

    private DeviceInfo deviceInfo;
    private DeviceInfo device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        device = (DeviceInfo) getIntent().getSerializableExtra("device");
        getActionBar().setTitle(device.getDeviceName());
        initView();
        getDeviceInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {

        nameItem = (DeviceItemView) findViewById(R.id.name);
        idItem = (DeviceItemView) findViewById(R.id.id_num);
        snItem = (DeviceItemView) findViewById(R.id.sn_num);
        macItem = (DeviceItemView) findViewById(R.id.mac_addr);
        timeItem = (DeviceItemView) findViewById(R.id.insert_time);
        voltageItem = (DeviceItemView) findViewById(R.id.voltage);
        onlineItem = (DeviceItemView) findViewById(R.id.online_status);
        alertItem = (DeviceItemView) findViewById(R.id.alert);
    }

    private void refreshView() {
        nameItem.setDescription(deviceInfo.getDeviceName());

        //TODO
//        idItem.setDescription(deviceInfo.getDeviceId());
        idItem.setDescription(deviceInfo.getMacAddr());

        snItem.setDescription(deviceInfo.getDevSn());

        //TODO
//        macItem.setDescription(deviceInfo.getMacAddr());
        macItem.setDescription(deviceInfo.getDeviceId());

        timeItem.setDescription(deviceInfo.getInsertTime());

        voltageItem.setDescription(deviceInfo.getBaterryValue());

        alertItem.setDescription(deviceInfo.getAlertStatus());

        onlineItem.setDescription(deviceInfo.getDeviceStatus());

    }

    private void getDeviceInfo() {
        RequestParams params = new RequestParams();
        params.put("DeviceId", device.getDeviceId());
        HttpUtil.jsonRequestGet(this, URLConstant.DEVICE_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                CommonResponse<DeviceInfo> result = GsonUtils.fromJson(response, new TypeToken<CommonResponse<DeviceInfo>>() {
                });
                if (result.getRc() == 200) {
                    deviceInfo = result.getData();
                    refreshView();
                }
            }

            @Override
            public void onNetError(VolleyError error) {

            }
        });
    }


}
