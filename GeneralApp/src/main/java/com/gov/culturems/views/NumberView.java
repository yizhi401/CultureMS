package com.gov.culturems.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gov.culturems.R;

import java.text.DecimalFormat;

/**
 * @author Peter
 */
public class NumberView extends RelativeLayout {

    private static int MAX_NUM = 10000;

    private Context context;
    private LayoutInflater inflater;

    private EditText numberView;
    private TextView unitText;
    private String unit;

    /**
     * 输入框小数的位数
     */
    private static final int DECIMAL_DIGITS = 1;

    private float currentNum;

    public NumberView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public NumberView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        getAttrs(attrs);
        init();
    }

    public NumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getAttrs(attrs);
        init();
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.NumberView);
        try {
            currentNum = ta.getFloat(R.styleable.NumberView_NumberViewDefaultNum, 1.0f);
            unit = ta.getString(R.styleable.NumberView_Unit);
        } finally {
            ta.recycle();
        }
    }

    private void init() {
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.num_view_layout, this);
        // TODO 这种写法不对，平白多了一层layout
        numberView = (EditText) findViewById(R.id.number_text);
        unitText = (TextView)findViewById(R.id.unit);
        unitText.setText(unit);
        InputFilter lengthfilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                // 删除等特殊字符，直接返回
                if ("".equals(source.toString())) {
                    return null;
                }
                String dValue = dest.toString();
                String[] splitArray = dValue.split("\\.");
                if (splitArray.length > 1) {
                    String dotValue = splitArray[1];
                    int diff = dotValue.length() + 1 - DECIMAL_DIGITS;
                    if (diff > 0) {
                        return source.subSequence(start, end - diff);
                    }
                }
                return null;
            }
        };
        numberView.setFilters(new InputFilter[]{lengthfilter, new InputFilter.LengthFilter(10)});
        numberView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(numberView.getWindowToken(), 0);
                }
            }
        });

        numberView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                try {
                    currentNum = Float.parseFloat(s.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        numberView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setNumberText(currentNum);
                }
            }
        });

        currentNum = 1.0f;

        setNumberText(currentNum);

    }

    public void setUnit(String unit){
        this.unit = unit;
        unitText.setText(unit);
    }

    public void setNumberText(float num) {
        currentNum = num;
        numberView.setText(getFormattedFloat(num));
    }

    public String getFormattedFloat(float num) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p = decimalFormat.format(num);//format 返回的是字符串
        return p;
    }

    public float getCurrentNum() {
        return currentNum;
    }

    public String getCurrentNumStr() {
        return numberView.getText().toString();
    }

}
