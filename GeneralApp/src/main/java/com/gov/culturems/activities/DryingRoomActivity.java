package com.gov.culturems.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.adapters.DryingRoomAdapter;
import com.gov.culturems.common.UserManager;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.common.http.response.DryingRoomResp;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.provider.MySuggestionProvider;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.LogUtil;
import com.gov.culturems.views.SearchGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 2015/11/7.
 */
public class DryingRoomActivity extends Activity {

    private static final int MENU_LOG_OUT = 1002;
    private static final int MENU_ITEM_SEARCH = 1000;
    private static final int MENU_ITEM_CLEAR = 1001;
    private static final int MENU_ITEM_MORE = 1003;

    public static final int TAB_TYPE = 100;
    public static final int TAB_STATUS = 101;

    public static final int REQUEST_CODE = 1000;
    public static final int RESULT_NEED_REFRESH = 1100;

    private int currentTab;
    private Button timeBtn, typeBtn;
    private TextView searchTextHint;
    private SearchGridView searchGridView;
    private PopupWindow mPopupWindow;

    private boolean doubleBackToExitPressedOnce = false;

    private RelativeLayout outerLayout;
    private ListView dryingRoomListView;
    private SearchView searchView;
    private DryingRoomAdapter adapter;
    private List<DryingRoom> fullData;
    private List<DryingRoom> searchData;
    private RelativeLayout dimLayout;
    private SwipeRefreshLayout swipeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        setContentView(R.layout.drying_room_activity);

