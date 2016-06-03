package com.gov.culturems.views.wheelview;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.gov.culturems.R;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * 公用的滚轮View，最多提供三列,具体各类滚轮wheel可以继承此基类并且提供各自的方法
 *
 * @author Peter
 */
public class CommonWheelView {

    public static final int ONE_WHEEL = 1;
    public static final int TWO_WHEEL = 2;
    public static final int THREE_WHEEL = 3;
    protected static final int TEXTSIZE = 18;
    protected static final int TEXTSIZESMALL = 15;

    protected PopupWindow popupWin;

    protected int textSizeSmall;

    protected WheelView leftWheel, centerWheel, rightWheel;

    /**
     * 注意：展示一个Wheel的时候，请设置centerArr 展示两个Wheel的时候，请设置leftArr和centerArr
     * 展示三个Wheel的时候，请设置三个
     */
    protected String[] leftArr, rightArr, centerArr;

    /**
     * 当前左中右三个wheel选中的text
     */
    protected String leftStr, rightStr, centerStr;

    protected ArrayWheelAdapter<String> leftAdapter, rightAdapter,
            centerAdapter;

    protected LinearLayout mainView;

    protected TextView okText, cancelText;

    protected Context context;

    /**
     * 用于展示结果的TextView,由调用者传入,可以是EditText
     */
    protected TextView resultView;

    /**
     * 在有多个wheel的情况下，结果之间的连接符： 比如日期的连接符："-" 城市的连接符"/"
     */
    protected String separator;
    /**
     * 要显示几个wheel
     */
    protected int wheelNums;

    protected CommonWheelClickListener wheelClickListener;

    protected CommonWheelChangedListener changedListener;

    public interface CommonWheelChangedListener {
        /**
         * 有时候，一个wheel内容改变可能影响到另外一个wheel的内容，子类可以在此方法中改变
         *
         * @param wheel
         * @param oldValue
         * @param newValue
         */
        void onLeftChanged(WheelView wheel, int oldValue, int newValue);

        /**
         * 有时候，一个wheel内容改变可能影响到另外一个wheel的内容，子类可以在此方法中改变
         *
         * @param wheel
         * @param oldValue
         * @param newValue
         */
        void onCenterChanged(WheelView wheel, int oldValue, int newValue);

        /**
         * 有时候，一个wheel内容改变可能影响到另外一个wheel的内容，子类可以在此方法中改变
         *
         * @param wheel
         * @param oldValue
         * @param newValue
         */
        void onRightChanged(WheelView wheel, int oldValue, int newValue);
    }

    public interface CommonWheelClickListener {
        /**
         * 确定按钮
         */
        void onOkBtnClicked();

        void onCancelBtnClicked();
    }

    public CommonWheelView(Context context) {
        this.context = context;
        init();
        initListeners();
    }

