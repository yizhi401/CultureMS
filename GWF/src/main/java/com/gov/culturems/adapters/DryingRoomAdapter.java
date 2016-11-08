package com.gov.culturems.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gov.culturems.R;
import com.gov.culturems.activities.DeviceDataActivity;
import com.gov.culturems.activities.DryingRoomActivity;
import com.gov.culturems.activities.DryingRoomHelper;
import com.gov.culturems.activities.SceneActivity;
import com.gov.culturems.common.base.MyBaseAdapter;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.utils.LogUtil;

import java.util.List;

/**
 * 首页的Adapter
 * 显示烘房的各种状态
 * Created by peter on 2015/11/7.
 */
public class DryingRoomAdapter extends MyBaseAdapter<DryingRoom> {

    public DryingRoomAdapter(List<DryingRoom> data, Context context) {
        super(data, context);
    }

    class Holder {
        TextView roomName;
        TextView sensor1;
        TextView sensor2;
        TextView sensor3;
        TextView teaType;
        TextView startTime;
        TextView endTime;

    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.drying_room_grid_item, null);
            holder = new Holder();
            holder.roomName = (TextView) convertView.findViewById(R.id.room_name);
            holder.sensor1 = (TextView) convertView.findViewById(R.id.sensor1);
            holder.sensor2 = (TextView) convertView.findViewById(R.id.sensor2);
            holder.sensor3 = (TextView) convertView.findViewById(R.id.sensor3);
            holder.teaType = (TextView) convertView.findViewById(R.id.good_type);
            holder.startTime = (TextView) convertView.findViewById(R.id.start_time);
            holder.endTime = (TextView) convertView.findViewById(R.id.end_time);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        final DryingRoom dryingRoom = data.get(position);
        holder.roomName.setText(dryingRoom.getName());
        holder.teaType.setText(String.format(context.getResources().getString(R.string.good_type), dryingRoom.getGoodsNameNonNull()));
        holder.startTime.setText(String.format(context.getResources().getString(R.string.begin_time), dryingRoom.getBeginTimeNonNull()));
        holder.endTime.setText(String.format(context.getResources().getString(R.string.end_time), dryingRoom.getEndTimeWithoutNullString()));
        holder.endTime.setVisibility(View.GONE);

        holder.sensor1.setVisibility(View.VISIBLE);
        holder.sensor1.setText(dryingRoom.getTempatureTxt());
        holder.sensor2.setVisibility(View.VISIBLE);
        holder.sensor2.setText(dryingRoom.getHumidityTxt());
        holder.sensor3.setVisibility(View.VISIBLE);
        holder.sensor3.setText(dryingRoom.getMoistureTxt());

        if ("未知".equals(holder.sensor1.getText().toString())) {
            holder.sensor1.setTextColor(context.getResources().getColor(R.color.text_gray_deep));
        } else {
            holder.sensor1.setTextColor(context.getResources().getColor(R.color.main_green));
        }
        if ("未知".equals(holder.sensor2.getText().toString())) {
            holder.sensor2.setTextColor(context.getResources().getColor(R.color.text_gray_deep));
        } else {
            holder.sensor2.setTextColor(context.getResources().getColor(R.color.main_green));
        }
        if ("未知".equals(holder.sensor3.getText().toString())) {
            holder.sensor3.setTextColor(context.getResources().getColor(R.color.text_gray_deep));
        } else {
            holder.sensor3.setTextColor(context.getResources().getColor(R.color.main_green));
        }


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(context, SceneActivity.class);
                intent1.putExtra("scene", data.get(position));
                ((Activity) context).startActivityForResult(intent1, SceneActivity.REQUEST_CODE);
                ((Activity) context).overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
//                tryStartDeviceActivity(data.get(position));
            }
        });
        return convertView;
    }

//    private boolean hasOnlineDevice(DryingRoom chosenRoom){
//        boolean hasOnlineDevice = false;
//        for (BaseDevice temp : chosenRoom.getDeviceDatas()) {
//            if (!BaseDevice.DEVICE_STATUS_OFFLINE.equals(temp.getDeviceStatus())) {
//                hasOnlineDevice = true;
//            }
//        }
//        return hasOnlineDevice;
//    }

    private void tryStartDeviceActivity(DryingRoom chosenRoom) {
        boolean hasOnlineDevice = chosenRoom.hasOnlineDevice();
        if (!hasOnlineDevice) {
            Toast.makeText(context, "设备离线!", Toast.LENGTH_SHORT).show();
        } else {
            DryingRoomHelper helper = DryingRoomHelper.getInstance();
            helper.initDryingRoomInfo(chosenRoom, new DryingRoomHelper.DryingRoomInitListener() {
                @Override
                public void onInitSucceed() {
                    Intent intent = new Intent(context, DeviceDataActivity.class);
                    ((Activity) context).startActivityForResult(intent, DryingRoomActivity.REQUEST_CODE);
                    ((Activity) context).overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                }

                @Override
                public void onInitFailed() {
                    LogUtil.e("init drying room failed");
                }
            });
        }
    }
}
