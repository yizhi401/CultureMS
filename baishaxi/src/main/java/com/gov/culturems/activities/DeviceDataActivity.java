package com.gov.culturems.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.entities.DeviceInfo;
import com.gov.culturems.fragments.ChartFragment;
import com.gov.culturems.fragments.TableFragment;
import com.gov.culturems.views.ChooseDateView;
import com.gov.culturems.views.VerticalViewPager;

import hirondelle.date4j.DateTime;

/**
 * 单一设备的所有sensor信息
 * Created by peter on 6/11/15.
 */
public class DeviceDataActivity extends FragmentActivity implements View.OnClickListener {

    private static final int MENU_INFO = 1001;
    private static final int MENU_FAN = 1002;
    private static final float MIN_SCALE = 0.75f;
    private static final float MIN_ALPHA = 0.75f;

    public static final int TAB_DAY = 0;
    public static final int TAB_WEEK = 1;

    private int currentTab;
    private Button dayBtn, weekBtn;
    private DeviceInfo deviceInfo;
    private ChooseDateView chooseDateView;

    private ChartFragment chartFragment;
    private TableFragment tableFragment;

    private VerticalViewPager verticalViewPager;

//    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_data_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device_data");

        getActionBar().setTitle(deviceInfo.getDeviceName());
        if (checkDeviceValid()) {
            initViews();
        } else {
            finish();
        }
    }

    public interface DeviceDataListener {
        void onDataChanged(DateTime changedDate);

        void onTabChanged(int tabType);
    }


    /**
     * 检查这个device是否不为空、有感受器
     *
     * @return
     */
    private boolean checkDeviceValid() {
        if (deviceInfo == null) {
            return false;
        }
        if (deviceInfo.getSensorTypes() == null || deviceInfo.getSensorTypes().size() == 0) {
            //这个device没有sensor，怎么可能呢？
            //还真有，楼上
            Toast.makeText(this, "设备暂无数据", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void initViews() {
        if (deviceInfo.getSensorTypes().size() != 1) {
            chartFragment = ChartFragment.newInstance(deviceInfo);
            chartFragment.setDeviceDataActivity(this);
        }
        tableFragment = TableFragment.newInstance(deviceInfo);

        chooseDateView = (ChooseDateView) findViewById(R.id.choose_date);
        chooseDateView.setViewType(ChooseDateView.TYPE_DAY);
        dayBtn = (Button) findViewById(R.id.day_btn);
        dayBtn.setOnClickListener(this);
        dayBtn.setSelected(true);
        weekBtn = (Button) findViewById(R.id.week_btn);
        weekBtn.setOnClickListener(this);

        verticalViewPager = (VerticalViewPager) findViewById(R.id.verticalviewpager);
        initVerticalViewPager();

        currentTab = TAB_DAY;

        chooseDateView.setDateChangeListener(new ChooseDateView.OnDateChangeListener() {
            @Override
            public void onDateChange(DateTime dateTime, String monthStr) {
                if (chartFragment != null)
                    chartFragment.onDataChanged(dateTime);
                tableFragment.onDataChanged(dateTime);
            }
        });

        if (chartFragment != null)
            chartFragment.setCurrentDate(chooseDateView.getDateTime());
        tableFragment.setCurrentDate(chooseDateView.getDateTime());
    }

//    public void onDateChange(DateTime dateTime, String monthStr) {
//        if (chartFragment != null)
//            chartFragment.onDataChanged(dateTime);
//        tableFragment.onDataChanged(dateTime);
//    }

    private void initVerticalViewPager() {
        verticalViewPager.setAdapter(new DummyAdapter(getSupportFragmentManager()));
        verticalViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.pagemargin));
        verticalViewPager.setPageMarginDrawable(new ColorDrawable(getResources().getColor(VersionController.getMainColor())));

        verticalViewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View view, float position) {
                int pageWidth = view.getWidth();
                int pageHeight = view.getHeight();

                if (position < -1) { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    view.setAlpha(0);

                } else if (position <= 1) { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                    float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                    float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                    if (position < 0) {
                        view.setTranslationY(vertMargin - horzMargin / 2);
                    } else {
                        view.setTranslationY(-vertMargin + horzMargin / 2);
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);

                    // Fade the page relative to its size.
                    view.setAlpha(MIN_ALPHA +
                            (scaleFactor - MIN_SCALE) /
                                    (1 - MIN_SCALE) * (1 - MIN_ALPHA));

                } else { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    view.setAlpha(0);
                }
            }
        });
    }

    public class DummyAdapter extends FragmentPagerAdapter {

        public DummyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (chartFragment == null) {
                return tableFragment;
            }
            switch (position) {
                case 0:
                    return chartFragment;
                case 1:
                    return tableFragment;
                default:
                    break;

            }
            return chartFragment;
        }

        @Override
        public int getCount() {
            if (chartFragment == null) {
                return 1;
            }
            return 2;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(0, MENU_INFO, 0, "设备信息");
        menuItem.setIcon(R.drawable.info_icon);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == MENU_INFO) {
            Intent intent = new Intent(DeviceDataActivity.this, DeviceInfoActivity.class);
            if (deviceInfo != null) {
                intent.putExtra("device", deviceInfo);
            }
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.day_btn:
                if (currentTab != TAB_DAY) {
                    currentTab = TAB_DAY;
                    tableFragment.onTabChanged(TAB_DAY);
                    if (chartFragment != null)
                        chartFragment.onTabChanged(TAB_DAY);
                    dayBtn.setSelected(true);
                    weekBtn.setSelected(false);
                    chooseDateView.setViewType(ChooseDateView.TYPE_DAY);
                }
                break;
            case R.id.week_btn:
                if (currentTab != TAB_WEEK) {
                    currentTab = TAB_WEEK;
                    tableFragment.onTabChanged(TAB_WEEK);
                    if (chartFragment != null)
                        chartFragment.onTabChanged(TAB_WEEK);
                    weekBtn.setSelected(true);
                    dayBtn.setSelected(false);
                    chooseDateView.setViewType(ChooseDateView.TYPE_WEEK);
                }
                break;
            default:
                break;
        }
    }


}
