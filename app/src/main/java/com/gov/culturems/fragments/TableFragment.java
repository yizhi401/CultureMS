package com.gov.culturems.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.activities.DeviceDataActivity;
import com.gov.culturems.common.base.MyBaseAdapter;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLConstant;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.DeviceData;
import com.gov.culturems.entities.DeviceInfo;
import com.gov.culturems.entities.Sensor;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.LogUtil;
import com.gov.culturems.utils.TimeUtil;
import com.gov.culturems.views.CustomListView;
import com.gov.culturems.views.LoadMoreListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hirondelle.date4j.DateTime;


/**
 * 显示数据详情表的Fragment
 * Created by peter on 10/29/15.
 */
public class TableFragment extends Fragment implements DeviceDataActivity.DeviceDataListener {

    private static final int PAGE_SIZE = 20;//每页显示条数
    private int currentTab;
    private DateTime currentDate;
    private String columnName1, columnName2, columnName3; //分别对应表中第一列第二列第三列

    /**
     * 表格中四列数据的标题
     */
    private TextView textView1, textView2, textView3, textView4, textView5;

    private DeviceInfo deviceInfo;

    private CustomListView dataList;
    private SensorAdapter adapter;
    private List<DeviceData> sensorData;

    //因为返回正常状态的字符串服务器总是变来变去，因此放到一个list里面以防万一
    private List<String> normalStatusList;

    private int pageIndex = 1;


    public static TableFragment newInstance(DeviceInfo deviceInfo) {
        TableFragment tableFragment = new TableFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("deviceInfo", deviceInfo);
        tableFragment.setArguments(bundle);
        return tableFragment;
    }

