package com.gov.culturems.views.wheelview;

import android.content.Context;
import android.widget.TextView;


public class SingleSelect {

	private CommonWheelView wheelView;
	
	public SingleSelect(Context context, String[] arr) {
		wheelView = new CommonWheelView(context);
		wheelView.setArrs(arr);
	}
	public CommonWheelView getWheelView() {
		return wheelView;
	}

	public void setWheelView(CommonWheelView wheelView) {
		this.wheelView = wheelView;
	}



	public void setCilckListener(CommonWheelView.CommonWheelClickListener listener){
		wheelView.setWheelClickListener(listener);
	}

	public void show(TextView textView) {
		wheelView.show(textView);
	}
}
