package com.example.bosong.slideview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by boson on 2017/8/1.
 */

public class SlideView extends LinearLayout {
    private static final String TAG = "SlideView";
    private ViewDragHelper mViewDragHelper;

    private int mDragLeftTo;
    private float mDownX;
    private float mDownY;

    private View mDragView;
    private View mOptionsView;
    private boolean mIsExpanded; // 是否已展开
    private OnSlideListener mListener;

    public SlideView(@NonNull Context context) {
        super(context);
        init();
    }

    public SlideView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        initStyle(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SlideView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initStyle(context, attrs, defStyleAttr);
    }

    private void init() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
    }

    private void initStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SlideView, defStyleAttr, 0);
        int dragLayoutId = attributes.getResourceId(R.styleable.SlideView_dragLayout, 0);
        if(dragLayoutId != 0) {
            mDragView = LayoutInflater.from(context).inflate(dragLayoutId, this, false);
            addView(mDragView);
        }
        int optionsLayoutId = attributes.getResourceId(R.styleable.SlideView_optionsLayout, 0);
        if(optionsLayoutId != 0) {
            mOptionsView = LayoutInflater.from(context).inflate(optionsLayoutId, this, false);
            addView(mOptionsView);
        }
    }

    public void setOnSlideLeftListener(OnSlideListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            mDownX = ev.getX();
            mDownY = ev.getY();
            Log.d(TAG, "onInterceptTouchEvent: action -> DOWN, mDownX ->" + mDownX + " mDownY ->" + mDownY);
        } else if (action == MotionEvent.ACTION_MOVE) {
            float deltaX = Math.abs(ev.getX() - mDownX);
            float deltaY = Math.abs(ev.getY() - mDownY);
            Log.d(TAG, "onInterceptTouchEvent: action -> MOVE, ev.getX() ->" + ev.getX() + " ev.getY() ->" + ev.getY() + " deltaX ->" + deltaX + " deltaY ->" + deltaY);
            if(deltaX > deltaY) { // 横向滑动的时候禁止父布局的拦截事件，如不允许父布局滚动
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mViewDragHelper.cancel();
            return false;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            mDownX = event.getX();
            mDownY = event.getY();
            Log.d(TAG, "onTouchEvent: action -> DOWN, mDownX ->" + mDownX + " mDownY ->" + mDownY);
        } else if (action == MotionEvent.ACTION_MOVE) {
            float deltaX = Math.abs(event.getX() - mDownX);
            float deltaY = Math.abs(event.getY() - mDownY);
            Log.d(TAG, "onTouchEvent: action -> MOVE, event.getX() ->" + event.getX() + " event.getY() ->" + event.getY() + " deltaX ->" + deltaX + " deltaY ->" + deltaY);
            if(deltaX > deltaY) { // 横向滑动的时候禁止父布局的拦截事件，如不允许父布局滚动
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void expand() {
        boolean shouldContinue = mViewDragHelper.smoothSlideViewTo(mDragView, -getPaddingLeft() - mOptionsView.getWidth(), 0);
        if(shouldContinue) {
            ViewCompat.postInvalidateOnAnimation(SlideView.this);
        }
        mIsExpanded = true;
        if(mListener != null) {
            mListener.onSlided(true);
        }
        Log.d(TAG, "expand: should continue ->" + shouldContinue);
    }

    public void collapse() {
        boolean shouldContinue = mViewDragHelper.smoothSlideViewTo(mDragView, 0, 0);
        if(shouldContinue) {
            ViewCompat.postInvalidateOnAnimation(SlideView.this);
        }
        mIsExpanded = false;
        if(mListener != null) {
            mListener.onSlided(false);
        }
        Log.d(TAG, "collapse: should continue ->" + shouldContinue);
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragView;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.d(TAG, "onViewReleased: xvel ->" + xvel + " yvel ->" + yvel);
            float velThreshold = 4000;
            int threshold = mOptionsView.getWidth()/2;
            int dragLeft = Math.abs(mDragLeftTo);
            Log.d(TAG, "onViewReleased: dragLeft ->" + dragLeft + " threshold ->" + threshold);
            if(xvel <= 0) { // 左滑
                if(-xvel > velThreshold || dragLeft >= threshold) {
                    expand();
                } else {
                    collapse();
                }
            } else if(xvel > 0){
                if(xvel > velThreshold || dragLeft <=  threshold) {
                    collapse();
                } else {
                    expand();
                }
            }
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int newLeft = Math.min(
                    Math.max((-getPaddingLeft() - mOptionsView.getWidth()), left), 0);
            Log.d(TAG, "clampViewPositionHorizontal: left ->" + left + " newLeft ->" + newLeft + " dx ->" + dx);
            mDragLeftTo = newLeft;
            return newLeft;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            mOptionsView.offsetLeftAndRight(dx);
            invalidate();
        }
    }

    public interface OnSlideListener {
        void onSlided(boolean expanded);
    }
}
