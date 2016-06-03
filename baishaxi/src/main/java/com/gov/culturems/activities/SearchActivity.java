package com.gov.culturems.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.gov.culturems.R;
import com.gov.culturems.adapters.DryingRoomAdapter;
import com.gov.culturems.common.base.MyBaseAdapter;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.provider.MySuggestionProvider;
import com.gov.culturems.utils.UIUtil;
import com.gov.culturems.views.SearchGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 2015/11/7.
 */
public class SearchActivity extends Activity implements View.OnClickListener {

    public static final int TAB_TYPE = 100;
    public static final int TAB_STATUS = 101;

    private int currentTab;
    private Button timeBtn, typeBtn;
    private TextView searchTextHint;


    private List<DryingRoom> allRooms;
    private List<DryingRoom> searchData;
    private SearchGridView searchGridView;

    private GridView dryingRoomGrid;
    private DryingRoomAdapter dryingRoomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle("查询");

        initViews();
    }

    private void initViews() {
        allRooms = (List<DryingRoom>) getIntent().getSerializableExtra("allRooms");
        timeBtn = (Button) findViewById(R.id.search_by_time);
        timeBtn.setSelected(true);
        timeBtn.setOnClickListener(this);
        typeBtn = (Button) findViewById(R.id.search_by_type);
        typeBtn.setSelected(false);
        typeBtn.setOnClickListener(this);
        currentTab = TAB_TYPE;

        searchTextHint = (TextView) findViewById(R.id.search_hint);

        dryingRoomGrid = (GridView) findViewById(R.id.drying_room_grid);
        dryingRoomAdapter = new DryingRoomAdapter(allRooms, this);
        dryingRoomGrid.setAdapter(dryingRoomAdapter);

        searchGridView = (SearchGridView) findViewById(R.id.search_grid_view);
        searchGridView.refreshViewsByData(getSearchConditions(allRooms));
        searchGridView.setOnGridClickedListener(new SearchGridView.OnGridClickedListener() {
            @Override
            public void onGridClicked(String query) {
                refreshGridViewByQuery(query);
            }
        });
    }

    private void refreshGridViewByQuery(String query) {
        if (allRooms == null || allRooms.size() == 0 || TextUtils.isEmpty(query))
            return;

        if (MySuggestionProvider.ALL.equals(query)) {
            dryingRoomAdapter.setData(allRooms);
            dryingRoomAdapter.notifyDataSetChanged();
            return;
        }

        if (searchData == null) {
            searchData = new ArrayList<>();
        } else {
            searchData.clear();
        }

        for (DryingRoom temp : allRooms) {
            if (temp.hasQueryCondition(query)) {
                searchData.add(temp);
            }
        }
        dryingRoomAdapter.setData(searchData);
        dryingRoomAdapter.notifyDataSetChanged();
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
            }
        }
        return searchConditionList;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    private void refreshViewByTab() {
        if (currentTab == TAB_TYPE) {
            searchTextHint.setText("黑茶类别选择");
        } else {
            searchTextHint.setText("运行状态选择");
        }
        refreshGridViewByQuery(MySuggestionProvider.ALL);
        searchGridView.refreshViewsByData(getSearchConditions(allRooms));
    }

    private class SearchConditionGridAdapter extends MyBaseAdapter<String> {

        public SearchConditionGridAdapter(List<String> data, Context context) {
            super(data, context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(SearchActivity.this);
            textView.setText(data.get(position));
            textView.setBackgroundColor(Color.WHITE);
            textView.setPadding(10, 10, 10, 10);
            textView.setHeight(UIUtil.dip2px(SearchActivity.this, 40));
            textView.setGravity(Gravity.CENTER);
            return textView;
        }
    }


}
