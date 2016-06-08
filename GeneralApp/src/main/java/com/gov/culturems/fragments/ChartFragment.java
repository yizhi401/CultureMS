package com.gov.culturems.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.gov.culturems.MyApplication;
import com.gov.culturems.R;
import com.gov.culturems.activities.DeviceDataActivity;
import com.gov.culturems.activities.DryingRoomActivity;
import com.gov.culturems.activities.DryingRoomHelper;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.BaseSensor;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.entities.ShouldDraw;
import com.gov.culturems.utils.FloatValueFormatter;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.UIUtil;
import com.gov.culturems.views.EasyScrollableView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

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

    private static final int REFRESH_INTERVAL = 10000;

    private DeviceDataActivity deviceDataActivity;

    private PieChart pieChart1, pieChart2;

    private DryingRoom dryingRoom;

    private TextView valueText1, valueText2;
    private LineChart mChart1, mChart2;
    private DateTime currentDate;

    private EasyScrollableView scrollableView;

    private List<DryingRoomHelper.TableValues> tableValueList;

    private TimeHandler timeHandler;
    private RefreshLoop refreshLoop;
    private boolean isRunning = false;
    //    private int countDown = 5;//如果多次请求温度数据而没有获得，退出当前页面
    private boolean isFirstEnter = true;

    private class RefreshLoop extends Thread {
        @Override
        public void run() {
            while (getActivity() != null && isVisible() && isRunning) {
                timeHandler.sendEmptyMessage(0);
                try {
                    Thread.sleep(REFRESH_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class TimeHandler extends Handler {
        private final WeakReference<ChartFragment> chartFragmentWeakReference;

        private TimeHandler(ChartFragment chartFragmentWeakReference) {
            this.chartFragmentWeakReference = new WeakReference<>(chartFragmentWeakReference);
        }

        @Override
        public void handleMessage(Message msg) {
            ChartFragment fragment = chartFragmentWeakReference.get();
            if (fragment == null) {
                return;
            }
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    fragment.refreshPieDatas();
                    break;
                default:
                    break;
            }
        }
    }


    public static ChartFragment newInstance(DryingRoom dryingRoom) {
        ChartFragment chartFragment = new ChartFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("dryingRoom", dryingRoom);
        chartFragment.setArguments(bundle);
        return chartFragment;
    }

    public ChartFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timeHandler = new TimeHandler(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dryingRoom = (DryingRoom) getArguments().getSerializable("dryingRoom");
        View rootView = inflater.inflate(R.layout.chart_fragment, container, false);

        scrollableView = (EasyScrollableView) rootView.findViewById(R.id.easyscrollview);

        valueText1 = (TextView) rootView.findViewById(R.id.value_text1);
        valueText2 = (TextView) rootView.findViewById(R.id.value_text2);

        mChart1 = (LineChart) rootView.findViewById(R.id.line_chart_1);
        mChart2 = (LineChart) rootView.findViewById(R.id.line_chart_2);

        pieChart1 = (PieChart) rootView.findViewById(R.id.pie_chart1);
        pieChart2 = (PieChart) rootView.findViewById(R.id.pie_chart2);

        initDeviceInfo();
        getDayData();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //开始循环
        isFirstEnter = true;
        isRunning = true;
        refreshLoop = new RefreshLoop();
        refreshLoop.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        //停止循环
        isRunning = false;
        refreshLoop = null;
    }


    public void refreshPieDatas() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("SceneId", dryingRoom.getId());
        requestParams.putWithoutFilter("t", getCurrentTimestamp());
        HttpUtil.jsonRequestGet(getActivity(), URLRequest.SCENE_DATAS_GET, requestParams, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                DryingRoomHelper.SceneDataListResponse listResponse = GsonUtils.fromJson(response, DryingRoomHelper.SceneDataListResponse.class);
                handlePieData(listResponse);
            }

            @Override
            public void onNetError(VolleyError error) {

            }
        });
    }

    private void handlePieData(DryingRoomHelper.SceneDataListResponse listResponse) {
        if (listResponse.rc == 200 && listResponse.Data != null && listResponse.Data.size() >= 1) {

            DryingRoomHelper.SceneData data0 = listResponse.Data.get(0);
            DryingRoomHelper.SceneData data1 = listResponse.Data.get(1);
            if (data0 == null || data1 == null ||
                    DryingRoomHelper.SceneData.STATUS_OFFLINE.equals(data0.Status) ||
                    DryingRoomHelper.SceneData.STATUS_OFFLINE.equals(data1.Status)) {
                handleOffline();
            } else {
                isFirstEnter = false;
                //保证温度在前
                if (data0.SensorType.equals(BaseSensor.SENSOR_TEMPERATURE)) {
                    setPieData(data0.SensorValue, data0.SensorType, pieChart1);
                    setPieData(data1.SensorValue, data1.SensorType, pieChart2);
                } else {
                    setPieData(data0.SensorValue, data0.SensorType, pieChart2);
                    setPieData(data1.SensorValue, data1.SensorType, pieChart1);
                }
            }
        } else {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "数据出错", Toast.LENGTH_SHORT).show();
                //停止循环更新
                getActivity().setResult(DryingRoomActivity.RESULT_NEED_REFRESH);
                getActivity().finish();
            }
        }
    }

    private void handleOffline() {
        if (getActivity() == null) {
            return;
        }
        getActivity().setResult(DryingRoomActivity.RESULT_NEED_REFRESH);
        if (isFirstEnter) {
            //一次进入，没有温湿度，表明已经离线
            getActivity().finish();
            //回到首页刷新列表
        } else {
            //用户在这个页面的时候离线
            //note 按照目前的逻辑，这种情况不会出现，放在这里以防万一吧
            Toast.makeText(getActivity(), "设备已经离线", Toast.LENGTH_SHORT).show();
            //停止循环更新
            isRunning = false;
        }
    }

    private String getCurrentTimestamp() {
        DateTime currentDate = DateTime.now(TimeZone.getTimeZone("Asia/Shanghai"));
        String dateStr = currentDate.format("YYYY-MM-DD hh:mm:ss");
        return dateStr.replace(" ", "%20");
    }

    public void setDeviceDataActivity(DeviceDataActivity deviceDataActivity) {
        this.deviceDataActivity = deviceDataActivity;
    }


    /**
     * 决定device是有几个sensor,它们的名字是什么
     */
    private void initDeviceInfo() {
        mChart1.setVisibility(View.GONE);
        mChart2.setVisibility(View.GONE);
        pieChart1.setVisibility(View.GONE);
        pieChart2.setVisibility(View.GONE);
        pieChart1.setHoleRadius(30);
        pieChart1.setDrawHoleEnabled(true);
        pieChart2.setHoleRadius(70);
        pieChart2.setDrawHoleEnabled(true);

        mChart1.setVisibility(View.VISIBLE);
        mChart2.setVisibility(View.VISIBLE);
        pieChart1.setVisibility(View.VISIBLE);
        pieChart1.setLayoutParams(getPieChartLayoutParam(PIE_CHART_SIZE_MIDDLE));
        pieChart2.setVisibility(View.VISIBLE);
        pieChart2.setLayoutParams(getPieChartLayoutParam(PIE_CHART_SIZE_MIDDLE));

        initChart(mChart1, BaseSensor.SENSOR_TEMPERATURE);
        initChart(mChart2, BaseSensor.SENSOR_HUMIDITY);

        initPieChart(pieChart1);
        initPieChart(pieChart2);
    }

    private void initPieChart(PieChart mChart) {
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
     * @param columnName
     * @param mChart
     */
    private void setPieData(String valueTxt, String columnName, PieChart mChart) {
        ArrayList<Integer> colors = new ArrayList<>();
        ArrayList<Entry> yVals1 = new ArrayList<>();
        mChart.needsHighlight(0, 0);
        mChart.needsHighlight(0, 1);
        mChart.needsHighlight(1, 0);
        mChart.needsHighlight(1, 1);

        if (columnName.toLowerCase().equals(BaseSensor.SENSOR_TEMPERATURE.toLowerCase())) {
            //Temperature
            yVals1.add(new Entry(100, 1));
            colors.add(COLOR_YELLOW);
            mChart.setRotationAngle(-90);
            mChart.setCenterTextColor(COLOR_YELLOW);
            mChart.setCenterText(valueTxt);
            mChart.setCenterTextSize(18);

        } else if (columnName.toLowerCase().equals(BaseSensor.SENSOR_HUMIDITY.toLowerCase())) {
            //Humidity
            yVals1.add(new Entry(100, 1));
            mChart.setRotationAngle(-90);
            mChart.setCenterText(valueTxt);
            mChart.setCenterTextColor(COLOR_BLUE);
            mChart.setCenterTextSize(18);

            colors.add(COLOR_BLUE);
        }

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
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTypeface(tf);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setSpaceBetweenLabels(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.resetAxisMaxValue();
        leftAxis.resetAxisMinValue();
        leftAxis.setStartAtZero(false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setValueFormatter(new FloatValueFormatter());
        leftAxis.setAxisMaxValue(100);
        leftAxis.setAxisMinValue(0);
        leftAxis.setLabelCount(5, true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private OnChartValueSelectedListener getOnChartValueSelected(final String chartTag) {
        return new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if (chartTag.equals(BaseSensor.SENSOR_TEMPERATURE)) {
                    //写死了第一列是温度
                    if (valueText1 != null) {
                        valueText1.setVisibility(View.VISIBLE);
                        valueText1.setText("温度: " + formatValue(e.getVal()) + "℃");
                    }
                } else if (chartTag.equals(BaseSensor.SENSOR_HUMIDITY)) {
                    //写死了第二列是湿度
                    if (valueText2 != null) {
                        valueText2.setVisibility(View.VISIBLE);
                        valueText2.setText("湿度: " + formatValue(e.getVal()) + "%RH");
                    }
                } else {
                    valueText1.setVisibility(View.GONE);
                    valueText2.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected() {
                if (chartTag.equals(BaseSensor.SENSOR_TEMPERATURE)) {
                    if (valueText1 != null) {
                        valueText1.setVisibility(View.GONE);
                    }
                } else if (chartTag.equals(BaseSensor.SENSOR_HUMIDITY)) {
                    if (valueText2 != null) {
                        valueText2.setVisibility(View.GONE);
                    }
                } else {
                    valueText1.setVisibility(View.GONE);
                    valueText2.setVisibility(View.GONE);
                }
            }
        };
    }

    private void setData() {


        if (tableValueList == null || tableValueList.size() == 0) {
            mChart1.clear();
            mChart1.invalidate();
            mChart2.clear();
            mChart2.invalidate();
            return;
        }
        valueText1.setVisibility(View.GONE);
        valueText2.setVisibility(View.GONE);

        mChart1.getXAxis().setLabelsToSkip(1);
        mChart2.getXAxis().setLabelsToSkip(1);

        ArrayList<Entry> yVals1 = new ArrayList<>();
        ArrayList<Entry> yVals2 = new ArrayList<>();

        try {
            for (int i = 0; i < tableValueList.size(); ++i) {
                DryingRoomHelper.TableValues temp = tableValueList.get(i);
                if (Integer.parseInt(temp.repHour) % 2 != 0)
                    continue;
                for (DryingRoomHelper.DataResponse dataResponse : temp.dataList) {
                    if (dataResponse.SensorType.equals(BaseSensor.SENSOR_TEMPERATURE)) {
                        Entry entry = new Entry(Float.valueOf(dataResponse.SensorValue), getXIndexByTime(dataResponse.RepHour));
                        entry.setData(new ShouldDraw(false));
                        yVals1.add(entry);
                    }
                    if (dataResponse.SensorType.equals(BaseSensor.SENSOR_HUMIDITY)) {
                        //已经有第二序列的值了
                        Entry entry = new Entry(Float.valueOf(dataResponse.SensorValue), getXIndexByTime(dataResponse.RepHour));
                        entry.setData(new ShouldDraw(false));
                        yVals2.add(entry);
                    }
                }
            }
        } catch (Exception e) {
            Log.i("mInfo", "parse exception");
        }

        flagMaxMin(yVals1);
        flagMaxMin(yVals2);
        setDataInChart1(yVals1);
        setDataInChart2(yVals2);

    }

    private void flagMaxMin(ArrayList<Entry> yVals) {
        if (yVals == null || yVals.size() == 0)
            return;
        int maxIndex = 0;
        int minIndex = 0;
        for (int i = 0; i < yVals.size(); i++) {
            if (yVals.get(maxIndex).getVal() < yVals.get(i).getVal()) {
                maxIndex = i;
            }
            if (yVals.get(minIndex).getVal() > yVals.get(i).getVal()) {
                minIndex = i;
            }
        }
        ((ShouldDraw) yVals.get(maxIndex).getData()).shouldDrawValue = true;
        ((ShouldDraw) yVals.get(maxIndex).getData()).shouldDrawUp = true;
        ((ShouldDraw) yVals.get(minIndex).getData()).shouldDrawValue = true;
        ((ShouldDraw) yVals.get(minIndex).getData()).shouldDrawUp = false;
    }

    /**
     * 图表2 用于湿度
     *
     * @param yVals2
     */
    private void setDataInChart2(ArrayList<Entry> yVals2) {
        //存在第二系列
        // create a dataset and give it a type
        LineDataSet set2 = new LineDataSet(yVals2, "湿度");
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setColor(getColorByColumnName(BaseSensor.SENSOR_HUMIDITY));
        set2.setCircleColor(getColorByColumnName(BaseSensor.SENSOR_HUMIDITY));
        set2.setLineWidth(2f);
        set2.setCircleSize(4f);
        set2.setFillAlpha(65);
        set2.setFillColor(getColorByColumnName(BaseSensor.SENSOR_HUMIDITY));
        set2.setDrawCircleHole(true);
        set2.setHighLightColor(Color.rgb(244, 117, 117));
        set2.setDrawValues(true);
        set2.setValueFormatter(new FloatValueFormatter());
        set2.setValueTextColor(getColorByColumnName(BaseSensor.SENSOR_HUMIDITY));

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set2); // add the datasets
        // create a data object with the datasets
        LineData data = new LineData(getXVals(), dataSets);
        data.setValueTextColor(getColorByColumnName(BaseSensor.SENSOR_HUMIDITY));
        data.setValueTextSize(9f);
        // set data
        mChart2.setData(data);
        mChart2.animateX(2500);
        mChart2.invalidate();
    }


    private void setDataInChart1(ArrayList<Entry> yVals1) {
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals1, "温度");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(getColorByColumnName(BaseSensor.SENSOR_TEMPERATURE));
        set1.setCircleColor(getColorByColumnName(BaseSensor.SENSOR_TEMPERATURE));
        set1.setLineWidth(2f);
        set1.setCircleSize(4f);
        set1.setFillAlpha(65);
        set1.setFillColor(getColorByColumnName(BaseSensor.SENSOR_TEMPERATURE));
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setCircleColorHole(Color.WHITE);
        set1.setDrawCircleHole(true);
        set1.setValueFormatter(new FloatValueFormatter());
        set1.setDrawValues(true);
//        set1.setValueTextColor(getColorByColumnName(BaseSensor.SENSOR_TEMPERATURE));


        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the datasets
        // create a data object with the datasets
        LineData data = new LineData(getXVals(), dataSets);
        data.setValueTextColor(getColorByColumnName(BaseSensor.SENSOR_TEMPERATURE));
        data.setValueTextSize(9f);
        // set data
        mChart1.setData(data);
        mChart1.animateX(2500);
        mChart1.invalidate();

    }

    private int getColorByColumnName(String columnName) {
        if (BaseSensor.SENSOR_HUMIDITY.equals(columnName)) {
            return COLOR_BLUE;
        } else if (BaseSensor.SENSOR_TEMPERATURE.equals(columnName)) {
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
        return Integer.parseInt(repHour);

    }

    private ArrayList<String> getXVals() {
        ArrayList<String> xVals = new ArrayList<>();
        //显示24个数字代表24小时即可
        for (int i = 0; i <= 23; i++) {
            xVals.add(i + "");
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
        String nowStr = currentDate.format("YYYY-MM-DD");
        return nowStr + "%2000:00:00";
    }

    /**
     * 如果当前是一天，返回一天的开始时间
     * 如果当前是一周，返回本周的开始时间
     *
     * @return
     */
    private String getEndTimeStr() {
        String nowStr = currentDate.format("YYYY-MM-DD");
        return nowStr + "%2023:59:59";
    }

    private void getDayData() {
        DryingRoomHelper.getInstance().getDayData(getActivity(),
                currentDate.format("YYYY-MM-DD"),
                new DryingRoomHelper.TableValuesListener() {
                    @Override
                    public void onTableValuesGet(List<DryingRoomHelper.TableValues> result) {
                        tableValueList = result;
                        setData();
                    }

                    @Override
                    public void onFailed() {

                    }
                });
    }


    @Override
    public void onDataChanged(DateTime changedDate) {
        currentDate = changedDate;
        getDayData();


        pieChart1.animateY(1500, Easing.EasingOption.EaseInOutQuad);
        pieChart2.animateY(1500, Easing.EasingOption.EaseInOutQuad);
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
