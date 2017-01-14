package com.gov.culturems.fragments;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.activities.DeviceDataActivity;
import com.gov.culturems.common.CommonConstant;
import com.gov.culturems.common.base.MyBaseAdapter;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.common.http.response.SensorResp;
import com.gov.culturems.entities.BaseDevice;
import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.entities.DeviceDataForChart;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.LogUtil;
import com.gov.culturems.utils.TimeUtil;
import com.gov.culturems.utils.UIUtil;
import com.gov.culturems.views.ChooseDateView;
import com.gov.culturems.views.CustomListView;
import com.gov.culturems.views.LoadMoreListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import hirondelle.date4j.DateTime;


/**
 * 显示数据详情表的Fragment
 * Created by peter on 10/29/15.
 */
public class TableFragment extends Fragment implements DeviceDataActivity.DeviceDataListener {

    private static final int PAGE_SIZE = 12;//每页显示条数,必须是4的倍数
    private DateTime currentDate;

    /**
     * 表格中四列数据的标题
     */
    private TextView textView1, textView2, textView3, textView5, textView4;

    private BaseDevice deviceInfo;

    private CustomListView dataList;
    private SensorAdapter adapter;
    private List<DeviceDataForChart> sensorData;
    private ChooseDateView chooseDateView;

    private int pageIndex = 1;

    public static TableFragment newInstance(BaseDevice deviceInfo) {
        TableFragment tableFragment = new TableFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("deviceInfo", deviceInfo);
        tableFragment.setArguments(bundle);
        return tableFragment;
    }

    public TableFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        deviceInfo = (BaseDevice) getArguments().getSerializable("deviceInfo");

        View rootView = inflater.inflate(R.layout.table_fragment, container, false);
        textView1 = (TextView) rootView.findViewById(R.id.text1);
        textView2 = (TextView) rootView.findViewById(R.id.text2);
        textView3 = (TextView) rootView.findViewById(R.id.text3);
        textView5 = (TextView) rootView.findViewById(R.id.text5);
        textView4 = (TextView) rootView.findViewById(R.id.text4);

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

        sensorData = new CopyOnWriteArrayList<>();
        adapter = new SensorAdapter(sensorData, getActivity());
        dataList.setAdapter(adapter);
        dataList.setDivider(new ColorDrawable(getResources().getColor(VersionController.getDrawable(VersionController.THEME_COLOR_LIGHT))));
        LinearLayout dataListTitle = (LinearLayout)rootView.findViewById(R.id.data_list_title);
        dataListTitle.setDividerDrawable(new ColorDrawable(getResources().getColor(VersionController.getDrawable(VersionController.THEME_COLOR_LIGHT))));

        getDeviceDatas();

