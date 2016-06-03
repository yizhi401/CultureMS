package com.gov.culturems.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.common.CommonConstant;
import com.gov.culturems.common.base.MyBaseAdapter;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLConstant;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.AlertInfo;
import com.gov.culturems.entities.DeviceInfo;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.entities.Sensor;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.UIUtil;
import com.gov.culturems.views.LoadMoreListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示了该场景所有设备的信息
 * Created by peter on 6/9/15.
 */
public class DryingRoomDeviceActivity extends Activity implements View.OnClickListener {

    private static final int TAB_DATA = 0;
    private static final int TAB_WARNING = 1;
    private static final int PAGE_SIZE = 20;

    private TextView dataTextView;
    private TextView warningTextView;


    //    private SwipeRefreshLayout dataSwipeLayout;
    private RelativeLayout dataLayout;
    //    private LoadMoreListView dataList;
//    private List<DeviceInfo> sceneData;
//    private int dataPageIndex = 1;
//    private boolean hasMoreData = false;
    private ListView deviceDataList;

    private SwipeRefreshLayout warningSwipeLayout;
    private RelativeLayout warningLayout;
    private DataListAdapter dataAdapter;
    private LoadMoreListView warningList;
    private List<AlertInfo> warningData;
    private WarningListAdapter warningAdapter;
    private int warningPageIndex = 1;
    private boolean hasMoreWarning = false;
    private DryingRoom dryingRoom;

    private int currentTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drying_room_device_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        dryingRoom = (DryingRoom) getIntent().getSerializableExtra("dryingRoom");
        getActionBar().setTitle(dryingRoom.getSceneName());

        initViews();
        currentTab = TAB_DATA;
        getDevicesData();
        getWarningData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {

        dataTextView = (TextView) findViewById(R.id.data_text);
        dataTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(VersionController.getDrawable(VersionController.SCENE_DATA_IC), 0, 0, 0);
        warningTextView = (TextView) findViewById(R.id.warning_text);
        warningTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(VersionController.getDrawable(VersionController.SCENE_WARNING_IC), 0, 0, 0);

        warningSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.warning_swipe_refresh);
        warningSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                warningPageIndex = 1;
                hasMoreWarning = false;
                warningData.clear();
                getWarningData();
            }
        });

        dataLayout = (RelativeLayout) findViewById(R.id.data_layout);
        dataLayout.setOnClickListener(this);
        dataLayout.setSelected(true);
        warningLayout = (RelativeLayout) findViewById(R.id.warning_layout);
        warningLayout.setOnClickListener(this);
        deviceDataList = (ListView) findViewById(R.id.data_list);
        dataAdapter = new DataListAdapter(dryingRoom.getDeviceInfoList(), this);
        deviceDataList.setAdapter(dataAdapter);
        warningList = (LoadMoreListView) findViewById(R.id.warning_list);
        warningData = new ArrayList<>();
        warningAdapter = new WarningListAdapter(warningData, this);
        warningList.setAdapter(warningAdapter);
        warningList.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (hasMoreWarning) {
                    getWarningData();
                } else {
                    Toast.makeText(DryingRoomDeviceActivity.this, "没有更多了", Toast.LENGTH_SHORT).show();
                    warningList.onLoadMoreComplete();
                }
            }
        });

        deviceDataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DryingRoomDeviceActivity.this, DeviceDataActivity.class);
