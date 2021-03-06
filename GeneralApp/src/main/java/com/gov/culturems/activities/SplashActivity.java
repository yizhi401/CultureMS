package com.gov.culturems.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;

import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.common.UserManager;
import com.gov.culturems.common.base.BaseActivity;
import com.gov.culturems.utils.SharePreferUtil;

/**
 * Created by peter on 6/23/15.
 */
public class SplashActivity extends Activity {

    private static final int delayTime = 2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        VersionController.setVersion(SharePreferUtil.getIntDataFromSharePreference(VersionController.VERSION_KEY));
        LinearLayout outerLayout = (LinearLayout) findViewById(R.id.outer_layout);
        outerLayout.setBackgroundResource(VersionController.getDrawable(VersionController.WELCOME_PAGE));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (UserManager.getInstance().isLogin()) {
//                    Intent intent = new Intent(SplashActivity.this, FactoryChooseActivity.class);
                    Intent intent = new Intent(SplashActivity.this, DryingRoomActivity.class);
//                    Intent intent = new Intent(SplashActivity.this, ChooseActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

                } else {
                    Intent intent = null;
                    if (VersionController.CURRENT_VERSION == VersionController.GONGWANGFU) {
                        intent = new Intent(SplashActivity.this, LoginActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, FactoryChooseActivity.class);
                    }
//                    Intent intent = new Intent(SplashActivity.this, ChooseActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                }
                finish();
            }
        }, delayTime);
    }


}
