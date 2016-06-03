package com.gov.culturems.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gov.culturems.R;
import com.gov.culturems.activities.SceneActivity;
import com.gov.culturems.common.base.MyBaseAdapter;
import com.gov.culturems.entities.DryingRoom;
import com.gov.culturems.entities.Scene;

import java.util.List;

/**
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
        TextView sensor4;
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
            holder.sensor4 = (TextView) convertView.findViewById(R.id.sensor4);
            holder.teaType = (TextView) convertView.findViewById(R.id.good_type);
            holder.startTime = (TextView) convertView.findViewById(R.id.start_time);
            holder.endTime = (TextView) convertView.findViewById(R.id.end_time);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        final DryingRoom dryingRoom = data.get(position);
        holder.roomName.setText(dryingRoom.getSceneName());
        holder.teaType.setText("(货品: " + dryingRoom.getGoodsNameWithoutNullString() + ")");
        holder.startTime.setText("开始时间: " + dryingRoom.getBeginTimeWithoutNullString());
        holder.endTime.setText("结束时间: " + dryingRoom.getEndTimeWithoutNullString());
        holder.endTime.setVisibility(View.GONE);

        holder.sensor3.setVisibility(View.GONE);
        holder.sensor4.setVisibility(View.GONE);

        holder.sensor1.setVisibility(View.VISIBLE);
        holder.sensor1.setText(Html.fromHtml(dryingRoom.getSensorText(0)));
        holder.sensor2.setVisibility(View.VISIBLE);
        holder.sensor2.setText(Html.fromHtml(dryingRoom.getSensorText(1)));

        if (dryingRoom.getControlDevice().size() > 0) {
            //有温湿度和控制节点
            holder.sensor3.setVisibility(View.VISIBLE);
            holder.sensor3.setText(Html.fromHtml(dryingRoom.getControlSensorText(0)));
            if (dryingRoom.getControlDevice().size() > 1) {
                holder.sensor4.setVisibility(View.VISIBLE);
                holder.sensor4.setText(Html.fromHtml(dryingRoom.getControlSensorText(1)));
            }
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Scene scene = new Scene();
                scene.setSceneId(data.get(position).getSceneId());
                scene.setSceneName(data.get(position).getSceneName());
                Intent intent = new Intent(context, SceneActivity.class);
                intent.putExtra("scene", scene);//TODO 选中的场景
                context.startActivity(intent);
            }
        });
        return convertView;
    }

}
