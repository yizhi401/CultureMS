package com.gov.culturems.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.common.CommonConstant;
import com.gov.culturems.common.UserManager;
import com.gov.culturems.common.http.CommonResponse;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLConstant;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.LoginInfo;
import com.gov.culturems.utils.EncodeUtil;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.UIUtil;

/**
 * Created by peter on 6/9/15.
 */
public class LoginActivity extends Activity {

    private EditText usernameEdit;
    private EditText passwordEdit;

    private ImageView usernameImg;
    private ImageView passwordImg;

    private Button loginBtn;
    private RelativeLayout outerLayout;


    private RadioGroup superGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        usernameEdit = (EditText) findViewById(R.id.username_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        usernameImg = (ImageView) findViewById(R.id.username_ic);
        passwordImg = (ImageView) findViewById(R.id.password_ic);
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        outerLayout = (RelativeLayout) findViewById(R.id.outer_layout);
        outerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        outerLayout.setBackgroundResource(VersionController.getDrawable(VersionController.LOGIN_BG));
        usernameImg.setImageResource(VersionController.getDrawable(VersionController.LOGIN_USERNAME));
        passwordImg.setImageResource(VersionController.getDrawable(VersionController.LOGIN_PASSWORD));

        superGroup = (RadioGroup) findViewById(R.id.super_radio);
        initSuperGroup();
    }

    private void initSuperGroup() {
        if (VersionController.isDebug) {
            superGroup.setVisibility(View.VISIBLE);
        } else {
            superGroup.setVisibility(View.GONE);
        }
        superGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.gongwangfu_btn:
                        VersionController.setVersion(VersionController.GONGWANGFU);
                        break;
                    case R.id.blue_btn:
                        VersionController.setVersion(VersionController.GENERAL);
                        break;
                    case R.id.baishaxi_btn:
                        VersionController.setVersion(VersionController.BAISHAXI);
                        break;
                    default:
                        VersionController.setVersion(VersionController.GENERAL);
                        break;
                }
            }
        });
    }

    private void login() {

        if (TextUtils.isEmpty(usernameEdit.getText().toString()) || TextUtils.isEmpty(passwordEdit.getText().toString())) {
            Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        UIUtil.showTipDialog(LoginActivity.this, CommonConstant.DIALOG_TYPE_WAITING, "正在登录...");
        RequestParams params = new RequestParams();
        params.put("LoginName", usernameEdit.getText().toString());
        params.put("LoginPass", EncodeUtil.getCustomMD5ofStr(passwordEdit.getText().toString(), usernameEdit.getText().toString()));
//        params.put("LoginName", "lis6");
//        params.put("LoginPass", "33f911f4a61b4991c0dda0c2c6002776");
        HttpUtil.jsonRequest(this, URLConstant.LOGIN, params, new VolleyRequestListener() {

            @Override
            public void onSuccess(String response) {
                UIUtil.dismissTipDialog(LoginActivity.this);
                CommonResponse<LoginInfo> result = GsonUtils.fromJson(response, new TypeToken<CommonResponse<LoginInfo>>() {
                });
                if (result.getRc() == 200) {
                    UserManager.getInstance().login(result.getData());
                    Intent intent = new Intent(LoginActivity.this, ChooseActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "用户名或者密码错误", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNetError(VolleyError error) {
                UIUtil.dismissTipDialog(LoginActivity.this);
            }
        });
    }

}

