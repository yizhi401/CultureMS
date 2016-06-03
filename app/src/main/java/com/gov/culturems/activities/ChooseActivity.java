package com.gov.culturems.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.R;
import com.gov.culturems.VersionController;
import com.gov.culturems.common.UserManager;
import com.gov.culturems.common.base.MyBaseAdapter;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLConstant;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.Scene;
import com.gov.culturems.entities.User;
import com.gov.culturems.utils.GsonUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 6/9/15.
 */
public class ChooseActivity extends Activity {

    private static final int MENU_LOG_OUT = 1000;

    private boolean doubleBackToExitPressedOnce = false;
    private ListView listView;
    private ChooseSceneAdapter sceneAdapter;
    private Map<String, List<Scene>> sceneData;
    private LinkedList<Scene> selectedSceneList;//当成一个栈使用,纪录了用户打开场景的路径
    private Scene selectedScene;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle("选择场景");
        listView = (ListView) findViewById(R.id.listview);
        sceneAdapter = new ChooseSceneAdapter(null, this);
        sceneData = new HashMap<>();
        selectedSceneList = new LinkedList<>();
        listView.setAdapter(sceneAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedScene = sceneAdapter.getData().get(position);
                sceneAdapter.notifyDataSetChanged();
                if (Integer.valueOf(selectedScene.getSubSceneCount()) > 0) {
                    //有子场景
                    //点击一次，进入更深一级，入栈
                    selectedSceneList.addLast(selectedScene);
                    if (sceneData.containsKey(selectedScene.getSceneId())) {
                        //已经获取过这个scene的子scene了
                        sceneAdapter.setData(sceneData.get(selectedScene.getSceneId()));
                        sceneAdapter.notifyDataSetChanged();
                    } else {
                        //尝试去服务器获取子Scene
                        getScenList(selectedScene.getSceneId());
                    }
                } else {
                    //没有子场景
                    Intent intent = new Intent(ChooseActivity.this, SceneActivity.class);
                    intent.putExtra("scene", selectedScene);//TODO 选中的场景
                    startActivity(intent);
                }

            }
        });
        getScenList("0");//传0代表获取根scene
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(0, MENU_LOG_OUT, 0, "切换账号");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackPressed();
            return true;
        } else if (item.getItemId() == MENU_LOG_OUT) {
            showLogOutDialog();
        }
        return super.onOptionsItemSelected(item);
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
                startActivity(new Intent(ChooseActivity.this, LoginActivity.class));
                finish();
            }
        });
        builder.show();
    }

    private void getScenList(final String parentSceneId) {
        RequestParams params = new RequestParams();
        params.put("pi", 1);
        params.put("ps", 999);
        if (VersionController.CURRENT_VERSION == VersionController.GONGWANGFU) {
            if (!"0".equals(parentSceneId)) {
                params.put("ParentSceneId", parentSceneId);
            } else {
                if (UserManager.getInstance().getUserType() == User.USER_TYPE_MANAGER) {
                    params.put("ParentSceneId", parentSceneId);
                } else if (UserManager.getInstance().getUserType() == User.USER_TYPE_NORMAL) {
                    //don't add anything here
                } else {
                    //don't add anything here
                }
            }
        } else {
            params.put("ParentSceneId", parentSceneId);
        }
        HttpUtil.jsonRequestGet(this, URLConstant.SCENES_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                ListResponse<Scene> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<Scene>>() {
                });

                if (listResponse.getRc() == 200) {
                    if (listResponse.getListData() == null || listResponse.getListData().size() == 0) {
                        //最后一级目录了
                        if (selectedScene != null) {
                            //因为是最后一级目录，点击过后并未进入更深一层，所以把之前入栈的数据弹出
                            selectedSceneList.removeLast();
                            Intent intent = new Intent(ChooseActivity.this, SceneActivity.class);
                            intent.putExtra("scene", selectedScene);//TODO 选中的场景
                            startActivity(intent);
                        }
                    } else {
                        //每一个子菜单列表，key都是这个子菜单父目录的id
                        sceneData.put(parentSceneId, listResponse.getListData());
                        //进入一级子菜单，默认选中第一个
                        sceneAdapter.setData(listResponse.getListData());
                        sceneAdapter.notifyDataSetChanged();

                    }
                }
            }

            @Override
            public void onNetError(VolleyError error) {

            }
        });
    }


    private class ChooseSceneAdapter extends MyBaseAdapter<Scene> {

        public ChooseSceneAdapter(List<Scene> data, Context context) {
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
                convertView = LayoutInflater.from(ChooseActivity.this).inflate(R.layout.choose_activity_list_item, null);
                holder = new Holder();
                holder.sceneName = (TextView) convertView.findViewById(R.id.name);
                holder.pointer = (ImageView) convertView.findViewById(R.id.pointer);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            Scene scene = data.get(position);
            if (selectedScene != null) {
                if (scene.getSceneId().equals(selectedScene.getSceneId())) {
                    holder.sceneName.setTextColor(getResources().getColor(VersionController.getMainColor()));
                } else {
                    holder.sceneName.setTextColor(getResources().getColor(R.color.black));
                }
                holder.sceneName.setCompoundDrawablesRelativeWithIntrinsicBounds(VersionController.getDrawable(VersionController.ICON_SCENE), 0, 0, 0);
            }
            holder.sceneName.setText(scene.getSceneName());
            return convertView;

        }
    }

    private void handleBackPressed() {
        //当前层级下的第一个
        /**
         Scene currentScene = sceneAdapter.getData().get(0);
         if (TextUtils.isEmpty(currentScene.getParentSceneId()) ||
         currentScene.getParentSceneId().equals("0")) {
         //            已经到了最上级菜单
         quitApplication();
         } else {
         **/
        if (selectedSceneList == null || selectedSceneList.size() == 0) {
            //当前未选中任何场景，也就是说明是最上一层的场景
            //这种情况的发生，是因为普通用户最上的场景不一定是父场景id未空或者0的场景
            quitApplication();
        } else {
            //点击一次返回键，出栈一次，直到栈顶，结束activity
            Scene parentScene = selectedSceneList.removeLast();
            if (sceneData.containsKey(parentScene.getParentSceneId())) {
                //回到上一级菜单
                sceneAdapter.setData(sceneData.get(parentScene.getParentSceneId()));
                sceneAdapter.notifyDataSetChanged();
            } else {
                quitApplication();
            }
        }
    }


    private void quitApplication() {
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

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

}
