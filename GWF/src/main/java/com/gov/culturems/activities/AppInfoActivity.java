package com.gov.culturems.activities;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.gov.culturems.R;
import com.gov.culturems.VersionController;

public class AppInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle("版本信息");

        setContentView(R.layout.activity_app_info);

        initViews();


        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(packageInfo != null){
            TextView version = (TextView)findViewById(R.id.version);
            version.setText(packageInfo.versionName);
        }
    }

    private void initViews(){
        ImageView imageIC = (ImageView)findViewById(R.id.icon);
        ImageView imageAndroid = (ImageView)findViewById(R.id.android_qr_img);
        ImageView imageIos = (ImageView)findViewById(R.id.ios_qr_img);
        imageIC.setImageResource(VersionController.getDrawable(VersionController.LAUNCHER));
        imageAndroid.setImageResource(VersionController.getDrawable(VersionController.ANDROID_QRCODE));
        imageIos.setImageResource(VersionController.getDrawable(VersionController.IOS_QRCODE));
        TextView title = (TextView)findViewById(R.id.title);
        title.setText(getResources().getString(R.string.app_name));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
