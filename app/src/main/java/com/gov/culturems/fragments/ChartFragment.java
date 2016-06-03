package com.gov.culturems.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.MyApplication;
import com.gov.culturems.R;
import com.gov.culturems.activities.DeviceDataActivity;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLConstant;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.DeviceData;
import com.gov.culturems.entities.DeviceInfo;
import com.gov.culturems.entities.Sensor;
import com.gov.culturems.utils.FloatValueFormatter;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.LogUtil;
import com.gov.culturems.utils.MathUtil;
import com.gov.culturems.utils.TimeUtil;
import com.gov.culturems.utils.UIUtil;
import com.gov.culturems.views.EasyScrollableView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hirondelle.date4j.DateTime;

/**
 * 显示数据详情图的Fragment
 * Created by peter on 10/29/15.
 */
public class ChartFragment extends Fragment implements DeviceDataActivity.DeviceDataListener {

    private static final int COLOR_BLUE = 0xff39a6f9;
    private static final int COLOR_YELLOW = 0xffffa628;
    private static final int COLOR_GREEN = 0xff81cc49;
    private static final int COLOR_GRAY = 0xffe8e8e8;

    private static final int PIE_CHART_SIZE_LARGE = 1;
    private static final int PIE_CHART_SIZE_MIDDLE = 2;
    private static final int PIE_CHART_SIZE_SMALL = 3;

    private Map<String, String> sensorNameMap;//records the sensor unit name
    private Map<String, String> sensorUnitMap;//records the sensor unit

    private int currentTab;

    private PieChart pieChart1, pieChart2, pieChart3;

    private DeviceInfo deviceInfo;
    private TextView valueText1, valueText2, valueText3;
    private LineChart mChart1, mChart2, mChart3;
    private DateTime currentDate;
    private String columnName1, columnName2, columnName3; //分别对应表中第一列第二列第三列

    private LinkedList<TableValues> tableValueList;

    //记录除温度，湿度以外第三个感受器的最大值，用于显示饼图
    private float maxSensorValue;

    private EasyScrollableView scrollableView;

    class TableValues {
        String repHour;
        List<DataResponse> dataList;
    }

    public static ChartFragment newInstance(DeviceInfo deviceInfo) {
        ChartFragment chartFragment = new ChartFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("deviceInfo", deviceInfo);
        chartFragment.setArguments(bundle);
        return chartFragment;
    }

    public ChartFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        deviceInfo = (DeviceInfo) getArguments().getSerializable("deviceInfo");
        View rootView = inflater.inflate(R.layout.chart_fragment, container, false);

        scrollableView = (EasyScrollableView) rootView.findViewById(R.id.easyscrollview);

        valueText1 = (TextView) rootView.findViewById(R.id.value_text1);
        valueText2 = (TextView) rootView.findViewById(R.id.value_text2);
        valueText3 = (TextView) rootView.findViewById(R.id.value_text3);

        mChart1 = (LineChart) rootView.findViewById(R.id.line_chart_1);
        mChart2 = (LineChart) rootView.findViewById(R.id.line_chart_2);
        mChart3 = (LineChart) rootView.findViewById(R.id.line_chart_3);

        pieChart1 = (PieChart) rootView.findViewById(R.id.pie_chart1);
        pieChart2 = (PieChart) rootView.findViewById(R.id.pie_chart2);
        pieChart3 = (PieChart) rootView.findViewById(R.id.pie_chart3);

