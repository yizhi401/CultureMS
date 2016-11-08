package com.gov.culturems.activities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.gov.culturems.MyApplication;
import com.gov.culturems.common.http.HttpUtil;
import com.gov.culturems.common.http.ListResponse;
import com.gov.culturems.common.http.RequestParams;
import com.gov.culturems.common.http.URLRequest;
import com.gov.culturems.common.http.VolleyRequestListener;
import com.gov.culturems.entities.BaseDevice;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.utils.GsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * 展示表格页面的HttpHelper，因为要从不同的地方调用
 * Created by peter on 2016/4/28.
 */
public class DryingRoomHelper {
    private static DryingRoomHelper instance = new DryingRoomHelper();

    private DryingRoom dryingRoom;
    private BaseDevice device;
    private LinkedList<TableValues> tableValueList;

    private DryingRoomHelper() {
    }

    public static DryingRoomHelper getInstance() {
        return instance;
    }

    public interface DryingRoomInitListener {
        void onInitSucceed();

        void onInitFailed();
    }

    public interface TableValuesListener {
        void onTableValuesGet(List<TableValues> tableValuesList);

        void onFailed();
    }

    public class TableValues {
        public String repHour;
        public List<DataResponse> dataList;
    }

    public class DataResponse {
        public String DeviceId;
        public String SensorType;
        public String SensorTypeName;
        public String RepHour;
        public String SceneId;
        public String SensorValue;
        public String upper;
        public String lower;
    }

    public void initDryingRoomInfo(DryingRoom thisRoom, DryingRoomInitListener listener) {
        this.dryingRoom = thisRoom;
        Context context = MyApplication.getInstance().getApplicationContext();
        if (dryingRoom == null) {
            Toast.makeText(context, "房间为空!", Toast.LENGTH_SHORT).show();
            listener.onInitFailed();
            return;
        } else if (dryingRoom.getDeviceDatas() == null) {
            Toast.makeText(context, "找不到设备数据!", Toast.LENGTH_SHORT).show();
            listener.onInitFailed();
            return;
        } else {
            for (BaseDevice temp : dryingRoom.getDeviceDatas()) {
                if (BaseDevice.USE_TYPE_CK.equals(temp.getUseType())) {
                    //测控设备优先选择
                    device = temp;
                    refreshPieDatas(listener);
                    return;
                }
            }
        }
        if (device == null) {
            Toast.makeText(context, "找不到检测设备!", Toast.LENGTH_SHORT).show();
            listener.onInitFailed();
        }
    }

    public void initDryingRoomInfo(DryingRoom thisRoom, BaseDevice baseDevice, DryingRoomInitListener listener) {
        this.dryingRoom = thisRoom;
        Context context = MyApplication.getInstance().getApplicationContext();
        if (dryingRoom == null) {
            Toast.makeText(context, "房间为空!", Toast.LENGTH_SHORT).show();
            listener.onInitFailed();
            return;
        } else {
            device = baseDevice;
            refreshPieDatas(listener);
        }
        if (device == null) {
            Toast.makeText(context, "找不到检测设备!", Toast.LENGTH_SHORT).show();
            listener.onInitFailed();
        }
    }


    public class SceneDataListResponse {
        public int rc;
        public String rm;
        public ArrayList<SceneData> Data;
    }

    public class SceneData {
        public static final String STATUS_OFFLINE = "2";
        public static final String STATUS_ONLINE = "0";
        public String SensorType;
        public String SensorValue;
        public String SensorTypeName;
        public String SensorUnit;
        public String Status;//0 = 正常  2 = 离线
    }

    private String getCurrentTimestamp() {
        DateTime currentDate = DateTime.now(TimeZone.getTimeZone("Asia/Shanghai"));
        String dateStr = currentDate.format("YYYY-MM-DD hh:mm:ss");
        return dateStr.replace(" ", "%20");
    }

    public DryingRoom getDryingRoom() {
        return dryingRoom;
    }

    public BaseDevice getDevice() {
        return device;
    }

    public void refreshPieDatas(final DryingRoomInitListener listener) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("SceneId", dryingRoom.getId());
        requestParams.putWithoutFilter("t", getCurrentTimestamp());
        final Context context = MyApplication.getInstance().getApplicationContext();
        HttpUtil.jsonRequestGet(context, URLRequest.SCENE_DATAS_GET, requestParams, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                SceneDataListResponse listResponse = GsonUtils.fromJson(response, SceneDataListResponse.class);
                if (listResponse.rc == 200 && listResponse.Data != null && listResponse.Data.size() >= 1) {
                    boolean hasOnlineSensor = false;
                    //此处认为，只要有一个设备在线，就都在线
                    for (SceneData temp : listResponse.Data) {
                        if (SceneData.STATUS_ONLINE.equals(temp.Status)) {
                            hasOnlineSensor = true;
                            break;
                        }
                    }
                    if (hasOnlineSensor) {
                        listener.onInitSucceed();
                    } else {
                        Toast.makeText(context, "设备已离线", Toast.LENGTH_LONG).show();
                        listener.onInitFailed();
                    }
                } else {
                    Toast.makeText(context, "数据出错", Toast.LENGTH_LONG).show();
                    listener.onInitFailed();
                }
            }

            @Override
            public void onNetError(VolleyError error) {

            }
        });
    }

    public void getDayData(final Context context, String DataDate, final TableValuesListener listener) {
        if (dryingRoom == null || device == null) {
            listener.onFailed();
            return;
        }
        RequestParams params = new RequestParams();
        params.put("SceneId", dryingRoom.getId());
        params.put("DeviceId", device.getId());
        params.put("DataDate", DataDate);
        HttpUtil.jsonRequestGet(context, URLRequest.DAY_DATAS_GET, params, new VolleyRequestListener() {
            @Override
            public void onSuccess(String response) {
                ListResponse<DataResponse> listResponse = GsonUtils.fromJson(response, new TypeToken<ListResponse<DataResponse>>() {
                });
                if (listResponse.getRc() == 200 && listResponse.getListData() != null) {
                    handleResponseData(listResponse.getListData());
                    listener.onTableValuesGet(tableValueList);
                } else {
                    Toast.makeText(context, "暂未获取表格数据,请稍后重试", Toast.LENGTH_LONG).show();
                    listener.onFailed();
                }
            }

            @Override
            public void onNetError(VolleyError error) {
                listener.onFailed();
            }
        });
    }

    private void handleResponseData(ArrayList<DataResponse> listData) {
        //把所有的DataResponse按照时间分组、排序
        tableValueList = new LinkedList<>();
        boolean addedFlag;
        for (DataResponse temp : listData) {
            addedFlag = false;
            for (TableValues tableTemp : tableValueList) {
                if (temp.RepHour.equals(tableTemp.repHour)) {
                    //already added to tableValueList
                    tableTemp.dataList.add(temp);
                    addedFlag = true;
                    break;
                }
            }
            if (!addedFlag) {
                //hasn't added to the table
                TableValues tableValues = new TableValues();
//                temp.upper = "80";
//                temp.lower = "10";
                tableValues.repHour = temp.RepHour;
                tableValues.dataList = new ArrayList<>();
                tableValues.dataList.add(temp);
                tableValueList.add(tableValues);
            }
        }
        //按照时间从小到大排序
        Collections.sort(tableValueList, new Comparator<TableValues>() {
            @Override
            public int compare(TableValues lhs, TableValues rhs) {
                try {
                    return Integer.valueOf(lhs.repHour) - Integer.valueOf(rhs.repHour);
                } catch (Exception e) {
                    Log.e("mInfo", "parse formate exception");
                    return 0;
                }
            }
        });
    }

}
