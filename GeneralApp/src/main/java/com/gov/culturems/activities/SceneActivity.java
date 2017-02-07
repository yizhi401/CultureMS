package com.gov.culturems.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
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
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.common.http.response.DeviceResp;
import com.gov.culturems.entities.AlertInfo;
import com.gov.culturems.entities.BaseDevice;
import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.entities.DCDevice;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.LogUtil;
import com.gov.culturems.utils.UIUtil;
import com.gov.culturems.views.LoadMoreListView;

import java.util.ArrayList;
import java.util.Collections;
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
    private boolean shouldShowDialog = true;

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
        if (VersionController.CURRENT_VERSION == VersionController.GONGWANGFU) {
            MenuItem itemEdit = menu.add(0, R.id.menu_edit, 0, "设置");
            itemEdit.setIcon(R.drawable.setting_icon);
            itemEdit.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            MenuItem itemMore = menu.add(0, R.id.menu_more, 0, "更多");
            itemMore.setIcon(R.drawable.menu_more);
            itemMore.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_edit) {
            jumpToRuleManageActivity();
        } else if (item.getItemId() == R.id.menu_more) {
            showMoreMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMoreMenu() {
        View popupMenuView = LayoutInflater.from(this).inflate(R.layout.device_data_popup_menu, null);
        final PopupWindow menuPopup =
                new PopupWindow(popupMenuView, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, true);
        menuPopup.setTouchable(true);
        menuPopup.setOutsideTouchable(true);
        menuPopup.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        menuPopup.showAsDropDown(findViewById(R.id.menu_more));
        menuPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
        Button ruleManageBtn = (Button) popupMenuView.findViewById(R.id.rule_manage);
        ruleManageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToRuleManageActivity();
                menuPopup.dismiss();
            }
        });
        ruleManageBtn.setVisibility(View.VISIBLE);
        Button goodsManageBtn = (Button) popupMenuView.findViewById(R.id.goods_manage);
        goodsManageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToGoodsManageActivity();
                menuPopup.dismiss();
            }
        });
    }

    private void jumpToGoodsManageActivity() {
        Intent i = new Intent(this, GoodsManageActivity.class);
        i.putExtra("scene", scene);
        startActivity(i);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

    }


    private void jumpToRuleManageActivity() {
        Intent intent = new Intent(SceneActivity.this, FanControlActivity.class);
        intent.putExtra("scene", scene);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

    }


    private void initViews() {

        dataSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.data_swipe_refresh);
        dataSwipeLayout.setEnabled(false);
        dataSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataPageIndex = 1;
                shouldShowDialog = true;
                sceneData.clear();
                getDevicesData();
                hasMoreData = false;
            }
        });

        dataList = (LoadMoreListView) findViewById(R.id.data_list);
        dataList.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.d("mInfo", "Loading More");
                if (hasMoreData) {
                    getDevicesData();
                } else {
                    if (shouldShowDialog) {
                        Toast.makeText(SceneActivity.this, "没有更多了", Toast.LENGTH_SHORT).show();
                        shouldShowDialog = false;
                    }
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
            return;
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
                    Collections.sort(sceneData);
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
            TextView alertInfo;
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
                holder.alertInfo = (TextView) convertView.findViewById(R.id.alert_info);
                convertView.setTag(holder);

            } else {
                holder = (DataHolder) convertView.getTag();

            }

            DCDevice temp = (DCDevice) data.get(position);

            List<BaseSensor> sensors = temp.getSensorTypes();
            holder.deviceName.setText(temp.getName());
            if (temp.isOffline() || sensors == null || sensors.size() == 0) {
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
                //仅有浸水传感器
//                holder.sensor1.setText();
                holder.sensor1.setText(Html.fromHtml(getSensorText(sensors.get(0))));
            } else if (sensors.size() >= 2) {
                //超过两个传感器的，都是包含了温湿度，让温度显示在第一个，湿度在第二个
                holder.sensor2.setVisibility(View.VISIBLE);
                holder.sensor1.setText(Html.fromHtml(getSensorText(getSpecificSenser(sensors, BaseSensor.SENSOR_TEMPERATURE))));
                holder.sensor2.setText(Html.fromHtml(getSensorText(getSpecificSenser(sensors, BaseSensor.SENSOR_HUMIDITY))));
                if (sensors.size() >= 3) {
                    holder.sensor3.setVisibility(View.VISIBLE);
                }
            }
            if (temp.getAlerts() == null || temp.getAlerts().size() == 0) {
                holder.alertInfo.setTextColor(getResources().getColor(R.color.text_green));
                holder.alertInfo.setText("无");
            } else {
                holder.alertInfo.setTextColor(getResources().getColor(R.color.main_red));
                holder.alertInfo.setText(getAlertInfoText(temp));
            }

            return convertView;
        }

        private String getAlertInfoText(DCDevice temp) {
            if (temp.getAlerts() == null || temp.getAlerts().size() == 0) {
                return "无";
            } else {
                StringBuilder sb = new StringBuilder();
                for (AlertInfo tt : temp.getAlerts()) {
                    sb.append(tt.getAlertTypeName()).append(", ");
                }
                sb.replace(sb.length() - 2, sb.length(), "");
                return sb.toString();
            }
        }

        private String getSensorText(BaseSensor sensor) {
            if (sensor == null)
                return "";
            String sensorName = sensor.getSensorTypeName().replace("传感器", "");
            String appendix = sensor.getSensorUnit();
            if (sensorName.contains("浸水")) {
                sensorName = "状态";
                if (sensor.getSensorValue().contains("1")) {
                    return getFormatedHtml(sensorName, "漏水", true);
                } else {
                    return getFormatedHtml(sensorName, "未漏水", false);
                }
            }
            return getFormatedHtml(sensorName, sensor.getSensorValue() + appendix, false);
        }

        private String getFormatedHtml(String name, String value, boolean isRed) {
            String HTMLPREFIX = "<font color=\"";
            String HTMLSUFIX = "</font>";
            String HTML_COLOR_RED = "#970303\">";
            String HTML_COLOR_BLACK = "black\">";
            String HTML_COLOR_GREEN = "#51bd48\">";

            if (isRed) {
                return HTMLPREFIX + HTML_COLOR_BLACK + name + ": " + HTMLSUFIX
                        + HTMLPREFIX + HTML_COLOR_RED + value + HTMLSUFIX;
            } else {

                return HTMLPREFIX + HTML_COLOR_BLACK + name + ": " + HTMLSUFIX
                        + HTMLPREFIX + HTML_COLOR_GREEN + value + HTMLSUFIX;
            }
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
