package com.gov.culturems.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * Created by peter on 2015/3/31.
 */
public class TimeUtil {
    public static String getTimeLast(String beginTime) {
        DateTime today = DateTime.now(TimeZone.getTimeZone("Asia/Shanghai"));
        @SuppressLint("SimpleDateFormat") DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(beginTime);
            long time = System.currentTimeMillis() - date.getTime();
            if (time < 24 * 60 * 60 * 1000) {
                //The time is in 24 hours
                if (time < 60 * 60 * 1000) {
                    //with in one hour
                    return time / 60 / 1000 + "分钟";
                } else {
                    int hour = (int) (time / (60 * 60 * 1000));
                    int minute = (int) ((time - hour * 60 * 60 * 1000) / 60 / 1000);
                    return hour + "小时" + minute + "分钟";
                }
            } else {
                //计算天数
                long oneDay = 24 * 60 * 60 * 1000;
                int dayPassed = (int) (time / oneDay);
                if (dayPassed * oneDay == time) {
                    return "第" + dayPassed + "天";
                } else {
                    return "第" + (dayPassed + 1) + "天";
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getRecentTime(String timeStr) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(timeStr);
            if (System.currentTimeMillis() - date.getTime() < 24 * 60 * 60 * 1000) {
                //The time is in 24 hours
                long time = System.currentTimeMillis() - date.getTime();
                if (time < 60 * 60 * 1000) {
                    //with in one hour
                    return time / 60 / 1000 + "分钟前";
                } else {
                    int hour = (int) (time / (60 * 60 * 1000));
                    int minute = (int) ((time - hour * 60 * 60 * 1000) / 60 / 1000);
                    return hour + "小时" + minute + "分钟前";
                }
            } else {
                return sdfDate.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStr;
    }


    public static int compareTwoUnformattedTimeStr(String str1, String str2) {
        String slash = "/";
        int result = 0;
        int year1 = Integer.valueOf(str1.substring(0, str1.indexOf(slash)));
        int year2 = Integer.valueOf(str2.substring(0, str2.indexOf(slash)));

        if (year1 != year2) {
            result = year1 - year2;
        } else {
            str1 = str1.substring(str1.indexOf(slash) + 1, str1.length());
            str2 = str2.substring(str2.indexOf(slash) + 1, str2.length());
            int month1 = Integer.valueOf(str1.substring(0, str1.indexOf(slash)));
            int month2 = Integer.valueOf(str2.substring(0, str2.indexOf(slash)));
            if (month1 != month2) {
                result = month1 - month2;
            } else {
                str1 = str1.substring(str1.indexOf(slash) + 1, str1.length());
                str2 = str2.substring(str2.indexOf(slash) + 1, str2.length());
                int day1 = Integer.valueOf(str1.substring(0, str1.indexOf(" ")));
                int day2 = Integer.valueOf(str2.substring(0, str2.indexOf(" ")));
                if (day1 != day2) {
                    result = day1 - day2;
                } else {
                    str1 = str1.substring(str1.indexOf(" ") + 1, str1.length());
                    str2 = str2.substring(str2.indexOf(" ") + 1, str2.length());
                    int hour1 = Integer.valueOf(str1.substring(0, str1.indexOf(":")));
                    int hour2 = Integer.valueOf(str2.substring(0, str2.indexOf(":")));
                    if (hour1 != hour2) {
                        result = hour1 - hour2;
                    } else {
                        str1 = str1.substring(str1.indexOf(":") + 1, str1.length());
                        str2 = str2.substring(str2.indexOf(":") + 1, str2.length());
                        int min1 = Integer.valueOf(str1.substring(0, str1.indexOf(":")));
                        int min2 = Integer.valueOf(str2.substring(0, str2.indexOf(":")));
                        if (min1 != min2) {
                            result = min1 - min2;
                        } else {
                            str1 = str1.substring(str1.indexOf(":") + 1, str1.length());
                            str2 = str2.substring(str2.indexOf(":") + 1, str2.length());
                            int second1 = Integer.valueOf(str1);
                            int second2 = Integer.valueOf(str2);
                            if (second1 != second2) {
                                result = second1 - second2;
                            } else {
                                result = 0;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
