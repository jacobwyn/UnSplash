package com.jacob.unsplash.view.pager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JacobsImageView extends android.support.v7.widget.AppCompatImageView {
    private com.jacob.unsplash.view.pager.OnDragListener listener;
    private float mYDiffInTouchPointAndViewTopLeftCorner;
    private SpringAnimation mSpringTranslationYAnimation;

    public JacobsImageView(Context context) {
        super(context);
        init();
    }

    public JacobsImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JacobsImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(onTouchListener);
        mSpringTranslationYAnimation = new SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, 0);

        SpringForce springForceY = new SpringForce(0f);
        springForceY.setStiffness(SpringForce.STIFFNESS_MEDIUM);
        springForceY.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
        mSpringTranslationYAnimation.setSpring(springForceY);
        mSpringTranslationYAnimation.addUpdateListener((animation, value, velocity) -> {
            if (listener != null) {
                listener.onDragUpdate(value);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener onTouchListener = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mYDiffInTouchPointAndViewTopLeftCorner = event.getRawY() - v.getY();
                mSpringTranslationYAnimation.cancel();
                onStartDrag();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float newTopLeftY = event.getRawY() - mYDiffInTouchPointAndViewTopLeftCorner;
                setY(newTopLeftY);
                onDragUpdate(newTopLeftY);
                break;
            }
            case MotionEvent.ACTION_UP: {
                float newTop = event.getRawY() - mYDiffInTouchPointAndViewTopLeftCorner;
                boolean handled = onStopDrag(newTop);
                if (!handled) {
                    mSpringTranslationYAnimation.start();
                }
                break;
            }
        }
        return true;
    };

    private void onStartDrag() {
        if (listener != null) {
            listener.onStartDrag();
        }
    }

    private void onDragUpdate(float newTopLeftY) {
        if (listener != null) {
            listener.onDragUpdate(newTopLeftY);
        }
    }

    private boolean onStopDrag(float newTop) {
        if (listener != null) {
            return listener.onStopDrag(newTop);
        } else {
            return false;
        }
    }

    void setListener(com.jacob.unsplash.view.pager.OnDragListener listener) {
        this.listener = listener;
    }
}
