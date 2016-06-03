package com.gov.culturems.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gov.culturems.R;
import com.gov.culturems.utils.ParseUtil;

/**
 * @author Peter
 */
public class NumberView extends RelativeLayout {

    private static int MAX_NUM = 10000;
    private static int MIN_TIME_INTERVAL = 200;

    private static final int INCREASE = 0;
    private static final int DECREASE = 1;
    private static final int INCREASE_CHANGE_IMAGE = 2;
    private static final int DECREASE_CHANGE_IMAGE = 3;

    private Context context;
    private LayoutInflater inflater;

    private ImageView decreaser;
    private EditText numberView;
    private ImageView increaser;

    private int currentNum;
    private int maxNum;
    private int minNum;

    private boolean isIncreasing;
    private boolean isDecreasing;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INCREASE:
                    numberView.setText(currentNum + "");
                    break;
                case DECREASE:
                    numberView.setText(currentNum + "");
                    break;
                case INCREASE_CHANGE_IMAGE:
                    increaser
                            .setImageResource(R.drawable.number_view_increaser_disable);
                    break;
                case DECREASE_CHANGE_IMAGE:
                    decreaser
                            .setImageResource(R.drawable.number_view_decreaser_disable);
                    break;
                default:
                    break;
            }
        }

    };

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
            currentNum = ta.getInt(R.styleable.NumberView_NumberViewDefaultNum,
                    1);
            maxNum = ta.getInt(R.styleable.NumberView_NumberViewMaxiumNum,
                    MAX_NUM);
            minNum = ta.getInt(R.styleable.NumberView_NumberViewMiniumNum, 1);

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
        decreaser = (ImageView) findViewById(R.id.decrease_img);
        increaser = (ImageView) findViewById(R.id.increase_img);

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
                    currentNum = Integer.parseInt(s.toString());
                    if (currentNum > 10000 || currentNum < 1) {
                        Toast.makeText(context, "请输入1-100的整数", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "请输入1-100的整数", Toast.LENGTH_SHORT).show();
                }
            }
        });

        currentNum = 1;
        maxNum = MAX_NUM;
        minNum = 1;

        setNumberText(currentNum);

        increaser.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startIncrease();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        stopIncrease();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        stopIncrease();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        decreaser.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startDecrease();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        stopDecrease();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        stopDecrease();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

    }

    protected void stopIncrease() {
        isIncreasing = false;
    }

    protected void startIncrease() {
        currentNum = ParseUtil.parseInt(numberView.getText().toString());
        if (currentNum == minNum) {
            decreaser.setImageResource(R.drawable.number_view_decreaser_enable);
        }
        isIncreasing = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (isIncreasing) {
                    if (currentNum < maxNum) {
                        currentNum++;
                        handler.sendEmptyMessage(INCREASE);
                        if (currentNum == maxNum)
                            handler.sendEmptyMessage(INCREASE_CHANGE_IMAGE);
                        try {
                            Thread.sleep(MIN_TIME_INTERVAL);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }

            }
        }).start();
    }

    protected void stopDecrease() {
        isDecreasing = false;
    }

    protected void startDecrease() {
        currentNum = ParseUtil.parseInt(numberView.getText().toString());

        if (currentNum == maxNum) {
            increaser.setImageResource(R.drawable.number_view_increaser_enable);
        }
        isDecreasing = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (isDecreasing) {
                    if (currentNum > minNum) {
                        currentNum--;
                        handler.sendEmptyMessage(DECREASE);
                        if (currentNum == minNum)
                            handler.sendEmptyMessage(DECREASE_CHANGE_IMAGE);
                        try {
                            Thread.sleep(MIN_TIME_INTERVAL);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }

    public void setNumberText(int num) {
        currentNum = num;
        numberView.setText(num + "");
    }

    public int getCurrentNum() {
        return ParseUtil.parseInt(numberView.getText().toString());
    }

}
