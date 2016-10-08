package com.wanghaisheng.view.stickynavlayout;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.wanghaisheng.stickynavlayout.R;

/**
 * Author: sheng on 2016/10/7 11:39
 * Email: 1392100700@qq.com
 */

public class StickyNavLayout2 extends LinearLayout {

    //顶部简介区域
    private View mTopView;

    //中间Tab导航区域
    private View mNavView;
    //下部内容区域
    private ViewPager mViewPager;

    //顶部区域高度
    private int mTopViewHeight;

    //内容区域ViewPager中的ScrollView
    private ViewGroup mInnerScrollView;

    private OverScroller mOverScroller;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;

    //拖动的最小值，用以区别是点击还是拖动事件
    private int mTouchSlop;

    //上一次移动的位置，用于处理Move事件
    private float mLastY;

    //drag的标志
    private boolean mDragging;

    public StickyNavLayout2(Context context) {
        this(context,null);
    }

    public StickyNavLayout2(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public StickyNavLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //设置垂直布局
        setOrientation(LinearLayout.VERTICAL);

        mOverScroller = new OverScroller(context);
        mVelocityTracker = VelocityTracker.obtain();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTopView = findViewById(R.id.stickynavlayout_topview);
        mNavView = findViewById(R.id.stickynavlayout_indicator);
        View view = findViewById(R.id.stickynavlayout_viewpager);

        if(!(view instanceof ViewPager)) {
            throw new RuntimeException("stickynavlayout_viewpager show used by ViewPager !");
        }

        mViewPager = (ViewPager) view;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTopViewHeight = mTopView.getMeasuredHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup.LayoutParams lp = mViewPager.getLayoutParams();
        lp.height = getMeasuredHeight() - mNavView.getMeasuredHeight();
    }

    /**
     * touch事件，模板代码
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(event);

        int action = event.getAction();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //当手指按下时，停止滑动动画
                if(!mOverScroller.isFinished()) {
                    mOverScroller.abortAnimation();
                }

                mLastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mLastY;
                //判断滑动状态
                if(!mDragging && Math.abs(dy)>mTouchSlop) {
                    mDragging = true;
                }

                if(mDragging) {
                    scrollBy(0, (int) -dy);
                }

                mLastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                recycleVelocityTracker();
                if(!mOverScroller.isFinished()) {
                    mOverScroller.abortAnimation();
                }

                break;
            case MotionEvent.ACTION_UP:
                mDragging = false;
                //当手指立刻屏幕时，获得速度，作为fling的初始速度
                mVelocityTracker.computeCurrentVelocity(1000,mMaximumVelocity);
                int velocityY = (int) mVelocityTracker.getYVelocity();
                if(Math.abs(velocityY)>mMinimumVelocity) {
                    // 由于坐标轴正方向问题，要加负号。
                    fling(-velocityY);
                }
                recycleVelocityTracker();
                break;

        }

        return super.onTouchEvent(event);
    }

    /**
     * 实现当手指离开屏幕之后，根据手指滑动速度，再滑动一段距离
     * @param velocityY
     */
    private void fling(int velocityY) {
        mOverScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mTopViewHeight);
        invalidate();
    }

    /**
     * 边界检测
     * @param x
     * @param y
     */
    @Override
    public void scrollTo(int x, int y) {
        if(y < 0) {
            y = 0;
        }
        if(y > mTopViewHeight) {
            y = mTopViewHeight;
        }

        if(y != getScrollY()) {
            super.scrollTo(x,y);
        }

    }

    @Override
    public void computeScroll()
    {
        //判断scroller是否结束，则scrollTo到相应的位置
        if (mOverScroller.computeScrollOffset())
        {
            scrollTo(0, mOverScroller.getCurrY());
            invalidate();
        }

    }

    /**
     * 根据滑动位置判断是否需要拦截滑动事件
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mLastY;
                getCurrentScrollView();
                //判断是否足够到达滑动的距离
                if(Math.abs(dy) > mTouchSlop) {
                    mDragging = true;
                    Log.d("TAG","minnerscrollview.getscrolly ="+mInnerScrollView.getScrollY()+" getscrolly ="+getScrollY()+" dy ="+dy);
                    /**
                     * 判断是否需要拦截事件，不传递给子View
                     * 有三种情况：
                     * 1、在topView边界中间的，即是：getScrollY()<mTopViewHeight&&getScrollY()>0
                     * 2、topView已经滑动到最下面，而且打算向上滑动时：getScrollY()==0&&dy<0
                     * 3、topView已经滑动到最上面，而且打算向下滑动时：getScrollY()==mTopViewHeight&&dy>0
                     */

                    if((getScrollY()<mTopViewHeight&&getScrollY()>0)||(getScrollY()==0&&dy<0)||(getScrollY()==mTopViewHeight&&dy>0)) {
                        return true;
                    }
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private void getCurrentScrollView() {

        int currentItem = mViewPager.getCurrentItem();
        PagerAdapter a = mViewPager.getAdapter();
        if (a instanceof FragmentPagerAdapter) {
            FragmentPagerAdapter fadapter = (FragmentPagerAdapter) a;
            Fragment item = (Fragment) fadapter.instantiateItem(mViewPager,
                    currentItem);
            mInnerScrollView = (ViewGroup) (item.getView()
                    .findViewById(R.id.stickynavlayout_innerscrollview));
        } else if (a instanceof FragmentStatePagerAdapter) {
            FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
            Fragment item = (Fragment) fsAdapter.instantiateItem(mViewPager,
                    currentItem);
            mInnerScrollView = (ViewGroup) (item.getView()
                    .findViewById(R.id.stickynavlayout_innerscrollview));
        }

    }

}
