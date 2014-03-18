package com.ks.modernapplocker.common;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

/**
 * Animation that either expands or collapses a view by sliding it down to make it visible.
 * Or by sliding it up so it will hide. It will look like it slides behind the view above.
 *
 * @auther tjerk
 * @date 6/9/12 4:58 PM
 */
public class ExpandCollapseAnimation extends Animation {
    private View mAnimatedView;
    private int mEndHeight;
    private int mType;
    public final static int COLLAPSE = 1;
    public final static int EXPAND = 0;
    private RelativeLayout.LayoutParams mLayoutParams;

    public ExpandCollapseAnimation(View view, int type, int measureheight) {
    	Log.e("EXpand", "height : "+measureheight);
    	mEndHeight=494;
        mAnimatedView = view;
        if (mAnimatedView.getMeasuredHeight() == 0) {
            mEndHeight = measureheight;
        } else {
            mEndHeight = mAnimatedView.getMeasuredHeight();
        }
        mLayoutParams = ((RelativeLayout.LayoutParams) view.getLayoutParams());
        mType = type;
        if (mType == EXPAND) {
            mLayoutParams.bottomMargin = -mEndHeight;
        } else {
            mLayoutParams.bottomMargin = 0;
        }
        view.setVisibility(View.VISIBLE);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0f) {
            if (mType == EXPAND) {
                mLayoutParams.bottomMargin = -mEndHeight + (int) (mEndHeight * interpolatedTime);
            } else {
                mLayoutParams.bottomMargin = -(int) (mEndHeight * interpolatedTime);
            }
            Log.d("ExpandCollapseAnimation", "anim height " + mLayoutParams.bottomMargin);
            mAnimatedView.requestLayout();
        } else {
            if (mType == EXPAND) {
                mLayoutParams.bottomMargin = 0;
                mAnimatedView.requestLayout();
            } else {
                mLayoutParams.bottomMargin = -mEndHeight;
                mAnimatedView.setVisibility(View.GONE);
                mAnimatedView.requestLayout();
            }
        }
    }
}