    public TableFragment() {
        initNormalList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        deviceInfo = (DeviceInfo) getArguments().getSerializable("deviceInfo");

        View rootView = inflater.inflate(R.layout.table_fragment, container, false);
        textView1 = (TextView) rootView.findViewById(R.id.text1);
        textView2 = (TextView) rootView.findViewById(R.id.text2);
        textView3 = (TextView) rootView.findViewById(R.id.text3);
        textView4 = (TextView) rootView.findViewById(R.id.text4);
        textView5 = (TextView) rootView.findViewById(R.id.text5);

        dataList = (CustomListView) rootView.findViewById(R.id.data_list);
        dataList.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                LogUtil.e("trying to load more");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getDeviceDatas();
                    }
                }, 1000);
            }
        });

        sensorData = new ArrayList<>();
        adapter = new SensorAdapter(sensorData, getActivity());
        dataList.setAdapter(adapter);

        initDeviceInfo();
        getDeviceDatas();

        return rootView;
    }

    /**
     * 决定device是有几个sensor,它们的名字是什么
     */
    private void initDeviceInfo() {
        Map<String, String> sensorNameMap;
        columnName2 = null;
        textView3.setVisibility(View.GONE);
        textView4.setVisibility(View.GONE);
        columnName3 = null;
        sensorNameMap = new HashMap<>();
        if (deviceInfo.getSensorTypes().size() >= 1) {
            //只有一个感受器的时候，只有净水感受器一种情况，此时不要显示图，只显示表格

            columnName1 = deviceInfo.getSensorTypes().get(0).getSensorType();
            sensorNameMap.put(columnName1, deviceInfo.getSensorTypes().get(0).getSensorUnitName());
            textView2.setText(sensorNameMap.get(columnName1));
            if (deviceInfo.getSensorTypes().size() >= 2) {

                columnName2 = deviceInfo.getSensorTypes().get(1).getSensorType();
                sensorNameMap.put(columnName2, deviceInfo.getSensorTypes().get(1).getSensorUnitName());
                textView3.setText(sensorNameMap.get(columnName2));
                textView3.setVisibility(View.VISIBLE);

                if (deviceInfo.getSensorTypes().size() == 3) {
                    columnName3 = deviceInfo.getSensorTypes().get(2).getSensorType();
                    sensorNameMap.put(columnName3, deviceInfo.getSensorTypes().get(2).getSensorUnitName());
                    textView4.setText(sensorNameMap.get(columnName3));
                    textView4.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * TODO 此处存在一个bug， 就是当数据返回的时候，用户恰好切换了ListView，会导致奔溃
     * 获得设备检测数据
     */
    private void getDeviceDatas() {
        RequestParams params = new RequestParams();
        params.put("SceneId", deviceInfo.getSceneId());
        params.put("pi", pageIndex);
        params.put("ps", PAGE_SIZE);
//        下面的参数均非必传
        params.put("DeviceId", deviceInfo.getDeviceId());
        params.putWithoutFilter("BeginTime", getBeginTimeStr());
        params.putWithoutFilter("EndTime", getEndTimeStr());

        HttpUtil.jsonRequestGet(getActivity(), URLConstant.DATAS_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                //views not inited
                ListResponse<Sensor> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<Sensor>>() {
                });

                if (listResponse.getRc() == 200 && listResponse.getListData() != null) {

                    if (listResponse.getListData().size() != 0) {
                        pageIndex++;
                        sensorData.addAll(handleSensorData(listResponse.getListData()));
                    }
                    adapter.notifyDataSetChanged();
                    dataList.onLoadMoreComplete();
                    if (listResponse.getListData().size() < PAGE_SIZE) {
                        if (adapter.getData().size() == 0) {
                            if (isVisible())
                                Toast.makeText(getActivity(), "暂无数据", Toast.LENGTH_SHORT).show();
                        } else {
                            if (isVisible())
                                Toast.makeText(getActivity(), "没有更多了", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }

            @Override
            public void onNetError(VolleyError error) {
                dataList.onLoadMoreComplete();
            }
        });
    }

    private List<DeviceData> handleSensorData(ArrayList<Sensor> listData) {
        List<DeviceData> deviceDataList = new ArrayList<>();
        boolean addedFlag;
        try {
            for (Sensor temp : listData) {
                addedFlag = false;
                for (DeviceData dataTemp : deviceDataList) {
                    if (temp.getInsertTime().equals(dataTemp.insertTime)) {
                        //already added to tableValueList
                        dataTemp.sensorMap.put(temp.getSensorType(), temp);
                        addedFlag = true;
                        break;
                    }
                }
                if (!addedFlag) {
                    //hasn't added to the table
                    DeviceData deviceData = new DeviceData();
                    deviceData.insertTime = temp.getInsertTime();
                    deviceData.sensorMap = new HashMap<>();
                    deviceData.sensorMap.put(temp.getSensorType(), temp);
                    deviceDataList.add(deviceData);
                }
            }
        } catch (Exception e) {
            //有的sensor返回的InsertTime为空
            Log.i("mInfo", "insert time is empty");
        }

        Collections.sort(deviceDataList, new Comparator<DeviceData>() {
            @Override
            public int compare(DeviceData lhs, DeviceData rhs) {
                //倒序排列
                return -TimeUtil.compareTwoUnformattedTimeStr(lhs.insertTime, rhs.insertTime);
            }
        });

        return deviceDataList;
    }

    private void initNormalList() {
        normalStatusList = new ArrayList<>();
        normalStatusList.add("正常");
        normalStatusList.add("无警报");
        normalStatusList.add("无报警");
    }

    private class SensorAdapter extends MyBaseAdapter<DeviceData> {

        public SensorAdapter(List<DeviceData> data, Context context) {
            super(data, context);
        }

        class Holder {
            TextView text1;
            TextView text2;
            TextView text4;
            TextView text3;
            TextView text5;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.device_detail_item, null);
                holder = new Holder();
                holder.text1 = (TextView) convertView.findViewById(R.id.text1);
                holder.text2 = (TextView) convertView.findViewById(R.id.text2);
                holder.text3 = (TextView) convertView.findViewById(R.id.text3);
                holder.text4 = (TextView) convertView.findViewById(R.id.text4);
                holder.text5 = (TextView) convertView.findViewById(R.id.text5);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            DeviceData temp = data.get(position);
            holder.text1.setText(temp.insertTime);

            holder.text3.setVisibility(View.GONE);
            holder.text4.setVisibility(View.GONE);
            String alertInfo = "";
            boolean hasAlert = false;
            switch (deviceInfo.getSensorTypes().size()) {
                case 1:
                    break;
                case 2:
                    holder.text3.setVisibility(View.VISIBLE);
                    holder.text3.setText("");
                    break;
                case 3:
                    holder.text3.setVisibility(View.VISIBLE);
                    holder.text3.setText("");
                    holder.text4.setVisibility(View.VISIBLE);
                    holder.text4.setText("");
                    break;
                default:
                    break;
            }
            if (deviceInfo.getSensorTypes().size() >= 1 && temp.sensorMap.get(columnName1) != null) {
                //set column1
                holder.text2.setText(temp.sensorMap.get(columnName1).getSensorValue());
                if (TextUtils.isEmpty(temp.sensorMap.get(columnName1).getAlertStatusName()) || normalStatusList.contains(temp.sensorMap.get(columnName1).getAlertStatusName())) {
                    holder.text2.setTextColor(getActivity().getResources().getColor(R.color.black));
                } else {
                    holder.text2.setTextColor(getActivity().getResources().getColor(R.color.red));
                    alertInfo = alertInfo + temp.sensorMap.get(columnName1).getAlertStatusName();
                    hasAlert = true;
                }
                //set column2
                if (deviceInfo.getSensorTypes().size() >= 2 && temp.sensorMap.get(columnName2) != null) {
                    holder.text3.setText(temp.sensorMap.get(columnName2).getSensorValue());
                    if (TextUtils.isEmpty(temp.sensorMap.get(columnName2).getAlertStatusName()) || normalStatusList.contains(temp.sensorMap.get(columnName2).getAlertStatusName())) {
                        holder.text3.setTextColor(getActivity().getResources().getColor(R.color.black));
                    } else {
                        holder.text3.setTextColor(getActivity().getResources().getColor(R.color.red));
                        alertInfo = alertInfo + "\n" + temp.sensorMap.get(columnName2).getAlertStatusName();
                        hasAlert = true;
                    }

                    //set column3
                    if (deviceInfo.getSensorTypes().size() >= 3 && temp.sensorMap.get(columnName3) != null) {
                        holder.text4.setText(temp.sensorMap.get(columnName3).getSensorValue());
                        if (TextUtils.isEmpty(temp.sensorMap.get(columnName3).getAlertStatusName()) || normalStatusList.contains(temp.sensorMap.get(columnName3).getAlertStatusName())) {
                            holder.text4.setTextColor(getActivity().getResources().getColor(R.color.black));
                        } else {
                            alertInfo = alertInfo + "\n" + temp.sensorMap.get(columnName3).getAlertStatusName();
                            holder.text4.setTextColor(getActivity().getResources().getColor(R.color.red));
                            hasAlert = true;
                        }
                    }
                }
            }

            if (hasAlert) {
                holder.text5.setTextColor(getActivity().getResources().getColor(R.color.red));
                alertInfo = "报警";
            } else {
                holder.text5.setTextColor(getActivity().getResources().getColor(R.color.black));
                alertInfo = "正常";
            }
            holder.text5.setText(alertInfo);

            return convertView;
        }
    }


    /**
     * 如果当前是一天，返回一天的开始时间
     * 如果当前是一周，返回本周的开始时间
     *
     * @return
     */
    private String getBeginTimeStr() {
        switch (currentTab) {
            case DeviceDataActivity.TAB_DAY:
                String nowStr = currentDate.format("YYYY-MM-DD");
                return nowStr + "%2000:00:00";
            case DeviceDataActivity.TAB_WEEK:
                DateTime firstDayOfWeek = currentDate.minus(0, 0, currentDate.getWeekDay() - 1, 0, 0, 0, 0, null);
                return firstDayOfWeek.format("YYYY-MM-DD") + "%2000:00:00";
            default:
                return "";
        }
    }

    /**
     * 如果当前是一天，返回一天的开始时间
     * 如果当前是一周，返回本周的开始时间
     *
     * @return
     */
    private String getEndTimeStr() {
        switch (currentTab) {
            case DeviceDataActivity.TAB_DAY:
                String nowStr = currentDate.format("YYYY-MM-DD");
                return nowStr + "%2023:59:59";
            case DeviceDataActivity.TAB_WEEK:
                DateTime firstDayOfWeek = currentDate.plus(0, 0, 7 - currentDate.getWeekDay(), 0, 0, 0, 0, null);
                return firstDayOfWeek.format("YYYY-MM-DD") + "%2023:59:59";
            default:
                return "";
        }
    }

    @Override
    public void onDataChanged(DateTime changedDate) {
        currentDate = changedDate;
        if (currentTab == DeviceDataActivity.TAB_DAY) {
            pageIndex = 1;
            sensorData.clear();
        } else {
            pageIndex = 1;
            sensorData.clear();
        }
        getDeviceDatas();
    }

    @Override
    public void onTabChanged(int tabType) {
        currentTab = tabType;
        if (tabType == DeviceDataActivity.TAB_DAY) {
            //clear current data
            pageIndex = 1;
            if (sensorData != null) {
                sensorData.clear();
            }
        } else {
            //clear current data
            pageIndex = 1;
            if (sensorData != null) {
                sensorData.clear();
            }
        }
        getDeviceDatas();
    }

    public void setCurrentDate(DateTime currentDate) {
        this.currentDate = currentDate;
    }
}