        // Associate searchable configuration with the SearchView
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setTitle(UserManager.getInstance().getFactoryName());
        }

        setUpViews();
        initPopupWindow();
        getDryingRooms();
    }


    @Override
    protected void onResume() {
        super.onResume();
        hideSoftInputKeyboard();
//        getDryingRooms();
    }


    private void getDryingRooms() {
        if (!swipeLayout.isRefreshing()) {
//            UIUtil.showTipDialog(this, CommonConstant.DIALOG_TYPE_WAITING, "正在请求数据...");
        }

        RequestParams params = new RequestParams();
        HttpUtil.jsonRequest(this, URLRequest.SCENE_GOODS_LIST_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
//                UIUtil.dismissTipDialog(DryingRoomActivity.this);
                swipeLayout.setRefreshing(false);
                ListResponse<DryingRoomResp> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<DryingRoomResp>>() {
                });
                if (listResponse.getRc() == 200) {
                    if (listResponse.getListData() != null && listResponse.getListData().size() > 0) {
                        fullData = DryingRoomResp.convertToDryingRoomList(listResponse.getListData());
                        adapter.setData(fullData);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNetError(VolleyError error) {
//                UIUtil.dismissTipDialog(DryingRoomActivity.this);
                swipeLayout.setRefreshing(false);
            }
        });
    }

    private void setUpViews() {
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryRefinementEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                refreshGridViewByQuery(newText);
                return false;
            }
        });
        dryingRoomListView = (ListView) findViewById(R.id.drying_room_grid);
        dryingRoomListView.setDividerHeight(0);
        dryingRoomListView.setDivider(null);
        dryingRoomListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState != 0) {
                    hideSoftInputKeyboard();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        fullData = new ArrayList<>();
        adapter = new DryingRoomAdapter(fullData, this);
        dryingRoomListView.setAdapter(adapter);
        dimLayout = (RelativeLayout) findViewById(R.id.bac_dim_layout);

        outerLayout = (RelativeLayout) findViewById(R.id.outer_layout);
        outerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInputKeyboard();
            }
        });

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDryingRooms();
            }
        });
    }

    private void hideSoftInputKeyboard() {
        if (outerLayout != null) {
            outerLayout.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(outerLayout.getWindowToken(), 0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem itemSearch = menu.add(0, R.id.menu_search, 0, "查询");
        itemSearch.setIcon(R.drawable.search_icon);
        itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem itemMore = menu.add(0, R.id.menu_more, 0, "更多");
        itemMore.setIcon(R.drawable.menu_more);
        itemMore.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d("On Activity Result,RequestCode ==" + requestCode + "Result Code ==" + resultCode);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_NEED_REFRESH) {
                getDryingRooms();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        hideSoftInputKeyboard();
        if (item.getItemId() == R.id.menu_search) {
            showSearchPopview();
        } else if (item.getItemId() == R.id.menu_more) {
            showMoreMenu();
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void showMoreMenu() {
        View popupMenuView = LayoutInflater.from(this).inflate(R.layout.drying_room_popup_menu, null);
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
        Button clearRecordBtn = (Button) popupMenuView.findViewById(R.id.clear_record);
        clearRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySuggestionProvider.getInstance().clearHistory();
                menuPopup.dismiss();
            }
        });
        Button logoutBtn = (Button) popupMenuView.findViewById(R.id.logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogOutDialog();
                menuPopup.dismiss();
            }
        });
    }

    private void showLogOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示").setMessage("确定要注销登录吗？").setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("注销", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserManager.getInstance().logout();
                dialog.dismiss();
                startActivity(new Intent(DryingRoomActivity.this, FactoryChooseActivity.class));
                finish();
            }
        });
        builder.show();
    }


    private void initPopupWindow() {
        final View popupView = getLayoutInflater().inflate(R.layout.search_popup_layout, null);

        timeBtn = (Button) popupView.findViewById(R.id.search_by_time);
        timeBtn.setSelected(true);
        timeBtn.setOnClickListener(getSearchButtonOnclickListener());
        typeBtn = (Button) popupView.findViewById(R.id.search_by_type);
        typeBtn.setSelected(false);
        typeBtn.setOnClickListener(getSearchButtonOnclickListener());
        currentTab = TAB_TYPE;

        searchTextHint = (TextView) popupView.findViewById(R.id.search_hint);

        searchGridView = (SearchGridView) popupView.findViewById(R.id.search_grid_view);
        searchGridView.refreshViewsByData(getSearchConditions(fullData));
        searchGridView.setOnGridClickedListener(new SearchGridView.OnGridClickedListener() {
            @Override
            public void onGridClicked(String query) {
                mPopupWindow.dismiss();
                refreshGridViewByQuery(query);
            }
        });


        mPopupWindow = new PopupWindow(popupView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
    }

    private void showSearchPopview() {
        searchGridView.refreshViewsByData(getSearchConditions(fullData));
        mPopupWindow.showAsDropDown(dimLayout, -dimLayout.getWidth(), -dimLayout.getHeight());
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });

    }

    private View.OnClickListener getSearchButtonOnclickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.search_by_time:
                        if (currentTab != TAB_TYPE) {
                            currentTab = TAB_TYPE;
                            timeBtn.setSelected(true);
                            typeBtn.setSelected(false);
                            refreshViewByTab();
                        }
                        break;
                    case R.id.search_by_type:
                        if (currentTab != TAB_STATUS) {
                            currentTab = TAB_STATUS;
                            typeBtn.setSelected(true);
                            timeBtn.setSelected(false);
                            refreshViewByTab();
                        }
                        break;
                    default:
                        break;
                }

            }
        };
    }

    private void refreshViewByTab() {
        if (currentTab == TAB_TYPE) {
            searchTextHint.setText("黑茶类别选择");
        } else {
            searchTextHint.setText("运行状态选择");
        }
        refreshGridViewByQuery(MySuggestionProvider.ALL);
        searchGridView.refreshViewsByData(getSearchConditions(fullData));
    }

    private List<String> getSearchConditions(List<DryingRoom> allRooms) {
        List<String> searchConditionList = new ArrayList<>();
        searchConditionList.add(MySuggestionProvider.ALL);

        if (currentTab == TAB_TYPE) {
            if (allRooms != null) {
                for (DryingRoom temp : allRooms) {
                    if (!TextUtils.isEmpty(temp.getGoodsName()) && !searchConditionList.contains(temp.getGoodsName()))
                        searchConditionList.add(temp.getGoodsName());
                }

            }
        } else {
            if (allRooms != null) {
                for (DryingRoom temp : allRooms) {
                    if (!TextUtils.isEmpty(temp.getGoodsName()) && !searchConditionList.contains(temp.getState()))
                        searchConditionList.add(temp.getState());
                }
                if (!searchConditionList.contains(DryingRoom.STATE_FINISHED)) {
                    searchConditionList.add(DryingRoom.STATE_FINISHED);
                }
                if (!searchConditionList.contains(DryingRoom.STATE_ONGOING)) {
                    searchConditionList.add(DryingRoom.STATE_ONGOING);
                }
            }
        }
        return searchConditionList;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        LogUtil.i("new Intent!");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            hideSoftInputKeyboard();
            String query = intent.getStringExtra(SearchManager.QUERY);
            MySuggestionProvider.getInstance().saveQuery(query);
            refreshGridViewByQuery(query);
        }
        if (intent.getBooleanExtra("Refresh", false)) {
            LogUtil.i("need to refresh!");
            getDryingRooms();
        }
    }

    private void refreshGridViewByQuery(String query) {

        if (fullData == null || fullData.size() == 0)
            return;

        if (MySuggestionProvider.ALL.equals(query) || TextUtils.isEmpty(query)) {
            adapter.setData(fullData);
            adapter.notifyDataSetChanged();
            return;
        }

        if (searchData == null) {
            searchData = new ArrayList<>();
        } else {
            searchData.clear();
        }

        for (DryingRoom temp : fullData) {
            if (temp.query(query)) {
                searchData.add(temp);
            }
        }
        adapter.setData(searchData);
        adapter.notifyDataSetChanged();
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


}