        initDeviceInfo();
        getDayData();
        getDeviceDatas();
        return rootView;
    }

    /**
     * 获得设备检测数据
     */
    private void getDeviceDatas() {
        RequestParams params = new RequestParams();
        params.put("SceneId", deviceInfo.getSceneId());
        params.put("pi", 1);
        params.put("ps", 20);
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
                        handleSensorData(listResponse.getListData());
                    }
                }
            }

            @Override
            public void onNetError(VolleyError error) {
            }
        });
    }

    private void handleSensorData(ArrayList<Sensor> listData) {
        List<DeviceData> deviceDataList = new ArrayList<>();
        boolean addedFlag;
        try {
            for (Sensor temp : listData) {
                try {
                    if (!Sensor.SENSOR_HUMIDITY.equals(temp.getSensorType()) && !Sensor.SENSOR_TEMPERATURE.equals(temp.getSensorType())) {
                        if (Float.valueOf(temp.getSensorValue()) > maxSensorValue) {
                            maxSensorValue = Float.valueOf(temp.getSensorValue());
                        }
                    }
                } catch (Exception e) {
                    LogUtil.e("number format exception in the sensor value");
                }
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

        DeviceData recentData = deviceDataList.get(0);
        if (deviceInfo.getSensorTypes().size() >= 2) {
            setPieData(Float.valueOf(recentData.sensorMap.get(Sensor.SENSOR_HUMIDITY).getSensorValue()), Sensor.SENSOR_HUMIDITY, pieChart1);
            setPieData(Float.valueOf(recentData.sensorMap.get(Sensor.SENSOR_TEMPERATURE).getSensorValue()), Sensor.SENSOR_TEMPERATURE, pieChart2);
            if (deviceInfo.getSensorTypes().size() >= 3) {
                setPieData(Float.valueOf(recentData.sensorMap.get(getTheThirdSensor()).getSensorValue()), getTheThirdSensor(), pieChart3);
            }
        }
    }

    /**
     * get the sensor that is not humidity or temperature
     *
     * @return
     */
    private String getTheThirdSensor() {
        if (!Sensor.SENSOR_HUMIDITY.equals(columnName3) && !Sensor.SENSOR_TEMPERATURE.equals(columnName3))
            return columnName3;
        else if (!Sensor.SENSOR_HUMIDITY.equals(columnName2) && !Sensor.SENSOR_TEMPERATURE.equals(columnName2))
            return columnName2;
        else if (!Sensor.SENSOR_HUMIDITY.equals(columnName1) && !Sensor.SENSOR_TEMPERATURE.equals(columnName1))
            return columnName1;
        return "";
    }


    /**
     * 决定device是有几个sensor,它们的名字是什么
     */
    private void initDeviceInfo() {

        columnName2 = null;
        columnName3 = null;
        mChart1.setVisibility(View.GONE);
        mChart2.setVisibility(View.GONE);
        mChart3.setVisibility(View.GONE);
        pieChart1.setVisibility(View.GONE);
        pieChart2.setVisibility(View.GONE);
        pieChart3.setVisibility(View.GONE);
        sensorNameMap = new HashMap<>();
        sensorUnitMap = new HashMap<>();
        if (deviceInfo.getSensorTypes().size() >= 1) {
            //只有一个感受器的时候，只有净水感受器一种情况，此时不要显示图，只显示表格

            columnName1 = deviceInfo.getSensorTypes().get(0).getSensorType();
            sensorNameMap.put(columnName1, deviceInfo.getSensorTypes().get(0).getSensorUnitName());
            sensorUnitMap.put(columnName1, deviceInfo.getSensorTypes().get(0).getSensorUnit());
            if (deviceInfo.getSensorTypes().size() >= 2) {
                mChart1.setVisibility(View.VISIBLE);
                mChart2.setVisibility(View.VISIBLE);
                pieChart1.setVisibility(View.VISIBLE);
                pieChart1.setLayoutParams(getPieChartLayoutParam(PIE_CHART_SIZE_MIDDLE));
                pieChart2.setVisibility(View.VISIBLE);
                pieChart2.setLayoutParams(getPieChartLayoutParam(PIE_CHART_SIZE_MIDDLE));

                columnName2 = deviceInfo.getSensorTypes().get(1).getSensorType();
                sensorNameMap.put(columnName2, deviceInfo.getSensorTypes().get(1).getSensorUnitName());
                sensorUnitMap.put(columnName2, deviceInfo.getSensorTypes().get(1).getSensorUnit());

                if (deviceInfo.getSensorTypes().size() == 3) {
                    mChart3.setVisibility(View.VISIBLE);

                    pieChart3.setVisibility(View.VISIBLE);
                    pieChart1.setLayoutParams(getPieChartLayoutParam(PIE_CHART_SIZE_SMALL));
                    pieChart2.setLayoutParams(getPieChartLayoutParam(PIE_CHART_SIZE_LARGE));
                    pieChart3.setLayoutParams(getPieChartLayoutParam(PIE_CHART_SIZE_SMALL));

                    columnName3 = deviceInfo.getSensorTypes().get(2).getSensorType();
                    sensorNameMap.put(columnName3, deviceInfo.getSensorTypes().get(2).getSensorUnitName());
                    sensorUnitMap.put(columnName3, deviceInfo.getSensorTypes().get(2).getSensorUnit());
                }
            }
        }
        initChart(mChart1, columnName1);
        initChart(mChart2, columnName2);
        initChart(mChart3, columnName3);

        initPieChart(pieChart1, Sensor.SENSOR_HUMIDITY);
        initPieChart(pieChart2, Sensor.SENSOR_TEMPERATURE);
        initPieChart(pieChart3, getTheThirdSensor());
    }


    class DataResponse {
        String DeviceId;
        String SensorType;
        String SensorTypeName;
        String RepHour;
        String SceneId;
        String SensorValue;
    }

    private void getWeekData() {
        RequestParams params = new RequestParams();
        params.put("SceneId", deviceInfo.getSceneId());
        params.put("DeviceId", deviceInfo.getDeviceId());
        params.putWithoutFilter("BeginTime", getBeginTimeStr());
        params.putWithoutFilter("EndTime", getEndTimeStr());
        HttpUtil.jsonRequestGet(getActivity(), URLConstant.WEEK_DATAS_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                ListResponse<DataResponse> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<DataResponse>>() {
                });
                if (listResponse.getRc() == 200 && listResponse.getListData() != null) {
                    handleResponseDataAndDraw(listResponse.getListData());
                }
            }

            @Override
            public void onNetError(VolleyError error) {

            }
        });
    }


    private void getDayData() {
        RequestParams params = new RequestParams();
        params.put("SceneId", deviceInfo.getSceneId());
        params.put("DeviceId", deviceInfo.getDeviceId());
        params.put("DataDate", currentDate.format("YYYY-MM-DD"));
        HttpUtil.jsonRequestGet(getActivity(), URLConstant.DAY_DATAS_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                ListResponse<DataResponse> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<DataResponse>>() {
                });
                if (listResponse.getRc() == 200 && listResponse.getListData() != null) {
                    handleResponseDataAndDraw(listResponse.getListData());
                }
            }

            @Override
            public void onNetError(VolleyError error) {

            }
        });
    }

    private void handleResponseDataAndDraw(ArrayList<DataResponse> listData) {
        //把所有的DataResponse按照时间分组、排序
        tableValueList = new LinkedList<>();
        boolean addedFlag;
        for (DataResponse temp : listData) {
            addedFlag = false;
            for (TableValues tableTemp : tableValueList) {
                if (temp.RepHour.equals(tableTemp.repHour)) {
                    //already added to tableValueList
                    tableTemp.dataList.add(temp);
                    addedFlag = true;
                    break;
                }
            }
            if (!addedFlag) {
                //hasn't added to the table
                TableValues tableValues = new TableValues();
                tableValues.repHour = temp.RepHour;
                tableValues.dataList = new ArrayList<>();
                tableValues.dataList.add(temp);
                tableValueList.add(tableValues);
            }
        }

        //按照时间从小到大排序
        Collections.sort(tableValueList, new Comparator<TableValues>() {
            @Override
            public int compare(TableValues lhs, TableValues rhs) {
                try {
                    if (currentTab == DeviceDataActivity.TAB_DAY) {
                        return Integer.valueOf(lhs.repHour) - Integer.valueOf(rhs.repHour);
                    } else {
                        return lhs.repHour.compareTo(rhs.repHour);
                    }
                } catch (Exception e) {
                    Log.e("mInfo", "parse formate exception");
                    return 0;
                }
            }
        });
        setData();
    }

    private void initPieChart(PieChart mChart, String columnName) {
        mChart.setUsePercentValues(true);
        mChart.setDescription("");

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setCenterTextTypeface(Typeface.createFromAsset(MyApplication.getInstance().getApplicationContext().getAssets(), "OpenSans-Regular.ttf"));

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);

        mChart.setTransparentCircleColor(Color.GRAY);

        mChart.setHoleRadius(90f);
        mChart.setTransparentCircleRadius(90f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(-90);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(false);

        mChart.setCenterTextColor(0xFF92BF26);
        mChart.setCenterTextSize(12);
        mChart.getLegend().setEnabled(false);

    }

    /**
     * 我真的不想这么写代码啊。。。。。
     * 原谅我吧....
     * <p/>
     * 规定，temperature永远在中间
     * humidity永远在左侧
     *
     * @param value
     * @param columnName
     * @param mChart
     */
    private void setPieData(float value, String columnName, PieChart mChart) {
        ArrayList<Integer> colors = new ArrayList<>();
        ArrayList<Entry> yVals1 = new ArrayList<>();
        if (columnName.toLowerCase().equals(Sensor.SENSOR_TEMPERATURE.toLowerCase())) {
            //Temperature
            yVals1.add(new Entry(100, 1));
//            yVals1.add(new Entry(0, 2));
            colors.add(COLOR_YELLOW);
            mChart.setRotationAngle(-90);
            mChart.setCenterTextColor(COLOR_YELLOW);
            mChart.setCenterText(formatValue(value) + "℃\n温度");

        } else if (columnName.toLowerCase().equals(Sensor.SENSOR_HUMIDITY.toLowerCase())) {
            //Humidity
            yVals1.add(new Entry(100, 1));
//            yVals1.add(new Entry(0, 2));
            mChart.setRotationAngle(-90);
            mChart.setCenterText(formatValue(value) + "%\n湿度");
            mChart.setCenterTextColor(COLOR_BLUE);
            colors.add(COLOR_BLUE);
        } else {
            //TODO
            //Other
            yVals1.add(new Entry(100, 1));
//            yVals1.add(new Entry(0, 2));
            mChart.setRotationAngle(-90);
            mChart.setCenterText(formatValue(value) + "\n" + sensorNameMap.get(columnName));
            mChart.setCenterTextColor(COLOR_GREEN);
            colors.add(COLOR_GREEN);
        }
//        colors.add(COLOR_GRAY);

        ArrayList<String> xVals = new ArrayList<>();

        for (int i = 0; i < 2; i++)
            xVals.add("");

        PieDataSet dataSet = new PieDataSet(yVals1, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        PieData data = new PieData(xVals, dataSet);
        mChart.setData(data);

        mChart.highlightValues(null);

        mChart.invalidate();

        mChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
    }

    private int getMaxValue() {

        return 0;
    }


    private void initChart(LineChart mChart, String chartTag) {
        // no description text
        mChart.setDescriptionColor(Color.DKGRAY);


//        setLineChartData(lineChart);
        // no description text


        mChart.setOnChartValueSelectedListener(getOnChartValueSelected(chartTag));

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("");
        mChart.setNoDataText("暂无数据");
        mChart.setTag(chartTag);

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
//        mChart1.setBackgroundColor(0xFFF7F7F7);
        mChart.setBackgroundColor(Color.WHITE);

        // add data
//        setData(20, 30);

        mChart.animateX(2500);
        mChart.setDrawBorders(true);
        mChart.setBorderColor(0xffe3e5da);

        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tf);
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTypeface(tf);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setSpaceBetweenLabels(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGridColor(0xffe3e5da);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.resetAxisMaxValue();
        leftAxis.resetAxisMinValue();
        leftAxis.setStartAtZero(false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
//        leftAxis.setGridColor(0xffe3e5da);
        leftAxis.setValueFormatter(new FloatValueFormatter());

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    private OnChartValueSelectedListener getOnChartValueSelected(final String chartTag) {
        return new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if (chartTag.equals(columnName1)) {
                    if (valueText1 != null) {
                        valueText1.setVisibility(View.VISIBLE);
                        valueText1.setText(sensorNameMap.get(columnName1) + ":" + e.getVal() + sensorUnitMap.get(columnName1));
                    }
                } else if (chartTag.equals(columnName2)) {
                    if (valueText2 != null) {
                        valueText2.setVisibility(View.VISIBLE);
                        valueText2.setText(sensorNameMap.get(columnName2) + ":" + e.getVal() + sensorUnitMap.get(columnName2));
                    }
                } else if (chartTag.equals(columnName3)) {
                    if (valueText3 != null) {
                        valueText3.setVisibility(View.VISIBLE);
                        valueText3.setText(sensorNameMap.get(columnName3) + ":" + e.getVal() + sensorUnitMap.get(columnName3));
                    }
                } else {
                    valueText1.setVisibility(View.GONE);
                    valueText2.setVisibility(View.GONE);
                    valueText3.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected() {
                if (chartTag.equals(columnName1)) {
                    if (valueText1 != null) {
                        valueText1.setVisibility(View.GONE);
                    }
                } else if (chartTag.equals(columnName2)) {
                    if (valueText2 != null) {
                        valueText2.setVisibility(View.GONE);
                    }
                } else if (chartTag.equals(columnName3)) {
                    if (valueText3 != null) {
                        valueText3.setVisibility(View.GONE);
                    }
                } else {
                    valueText1.setVisibility(View.GONE);
                    valueText2.setVisibility(View.GONE);
                    valueText3.setVisibility(View.GONE);
                }
            }
        };
    }


    private void setData() {

        if (tableValueList == null || tableValueList.size() == 0) {
            mChart1.clear();
            mChart1.invalidate();
            mChart2.clear();
            mChart3.clear();
            return;
        }
        valueText1.setVisibility(View.GONE);
        valueText3.setVisibility(View.GONE);
        valueText2.setVisibility(View.GONE);

        if (currentTab == DeviceDataActivity.TAB_DAY) {
            mChart1.getXAxis().setLabelsToSkip(1);
            mChart2.getXAxis().setLabelsToSkip(1);
            mChart3.getXAxis().setLabelsToSkip(1);
        } else {
            mChart1.getXAxis().setLabelsToSkip(6);
            mChart2.getXAxis().setLabelsToSkip(6);
            mChart3.getXAxis().setLabelsToSkip(6);
        }

//        ArrayList<String> xVals = getXVals();

        ArrayList<Entry> yVals1 = new ArrayList<>();
        ArrayList<Entry> yVals2 = new ArrayList<>();
        ArrayList<Entry> yVals3 = new ArrayList<>();

        try {
            for (int i = 0; i < tableValueList.size(); i++) {
                TableValues temp = tableValueList.get(i);
                for (DataResponse dataResponse : temp.dataList) {
                    if (dataResponse.SensorType.equals(columnName1)) {
                        yVals1.add(new Entry(Float.valueOf(dataResponse.SensorValue), getXIndexByTime(dataResponse.RepHour)));
                    }
                    if (columnName2 != null && dataResponse.SensorType.equals(columnName2)) {
                        //已经有第二序列的值了
                        yVals2.add(new Entry(Float.valueOf(dataResponse.SensorValue), getXIndexByTime(dataResponse.RepHour)));
                    }
                    if (columnName3 != null && dataResponse.SensorType.equals(columnName3)) {
                        //已经有第二序列的值了
                        yVals3.add(new Entry(Float.valueOf(dataResponse.SensorValue), getXIndexByTime(dataResponse.RepHour)));
                    }
                }
            }
        } catch (Exception e) {
            Log.i("mInfo", "parse exception");
        }

        setDataInChart1(yVals1);

        if (!TextUtils.isEmpty(columnName2)) {
            setDataInChart2(yVals2);
        }

        if (!TextUtils.isEmpty(columnName3)) {
            setDataInChart3(yVals3);
            //存在第三系列
        }

    }


    private void setDataInChart3(ArrayList<Entry> yVals3) {
        // create a dataset and give it a type
        LineDataSet set3 = new LineDataSet(yVals3, sensorNameMap.get(columnName3));
        set3.setAxisDependency(YAxis.AxisDependency.LEFT);
        set3.setColor(getColorByColumnName(columnName3));
        set3.setCircleColor(getColorByColumnName(columnName3));
        set3.setLineWidth(1f);
        if (currentTab == DeviceDataActivity.TAB_DAY) {
            set3.setCircleSize(3f);
        } else {
            set3.setCircleSize(2f);
        }
        set3.setFillAlpha(65);
        set3.setFillColor(getColorByColumnName(columnName3));
        set3.setDrawCircleHole(true);
        set3.setHighLightColor(Color.rgb(244, 117, 117));
        set3.setDrawValues(false);

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set3); // add the datasets
        // create a data object with the datasets
        LineData data = new LineData(getXVals(), dataSets);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);
        // set data
        setValueRange(mChart3, yVals3);
        mChart3.setData(data);
        mChart3.animateX(2500);
        mChart3.invalidate();

    }

    private void setDataInChart2(ArrayList<Entry> yVals2) {
        //存在第二系列
        // create a dataset and give it a type
        LineDataSet set2 = new LineDataSet(yVals2, sensorNameMap.get(columnName2));
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setColor(getColorByColumnName(columnName2));
        set2.setCircleColor(getColorByColumnName(columnName2));
        set2.setLineWidth(1f);
        if (currentTab == DeviceDataActivity.TAB_DAY) {
            set2.setCircleSize(3f);
        } else {
            set2.setCircleSize(2f);
        }
        set2.setFillAlpha(65);
        set2.setFillColor(getColorByColumnName(columnName2));
        set2.setDrawCircleHole(true);
        set2.setHighLightColor(Color.rgb(244, 117, 117));
        set2.setDrawValues(false);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set2); // add the datasets
        // create a data object with the datasets
        LineData data = new LineData(getXVals(), dataSets);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);
        // set data
        setValueRange(mChart2, yVals2);
        mChart2.setData(data);
        mChart2.animateX(2500);
        mChart2.invalidate();
    }

    private void setValueRange(LineChart mChart, ArrayList<Entry> yVals) {
        double deviation = MathUtil.getStandardDevition(yVals);
        double average = MathUtil.getAverage(yVals);
        YAxis leftAxis = mChart.getAxisLeft();
        if (deviation != 0) {
            leftAxis.setAxisMinValue((float) (average - 10 * deviation));
            leftAxis.setAxisMaxValue((float) (average + 10 * deviation));
        } else {
            leftAxis.resetAxisMaxValue();
            leftAxis.resetAxisMinValue();
        }
    }

    private void setDataInChart1(ArrayList<Entry> yVals1) {
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals1, sensorNameMap.get(columnName1));
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(getColorByColumnName(columnName1));
        set1.setCircleColor(getColorByColumnName(columnName1));
        set1.setLineWidth(1f);
        if (currentTab == DeviceDataActivity.TAB_DAY) {
            set1.setCircleSize(3f);
        } else {
            set1.setCircleSize(2f);
        }
        set1.setFillAlpha(65);
        set1.setFillColor(getColorByColumnName(columnName1));
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setCircleColorHole(Color.WHITE);
        set1.setDrawCircleHole(true);
        set1.setDrawValues(false);