//                intent.putExtra("device_data", sceneData.get(position));
                startActivity(intent);
            }
        });

        warningList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.data_layout:
                if (currentTab != TAB_DATA) {
                    currentTab = TAB_DATA;
                    dataLayout.setSelected(true);
                    warningLayout.setSelected(false);
                    warningSwipeLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.warning_layout:
                if (currentTab != TAB_WARNING) {
//                    dataTextView.setSelected(false);
//                    warningTextView.setSelected(true);
                    dataLayout.setSelected(false);
                    warningLayout.setSelected(true);
                    currentTab = TAB_WARNING;
                    warningSwipeLayout.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    private void getDevicesData() {
    }

    /**
     * 获得该场景报警讯息
     */
    private void getWarningData() {
        if (!warningSwipeLayout.isRefreshing()) {
            UIUtil.showTipDialog(this, CommonConstant.DIALOG_TYPE_WAITING, "正在请求数据...");
        }
        RequestParams params = new RequestParams();
        params.put("SceneId", dryingRoom.getSceneId());
        params.put("pi", warningPageIndex);
        params.put("ps", PAGE_SIZE);
        HttpUtil.jsonRequestGet(this, URLConstant.ALERTS_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                UIUtil.dismissTipDialog(DryingRoomDeviceActivity.this);
                ListResponse<AlertInfo> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<AlertInfo>>() {
                });
                if (listResponse.getRc() == 200 && listResponse.getListData() != null) {
                    warningPageIndex++;
                    if (listResponse.getListData() != null && listResponse.getListData().size() >= PAGE_SIZE) {
                        hasMoreWarning = true;
                    } else {
                        hasMoreWarning = false;
                    }
                    warningData.addAll(listResponse.getListData());
//                    warningAdapter.setData(warningData);
                    warningAdapter.notifyDataSetChanged();
                }

                warningList.onLoadMoreComplete();
                if (warningSwipeLayout.isRefreshing()) {
                    warningSwipeLayout.setRefreshing(false);
                }
            }

            @Override
            public void onNetError(VolleyError error) {
                UIUtil.dismissTipDialog(DryingRoomDeviceActivity.this);
                warningList.onLoadMoreComplete();
                if (warningSwipeLayout.isRefreshing()) {
                    warningSwipeLayout.setRefreshing(false);
                }
            }
        });
    }

    private class DataListAdapter extends MyBaseAdapter<DeviceInfo> {

        public DataListAdapter(List<DeviceInfo> data, Context context) {
            super(data, context);
        }

        class DataHolder {
            TextView deviceName;
            TextView sensor1;
            TextView sensor2;
            TextView sensor3;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DataHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(DryingRoomDeviceActivity.this).inflate(R.layout.scene_data_list_item, null);
                holder = new DataHolder();
                holder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
                holder.sensor1 = (TextView) convertView.findViewById(R.id.sensor1);
                holder.sensor2 = (TextView) convertView.findViewById(R.id.sensor2);
                holder.sensor3 = (TextView) convertView.findViewById(R.id.sensor3);
                convertView.setTag(holder);
            } else {
                holder = (DataHolder) convertView.getTag();
            }
            DeviceInfo temp = data.get(position);
            holder.deviceName.setText(temp.getDeviceName());
            if ("offline".equals(temp.getDeviceStatus())) {
                holder.sensor1.setText("状态：离线");
                convertView.setBackgroundColor(getResources().getColor(R.color.gray_bg));
                holder.sensor2.setVisibility(View.GONE);
                holder.sensor3.setVisibility(View.GONE);
                return convertView;
            }
            convertView.setBackgroundColor(getResources().getColor(R.color.white));
            List<Sensor> sensors = temp.getSensorTypes();
            holder.sensor2.setVisibility(View.GONE);
            holder.sensor3.setVisibility(View.GONE);
            if (sensors == null || sensors.size() == 0) {
                //无传感器
                holder.sensor1.setText("暂无数据");
            } else if (sensors.size() == 1) {
                holder.sensor1.setText(getSensorText(sensors.get(0)));
            } else if (sensors.size() >= 2) {
                //超过两个传感器的，都是包含了温湿度，让温度显示在第一个，湿度在第二个
                holder.sensor2.setVisibility(View.VISIBLE);
                holder.sensor1.setText(getSensorText(getSpecificSenser(sensors, Sensor.SENSOR_TEMPERATURE)));
                holder.sensor2.setText(getSensorText(getSpecificSenser(sensors, Sensor.SENSOR_HUMIDITY)));
                if (sensors.size() >= 3) {
                    holder.sensor3.setVisibility(View.VISIBLE);
                    holder.sensor3.setText(getSensorText(getSensorLeft(sensors)));
                }
            }

            return convertView;

        }

        private String getSensorText(Sensor sensor) {
            if (sensor == null)
                return "";
            String sensorName = sensor.getSensorTypeName().replace("传感器", "");
            String appendix = sensor.getSensorUnit();
            if (sensor.getSensorType().equals(Sensor.SENSOR_TEMPERATURE)) {
                appendix = "℃";
            }
            if (sensorName.contains("浸水")) {
                sensorName = "状态";
            }
            return sensorName + ":" + sensor.getSensorValue() + " " + appendix;
        }

    }

    /**
     * get the sensor that is not humidity/temperature
     *
     * @param sensors
     * @return
     */
    private Sensor getSensorLeft(List<Sensor> sensors) {
        if (sensors == null)
            return null;
        for (Sensor temp : sensors) {
            if (!temp.getSensorType().equals(Sensor.SENSOR_TEMPERATURE) && !temp.getSensorType().equals(Sensor.SENSOR_HUMIDITY)) {
                return temp;
            }
        }
        return null;
    }

    /**
     * get humidity sensor or temperature sensor
     *
     * @param sensors
     * @param sensorType
     * @return
     */
    private Sensor getSpecificSenser(List<Sensor> sensors, String sensorType) {
        if (sensors == null)
            return null;
        for (Sensor temp : sensors)
            if (temp.getSensorType().equals(sensorType))
                return temp;
        return null;
    }


    private class WarningListAdapter extends MyBaseAdapter<AlertInfo> {

        public WarningListAdapter(List<AlertInfo> data, Context context) {
            super(data, context);
        }

        class AlertHolder {
            TextView deviceName;
            TextView sensorValue;
            TextView threshold;
            TextView warningTime;
            TextView alertType;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AlertHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(DryingRoomDeviceActivity.this).inflate(R.layout.scene_warning_list_item, null);
                holder = new AlertHolder();
                holder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
                holder.alertType = (TextView) convertView.findViewById(R.id.alert_type);
                holder.sensorValue = (TextView) convertView.findViewById(R.id.sensor_value);
                holder.warningTime = (TextView) convertView.findViewById(R.id.warning_time);
                holder.threshold = (TextView) convertView.findViewById(R.id.thresold);
                convertView.setTag(holder);
            } else {
                holder = (AlertHolder) convertView.getTag();
            }
            AlertInfo temp = data.get(position);

            holder.deviceName.setText(temp.getDeviceName());
            holder.sensorValue.setText("传感器值:" + temp.getSensorValue());
            holder.threshold.setText("警戒线:" + temp.getThreshold());
            holder.warningTime.setText("报警时间:" + temp.getInsertTime());
            holder.alertType.setText("报警类型:" + temp.getAlertTypeName());

            return convertView;

        }

    }
}
