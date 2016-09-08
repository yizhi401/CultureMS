package com.gov.culturems.views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gov.culturems.R;

import java.text.DecimalFormat;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * 通过左右的滑动，可以加减一周或者一天，返回选中的当天时间
 * Created by peter on 5/20/15.
 */
public class ChooseDateView extends RelativeLayout {

    public static final int TYPE_DAY = 1;
    public static final int TYPE_WEEK = 7;

    private Context context;
    private DateTime dateTime;
    private Button preBtn;
    private Button dateText;
    private String currentDate;
    private Button nextBtn;
    private OnDateChangeListener listener;
    private DateTime today;
    private Button timeView;
    private final StringBuilder monthYearStringBuilder = new StringBuilder(50);
    private int viewType;
    private int hourOfDay;
    private int minute;
    private boolean hasTimeChosen;

    public interface OnDateChangeListener {
        void onDateChange(DateTime dateTime, String monthStr);
    }

    public ChooseDateView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ChooseDateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ChooseDateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        dateTime = DateTime.now(TimeZone.getTimeZone("Asia/Shanghai"));
        today = DateTime.today(TimeZone.getTimeZone("Asia/Shanghai"));
        LayoutInflater.from(context).inflate(R.layout.choose_date_view, this);
        preBtn = (Button) findViewById(R.id.calendar_left_arrow);
        nextBtn = (Button) findViewById(R.id.calendar_right_arrow);
        dateText = (Button) findViewById(R.id.calendar_date_textview);
        dateText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        timeView = (Button)findViewById(R.id.calendar_time_text);
        timeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
        preBtn.setOnClickListener(getCalendarOnClickListener());
        nextBtn.setOnClickListener(getCalendarOnClickListener());
        currentDate = dateTime.format("YYYY-MM-DD");
        dateText.setText(currentDate);
//        refreshDateTextView();
    }

    private void showTimePicker() {
        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                ChooseDateView.this.hourOfDay = hourOfDay;
                ChooseDateView.this.minute = minute;
                timeView.setText(format00(hourOfDay) + ":" + format00(minute));
                hasTimeChosen = true;
                 if (listener != null) {
                    listener.onDateChange(dateTime, currentDate);
                }
            }
        },hourOfDay,minute,true).show();
   }

    private String format00(int num){
        DecimalFormat mFormat = new DecimalFormat("00");
        return mFormat.format(num);
    }

    public String getSelectedBeginTime(){
        return "00:00:00";
   }

    public String getSelectedEndTime(){
         if(hasTimeChosen)
            return format00(hourOfDay) + ":"+format00(minute)+":59";
        else
            return "23:59:59";
    }

    private void showDatePicker() {
        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateTime = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0, 0, 0);
                currentDate = dateTime.format("YYYY-MM-DD");
                dateText.setText(currentDate);
                if (listener != null) {
                    listener.onDateChange(dateTime, currentDate);
                }
            }
        }, dateTime.getYear(), dateTime.getMonth() - 1, dateTime.getDay()).show();
   }

    /**
     * Refresh month title text view when user swipe
     */
//    protected void refreshDateTextView() {
    // Refresh title view
//        long millis = dateTime.getMilliseconds(TimeZone.getTimeZone("Asia/Shanghai"));

    // This is the method used by the platform Calendar app to get a
    // correctly localized month name for display on a wall calendar
//        monthYearStringBuilder.setLength(0);
//        String monthTitle = DateUtils.formatDateRange(context,
//                monthYearFormatter, millis, millis, DATE_FLAG).toString();

//        dateText.setText(monthTitle.toUpperCase(Locale.getDefault()));

//    }
    private View.OnClickListener getCalendarOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.calendar_left_arrow:
                        dateTime = dateTime.minus(0, 0, viewType, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                        break;
                    case R.id.calendar_right_arrow:
                        if (dateTime.gt(today)) {
                            Toast.makeText(context, "不能往后了", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            dateTime = dateTime.plus(0, 0, viewType, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                        }
                        break;
                    default:
                        break;
                }

                currentDate = dateTime.format("YYYY-MM-DD");
                dateText.setText(currentDate);
                if (listener != null) {
                    DateTime temp = new DateTime(dateTime.toString());
                    listener.onDateChange(temp, currentDate);
                }
                //当日期改变的时候，恢复时间选择器
                restoreTimeView();
            }
        };
    }

    private void restoreTimeView(){
        hasTimeChosen = false;
        hourOfDay = 0;
        minute = 0;
        timeView.setText(getResources().getString(R.string.choose_time));
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void isTimeViewShow(boolean isShow){
        if(isShow){
            timeView.setVisibility(View.VISIBLE);
        }else{
            timeView.setVisibility(View.GONE);
        }
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setDateChangeListener(OnDateChangeListener listener) {
        this.listener = listener;
    }
}
