# SearchView
自定义view————搜索动画

经常看iOS搜索的时候有个动画，最近在学习自定义view，就寻思的也弄个搜索动画，先上效果图。

![效果图](/images/search.gif)

具体实现思路

### 先画整体显示的搜索及其圆圈

先把搜索动画显示的整体效果全部画出来，然后再做动画，重点就是把Path 的走向要把握好

```

  private void initPath() {
        path_srarch = new Path();
        path_circle = new Path();

        mMeasure = new PathMeasure();

        // 注意,不要到360度,否则内部会自动优化,测量不能取到需要的数值
        //默认设置的是搜索的圆是最外层圆的一半
        RectF oval1 = new RectF(-50, -50, 50, 50);          // 放大镜圆环
        path_srarch.addArc(oval1, 45, 359.9f);

        RectF oval2 = new RectF(-100, -100, 100, 100);      // 外部圆环
        path_circle.addArc(oval2, 45, -359.9f);

        float[] pos = new float[2];

        mMeasure.setPath(path_circle, false);               // 放大镜把手的位置
        mMeasure.getPosTan(0, pos, null);

        path_srarch.lineTo(pos[0], pos[1]);                 // 放大镜把手

        Log.i("TAG", "pos=" + pos[0] + ":" + pos[1]);
    }

```

### 动画状态与时间关联

此处使用的是 ValueAnimator，它可以将一段时间映射到一段数值上，随着时间变化不断的更新数值，并且可以使用插值器开控制数值变化规律(此处使用的是默认插值器)。


```
mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //重点是这里
                mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        };
                
```

### 具体绘制

绘制部分是根据 当前状态以及从 ValueAnimator 获得的数值来截取 Path 中合适的部分绘制出来。

```

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
    

```

写到这里再把其他的补充上，手动掉开始搜索动画，count>2结束搜索动画，这就是一个简单的搜索动画了，缺陷就是不能自己去设置开始，结束，附上这部分代码

```
public class SearchView extends View {

    // 画笔
    private Paint mPaint;

    // View 宽高
    private int mViewWidth;
    private int mViewHeight;

    public SearchView(Context context) {
        this(context,null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAll();
    }

    public void initAll() {

        initPaint();

        initPath();

        initListener();

        initHandler();

        initAnimator();

        // 进入开始动画
        mCurrentState = State.STARTING;
        mStartingAnimator.start();

    }

    // 这个视图拥有的状态
    public  enum State {
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

    // 判断是否已经搜索结束
    private boolean isOver = false;

    private int count = 0;



    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(15);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
    }

    private void initPath() {
        path_srarch = new Path();
        path_circle = new Path();

        mMeasure = new PathMeasure();

        // 注意,不要到360度,否则内部会自动优化,测量不能取到需要的数值
        RectF oval1 = new RectF(-50, -50, 50, 50);          // 放大镜圆环
        path_srarch.addArc(oval1, 45, 359.9f);

        RectF oval2 = new RectF(-100, -100, 100, 100);      // 外部圆环
        path_circle.addArc(oval2, 45, -359.9f);

        float[] pos = new float[2];

        mMeasure.setPath(path_circle, false);               // 放大镜把手的位置
        mMeasure.getPosTan(0, pos, null);

        path_srarch.lineTo(pos[0], pos[1]);                 // 放大镜把手

        Log.i("TAG", "pos=" + pos[0] + ":" + pos[1]);
    }

    private void initListener() {
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        };

        mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // getHandle发消息通知动画状态更新
                mAnimatorHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

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
                        isOver = false;
                        mCurrentState = State.SEARCHING;
                        mStartingAnimator.removeAllListeners();
                        mSearchingAnimator.start();
                        break;
                    case SEARCHING:
                        if (!isOver) {  // 如果搜索未结束 则继续执行搜索动画
                            mSearchingAnimator.start();
                            Log.e("Update", "RESTART");

                            count++;
                            if (count>2){       // count大于2则进入结束状态
                                isOver = true;
                            }
                        } else {        // 如果搜索已经结束 则进入结束动画
                            mCurrentState = State.ENDING;
                            mEndingAnimator.start();
                        }
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

        mPaint.setColor(Color.WHITE);


        canvas.translate(mViewWidth / 2, mViewHeight / 2);

        canvas.drawColor(Color.parseColor("#0082D7"));

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
        }
    }
}

```

### 接下来设置开始搜索及其结束搜索的方法

开始搜索结合ValueAnimator中的方法：mStartingAnimator.start();
结束搜索同理

```

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


```

### 属性采用xml／代码双设置

将一些可采取动态设置的属性写成动态设置

attrs.xml中设置相关属性

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="SearchView">
        <attr name="circleVaule" format="float"/>
        <attr name="backgroundColor" format="color"/>
        <attr name="paintColor" format="color"/>
        <attr name="paintStrokeWidth" format="float"/>
        <attr name="divisor" format="float"/>
    </declare-styleable>
</resources>

```

代码中将attrs.xml的属性值获取

```
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
```

代码中报方法来设置这些属性

```

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

```

到此为止，附上整体代码

```
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


```

[个人博客](https://madreain.github.io)





