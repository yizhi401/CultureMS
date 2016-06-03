package com.gov.culturems.common.http.response;

import com.gov.culturems.entities.User;

import java.util.List;

/**
 * 登陆信息返回Json
 * Created by peter on 6/22/15.
 */
public class LoginResp {

    private String DeviceID;
    private String UserID;
    private String UserName;
    private String Status;
    private String LoginName;
    private String UserType;
    private String Email;
    private String MobileNo;
    private List<String> SceneIds;
    private String SceneId;
    private String SceneName;
    private String SceneNames;
    private String RegRemark;


    public User convertToUser() {
        User user = new User();
        user.setUserID(UserID);
        user.setUserType(Integer.parseInt(UserType));
        user.setMobileNo(MobileNo);//目前的username和手机号是一样的
        user.setUserName(UserName);
        user.setPassword("");
        user.setStatus(Integer.parseInt(Status));
        user.setEmail(Email);
        user.setLoginName(LoginName);
        return user;
    }

}