        return rootView;
    }

    /**
     * 获得设备检测数据
     */
    private void getDeviceDatas() {
        RequestParams params = new RequestParams();
        params.put("SceneId", deviceInfo.getParentScene().getId());
        params.put("pi", pageIndex);
        params.put("ps", PAGE_SIZE);
//        下面的参数均非必传
        params.put("DeviceId", deviceInfo.getId());
        params.putWithoutFilter("BeginTime", getBeginTimeStr());
        params.putWithoutFilter("EndTime", getEndTimeStr());

        if (pageIndex <= 1) {
            //请求第一页的时候，给一个提示框，否则用户不知道请求在进行
            UIUtil.showTipDialog(getActivity(), CommonConstant.DIALOG_TYPE_WAITING, "正在请求数据");
        }
        HttpUtil.jsonRequestGet(getActivity(), URLRequest.DATAS_HT_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                //views not inited
                ListResponse<DeviceDataForChart> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<DeviceDataForChart>>() {
                });

                if (listResponse.getRc() == 200 && listResponse.getListData() != null) {
                    //如果获取数据大小小于每一页的数量
                    //意味着取到最后一页了
                    if (listResponse.getListData().size() < PAGE_SIZE) {
                        //如果当前页码是第一页，并且返回数据量为0，就说明一条数据没有
                        if (pageIndex <= 1 && listResponse.getListData().size() == 0) {
                            if (isVisible())
                                Toast.makeText(getActivity(), "暂无数据", Toast.LENGTH_SHORT).show();
                        } else {
                            //当前页码大于1，说明用户翻页了，这时候提示没有更多
                            if (isVisible() && pageIndex > 1)
                                Toast.makeText(getActivity(), "没有更多了", Toast.LENGTH_SHORT).show();
                        }
                    }
                    //页码+1，然后刷新列表
                    if (listResponse.getListData().size() != 0) {
                        pageIndex++;
                        addDataAndResort(listResponse.getListData());
                    }
                    adapter.notifyDataSetChanged();
                    dataList.onLoadMoreComplete();
                    UIUtil.dismissTipDialog(getActivity());
                }
            }

            @Override
            public void onNetError(VolleyError error) {
                dataList.onLoadMoreComplete();
            }
        });
    }

    private void addDataAndResort(ArrayList<DeviceDataForChart> listData) {
        if (listData == null || listData.size() == 0) {
            return;
        }
        Iterator<DeviceDataForChart> iterator = listData.iterator();
        DeviceDataForChart newData;
        //首先去除可能重复的数据
        while (iterator.hasNext()) {
            newData = iterator.next();
            for (DeviceDataForChart oldData : sensorData) {
                if (newData.InsertTime.equals(oldData.InsertTime)) {
                    iterator.remove();
                    break;
                }
            }
        }
        Collections.sort(listData, new Comparator<DeviceDataForChart>() {
            @Override
            public int compare(DeviceDataForChart lhs, DeviceDataForChart rhs) {
                return -TimeUtil.compareTwoUnformattedTimeStr(lhs.InsertTime, rhs.InsertTime);
            }
        });
        sensorData.addAll(listData);
    }


    private class SensorAdapter extends MyBaseAdapter<DeviceDataForChart> {

        public SensorAdapter(List<DeviceDataForChart> data, Context context) {
            super(data, context);
        }

        class Holder {
            TextView text1;
            TextView text2;
            TextView text3;
            //            TextView text5;
            TextView text4;
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
//                holder.text5 = (TextView) convertView.findViewById(R.id.text5);
                holder.text4 = (TextView) convertView.findViewById(R.id.text4);
                holder.text4.setBackgroundResource(VersionController.getDrawable(VersionController.));
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            DeviceDataForChart temp = data.get(position);
            holder.text1.setText(temp.SensorId + "号");
            int firstSpace = temp.InsertTime.indexOf(" ");
            holder.text4.setText(temp.InsertTime.substring(firstSpace + 1, temp.InsertTime.length()));

            holder.text3.setVisibility(View.GONE);
            String alertInfo = "";
            holder.text3.setVisibility(View.VISIBLE);
            holder.text3.setText("");
            //set column1
            holder.text2.setText(temp.SensorValueT + getResources().getString(R.string.temperature_unit));
            holder.text2.setTextColor(getActivity().getResources().getColor(R.color.black));
            holder.text3.setText(temp.SensorValueH + getResources().getString(R.string.humidity_unit));
//            holder.text5.setTextColor(getActivity().getResources().getColor(R.color.black));
//            if (DeviceDataForChart.ALERT_STATUS_NORMAL.equals(temp.AlertStatusH)) {
//                if (DeviceDataForChart.ALERT_STATUS_NORMAL.equals(temp.AlertStatusT)) {
//                    alertInfo = "正常";
//                } else {
//                    holder.text5.setTextColor(getActivity().getResources().getColor(R.color.red));
//                    alertInfo = temp.AlertStatusT;
//                }
//            } else {
//                holder.text5.setTextColor(getActivity().getResources().getColor(R.color.red));
//                alertInfo = temp.AlertStatusH;
//                if (DeviceDataForChart.ALERT_STATUS_NORMAL.equals(temp.AlertStatusT)) {
//                } else {
//                    alertInfo = alertInfo + "\n" + temp.AlertStatusT;
//                }
//            }
//            holder.text5.setText(alertInfo);
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
        String nowStr = currentDate.format("YYYY-MM-DD");
        if (chooseDateView != null) {
            return nowStr + "%20" + chooseDateView.getSelectedBeginTime();
        } else {
            return nowStr + "%2000:00:00";
        }
    }

    /**
     * 如果当前是一天，返回一天的开始时间
     * 如果当前是一周，返回本周的开始时间
     *
     * @return
     */
    private String getEndTimeStr() {
        String nowStr = currentDate.format("YYYY-MM-DD");
        if (chooseDateView != null) {
            return nowStr + "%20" + chooseDateView.getSelectedEndTime();
        } else {
            return nowStr + "%2023:59:59";
        }
    }

    @Override
    public void onDataChanged(DateTime changedDate) {
        currentDate = changedDate;
        pageIndex = 1;
        //如果正在加载更多，取消加载，否则会在小米手机上导致crash
        sensorData.clear();
        dataList.onLoadMoreComplete();
        adapter.notifyDataSetChanged();
        getDeviceDatas();
    }

    public void setChooseDateView(ChooseDateView chooseDateView) {
        this.chooseDateView = chooseDateView;
    }

    public void setCurrentDate(DateTime currentDate) {
        this.currentDate = currentDate;
    }
}
