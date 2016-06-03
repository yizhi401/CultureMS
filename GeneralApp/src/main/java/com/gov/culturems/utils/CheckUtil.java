package com.gov.culturems.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peter on 4/7/15.
 */
public class CheckUtil {
    /**
     * judge the number is mobile number or not.
     *
     * @param mobiles mobile number
     * @return whether the mobile number is
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8,12}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static boolean isEmailAddress(String email) {
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        // .compile("^/w+([-.]/w+)*@/w+([-]/w+)*/.(/w+([-]/w+)*/.)*[a-z]{2,3}$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isYoyoID(String yoyoID) {
        return Pattern.matches("\\d{1,10}$", yoyoID);
    }

    public final static boolean isNumeric(String numberStr) {
        if (numberStr != null && !"".equals(numberStr.trim())) {
            return numberStr.matches("^[0-9]*$");
        } else {
            return false;
        }
    }

}
