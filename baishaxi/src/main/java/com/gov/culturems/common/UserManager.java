package com.gov.culturems.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.gov.culturems.entities.LoginInfo;
import com.gov.culturems.entities.User;

/**
 * this class manages all the user info, including:
 * <p/>
 * user logged in
 * user logged out
 * user type
 * save user info to sharedPreference
 * retrieve user info from sharedPreference
 * <p/>
 * <p/>
 * Created by peter on 2015/3/5.
 */
public class UserManager {

    public static final String USER_SP = "user_sp";

    private static UserManager instance;
    private boolean isLogin;
    private User user;

    private SharedPreferences userSP;

    private Context context;

    private UserManager(Context context) {
        this.context = context;
        userSP = context.getSharedPreferences(USER_SP, Context.MODE_PRIVATE);
        user = User.retrieveUser(userSP);
        if (user != null) {
            isLogin = true;
        } else {
            isLogin = false;
        }
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
    }


    public static UserManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("please init the user manager first");
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public int getUserType() {
        return user.getUserType();
    }

    public void setMobileNo(String mobile) {
        user.setMobileNo(mobile);
        user.saveUser(userSP);
    }


    public String getMobileNo() {
        return user.getMobileNo();
    }

    public String getUserId() {
        return user.getUserID();
    }

    public void login(LoginInfo loginInfo) {
        if (isLogin) {
            logout();
        }
        user = new User();
        user.initWithLoginInfo(loginInfo);
        user.saveUser(userSP);
        isLogin = true;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setIsLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }

    public void logout() {
        if (user != null)
            user.clearSP(userSP);
        user = null;
        isLogin = false;
    }

    @Override
    protected void finalize() throws Throwable {
        if (user != null && userSP != null) {
            user.saveUser(userSP);
        }
        super.finalize();
    }
}

