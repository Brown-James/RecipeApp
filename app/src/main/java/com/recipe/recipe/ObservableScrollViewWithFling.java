package com.recipe.recipe;

import android.content.Context;
import android.util.AttributeSet;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;

/**
 * Created by james on 17/08/16.
 * https://github.com/SnowdogApps/Material-Flexible-Space-Header/blob/master/MaterialFlexibleSpaceHeader/app/src/main/java/pl/snowdog/materialflexiblespaceheader/view/ObservableScrollViewWithFling.java
 */
public class ObservableScrollViewWithFling extends com.github.ksoichiro.android.observablescrollview.ObservableScrollView {
    private static String TAG = "ObservableScrollViewWithFling";

    private static final int DELAY_MILLIS = 50;

    public interface OnFlingListener {
        public void onFlingStarted();
        public void onFlingStopped();
    }

    private OnFlingListener mFlingListener;
    private Runnable mScrollChecker;
    private int mPreviousPosition;

    public ObservableScrollViewWithFling(Context context) {
        this(context, null, 0);
    }

    public ObservableScrollViewWithFling(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ObservableScrollViewWithFling(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mScrollChecker = new Runnable() {
            @Override
            public void run() {
                int position = getScrollY();
                if(mPreviousPosition - position == 0) {
                    mFlingListener.onFlingStopped();
                    removeCallbacks(mScrollChecker);
                } else {
                    mPreviousPosition = getScrollY();
                    postDelayed(mScrollChecker, DELAY_MILLIS);
                }
            }
        };
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);

        if(mFlingListener != null) {
            mFlingListener.onFlingStarted();
            post(mScrollChecker);
        }
    }

    public OnFlingListener getOnFlingListener() {
        return mFlingListener;
    }

    public void setOnFlingListener(OnFlingListener mOnFlingListener) {
        this.mFlingListener = mOnFlingListener;
    }
}
