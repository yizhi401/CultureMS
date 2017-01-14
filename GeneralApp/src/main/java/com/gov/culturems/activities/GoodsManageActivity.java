package com.gov.culturems.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.common.CommonConstant;
import com.gov.culturems.common.base.BaseActivity;
import com.gov.culturems.common.http.CommonResponse;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.entities.Goods;
import com.gov.culturems.utils.GsonUtils;
import com.gov.culturems.utils.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class GoodsManageActivity extends BaseActivity implements View.OnClickListener {

    private TextView goodsText;
    private EditText remarkEdit;
    private Button startBtn;
    private Button endBtn;

    private List<Goods> goodsList = new ArrayList<>();

    private DryingRoom dryingRoom;
    private Goods chosenGoods;

    private LinearLayout outerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_manage);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getGoodsList();
        dryingRoom = DryingRoomHelper.getInstance().getDryingRoom();
        getActionBar().setTitle(dryingRoom.getName());
        initViews();
    }

    private void getGoodsList() {
        RequestParams params = new RequestParams();
        HttpUtil.jsonRequestGet(this, URLRequest.GOODS_LIST_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                ListResponse<Goods> result = GsonUtils.fromJson(response, new TypeToken<ListResponse<Goods>>() {
                });
                if (result.getRc() == 200 && result.getListData() != null) {
                    goodsList.addAll(result.getListData());
                    //设置一个默认值
                    if (!isBaking()) {
                        if (TextUtils.isEmpty(goodsText.getText().toString()) ||
                                goodsText.getText().toString().equals(getResources().getString(R.string.choose_goods))) {
                            if (goodsList.size() > 0) {
                                goodsText.setText(goodsList.get(0).GoodsName);
                                chosenGoods = goodsList.get(0);
                            }

                        }
                    }
                }

            }

            @Override
            public void onNetError(VolleyError error) {
                Toast.makeText(GoodsManageActivity.this, "网络错误", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void stopBaking() {
        RequestParams params = new RequestParams();
        params.put("SGId", dryingRoom.getSGId());
        params.put("Memo", "");
        params.putWithoutFilter("EndTime", getCurrentTimestampGet());
        UIUtil.showTipDialog(this, CommonConstant.DIALOG_TYPE_WAITING, "正常请求...");
        HttpUtil.jsonRequestGet(this, URLRequest.SCENE_GOODS_END, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                CommonResponse commonResponse = GsonUtils.fromJson(response, CommonResponse.class);
                if (commonResponse.getRc() == 200) {
                    Toast.makeText(GoodsManageActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    Toast.makeText(GoodsManageActivity.this, commonResponse.getRm(), Toast.LENGTH_SHORT).show();
                }
                UIUtil.dismissTipDialog(GoodsManageActivity.this);

            }

            @Override
            public void onNetError(VolleyError error) {
                UIUtil.dismissTipDialog(GoodsManageActivity.this);
                Toast.makeText(GoodsManageActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void startMainActivity() {
        Intent intent = new Intent(this, DryingRoomActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("Refresh", true);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    private void startBaking() {
        if (chosenGoods == null) {
            Toast.makeText(this, "请选择茶品类型", Toast.LENGTH_SHORT).show();
            return;
        }
        UIUtil.showTipDialog(this, CommonConstant.DIALOG_TYPE_WAITING, "正常请求...");
        RequestParams params = new RequestParams();
        params.put("SGId", "");
        params.put("GoodsId", chosenGoods.GoodsId);
        params.put("SceneId", dryingRoom.getId());
        params.put("Memo", remarkEdit.getText().toString());
        params.putWithoutFilter("BeginTime", getCurrentTimestampPost());
        params.putWithoutFilter("EndTime", "");
        HttpUtil.jsonRequest(this, URLRequest.SCENE_GOODS_START, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                CommonResponse commonResponse = GsonUtils.fromJson(response, CommonResponse.class);
                if (commonResponse.getRc() == 200) {
                    Toast.makeText(GoodsManageActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    Toast.makeText(GoodsManageActivity.this, commonResponse.getRm(), Toast.LENGTH_SHORT).show();
                }
                UIUtil.dismissTipDialog(GoodsManageActivity.this);

            }

            @Override
            public void onNetError(VolleyError error) {
                Toast.makeText(GoodsManageActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                UIUtil.dismissTipDialog(GoodsManageActivity.this);
            }
        });
    }

    private String getCurrentTimestampGet() {
        DateTime currentDate = DateTime.now(TimeZone.getTimeZone("Asia/Shanghai"));
        String dateStr = currentDate.format("YYYY-MM-DD hh:mm:ss");
        return dateStr.replace(" ", "%20");
    }

    private String getCurrentTimestampPost() {
        DateTime currentDate = DateTime.now(TimeZone.getTimeZone("Asia/Shanghai"));
        String dateStr = currentDate.format("YYYY-MM-DD hh:mm:ss");
        return dateStr;
    }


    private void initViews() {
        goodsText = (TextView) findViewById(R.id.goods);
        remarkEdit = (EditText) findViewById(R.id.remark);
        remarkEdit.setText(dryingRoom.getMemo());
        startBtn = (Button) findViewById(R.id.start_btn);
        startBtn.setOnClickListener(this);
        endBtn = (Button) findViewById(R.id.stop_btn);
        endBtn.setOnClickListener(this);
        outerLayout = (LinearLayout) findViewById(R.id.outer_layout);
        outerLayout.setOnClickListener(this);

        refreshViewByBaking();
    }

    private void refreshViewByBaking() {
        if (isBaking()) {
            startBtn.setEnabled(false);
            endBtn.setEnabled(true);
            remarkEdit.setEnabled(false);
            if (!TextUtils.isEmpty(dryingRoom.getMemo())) {
                remarkEdit.setText(dryingRoom.getMemo());
            }
            goodsText.setOnClickListener(null);
            goodsText.setText(dryingRoom.getGoodsName());
        } else {
            startBtn.setEnabled(true);
            endBtn.setEnabled(false);
            remarkEdit.setEnabled(true);
            goodsText.setOnClickListener(getChoseGoodsClickListener());
            goodsText.setText(getResources().getString(R.string.choose_goods));
        }
    }


    private View.OnClickListener getChoseGoodsClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goodsList.size() == 0) {
                    getGoodsList();
                } else {
                    showChoseGoodsDialog();
                }
            }
        };
    }

    private void showChoseGoodsDialog() {
        PopupMenu popupMenu = new PopupMenu(this, goodsText);
        for (Goods temp : goodsList) {
            popupMenu.getMenu().add(temp.GoodsName);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                for (Goods temp : goodsList) {
                    if (item.getTitle().equals(temp.GoodsName)) {
                        chosenGoods = temp;
                        goodsText.setText(item.getTitle());
                        break;
                    }
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private String[] getGoodsArr() {
        String[] arr = new String[goodsList.size()];
        for (int i = 0; i < goodsList.size(); i++) {
            arr[i] = goodsList.get(i).GoodsName;
        }
        return arr;
    }


    private boolean isBaking() {
        return DryingRoom.STATE_ONGOING.equals(dryingRoom.getState());
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
            case R.id.start_btn:
                startBaking();
                break;
            case R.id.stop_btn:
                stopBaking();
                break;
            case R.id.outer_layout:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            default:
                break;
        }
    }
}
