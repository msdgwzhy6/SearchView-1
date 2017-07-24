package com.searchview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wujun on 2017/7/24.
 * 搜索动画
 *
 * @author madreain
 * @desc 支持xml、代码双设置，
 * 主要方法：开始搜索、结束搜索
 */

public class SearchView extends View {

    // 画笔
    private Paint mPaint;
    // View 宽高
    private int mViewWidth;
    private int mViewHeight;
    //设置的相关参数
    float circleVaule = 100;
    int backgroundColor = Color.parseColor("#0082D7");
    int paintColor = Color.WHITE;
    float paintStrokeWidth = 15;
    //除数
    float divisor=2;

    public SearchView(Context context) {
        super(context);
        initAll();
    }

    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs);
        initAll();
    }

    public SearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
        initAll();
    }

    private void initTypedArray(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SearchView);
        circleVaule = typedArray.getFloat(R.styleable.SearchView_circleVaule, 100);
        backgroundColor = typedArray.getColor(R.styleable.SearchView_backgroundColor, Color.parseColor("#0082D7"));
        paintColor = typedArray.getColor(R.styleable.SearchView_paintColor, Color.WHITE);
        paintStrokeWidth = typedArray.getFloat(R.styleable.SearchView_paintStrokeWidth, 15);
        divisor=typedArray.getFloat(R.styleable.SearchView_divisor,2);
        //获取资源后要及时回收
        typedArray.recycle();
    }

    public void setCircleVaule(float circleVaule) {
        this.circleVaule = circleVaule;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setPaintColor(int paintColor) {
        this.paintColor = paintColor;
    }

    public void setPaintStrokeWidth(int paintStrokeWidth) {
        this.paintStrokeWidth = paintStrokeWidth;
    }

    public void setDivisor(float divisor) {
        this.divisor = divisor;
    }

    private void initAll() {
        initPaint();

        initPath();

        initListener();

        initHandler();

        initAnimator();

    }

    // 这个视图拥有的状态
    private enum State {
        NONE,
        STARTING,
        SEARCHING,
        ENDING
    }

    // 当前的状态(非常重要)
    private State mCurrentState = State.NONE;

    // 放大镜与外部圆环
    private Path path_srarch;
    private Path path_circle;

    // 测量Path 并截取部分的工具
    private PathMeasure mMeasure;

    // 默认的动效周期 2s
    private int defaultDuration = 2000;

    // 控制各个过程的动画
    private ValueAnimator mStartingAnimator;
    private ValueAnimator mSearchingAnimator;
    private ValueAnimator mEndingAnimator;

    // 动画数值(用于控制动画状态,因为同一时间内只允许有一种状态出现,具体数值处理取决于当前状态)
    private float mAnimatorValue = 0;

    // 动效过程监听器
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;
    private Animator.AnimatorListener mAnimatorListener;

    // 用于控制动画状态转换
    private Handler mAnimatorHandler;

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(paintStrokeWidth);
        mPaint.setColor(paintColor);
    }

    private void initPath() {
        path_srarch = new Path();
        path_circle = new Path();

        mMeasure = new PathMeasure();

        // 注意,不要到360度,否则内部会自动优化,测量不能取到需要的数值
        //默认设置的是搜索的圆是最外层圆的一半
        float srarchCircleVaule = circleVaule / divisor;
        RectF oval1 = new RectF(-srarchCircleVaule, -srarchCircleVaule, srarchCircleVaule, srarchCircleVaule);          // 放大镜圆环
        path_srarch.addArc(oval1, 45, 359.9f);

        RectF oval2 = new RectF(-circleVaule, -circleVaule, circleVaule, circleVaule);      // 外部圆环
        path_circle.addArc(oval2, 45, -359.9f);

        float[] pos = new float[2];

        mMeasure.setPath(path_circle, false);               // 放大镜把手的位置
        mMeasure.getPosTan(0, pos, null);

        path_srarch.lineTo(pos[0], pos[1]);                 // 放大镜把手

//        Log.i("TAG", "pos=" + pos[0] + ":" + pos[1]);
    }

    private void initListener() {
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        };

        //通知动画状态更新
        mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // getHandle发消息通知动画状态更新
                mAnimatorHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };


    }

    private void initHandler() {
        mAnimatorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (mCurrentState) {
                    case STARTING:
                        // 从开始动画转换好搜索动画
                        mCurrentState = State.SEARCHING;
                        // getHandle发消息通知动画状态更新
                        mAnimatorHandler.sendEmptyMessage(0);
                        break;
                    case SEARCHING:
                        mSearchingAnimator.start();
                        break;
                    case ENDING:
                        // 从结束动画转变为无状态
                        mCurrentState = State.NONE;
                        break;
                }
            }
        };
    }

    private void initAnimator() {
        mStartingAnimator = ValueAnimator.ofFloat(0, 1).setDuration(defaultDuration);
        mSearchingAnimator = ValueAnimator.ofFloat(0, 1).setDuration(defaultDuration);
        mEndingAnimator = ValueAnimator.ofFloat(1, 0).setDuration(defaultDuration);
    }

    private void addListener() {
        mStartingAnimator.addUpdateListener(mUpdateListener);
        mSearchingAnimator.addUpdateListener(mUpdateListener);
        mEndingAnimator.addUpdateListener(mUpdateListener);

        mStartingAnimator.addListener(mAnimatorListener);
        mSearchingAnimator.addListener(mAnimatorListener);
        mEndingAnimator.addListener(mAnimatorListener);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSearch(canvas);
    }

    private void drawSearch(Canvas canvas) {

        canvas.translate(mViewWidth / 2, mViewHeight / 2);

        canvas.drawColor(backgroundColor);

        switch (mCurrentState) {
            case NONE:
                canvas.drawPath(path_srarch, mPaint);
                break;
            case STARTING:
                mMeasure.setPath(path_srarch, false);
                Path dst = new Path();
                mMeasure.getSegment(mMeasure.getLength() * mAnimatorValue, mMeasure.getLength(), dst, true);
                canvas.drawPath(dst, mPaint);
                break;
            case SEARCHING:
                mMeasure.setPath(path_circle, false);
                Path dst2 = new Path();
                float stop = mMeasure.getLength() * mAnimatorValue;
                float start = (float) (stop - ((0.5 - Math.abs(mAnimatorValue - 0.5)) * 200f));
                mMeasure.getSegment(start, stop, dst2, true);
                canvas.drawPath(dst2, mPaint);
                break;
            case ENDING:
                mMeasure.setPath(path_srarch, false);
                Path dst3 = new Path();
                mMeasure.getSegment(mMeasure.getLength() * mAnimatorValue, mMeasure.getLength(), dst3, true);
                canvas.drawPath(dst3, mPaint);
                break;
            default:
                break;
        }
    }


    /**
     * 开始搜索
     */
    public void setStartSearch() {
        //NONE 才可以执行搜索的开始操作
        if (mCurrentState == State.NONE || mCurrentState == State.ENDING) {
            addListener();
            // 进入开始动画
            mCurrentState = State.STARTING;
            mStartingAnimator.start();
        }
    }

    /**
     * 结束搜索
     */
    public void setEndSearch() {
        mCurrentState = State.ENDING;
        mEndingAnimator.start();
        //搜索结束移除所有的监听方法
        mStartingAnimator.removeAllListeners();
        mSearchingAnimator.removeAllListeners();
        mEndingAnimator.removeAllListeners();
    }

}
