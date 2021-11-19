package com.swmansion.gesturehandler;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class PinchGestureHandler extends GestureHandler<PinchGestureHandler> {
  private long time;
  private double scale;
  private double velocity;
  private float startingSpan;
  private float focalPointX, focalPointY;

  public PinchGestureHandler() {
  }

  private float getX(MotionEvent event) {
    float total = 0;
    int i = 0;
    for (; i < event.getPointerCount(); i++) {
      try {
        int id = event.getPointerId(i);
        total += event.getX(id);
      } catch (IllegalArgumentException ex) {
        ex.printStackTrace();
      }
    }
    return total / i;
  }

  private float getY(MotionEvent event) {
    float total = 0;
    int i = 0;
    for (; i < event.getPointerCount(); i++) {
      try {
        int id = event.getPointerId(i);
        total += event.getY(id);
      } catch (IllegalArgumentException ex) {
        ex.printStackTrace();
      }
    }
    return total / i;
  }

  private float getSpan(MotionEvent event) {
    float minX = Integer.MAX_VALUE;
    float minY = Integer.MAX_VALUE;
    float maxX = Integer.MIN_VALUE;
    float maxY = Integer.MIN_VALUE;
    for (int i = 0; i < event.getPointerCount(); i++) {
      try {
        int id = event.getPointerId(i);
        float x = event.getX(id);
        float y = event.getY(id);
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
      } catch (IllegalArgumentException ex) {
        ex.printStackTrace();
      }
    }
    float spanX = maxX - minX;
    float spanY = maxY - minY;
    return (float)Math.sqrt(spanX * spanX + spanY * spanY);
  }

  @Override
  protected void onHandle(MotionEvent event) {
    int state = getState();
    int action = event.getActionMasked();
    int activePointers = event.getPointerCount();
    if (action == MotionEvent.ACTION_POINTER_UP) {
      activePointers -= 1;
    }

    if (activePointers >= 2) {
      focalPointX = getX(event);
      focalPointY = getY(event);
      long prevTime = time;
      time = event.getEventTime();

      if (state == STATE_UNDETERMINED) {
        velocity = 0.0;
        scale = 1.0;
        startingSpan = getSpan(event);
        begin();
      } else {
        double prevScale = scale;
        float span = getSpan(event);
        
        scale = (double)(span / startingSpan);
        long delta = time - prevTime;
        if (delta > 0) {
          velocity = (scale - prevScale) / delta;
        }

        if (state == STATE_BEGAN) {
          activate();
        }
      }
    }

    if (state == STATE_ACTIVE && activePointers < 2) {
      end();
    } else if (action == MotionEvent.ACTION_UP) {
      fail();
    }
  }

  @Override
  protected void onReset() {
    velocity = 0.0;
    scale = 1.0;
  }

  public double getScale() {
    return scale;
  }

  public double getVelocity() {
    return velocity;
  }

  public float getFocalPointX() {
    return focalPointX;
  }

  public float getFocalPointY() {
    return focalPointY;
  }
}
