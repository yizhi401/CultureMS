package com.gov.culturems.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.common.http.response.LoginResp;
import com.gov.culturems.entities.BaseScene;
import com.gov.culturems.entities.TeaFactory;
import com.gov.culturems.utils.EncodeUtil;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.UIUtil;

/**
 * 用户选择工厂后登陆页面
 * //TODO 需要增加返回重新选择工厂按钮和显示已经选择的工厂名称
 * Created by peter on 6/9/15.
 */
public class LoginActivity extends Activity {

    private boolean doubleBackToExitPressedOnce = false;

    private EditText usernameEdit;
    private EditText passwordEdit;

    private ImageView usernameImg;
    private ImageView passwordImg;

    private Button loginBtn;
    private RelativeLayout outerLayout;

    private BaseScene baseScene;
    private TextView returnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        baseScene = new BaseScene();
        baseScene.setId("279");
        baseScene.setName("恭王府");

        setUpView();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        teaFactory = (TeaFactory) getIntent().getSerializableExtra("factory");
//        if (teaFactory == null) {
//            Intent intent = new Intent(LoginActivity.this, FactoryChooseActivity.class);
//            startActivity(intent);
//            overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
//        }
    }

    private void setUpView() {
        usernameEdit = (EditText) findViewById(R.id.username_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        usernameImg = (ImageView) findViewById(R.id.username_ic);
        passwordImg = (ImageView) findViewById(R.id.password_ic);
        returnText = (TextView) findViewById(R.id.return_text);
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
        returnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,FactoryChooseActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

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
        params.put("SceneId", baseScene.getId());
        HttpUtil.jsonRequest(this, URLRequest.LOGIN, params, new VolleyRequestListener() {

            @Override
            public void onSuccess(String response) {
                UIUtil.dismissTipDialog(LoginActivity.this);
                CommonResponse<LoginResp> result = GsonUtils.fromJson(response, new TypeToken<CommonResponse<LoginResp>>() {
                });
                if (result.getRc() == 200) {
                    UserManager.getInstance().login(result.getData(),baseScene);
                    Intent intent = new Intent(LoginActivity.this, DryingRoomActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
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

