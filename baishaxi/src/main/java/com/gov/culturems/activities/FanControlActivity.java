package com.gov.culturems.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.gov.culturems.R;
import com.gov.culturems.common.http.CommonResponse;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLConstant;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.DeviceInfo;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.ParseUtil;
import com.gov.culturems.utils.SharePreferUtil;
import com.gov.culturems.views.ActionSheetDialog;
import com.gov.culturems.views.NumberView;

/**
 * Created by peter on 11/10/15.
 */
public class FanControlActivity extends Activity implements View.OnClickListener {

    private static final String PREFERENCE_OPEN_THRESHOLD = "sp_open_threshold";
    private static final String PREFERENCE_CLOSE_THRESHOLD = "sp_close_threshold";


    private TextView closeCondition;
    private TextView openCondition;
    private NumberView openThreshold;
    private NumberView closeThreshold;

    private Button finishedBtn;
    private DeviceInfo deviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fan_setting_activity);


        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device_data");

        getActionBar().setTitle(deviceInfo.getDeviceName());

        closeCondition = (TextView) findViewById(R.id.close_condition);
        closeCondition.setText(DeviceInfo.conditionMap.get(deviceInfo.getDeviceConClose()));
        closeCondition.setOnClickListener(this);
        openCondition = (TextView) findViewById(R.id.open_condition);
        openCondition.setText(DeviceInfo.conditionMap.get(deviceInfo.getDeviceConOpen()));
        openCondition.setOnClickListener(this);
        openThreshold = (NumberView) findViewById(R.id.open_threshold);
        openThreshold.setNumberText(ParseUtil.parseInt(deviceInfo.getThresholdOpen()));
        closeThreshold = (NumberView) findViewById(R.id.close_threshold);
        closeThreshold.setNumberText(ParseUtil.parseInt(deviceInfo.getThresholdClose()));

        finishedBtn = (Button) findViewById(R.id.finish_button);
        finishedBtn.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadDeviceControl() {
        RequestParams params = new RequestParams();
        params.put("DeviceId", deviceInfo.getDeviceId());
        params.put("DeviceConOpen", DeviceInfo.conditionMap.get(openCondition.getText().toString()));
        params.put("ThresholdOpen", openThreshold.getCurrentNum() + "");
        params.put("DeviceConClose", DeviceInfo.conditionMap.get(closeCondition.getText().toString()));
        params.put("ThresholdClose", closeThreshold.getCurrentNum() + "");
        HttpUtil.jsonRequest(this, URLConstant.DEVICE_CTRL, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                CommonResponse commonResponse = GsonUtils.fromJson(response, CommonResponse.class);
                if (commonResponse.getRc() == 200) {
                    SharePreferUtil.saveIntToSharePrefer(PREFERENCE_OPEN_THRESHOLD, openThreshold.getCurrentNum());
                    SharePreferUtil.saveIntToSharePrefer(PREFERENCE_CLOSE_THRESHOLD, closeThreshold.getCurrentNum());
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_condition:
                showOpenCondition();
                break;
            case R.id.close_condition:
                showCloseCondition();
                break;
            case R.id.finish_button:
                uploadDeviceControl();
                break;
            default:
                break;
        }

    }

    private void showCloseCondition() {

        final ActionSheetDialog dialog = new ActionSheetDialog(FanControlActivity.this)
                .builder()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .addSheetItem(DeviceInfo.TEMPERATURE_HIGHER, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                closeCondition.setText(DeviceInfo.TEMPERATURE_HIGHER);
                            }
                        })
                .addSheetItem(DeviceInfo.TEMPERATURE_LOWER, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                closeCondition.setText(DeviceInfo.TEMPERATURE_LOWER);
                            }
                        })
                .addSheetItem(DeviceInfo.HUMIDITY_HIGHER, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                closeCondition.setText(DeviceInfo.HUMIDITY_HIGHER);
                            }
                        })
                .addSheetItem(DeviceInfo.HUMIDITY_LOWER, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                closeCondition.setText(DeviceInfo.HUMIDITY_LOWER);
                            }
                        });
        dialog.show();
    }

    private void showOpenCondition() {
        final ActionSheetDialog dialog = new ActionSheetDialog(FanControlActivity.this)
                .builder()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .addSheetItem(DeviceInfo.TEMPERATURE_HIGHER, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                openCondition.setText(DeviceInfo.TEMPERATURE_HIGHER);
                            }
                        })
                .addSheetItem(DeviceInfo.TEMPERATURE_LOWER, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                openCondition.setText(DeviceInfo.TEMPERATURE_LOWER);
                            }
                        })
                .addSheetItem(DeviceInfo.HUMIDITY_HIGHER, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                openCondition.setText(DeviceInfo.HUMIDITY_HIGHER);
                            }
                        })
                .addSheetItem(DeviceInfo.HUMIDITY_LOWER, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                openCondition.setText(DeviceInfo.HUMIDITY_LOWER);
                            }
                        });
        dialog.show();

    }
}
