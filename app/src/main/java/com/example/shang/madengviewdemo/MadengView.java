package com.example.shang.madengviewdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.View;

public class MadengView extends View {

    private String mContent; // 内容
    private float mSize = 12;    // 大小
    private float mColor = Color.BLACK;    // 颜色，默认黑色
    private float mSpeed = 1.0f; // 速度，默认为1
    private int mMode = RECYCLE_ONCE; // 模式默认为1次
    private static final int RECYCLE_ONCE = 0;         // 一次结束
    private static final int RECYCLE_INTERVAL = 1;    // 一次接着一次
    private static final int RECYCLE_CONTINUOUS = 2;  // 内容结束，立即下一次
    private int mDistance = 10;              // 每个item间距
    private float mStartLocation = 1.0f;      // 开始的位置，百分比来的，距离左边，0 - 1，0代表不间距，1的话代表，从右面，1/2代表中间。
    private boolean mIsClickStop = false;      // 点击是否停止
    private boolean mIsResetLocation = true; // 重新改变内容的时候 ， 是否初始化

    public MadengView(Context context) {
        this(context, null);
    }

    public MadengView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MadengView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化属性
        initAttrs(attrs);
        // 初始化画笔
        initPaint();
        // 初始化点击事件
        initClick();

    }

    private void initClick() {

    }

    private void initPaint() {

    }

    @SuppressLint("RestrictedApi")
    private void initAttrs(AttributeSet attrs) {
        TintTypedArray array = TintTypedArray.obtainStyledAttributes(getContext(),attrs,R.styleable.MadengView);


        mSize = array.getFloat(R.styleable.MadengView_madeng_text_size,mSize);
        mColor = array.getFloat(R.styleable.MadengView_madeng_text_color,mColor);
        mSpeed = array.getFloat(R.styleable.MadengView_madeng_text_speed,mSpeed);
        mMode = array.getInt(R.styleable.MadengView_madeng_recycle_mode,mMode);
        mDistance = array.getInt(R.styleable.MadengView_madeng_text_distance,mDistance);
        mStartLocation = array.getFloat(R.styleable.MadengView_madeng_text_startlocaiton,mStartLocation);
        mIsClickStop = array.getBoolean(R.styleable.MadengView_madeng_isclickable_stop,mIsClickStop);
        mIsResetLocation = array.getBoolean(R.styleable.MadengView_madeng_isresetlocation,mIsResetLocation);
        array.recycle();// 记得加上这个
    }


}
