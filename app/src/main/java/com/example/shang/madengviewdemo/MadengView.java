package com.example.shang.madengviewdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.TintTypedArray;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

public class MadengView extends View implements Runnable{

    private static final String TAG = "MadengView";
    private String mContent; // 最终绘制的内容
    private float mSize = 12;    // 大小
    private int mColor = Color.BLACK;    // 颜色，默认黑色
    private float mSpeed = 1.0f; // 速度，默认为1
    private int mMode = RECYCLE_ONCE; // 模式默认为1次
    private static final int RECYCLE_ONCE = 0;         // 一次结束
    private static final int RECYCLE_INTERVAL = 1;    // 一次接着一次
    private static final int RECYCLE_CONTINUOUS = 2;  // 内容结束，立即下一次
    private int mDistance;
    private int mDistance1 = 10;              // item间距,单位dp
    private float mStartLocation = 1.0f;      // 开始的位置，百分比来的，距离左边，0 - 1，0代表不间距，1的话代表，从右面，1/2代表中间。
    private boolean mIsClickStop = false;      // 点击是否停止
    private boolean mIsResetLocation = true; // 重新改变内容的时候 ， 是否初始化

    private Rect mRect;
    private Paint mPaint;

    private boolean mIsRolling = false; // 是否继续滚动
    private Thread mThread; // 滚动线程

    private String mAddContent = ""; // 追加内容?

    private float mXLocation = 0; // 文本的x坐标

    private boolean mResetInit = true; // 重新?

    private float mOneBlackWidth; // 空格的宽度
    private float mTextHeight;// 文字的高度

    private int mContentWidth;//内容的宽度

    private String mBlackCount = "";//间距转化成空格距离

    private int mRepetCount = 0;//

    /**
     * 设置文字颜色
     */
    public void setTextColor(int textColor) {
        if (textColor != 0) {
            this.mColor = textColor;
            mPaint.setColor(getResources().getColor(textColor));//文字颜色值,可以不设定
        }
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        if (textSize > 0) {
            this.mSize = textSize;
            mPaint.setTextSize(dp2px(textSize));//文字颜色值,可以不设定
            mContentWidth = (int) (getContentWidth(mAddContent) + mDistance);//大小改变，需要重新计算宽高
        }
    }
    /**
     * 设置滚动速度
     */
    public void setSpeed(float speed) {
        this.mSpeed = speed;
    }

    public void setMode(int mode) {
        mMode = mode;
        mResetInit = true;
        setContent(mAddContent);
    }

    /**
     * 点击是否暂停，默认是不
     */
    private void setClickStop(boolean isClickStop) {
        this.mIsClickStop = isClickStop;
    }


    /**
     * 是否循环滚动
     */
    private void setContinueble(int isContinuable) {
        this.mMode = isContinuable;
    }

