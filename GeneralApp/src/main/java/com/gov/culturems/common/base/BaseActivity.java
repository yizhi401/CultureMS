package com.gov.culturems.common.base;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.gov.culturems.R;
import com.gov.culturems.VersionController;

import junit.runner.Version;

/**
 * Created by peter on 14/01/2017.
 */

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setBackgroundDrawable(getDrawableByVersion());
    }

    private Drawable getDrawableByVersion() {
        if(VersionController.CURRENT_VERSION == VersionController.GENERAL){
            return getResources().getDrawable(R.drawable.title_bg_blue);
        }else if(VersionController.CURRENT_VERSION == VersionController.TEACORP){
            return getResources().getDrawable(R.drawable.title_bg_green);
        }else if(VersionController.CURRENT_VERSION == VersionController.GONGWANGFU){
            return new ColorDrawable(getResources().getColor(R.color.theme_color_red));
        }
        return null;
    }
}
