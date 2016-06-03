package com.gov.culturems.views.wheelview;

import android.content.Context;
import android.widget.TextView;

import com.gov.culturems.R;

import java.util.Calendar;

import kankan.wheel.widget.WheelView;

/**
 * 汇率查询的wheel类，只显示当前值以及之前的3个月汇率
 *
 * @author Peter
 */
public class DateSelect {

    private final Calendar calendar;

    private final int year, month, day;

    private String[] yearArr, monthArr, dayArr;

    private CommonWheelView wheelView;

    private Context context;

    int[] months_big = {1, 3, 5, 7, 8, 10, 12};
    int[] months_little = {4, 6, 9, 11};

    /**
     * 控件对象
     *
     * @param context
     */
    public DateSelect(Context context) {
        this.context = context;
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DATE);

        init();
    }

    private void init() {
        initData();
        wheelView = new CommonWheelView(context);
        wheelView.setWheelNums(CommonWheelView.THREE_WHEEL);
        wheelView.setChangedListener(new CommonWheelView.CommonWheelChangedListener() {

            @Override
            public void onLeftChanged(WheelView wheel, int oldValue,
                                      int newValue) {
                onYearChanged(wheel, oldValue, newValue);
            }

            @Override
            public void onCenterChanged(WheelView wheel, int oldValue,
                                        int newValue) {
                onMiddleChanged(wheel, oldValue, newValue);
            }

            @Override
            public void onRightChanged(WheelView wheel, int oldValue,
                                       int newValue) {
            }
        });

        wheelView.setLeftArr(yearArr);
        wheelView.setLeftCurrentItem(10);

        wheelView.setCenterArr(monthArr);
        wheelView.setCenterCurrentItem(month - 1);

        wheelView.setRightArr(dayArr);
        wheelView.setRightCurrentItem(day - 1);
    }

    protected void onYearChanged(WheelView wheel, int oldValue, int newValue) {

        initDayArrWithMonth(newValue + year - 10, wheelView.getCenterCurrentItem() + 1);

        int currentItem = wheelView.getRightCurrentItem();
        wheelView.setRightArr(dayArr);
        if (currentItem > dayArr.length - 1) {
            // 当前右侧选中的日期，新的月份没有
            wheelView.setRightCurrentItem(dayArr.length - 1);
        } else {
            wheelView.setRightCurrentItem(currentItem);
        }

    }

    private void initData() {

        // 从两个个月前开始算起
        yearArr = new String[20];
        //一共设置前后20年可选
        for (int i = 0; i < 20; i++) {
            yearArr[i] = year + i - 10 + context.getResources().getString(R.string.year);
        }

        monthArr = new String[12];
        for (int i = 0; i < 12; i++) {
            monthArr[i] = i + 1 + context.getResources().getString(R.string.month);
            if (i < 9) {
                monthArr[i] = "0" + monthArr[i];
            }
        }

        initDayArrWithMonth(year, month);


    }

    private void initDayArrWithMonth(int year, int month) {
        if (month == 2) {
            if (isRunnian(year)) {
                dayArr = new String[29];
            } else {
                dayArr = new String[28];
            }
        } else {
            for (int aMonths_big : months_big) {
                if (aMonths_big == month) {
                    dayArr = new String[31];
                    break;
                }
            }
            for (int aMonths_little : months_little) {
                if (aMonths_little == month) {
                    dayArr = new String[30];
                    break;
                }
            }
        }

        for (int i = 0; i < dayArr.length; i++) {
            dayArr[i] = i + 1 + context.getResources().getString(R.string.day);
        }

    }

    private boolean isRunnian(int year) {
        if (year % 100 == 0) {
            if (year % 400 == 0) {
                return true;
            }
        } else {
            if (year % 4 == 0) {
                return true;
            }
        }
        return false;
    }

    private void onMiddleChanged(WheelView wheel, int oldValue, int newValue) {

        initDayArrWithMonth(wheelView.getLeftCurrentItem() + year - 10, newValue + 1);

        int currentItem = wheelView.getRightCurrentItem();
        wheelView.setRightArr(dayArr);
        if (currentItem > dayArr.length - 1) {
            // 当前右侧选中的日期，新的月份没有
            wheelView.setRightCurrentItem(dayArr.length - 1);
        } else {
            wheelView.setRightCurrentItem(currentItem);
        }

    }

    public void setClickListener(CommonWheelView.CommonWheelClickListener listener) {
        wheelView.setWheelClickListener(listener);
    }

    /**
     */
    public void show(TextView textView) {
        wheelView.show(textView);
    }
    public String getDate(){
        return wheelView.getResult();
    }
}