    /**
     * 从新添加内容的时候，是否初始化位置
     */
    private void setResetLocation(boolean isReset) {
        mIsResetLocation = isReset;
    }

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
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsClickStop){
                    if (mIsRolling){
                        stopRoll();
                    }else {
                        continueRoll();
                    }
                }
            }
        });
    }

    // 继续滚动
    public void continueRoll() {
        if (!mIsRolling){
            if (mThread!=null){
                mThread.interrupt();
                mThread = null;
            }
            mIsRolling = true;
            mThread = new Thread(this);
            mThread.start(); // 开启死循环
        }

    }

    // 停止滚动
    public void stopRoll() {
        mIsRolling = false;
        if (mThread !=null){
            mThread.interrupt();
            mThread = null;
        }
    }

    private void initPaint() {
        mRect = new Rect();
        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColor);
        mPaint.setTextSize(dp2px(mSize));
    }

    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @SuppressLint("RestrictedApi")
    private void initAttrs(AttributeSet attrs) {
        TintTypedArray array = TintTypedArray.obtainStyledAttributes(getContext(),attrs,R.styleable.MadengView);

        mSize = array.getFloat(R.styleable.MadengView_madeng_text_size,mSize);
        mColor = array.getColor(R.styleable.MadengView_madeng_text_color,mColor);
        mSpeed = array.getFloat(R.styleable.MadengView_madeng_text_speed,mSpeed);

        mMode = array.getInt(R.styleable.MadengView_madeng_recycle_mode,mMode);

        mDistance1 = array.getInt(R.styleable.MadengView_madeng_text_distance,mDistance1);
        mStartLocation = array.getFloat(R.styleable.MadengView_madeng_text_startlocaiton,mStartLocation);

        mIsClickStop = array.getBoolean(R.styleable.MadengView_madeng_isclickable_stop,mIsClickStop);
        mIsResetLocation = array.getBoolean(R.styleable.MadengView_madeng_isresetlocation,mIsResetLocation);
        array.recycle();// 记得加上这个
    }


    @Override
    public void run() {
        while (mIsRolling && !TextUtils.isEmpty(mAddContent)) {
            try {
                Thread.sleep(10);
                mXLocation = mXLocation - mSpeed;
                postInvalidate();//每隔10毫秒重绘视图
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (mResetInit) {
            setTextDistance(mDistance1);

            if (mStartLocation < 0) {
                mStartLocation = 0;
            } else if (mStartLocation > 1) {
                mStartLocation = 1;
            }
            mXLocation = getWidth() * mStartLocation;
            mResetInit = false;
        }


        //需要判断滚动模式的
        switch (mMode) {
            case RECYCLE_ONCE:

                if (mContentWidth < (-mXLocation)) {
                    //也就是说文字已经到头了
//                    此时停止线程就可以了
                    stopRoll();
                }
                break;
            case RECYCLE_INTERVAL:
                if (mContentWidth <= (-mXLocation)) {
                    //也就是说文字已经到头了
                    mXLocation = getWidth();
                }
                break;

            case RECYCLE_CONTINUOUS:


                if (mXLocation < 0) {
                    int beAppend = (int) ((-mXLocation) / mContentWidth);

                    Log.e(TAG, "onDraw: ---" + mContentWidth + "--------" + (-mXLocation) + "------" + beAppend);

                    if (beAppend >= mRepetCount) {
                        mRepetCount++;
                        //也就是说文字已经到头了
//                    xLocation = speed;//这个方法有问题，所以采取了追加字符串的 方法
                        mContent = mContent + mAddContent;
                    }
                }
                //此处需要判断的xLocation需要加上相应的宽度
                break;

            default:
                //默认一次到头好了
                if (mContentWidth < (-mXLocation)) {
                    //也就是说文字已经到头了
//                    此时停止线程就可以了
                    stopRoll();
                }
                break;
        }


        //把文字画出来
        if (mContent != null) {
            canvas.drawText(mContent, mXLocation, getHeight() / 2 + mTextHeight / 2, mPaint);
        }
    }

    // 设置文字间距  不过如果内容是List形式的，该方法不适用 ,list的数据源，必须在设置setContent之前调用此方法。
    public void setTextDistance(int distance1) {

        //设置之后就需要初始化了

        String black = " ";
        mOneBlackWidth = getBlacktWidth();//空格的宽度
        distance1 = dp2px(distance1);
        int count = (int) (distance1 / mOneBlackWidth);//空格个数，有点粗略，有兴趣的朋友可以精细

        if (count == 0) {
            count = 1;
        }

        mDistance = (int) (mOneBlackWidth * count);
        mBlackCount = "";
        for (int i = 0; i <= count; i++) {
            mBlackCount = mBlackCount + black;//间隔字符串
        }
        setContent(mAddContent);//设置间距以后要重新刷新内容距离，不过如果内容是List形式的，该方法不适用
    }

    /**
     * 设置滚动的条目内容， 集合形式的
     */
    public void setContent(List<String> strings) {
        setTextDistance(mDistance1);
        String temString = "";

        if (strings != null && strings.size() != 0) {

            for (int i = 0; i <strings.size(); i++) {

                temString = temString+strings.get(i) + mBlackCount;
            }

        }
        setContent(temString);
    }

    //设置滚动的条目内容  字符串形式的
    public void setContent(String content) {
        if (TextUtils.isEmpty(content)){
            return;
        }
        if (mIsResetLocation) {//控制重新设置文本内容的时候，是否初始化xLocation。
            mXLocation = getWidth() * mStartLocation;
        }

        if (!content.endsWith(mBlackCount)) {
            content = content + mBlackCount;//避免没有后缀
        }
        mAddContent = content;

        //这里需要计算宽度啦，当然要根据模式来搞
        if (mMode == RECYCLE_CONTINUOUS) {//如果说是循环的话，则需要计算 文本的宽度 ，然后再根据屏幕宽度 ， 看能一个屏幕能盛得下几个文本

            mContentWidth = (int) (getContentWidth(content) + mDistance);//可以理解为一个单元内容的长度
            //从0 开始计算重复次数了， 否则到最后 会跨不过这个坎而消失。
            mRepetCount = 0;
            int contentCount = (getWidth() / mContentWidth) + 2;
            mContent = "";
            for (int i = 0; i <= contentCount; i++) {
                mContent = mContent + mAddContent;//根据重复次数去叠加。
            }

        } else {
            if (mXLocation < 0 && mMode == RECYCLE_ONCE) {
                if (-mXLocation > mContentWidth) {
                    mXLocation = getWidth() * mStartLocation;
                }
            }
            mContentWidth = (int) getContentWidth(content);

            mContent = content;
        }

        if (!mIsRolling) {//如果没有在滚动的话，重新开启线程滚动
            continueRoll();
        }

    }

    private float getBlacktWidth() {
        String text1 = "en en";
        String text2 = "enen";
        return getContentWidth(text1) - getContentWidth(text2);
    }

    private float getContentWidth(String black) {
        if (black == null || black == "") {
            return 0;
        }

        if (mRect == null) {
            mRect = new Rect();
        }
        mPaint.getTextBounds(black, 0, black.length(), mRect);
        mTextHeight = getContentHeight();

        return mRect.width();
    }

    // 获取内容高度
    // http://blog.csdn.net/u014702653/article/details/51985821
    private float getContentHeight() {
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        return Math.abs((fontMetrics.bottom - fontMetrics.top)) / 2;
    }
}