    protected void initListeners() {
        cancelText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWin != null)
                    popupWin.dismiss();
//                if (resultView != null)
//                    resultView.setText("");
                if (wheelClickListener != null) {
                    wheelClickListener.onCancelBtnClicked();
                }

            }
        });
        okText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                refreshResult();
                if (popupWin != null)
                    popupWin.dismiss();
                if (wheelClickListener != null) {
                    wheelClickListener.onOkBtnClicked();
                }

            }
        });

        leftWheel.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                refreshResult();
                if (changedListener != null)
                    changedListener.onLeftChanged(wheel, oldValue, newValue);
            }

        });

        centerWheel.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                refreshResult();
                if (changedListener != null)
                    changedListener.onCenterChanged(wheel, oldValue, newValue);
            }
        });

        rightWheel.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                refreshResult();
                if (changedListener != null)
                    changedListener.onRightChanged(wheel, oldValue, newValue);
            }
        });
    }

    protected void init() {
        getMainView();
        textSizeSmall = TEXTSIZE;
        leftWheel = (WheelView) mainView.findViewById(R.id.left);
        centerWheel = (WheelView) mainView.findViewById(R.id.center);
        rightWheel = (WheelView) mainView.findViewById(R.id.right);
        showWheelViews();
        okText = (TextView) mainView.findViewById(R.id.ok);
        cancelText = (TextView) mainView.findViewById(R.id.cancel);
        initPopupWindow();
    }

    /**
     * 这个方法给子类提供使用不同布局的空间
     */
    protected void getMainView() {
        mainView = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.wheel_common_wheel, null);
    }

    protected void initPopupWindow() {
        popupWin = new PopupWindow(mainView, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        popupWin.setBackgroundDrawable(context.getResources().getDrawable(
                R.color.transparent));
        popupWin.setFocusable(true);
        popupWin.setAnimationStyle(android.R.style.Animation_Dialog);
    }

    public void show(TextView resultView) {
        this.resultView = resultView;
        popupWin.showAtLocation(mainView, Gravity.BOTTOM, 0, 0);
    }

    protected void showWheelViews() {
        leftWheel.setVisibility(View.GONE);
        rightWheel.setVisibility(View.GONE);
        centerWheel.setVisibility(View.GONE);
        switch (wheelNums) {
            case ONE_WHEEL:
                leftWheel.setVisibility(View.VISIBLE);
                break;
            case TWO_WHEEL:
                centerWheel.setVisibility(View.VISIBLE);
                leftWheel.setVisibility(View.VISIBLE);
                break;
            case THREE_WHEEL:
                leftWheel.setVisibility(View.VISIBLE);
                rightWheel.setVisibility(View.VISIBLE);
                centerWheel.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    protected void refreshResult() {
        StringBuffer result = new StringBuffer();
        if (TextUtils.isEmpty(separator)) {
            separator = "";
        }
        if (leftAdapter != null && leftWheel != null && wheelNums >= ONE_WHEEL) {
            leftStr = leftAdapter.getItemText(leftWheel.getCurrentItem())
                    .toString();
            if (!TextUtils.isEmpty(leftStr)) {
                result.append(leftStr);
            }
        }
        if (centerAdapter != null && centerWheel != null
                && wheelNums >= TWO_WHEEL) {
            centerStr = centerAdapter.getItemText(centerWheel.getCurrentItem())
                    .toString();
            if (!TextUtils.isEmpty(centerStr)) {
                result.append(separator).append(centerStr);
            }
        }
        if (rightAdapter != null && rightWheel != null
                && wheelNums >= THREE_WHEEL) {
            rightStr = rightAdapter.getItemText(rightWheel.getCurrentItem())
                    .toString();
            if (!TextUtils.isEmpty(rightStr)) {
                result.append(separator).append(rightStr);
            }
        }
        if (resultView != null) {
            String tmp = result.toString();
            tmp = tmp.replace(context.getResources().getString(R.string.year), "-");
            tmp = tmp.replace(context.getResources().getString(R.string.month), "-");
            tmp = tmp.replace(context.getResources().getString(R.string.day), "");
            resultView.setText(tmp);

        }
    }

    /**
     * 通过这个函数来设置各个滚轮的 提供几个参数，就出现几个滚轮 1-3个参数多了不接受
     *
     * @param arr
     */
    public void setArrs(String[]... arr) {
        int len = arr.length;
        switch (len) {
            case 1:
                wheelNums = ONE_WHEEL;
                setLeftArr(arr[0]);
                break;
            case 2:
                wheelNums = TWO_WHEEL;
                setLeftArr(arr[0]);
                setCenterArr(arr[1]);
                break;
            case 3:
                wheelNums = THREE_WHEEL;
                setLeftArr(arr[0]);
                setCenterArr(arr[1]);
                setRightArr(arr[2]);
                break;
            default:
                break;
        }

        showWheelViews();

    }

    /**
     * 除非你想单独设置左侧滚轮，否则不要调用这个方法 调用setArrs()方法来统一设置
     *
     * @param leftArr
     */
    public void setLeftArr(String[] leftArr) {
        this.leftArr = leftArr;
        leftAdapter = new ArrayWheelAdapter<String>(context, leftArr);
        leftAdapter.setTextSize(TEXTSIZE);
        leftAdapter.setTextSizeSmall(textSizeSmall);
        leftWheel.setViewAdapter(leftAdapter);
        leftWheel.setCurrentItem(0);
        refreshResult();
    }

    public void setLeftCurrentItem(int index) {
        if (leftWheel != null)
            leftWheel.setCurrentItem(index);
        refreshResult();
    }

    public int getLeftCurrentItem() {
        if (leftWheel != null)
            return leftWheel.getCurrentItem();
        else
            return 0;
    }

    public void setLeftCyclic(boolean isCyclic) {
        if (leftWheel != null) {
            leftWheel.setCyclic(isCyclic);
        }
    }

    /**
     * 除非你想单独设置中间滚轮，否则不要调用这个方法 调用setArrs()方法来统一设置
     *
     * @param
     */
    public void setRightArr(String[] rightArr) {
        this.rightArr = rightArr;
        rightAdapter = new ArrayWheelAdapter<String>(context, rightArr);
        rightAdapter.setTextSize(TEXTSIZE);
        rightAdapter.setTextSizeSmall(textSizeSmall);
        rightWheel.setViewAdapter(rightAdapter);
        rightWheel.setCurrentItem(0);
        refreshResult();
    }

    public void setRightCurrentItem(int index) {
        if (rightWheel != null)
            rightWheel.setCurrentItem(index);
        refreshResult();

    }

    public int getRightCurrentItem() {
        if (rightWheel != null)
            return rightWheel.getCurrentItem();
        else
            return 0;
    }

    public void setRightCyclic(boolean isCyclic) {
        if (rightWheel != null) {
            rightWheel.setCyclic(isCyclic);
        }
    }

    /**
     * 除非你想单独设置右侧滚轮，否则不要调用这个方法 调用setArrs()方法来统一设置
     *
     * @param
     */
    public void setCenterArr(String[] centerArr) {
        this.centerArr = centerArr;
        centerAdapter = new ArrayWheelAdapter<String>(context, centerArr);
        centerAdapter.setTextSize(TEXTSIZE);
        centerAdapter.setTextSizeSmall(textSizeSmall);
        centerWheel.setViewAdapter(centerAdapter);
        centerWheel.setCurrentItem(0);
        refreshResult();
    }

    public int getTextSizeSmall() {
        return textSizeSmall;
    }

    public void setTextSizeSmall(int textSizeSmall) {
        this.textSizeSmall = textSizeSmall;
    }

    public void setCenterCyclic(boolean isCyclic) {
        if (centerWheel != null) {
            centerWheel.setCyclic(isCyclic);
        }
    }

    public void setCenterCurrentItem(int index) {
        if (centerWheel != null)
            centerWheel.setCurrentItem(index);
        refreshResult();

    }

    public int getCenterCurrentItem() {
        if (centerWheel != null)
            return centerWheel.getCurrentItem();
        else
            return 0;
    }

    public String getResult() {
        refreshResult();
        if (resultView != null) {
            return resultView.getText().toString();
        } else {
            return null;
        }
    }

    public int getWheelNums() {
        return wheelNums;
    }

    public void setWheelNums(int wheelNums) {
        this.wheelNums = wheelNums;
        showWheelViews();
    }

    public CommonWheelChangedListener getChangedListener() {
        return changedListener;
    }

    public void setChangedListener(CommonWheelChangedListener changedListener) {
        this.changedListener = changedListener;
    }

    public CommonWheelClickListener getWheelClickListener() {
        return wheelClickListener;
    }

    public void setWheelClickListener(CommonWheelClickListener wheelClickListener) {
        this.wheelClickListener = wheelClickListener;
    }


}
