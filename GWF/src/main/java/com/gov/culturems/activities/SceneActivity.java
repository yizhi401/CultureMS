package com.gov.culturems.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.common.CommonConstant;
import com.gov.culturems.common.base.MyBaseAdapter;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.common.http.response.DeviceResp;
import com.gov.culturems.entities.BaseDevice;
import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.LogUtil;
import com.gov.culturems.utils.UIUtil;
import com.gov.culturems.views.LoadMoreListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示了该场景所有设备的信息
 * Created by peter on 6/9/15.
 */
public class SceneActivity extends Activity {

    public static final int REQUEST_CODE = 1000;
    public static final int RESULT_NEED_REFRESH = 1100;

    private static final int PAGE_SIZE = 20;


    private SwipeRefreshLayout dataSwipeLayout;
    private LoadMoreListView dataList;
    private List<BaseDevice> sceneData;
    private int dataPageIndex = 1;
    private boolean hasMoreData = false;

    private DataListAdapter dataAdapter;
    private DryingRoom scene;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scene_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        scene = (DryingRoom) getIntent().getSerializableExtra("scene");
        getActionBar().setTitle(scene.getName());

        initViews();
        getDevicesData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem itemEdit = menu.add(0, R.id.menu_edit, 0, "设置");
        itemEdit.setIcon(R.drawable.edit_icon_blue);
        itemEdit.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_edit) {
            jumpToRuleManageActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void jumpToRuleManageActivity() {
        Intent intent = new Intent(SceneActivity.this, FanControlActivity.class);
        intent.putExtra("scene",scene);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

    }


    private void initViews() {

        dataSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.data_swipe_refresh);
        dataSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataPageIndex = 1;
                sceneData.clear();
                getDevicesData();
                hasMoreData = false;
            }
        });

        dataList = (LoadMoreListView) findViewById(R.id.data_list);
        dataList.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (hasMoreData) {
                    getDevicesData();
                } else {
                    Toast.makeText(SceneActivity.this, "没有更多了", Toast.LENGTH_SHORT).show();
                    dataList.onLoadMoreComplete();
                }
            }
        });
        sceneData = new ArrayList<>();
        scene.setDeviceDatas(sceneData);
        dataAdapter = new DataListAdapter(sceneData, this);
        dataList.setAdapter(dataAdapter);

        dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(SceneActivity.this, DeviceDataActivity.class);
//                intent.putExtra("device_data", sceneData.get(position));
//                startActivity(intent);
                tryStartDeviceActivity(sceneData.get(position));
            }
        });
    }

    private void tryStartDeviceActivity(BaseDevice device) {
        if ("offline".equals(device.getDeviceStatus())) {
            Toast.makeText(this, "设备离线!", Toast.LENGTH_SHORT).show();
        }

        DryingRoomHelper helper = DryingRoomHelper.getInstance();
        helper.initDryingRoomInfo(scene, device, new DryingRoomHelper.DryingRoomInitListener() {
            @Override
            public void onInitSucceed() {
                Intent intent = new Intent(SceneActivity.this, DeviceDataActivity.class);
                SceneActivity.this.startActivityForResult(intent, DryingRoomActivity.REQUEST_CODE);
                SceneActivity.this.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            }

            @Override
            public void onInitFailed() {
                LogUtil.e("init drying room failed");
            }
        });
    }

    private void getDevicesData() {
        if (!dataSwipeLayout.isRefreshing()) {
            UIUtil.showTipDialog(this, CommonConstant.DIALOG_TYPE_WAITING, "正在请求数据...");
        }
        RequestParams params = new RequestParams();
        params.put("SceneId", scene.getId());
        params.put("pi", dataPageIndex);
        params.put("ps", PAGE_SIZE);
        HttpUtil.jsonRequestGet(this, URLRequest.DEVICES_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                UIUtil.dismissTipDialog(SceneActivity.this);
                ListResponse<DeviceResp> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<DeviceResp>>() {
                });
                if (listResponse.getRc() == 200) {
                    dataPageIndex++;
                    if (listResponse.getListData() != null && listResponse.getListData().size() >= PAGE_SIZE) {
                        hasMoreData = true;
                    } else {
                        hasMoreData = false;
                    }
                    sceneData.addAll(DeviceResp.convertToDeviceList(listResponse.getListData()));
                    dataAdapter.notifyDataSetChanged();
                }

                dataList.onLoadMoreComplete();
                if (dataSwipeLayout.isRefreshing()) {
                    dataSwipeLayout.setRefreshing(false);
                }
            }

            @Override
            public void onNetError(VolleyError error) {
                UIUtil.dismissTipDialog(SceneActivity.this);
                if (dataSwipeLayout.isRefreshing()) {
                    dataSwipeLayout.setRefreshing(false);
                }

            }
        });
    }


    private class DataListAdapter extends MyBaseAdapter<BaseDevice> {

        public DataListAdapter(List<BaseDevice> data, Context context) {
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
                convertView = LayoutInflater.from(SceneActivity.this).inflate(R.layout.scene_data_list_item, null);
                holder = new DataHolder();
                holder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
                holder.sensor1 = (TextView) convertView.findViewById(R.id.sensor1);
                holder.sensor2 = (TextView) convertView.findViewById(R.id.sensor2);
                holder.sensor3 = (TextView) convertView.findViewById(R.id.sensor3);
                convertView.setTag(holder);

            } else {
                holder = (DataHolder) convertView.getTag();

            }

            BaseDevice temp = data.get(position);

            List<BaseSensor> sensors = temp.getSensorTypes();
            holder.deviceName.setText(temp.getName());
            if ("offline".equals(temp.getDeviceStatus()) || sensors == null || sensors.size() == 0) {
                holder.sensor1.setText("状态：离线");
                convertView.setBackgroundColor(getResources().getColor(R.color.gray_bg));
                holder.sensor2.setVisibility(View.GONE);
                holder.sensor3.setVisibility(View.GONE);
                return convertView;
            }

            convertView.setBackgroundColor(getResources().getColor(R.color.white));

            if (sensors.size() == 1) {
                //浸水检测器
                holder.sensor1.setText(getSensorText(sensors.get(0)));
            } else if (sensors.size() == 2) {
                //温湿度检测器
            }

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
                holder.sensor1.setText(getSensorText(getSpecificSenser(sensors, BaseSensor.SENSOR_TEMPERATURE)));
                holder.sensor2.setText(getSensorText(getSpecificSenser(sensors, BaseSensor.SENSOR_HUMIDITY)));
                if (sensors.size() >= 3) {
                    holder.sensor3.setVisibility(View.VISIBLE);
                }
            }

            return convertView;
        }

        private String getSensorText(BaseSensor sensor) {
            if (sensor == null)
                return "";
            String sensorName = sensor.getSensorTypeName().replace("传感器", "");
            String appendix = sensor.getSensorUnit();
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
    private BaseSensor getSensorLeft(List<BaseSensor> sensors) {
        if (sensors == null)
            return null;
        for (BaseSensor temp : sensors) {
            if (!temp.getSensorType().equals(BaseSensor.SENSOR_TEMPERATURE) && !temp.getSensorType().equals(BaseSensor.SENSOR_HUMIDITY)) {
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
    private BaseSensor getSpecificSenser(List<BaseSensor> sensors, String sensorType) {
        if (sensors == null)
            return null;
        for (BaseSensor temp : sensors)
            if (temp.getSensorType().equals(sensorType))
                return temp;
        return null;
    }

}