//        set1.setCircleHoleColor(Color.WHITE);
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets
        // create a data object with the datasets
        LineData data = new LineData(getXVals(), dataSets);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);
        // set data
        setValueRange(mChart1, yVals1);
        mChart1.setData(data);
        mChart1.animateX(2500);
        mChart1.invalidate();

    }

    private int getColorByColumnName(String columnName) {
        if (Sensor.SENSOR_HUMIDITY.equals(columnName)) {
            return COLOR_BLUE;
        } else if (Sensor.SENSOR_TEMPERATURE.equals(columnName)) {
            return COLOR_YELLOW;
        } else {
            return COLOR_GREEN;
        }
    }

    private int getXIndexByTime(String repHour) {
        //根据服务器返回
        //如果是天，repHour就是每一天的小时，如0,1,,,,,,,10,11,...23

        //如果是周,repHour是日期+小时 如，2017-07-01 00， 2017-07-02 06 2017-07-02 12 2017-07-02 18 四个点
        if (TextUtils.isEmpty(repHour)) {
            return 0;
        }
        try {
            if (currentTab == DeviceDataActivity.TAB_DAY) {
                return Integer.parseInt(repHour);

            } else if (currentTab == DeviceDataActivity.TAB_WEEK) {
                DateTime repHourDate = new DateTime(repHour.substring(0, 10));
                int day = repHourDate.getWeekDay() - 1;
                if (repHour.substring(11, repHour.length()).equals("00")) {
                    return day * 6 + 1;
                } else if (repHour.substring(11, repHour.length()).equals("04")) {
                    return day * 6 + 2;
                } else if (repHour.substring(11, repHour.length()).equals("08")) {
                    return day * 6 + 3;
                } else if (repHour.substring(11, repHour.length()).equals("12")) {
                    return day * 6 + 4;
                } else if (repHour.substring(11, repHour.length()).equals("16")) {
                    return day * 6 + 5;
                } else if (repHour.substring(11, repHour.length()).equals("20")) {
                    return day * 6 + 6;
                } else {
                    return 0;
                }
            }
        } catch (Exception e) {
            Log.e("mInfo", "RepHour format exception");
            return 0;
        }
        return 0;
    }

    private ArrayList<String> getXVals() {
        ArrayList<String> xVals = new ArrayList<>();
        if (currentTab == DeviceDataActivity.TAB_DAY) {
            //显示24个数字代表24小时即可
            for (int i = 0; i <= 23; i++) {
                xVals.add(i + "");
            }
        }
        if (currentTab == DeviceDataActivity.TAB_WEEK) {
            DateTime dateTime = new DateTime(currentDate.format("YYYY-MM-DD"));
            dateTime = dateTime.minus(0, 0, dateTime.getWeekDay() - 1, 0, 0, 0, 0, null);
            for (int i = 1; i <= 42; i++) {
                if (i % 6 == 1) {
                    xVals.add(dateTime.format("DD日"));
                    dateTime = dateTime.plus(0, 0, 1, 0, 0, 0, 0, null);
                }
                xVals.add("");
            }
        }
        return xVals;
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
            getDayData();
        } else {
            getWeekData();
        }

        pieChart1.animateY(1500, Easing.EasingOption.EaseInOutQuad);
        pieChart2.animateY(1500, Easing.EasingOption.EaseInOutQuad);
        pieChart3.animateY(1500, Easing.EasingOption.EaseInOutQuad);
    }

    @Override
    public void onTabChanged(int tabType) {
        currentTab = tabType;
        if (tabType == DeviceDataActivity.TAB_DAY) {
            //clear current data
            getDayData();
        } else {
            //clear current data
            getWeekData();
        }
    }

    public void setCurrentDate(DateTime currentDate) {
        this.currentDate = currentDate;
    }

    private LinearLayout.LayoutParams getPieChartLayoutParam(int size) {
        int length = 100;
        switch (size) {
            case PIE_CHART_SIZE_LARGE:
                length = UIUtil.dip2px(getActivity(), 140);
                break;
            case PIE_CHART_SIZE_MIDDLE:
                length = UIUtil.dip2px(getActivity(), 120);
                break;
            case PIE_CHART_SIZE_SMALL:
                length = UIUtil.dip2px(getActivity(), 100);
                break;
            default:
                break;
        }

        return new LinearLayout.LayoutParams(length, length, 1);
    }

    public static String formatValue(float value) {
        DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p = decimalFormat.format(value);//format 返回的是字符串
        return p;
    }


}
