package com.gov.culturems.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.entities.BaseDevice;
import com.gov.culturems.entities.DCDevice;
import com.gov.culturems.entities.DryingRoom;
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

    private DryingRoom dryingRoom;
    private BaseDevice device;
    private ChooseDateView chooseDateView;

    private ChartFragment chartFragment;
    private TableFragment tableFragment;

    private VerticalViewPager verticalViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_data_activity);

        getDryingRoomInfo();
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setTitle(dryingRoom.getName());
            getActionBar().setBackgroundDrawable(getDrawableByVersion());
        }

        initViews();
    }

    public interface DeviceDataListener {
        void onDataChanged(DateTime changedDate);
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
    private void getDryingRoomInfo() {
        dryingRoom = DryingRoomHelper.getInstance().getDryingRoom();
        device = DryingRoomHelper.getInstance().getDevice();
    }

    public BaseDevice getCurrentDevice() {
        return device;
    }

    private void initViews() {
        chooseDateView = (ChooseDateView) findViewById(R.id.choose_date);
        chooseDateView.setViewType(ChooseDateView.TYPE_DAY);

        chartFragment = ChartFragment.newInstance(dryingRoom);
        chartFragment.setDeviceDataActivity(this);
        tableFragment = TableFragment.newInstance(device);
        tableFragment.setChooseDateView(chooseDateView);

        verticalViewPager = (VerticalViewPager) findViewById(R.id.verticalviewpager);
        initVerticalViewPager();

        chooseDateView.setDateChangeListener(new ChooseDateView.OnDateChangeListener() {
            @Override
            public void onDateChange(DateTime dateTime, String monthStr) {
                chartFragment.onDataChanged(dateTime);
                tableFragment.onDataChanged(dateTime);
            }
        });

        chartFragment.setCurrentDate(chooseDateView.getDateTime());
        tableFragment.setCurrentDate(chooseDateView.getDateTime());
    }

    private void initVerticalViewPager() {
        verticalViewPager.setAdapter(new DummyAdapter(getSupportFragmentManager()));
        verticalViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.pagemargin));
        verticalViewPager.setPageMarginDrawable(new ColorDrawable(getResources().getColor(VersionController.getMainColor())));
        verticalViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    chooseDateView.isTimeViewShow(false);
                    chooseDateView.setCanChoseData(true);
                } else {
                    chooseDateView.isTimeViewShow(true);
                    chooseDateView.setCanChoseData(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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


    private boolean showFanControl() {
//        if (UserManager.getInstance().getUserType() == User.USER_TYPE_MANAGER
//                || UserManager.getInstance().getUserType() == User.USER_TYPE_SUPER_MANAGER) {
        if (device instanceof DCDevice) {
            return true;
        }
//        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (showFanControl()) {
//            MenuItem menuItem = menu.add(0, MENU_INFO, 0, "设备信息");
//            menuItem.setIcon(R.drawable.setting_icon);
//            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        }
        MenuItem itemMore = menu.add(0, R.id.menu_more, 0, "更多");
        itemMore.setIcon(R.drawable.menu_more);
        itemMore.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
            return true;
        } else if (item.getItemId() == MENU_INFO) {
           return true;
        } else if (item.getItemId() == R.id.menu_more) {
            showMoreMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMoreMenu() {
        View popupMenuView = LayoutInflater.from(this).inflate(R.layout.device_data_popup_menu, null);
        final PopupWindow menuPopup =
                new PopupWindow(popupMenuView, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, true);
        menuPopup.setTouchable(true);
        menuPopup.setOutsideTouchable(true);
        menuPopup.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        menuPopup.showAsDropDown(findViewById(R.id.menu_more));
        menuPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
        Button ruleManageBtn = (Button) popupMenuView.findViewById(R.id.rule_manage);
        ruleManageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToRuleManageActivity();
                menuPopup.dismiss();
            }
        });
        if(showFanControl()){
            ruleManageBtn.setVisibility(View.VISIBLE);
        }else{
            ruleManageBtn.setVisibility(View.GONE);
        }
        Button goodsManageBtn= (Button) popupMenuView.findViewById(R.id.goods_manage);
        goodsManageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToGoodsManageActivity();
                menuPopup.dismiss();
            }
        });
   }

    private void jumpToRuleManageActivity() {
            Intent intent = new Intent(DeviceDataActivity.this, FanControlActivity.class);
            DCDevice dcDevice = null;
            for (BaseDevice temp : dryingRoom.getDeviceDatas()) {
                if (temp.getUseType().equals(BaseDevice.USE_TYPE_CK) &&
                        temp instanceof DCDevice &&
                        temp.getSensorTypes() != null) {
                    dcDevice = (DCDevice) temp;
                    break;
                }
            }
            if (dcDevice != null) {
                intent.putExtra("dc_device", dcDevice);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            } else {
                Toast.makeText(this, "未找到测控设备", Toast.LENGTH_SHORT).show();
            }
    }

    private void jumpToGoodsManageActivity(){
        Intent i = new Intent(this,GoodsManageActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

    }

    @Override
    public void onClick(View v) {
    }

}
