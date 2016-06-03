package com.gov.culturems.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gov.culturems.R;

/**
 * Created by peter on 6/9/15.
 */
public class DeviceItemView extends RelativeLayout {

    public static final int TYPE_ALONE = 0;
    public static final int TYPE_UPPER = 1;
    public static final int TYPE_MIDDLE = 2;
    public static final int TYPE_BOTTOM = 3;

    private Context context;

    private String title;
    private String description;
    private int type;

    private TextView titleView;
    private TextView desView;

    public DeviceItemView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public DeviceItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DeviceItemView);
        type = typedArray.getInt(R.styleable.DeviceItemView_item_type, TYPE_ALONE);
        title = typedArray.getString(R.styleable.DeviceItemView_item_title);
        description = typedArray.getString(R.styleable.DeviceItemView_item_description);
        typedArray.recycle();
        init();
    }

    public DeviceItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DeviceItemView);
        type = typedArray.getInt(R.styleable.DeviceItemView_item_type, TYPE_ALONE);
        title = typedArray.getString(R.styleable.DeviceItemView_item_title);
        description = typedArray.getString(R.styleable.DeviceItemView_item_description);
        typedArray.recycle();
        init();
    }


    private void init() {
        LayoutInflater.from(context).inflate(R.layout.device_item_view, this);
        titleView = (TextView) findViewById(R.id.title);
        desView = (TextView) findViewById(R.id.description);
        setTitle(title);
        setDescription(description);
        setType(type);
    }


    public void setTitle(String title) {
        this.title = title;
        titleView.setText(title);
    }

    public void setDescription(String description) {
        this.description = description;
        this.desView.setText(description);
    }

    public void setType(int type) {
        this.type = type;
        switch (type) {
            case TYPE_ALONE:
                Log.i("mInfo", "alone " + title + type);
                setBackgroundResource(R.drawable.bg_alone);
                break;
            case TYPE_UPPER:
                Log.i("mInfo", "upper" + title + type);
                setBackgroundResource(R.drawable.bg_upper);
                break;
            case TYPE_MIDDLE:
                Log.i("mInfo", "middle" + title + type);
                setBackgroundResource(R.drawable.bg_middle);
                break;
            case TYPE_BOTTOM:
                Log.i("mInfo", "bottom" + title + type);
                setBackgroundResource(R.drawable.bg_bottom);
                break;
            default:
                break;
        }
    }

}
