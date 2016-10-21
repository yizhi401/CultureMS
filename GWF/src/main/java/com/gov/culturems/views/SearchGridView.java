package com.gov.culturems.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gov.culturems.R;

import java.util.List;

/**
 * Created by peter on 11/11/15.
 */
public class SearchGridView extends LinearLayout {

    private Context context;
    private List<String> searchData;
    private OnGridClickedListener onGridClickedListener;

    public interface OnGridClickedListener {
        void onGridClicked(String query);
    }

    public SearchGridView(Context context) {
        super(context);
        this.context = context;
    }

    public SearchGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public SearchGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setSearchData(List<String> searchData) {
        this.searchData = searchData;
    }

    public void setOnGridClickedListener(OnGridClickedListener onGridClickedListener) {
        this.onGridClickedListener = onGridClickedListener;
    }

    public void refreshViewsByData(List<String> searchData) {
        this.searchData = searchData;
        int pointer = 0;
        this.setOrientation(VERTICAL);
        this.removeAllViews();
        while (true) {
            RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.search_grid_item, null, false);
            TextView textView1 = (TextView) layout.findViewById(R.id.text1);
            TextView textView2 = (TextView) layout.findViewById(R.id.text2);
            TextView textView3 = (TextView) layout.findViewById(R.id.text3);
            View divider2 = layout.findViewById(R.id.divider2);
            if (pointer < searchData.size()) {
                this.addView(layout);
                textView1.setText(searchData.get(pointer));
                textView1.setOnClickListener(getOnClickListener());
                pointer++;
                if (pointer < searchData.size()) {
                    textView2.setText(searchData.get(pointer));
                    textView2.setOnClickListener(getOnClickListener());
                    pointer++;
                    if (pointer < searchData.size()) {
                        textView3.setText(searchData.get(pointer));
                        textView3.setOnClickListener(getOnClickListener());
                        pointer++;
                    } else {
                        textView3.setVisibility(View.INVISIBLE);
                        break;
                    }
                } else {
                    textView2.setVisibility(View.INVISIBLE);
                    divider2.setVisibility(View.INVISIBLE);
                    textView3.setVisibility(View.INVISIBLE);
                    break;
                }
            } else {
                break;
            }
        }
    }

    private OnClickListener getOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onGridClickedListener != null) {
                    onGridClickedListener.onGridClicked(((TextView) v).getText().toString());
                }
            }
        };
    }

}
