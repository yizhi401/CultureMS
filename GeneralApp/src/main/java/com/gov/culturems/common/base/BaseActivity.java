package com.gov.culturems.common.base;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
        int resId = VersionController.getDrawable(VersionController.TITLE_BG);
        if(VersionController.CURRENT_VERSION == VersionController.GENERAL){
            return getResources().getDrawable(resId);
        }else if(VersionController.CURRENT_VERSION == VersionController.TEACORP){
            return getResources().getDrawable(resId);
        }else if(VersionController.CURRENT_VERSION == VersionController.GONGWANGFU){
            return new ColorDrawable(getResources().getColor(resId));
        }
        return null;
    }
}
