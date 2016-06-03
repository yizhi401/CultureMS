package com.gov.culturems.entities;

import android.content.SharedPreferences;

import java.io.Serializable;

/**
 * Created by peter on 6/22/15.
 */
public class User implements Serializable {

    public static final int USER_TYPE_MANAGER = 0;
    public static final int USER_TYPE_NORMAL = 1;
    public static final int USER_TYPE_UNKNOWN = 2;

    private static final int USER_STATUS_NORMAL = 1;

    private String password;
    private int status;
    private String loginName;
    private String userName;
    private int userType;
    private String userID;
    private String email;
    private String mobileNo;


    public void saveUser(SharedPreferences userSP) {
        SharedPreferences.Editor editor = userSP.edit();
        editor.putBoolean("isUserExist", true);
        editor.putString("password", password);
        editor.putInt("status", status);
        editor.putString("loginName", loginName);
        editor.putString("userName", userName);
        editor.putInt("userType", userType);
        editor.putString("userId", userID);
        editor.putString("email", email);
        editor.putString("mobile", mobileNo);

        editor.apply();
    }

    public static User retrieveUser(SharedPreferences userSP) {
        if (userSP.getBoolean("isUserExist", false)) {
            User user = new User();
            user.setUserID(userSP.getString("userId", ""));
            user.setUserName(userSP.getString("userName", ""));
            user.setUserType(userSP.getInt("userType", USER_TYPE_NORMAL));
            user.setPassword(userSP.getString("password", ""));
            user.setMobileNo(userSP.getString("mobile", ""));
            user.setStatus(userSP.getInt("status", USER_STATUS_NORMAL));

            return user;

        } else {
            return null;
        }
    }

    public void clearSP(SharedPreferences userSP) {
        SharedPreferences.Editor editor = userSP.edit();
        editor.putBoolean("isUserExist", false);
        editor.apply();
    }


    public void initWithLoginInfo(LoginInfo loginInfo) {
        setUserID(loginInfo.getUserID());
        setUserType(loginInfo.getUserType());
        setMobileNo(loginInfo.getMobileNo());//目前的username和手机号是一样的
        setUserName(loginInfo.getUserName());
        setPassword("");
        setStatus(loginInfo.getStatus());
        setEmail(loginInfo.getEmail());
        setLoginName(loginInfo.getLoginName());
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }
}
