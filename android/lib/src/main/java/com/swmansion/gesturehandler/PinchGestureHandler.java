package com.swmansion.gesturehandler;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class PinchGestureHandler extends GestureHandler<PinchGestureHandler> {

  private long mLastTime;
  private double mLastScaleFactor;
  private double mLastVelocity;

  private float mStartingSpan;
  private float mLastSpan;
  private float mLastX, mLastY;

  public PinchGestureHandler() {
  }

  private float getX(MotionEvent event) {
    float totalX = 0;
    int pointers = event.getPointerCount();
    for (int i = 0; i < pointers; i++) {
      int id = event.getPointerId(i);
      totalX += event.getX(id);
    }
    return totalX / pointers;
  }

  private float getY(MotionEvent event) {
    float totalY = 0;
    int pointers = event.getPointerCount();
    for (int i = 0; i < pointers; i++) {
      int id = event.getPointerId(i);
      totalY += event.getY(id);
    }
    return totalY / pointers;
  }

  private float getSpan(MotionEvent event) {
    float minX = Integer.MAX_VALUE;
    float minY = Integer.MAX_VALUE;
    float maxX = Integer.MIN_VALUE;
    float maxY = Integer.MIN_VALUE;
    for (int i = 0; i < event.getPointerCount(); i++) {
      int id = event.getPointerId(i);
      float x = event.getX(id);
      float y = event.getY(id);
      minX = Math.min(minX, x);
      maxX = Math.max(maxX, x);
      minY = Math.min(minY, y);
      maxY = Math.max(maxY, y);
    }
    float spanX = maxX - minX;
    float spanY = maxY - minY;
    return (float)Math.sqrt(spanX * spanX + spanY * spanY);
  }

  @Override
  protected void onHandle(MotionEvent event) {
    int state = getState();
    int action = event.getActionMasked();

    if (state == STATE_UNDETERMINED && event.getPointerCount() >= 2) {
      mLastX = getX(event);
      mLastY = getY(event);
      mLastTime = event.getEventTime();
      mStartingSpan = getSpan(event);
      
      begin();
    } else if (event.getPointerCount() >= 2) {
      mLastX = getX(event);
      mLastY = getY(event);
      mLastSpan = getSpan(event);
      double mPrevScaleFactor = mLastScaleFactor;
      mLastScaleFactor = mLastSpan / mStartingSpan;
      long mPrevTime = mLastTime;
      mLastTime = event.getEventTime();
      long delta = mLastTime - mPrevTime;
      if (delta > 0) {
        mLastVelocity = (mLastScaleFactor - mPrevScaleFactor) / delta;
      }
    }

    if (action == MotionEvent.ACTION_UP) {
      if (state == STATE_ACTIVE || state == STATE_BEGAN) {
        end();
      } else {
        fail();
      }
    } else if (action == MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount() > 10) {
      // When new finger is placed down (POINTER_DOWN) we check if MAX_POINTERS is not exceeded
      if (state == STATE_ACTIVE) {
        cancel();
      } else {
        fail();
      }
    } else if (action == MotionEvent.ACTION_POINTER_UP && state == STATE_ACTIVE
            && event.getPointerCount() < 1) {
      // When finger is lifted up (POINTER_UP) and the number of pointers falls below MIN_POINTERS
      // threshold, we only want to take an action when the handler has already activated. Otherwise
      // we can still expect more fingers to be placed on screen and fulfill MIN_POINTERS criteria.
      fail();
    } else if (state == STATE_BEGAN) {
      activate();
    }
  }

  @Override
  protected void onReset() {
    mLastVelocity = 0f;
    mLastScaleFactor = 1f;
  }

  public double getScale() {
    return mLastScaleFactor;
  }

  public double getVelocity() {
    return mLastVelocity;
  }

  public float getFocalPointX() {
    return mLastX;
  }

  public float getFocalPointY() {
    return mLastY;
  }
}
