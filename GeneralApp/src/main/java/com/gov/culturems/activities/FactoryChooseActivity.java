package com.gov.culturems.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.common.base.BaseActivity;
import com.gov.culturems.common.base.MyBaseAdapter;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.common.http.response.SceneResp;
import com.gov.culturems.entities.TeaFactory;
import com.gov.culturems.provider.MySuggestionProvider;
import com.gov.culturems.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录前，用户选择所属工厂的页面
 * Created by peter on 3/25/16.
 */
public class FactoryChooseActivity extends Activity {

    private boolean doubleBackToExitPressedOnce = false;

    private RelativeLayout outerLayout;
    private ListView factoryListView;
    private SearchView searchView;
    private ChooseFactoryAdapter adapter;
    private List<TeaFactory> fullData;
    private List<TeaFactory> searchData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_factory_activity);
        setUpViews();
        getFactoryList();
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
        factoryListView = (ListView) findViewById(R.id.factory_list);
        factoryListView.setDividerHeight(0);
        factoryListView.setDivider(null);
        factoryListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
        adapter = new ChooseFactoryAdapter(fullData, this);
        factoryListView.setAdapter(adapter);

        factoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TeaFactory teaFactory = adapter.getData().get(position);
                popConfirmDialog(teaFactory);
            }
        });
        outerLayout = (RelativeLayout) findViewById(R.id.outer_layout);
        outerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInputKeyboard();
            }
        });
        TextView chooseHint = (TextView)findViewById(R.id.choose_hint);
        chooseHint.setTextColor(getResources().getColor(VersionController.getDrawable(VersionController.THEME_COLOR)));
    }

    private void popConfirmDialog(final TeaFactory teaFactory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示").setMessage("确定选择" + teaFactory.getName() + "吗？").setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(FactoryChooseActivity.this, LoginActivity.class);
                intent.putExtra("factory", teaFactory);
                startActivity(intent);
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                finish();
            }
        });
        builder.show();
    }

    private void hideSoftInputKeyboard() {
        if (outerLayout != null) {
            outerLayout.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(outerLayout.getWindowToken(), 0);
        }
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        hideSoftInputKeyboard();
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        hideSoftInputKeyboard();
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

        for (TeaFactory temp : fullData) {
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


    private void getFactoryList() {
        RequestParams params = new RequestParams();
        params.put("pi", 1);
        params.put("ps", 999);

        params.put("ParentSceneId", "0");
        HttpUtil.jsonRequestGet(this, URLRequest.SCENES_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                ListResponse<SceneResp> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<SceneResp>>() {
                });

                if (listResponse.getRc() == 200) {
                    fullData = SceneResp.convertToFactoryList(listResponse.getListData());
                    adapter.setData(fullData);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNetError(VolleyError error) {
            }
        });

    }

    private class ChooseFactoryAdapter extends MyBaseAdapter<TeaFactory> {

        public ChooseFactoryAdapter(List<TeaFactory> data, Context context) {
            super(data, context);
        }

        class Holder {
            TextView sceneName;
            ImageView pointer;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(FactoryChooseActivity.this).inflate(R.layout.choose_factory_list_item, null);
                holder = new Holder();
                holder.sceneName = (TextView) convertView.findViewById(R.id.name);
                holder.pointer = (ImageView) convertView.findViewById(R.id.pointer);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            TeaFactory teaFactory = data.get(position);
            holder.sceneName.setText(teaFactory.getName());
            return convertView;
        }
    }

}
