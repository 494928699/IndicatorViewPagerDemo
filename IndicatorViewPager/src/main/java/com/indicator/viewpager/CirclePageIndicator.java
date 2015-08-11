/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indicator.viewpager;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

/**
 * Draws circles (one for each com.xmchoice.yyxsjcontrol.view). The current
 * com.xmchoice.yyxsjcontrol.view position is filled and
 * others are only stroked.
 *
 * 修改过的pagerindicator，支持viewpager无限张的时候设置点的个数
 *
 * 原本默认 点之间的间隙为点的半径，现在修改为：默认为点的半径，xml里面可以设置  app:pointMargin来修改
 *
 * 修改了点的x轴其实位置，加了半个圆之间的间隙(pointmargin/2)，确保完全居中
 *
 * 目前只是处理了  水平方向。垂直放暂未处理(实际场景用的不多)
 */
public class CirclePageIndicator extends View implements PageIndicator {
  private static final int INVALID_POINTER = -1;

  private float mRadius;
  private final Paint mPaintPageFill = new Paint(ANTI_ALIAS_FLAG);
  private final Paint mPaintStroke = new Paint(ANTI_ALIAS_FLAG);
  private final Paint mPaintFill = new Paint(ANTI_ALIAS_FLAG);
  private ViewPager mViewPager;
  private ViewPager.OnPageChangeListener mListener;
  private int mCurrentPage;
  private int mSnapPage;
  private float mPageOffset;
  private int mScrollState;
  private int mOrientation;
  private float mPointMargin;
  private boolean mCentered;
  private boolean mSnap;

  private int mTouchSlop;
  private float mLastMotionX = -1;
  private int mActivePointerId = INVALID_POINTER;
  private boolean mIsDragging;
  private int mPointCount = -1;

  private int mPaddingSide = 0;

  public CirclePageIndicator(Context context) {
    this(context, null);
  }

  public CirclePageIndicator(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.vpiCirclePageIndicatorStyle);
  }

  public CirclePageIndicator(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    if (isInEditMode()) return;

    // Load defaults from resources
    final Resources res = getResources();
    final int defaultPageColor = res.getColor(R.color.default_circle_indicator_page_color);
    final int defaultFillColor = res.getColor(R.color.default_circle_indicator_fill_color);
    final int defaultOrientation = res.getInteger(R.integer.default_circle_indicator_orientation);
    final int defaultStrokeColor = res.getColor(R.color.default_circle_indicator_stroke_color);
    final float defaultStrokeWidth =
        res.getDimension(R.dimen.default_circle_indicator_stroke_width);
    final float defaultRadius = res.getDimension(R.dimen.default_circle_indicator_radius);
    final boolean defaultCentered = res.getBoolean(R.bool.default_circle_indicator_centered);
    final boolean defaultSnap = res.getBoolean(R.bool.default_circle_indicator_snap);
    final float pointMargin = res.getDimension(R.dimen.default_circle_point_margin);
    // Retrieve styles attributes
    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.CirclePageIndicator, defStyle, 0);

    mCentered = a.getBoolean(R.styleable.CirclePageIndicator_centered, defaultCentered);
    mOrientation =
        a.getInt(R.styleable.CirclePageIndicator_android_orientation, defaultOrientation);
    mPaintPageFill.setStyle(Style.FILL);
    mPaintPageFill.setColor(
        a.getColor(R.styleable.CirclePageIndicator_pageColor, defaultPageColor));
    mPaintStroke.setStyle(Style.STROKE);
    mPaintStroke.setColor(
        a.getColor(R.styleable.CirclePageIndicator_strokeColor, defaultStrokeColor));
    mPaintStroke.setStrokeWidth(
        a.getDimension(R.styleable.CirclePageIndicator_strokeWidth, defaultStrokeWidth));
    mPaintFill.setStyle(Style.FILL);
    mPaintFill.setColor(a.getColor(R.styleable.CirclePageIndicator_fillColor, defaultFillColor));
    mRadius = a.getDimension(R.styleable.CirclePageIndicator_radius, defaultRadius);
    mSnap = a.getBoolean(R.styleable.CirclePageIndicator_snap, defaultSnap);
    mPointMargin = a.getDimension(R.styleable.CirclePageIndicator_pointMargin, pointMargin);

    Drawable background = a.getDrawable(R.styleable.CirclePageIndicator_android_background);
    if (background != null) {
      setBackgroundDrawable(background);
    }

    a.recycle();

    final ViewConfiguration configuration = ViewConfiguration.get(context);
    mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
  }

  public void setCentered(boolean centered) {
    mCentered = centered;
    invalidate();
  }

  public boolean isCentered() {
    return mCentered;
  }

  public void setPageColor(int pageColor) {
    mPaintPageFill.setColor(pageColor);
    invalidate();
  }

  public int getPageColor() {
    return mPaintPageFill.getColor();
  }

  public void setFillColor(int fillColor) {
    mPaintFill.setColor(fillColor);
    invalidate();
  }

  public int getFillColor() {
    return mPaintFill.getColor();
  }

  public void setOrientation(int orientation) {
    switch (orientation) {
      case HORIZONTAL:
      case VERTICAL:
        mOrientation = orientation;
        requestLayout();
        break;

      default:
        throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.");
    }
  }

  public int getOrientation() {
    return mOrientation;
  }

  public void setStrokeColor(int strokeColor) {
    mPaintStroke.setColor(strokeColor);
    invalidate();
  }

  public int getStrokeColor() {
    return mPaintStroke.getColor();
  }

  public float getmPointMargin() {
    return mPointMargin;
  }

  public void setPointMargin(float mPointMargin) {
    this.mPointMargin = mPointMargin;
    invalidate();
  }

  public void setStrokeWidth(float strokeWidth) {
    mPaintStroke.setStrokeWidth(strokeWidth);
    invalidate();
  }

  public float getStrokeWidth() {
    return mPaintStroke.getStrokeWidth();
  }

  public void setRadius(float radius) {
    mRadius = radius;
    invalidate();
  }

  public float getRadius() {
    return mRadius;
  }

  public void setSnap(boolean snap) {
    mSnap = snap;
    invalidate();
  }

  public boolean isSnap() {
    return mSnap;
  }

  public int getPaddingSide() {
    return mPaddingSide;
  }

  public void setPaddingSide(int paddingSide) {
    mPaddingSide = paddingSide;
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mViewPager == null) {
      return;
    }
    final int count;

    if (mPointCount != -1) {
      count = mPointCount;
    } else {
      count = mViewPager.getAdapter().getCount();
    }

    if (count == 0) {
      return;
    }
    if (mPointCount != -1) {
      if (mCurrentPage % mPointCount >= count) {
        setCurrentItem(count - 1);
        return;
      }
    } else {
      if (mCurrentPage >= count) {
        setCurrentItem(count - 1);
        return;
      }
    }

    int longSize;
    int longPaddingBefore;
    int longPaddingAfter;
    int shortPaddingBefore;
    if (mOrientation == HORIZONTAL) {
      longSize = getWidth();
      longPaddingBefore = getPaddingLeft();
      longPaddingAfter = getPaddingRight();
      shortPaddingBefore = getPaddingTop();
    } else {
      longSize = getHeight();
      longPaddingBefore = getPaddingTop();
      longPaddingAfter = getPaddingBottom();
      shortPaddingBefore = getPaddingLeft();
    }

    final float threeRadius;

    if (mPointMargin < mRadius) {
      threeRadius = mRadius * 3;
    } else {
      threeRadius = mRadius * 2 + mPointMargin;
    }

    final float shortOffset = shortPaddingBefore + mRadius;
    float longOffset = longPaddingBefore + mRadius;
    if (mCentered) {
      longOffset +=
          ((longSize - longPaddingBefore - longPaddingAfter) / 2.0f) - ((count * threeRadius)
              / 2.0f);
    }

    float dX;
    float dY;

    float pageFillRadius = mRadius;
    if (mPaintStroke.getStrokeWidth() > 0) {
      pageFillRadius -= mPaintStroke.getStrokeWidth() / 2.0f;
    }

    // Draw stroked circles
    for (int iLoop = 0; iLoop < count; iLoop++) {
      float drawLong = longOffset + (iLoop * threeRadius);
      if (mOrientation == HORIZONTAL) {
        if (mPaddingSide == 0) {
          dX = drawLong + ((threeRadius - mRadius * 2) / 2);
        } else if (mPaddingSide < 0) {
          dX = drawLong + ((threeRadius - mRadius * 2) / 2) - (longOffset + mPaddingSide);
        } else {
          dX = drawLong + ((threeRadius - mRadius * 2) / 2) + (longOffset - mPaddingSide);
        }
        dY = shortOffset;
      } else {
        dX = shortOffset;
        dY = drawLong;
      }
      // Only paint fill if not completely transparent
      if (mPaintPageFill.getAlpha() > 0) {
        canvas.drawCircle(dX, dY, pageFillRadius, mPaintPageFill);
      }

      // Only paint stroke if a stroke width was non-zero
      if (pageFillRadius != mRadius) {
        canvas.drawCircle(dX, dY, mRadius, mPaintStroke);
      }
    }

    // Draw the filled circle according to the current scroll
    float cx;

    if (mPointCount != -1) {
      cx = (mSnap ? mSnapPage : mCurrentPage % mPointCount) * threeRadius;
    } else {
      cx = (mSnap ? mSnapPage : mCurrentPage) * threeRadius;
    }

    if (!mSnap) {
      cx += mPageOffset * threeRadius;
    }
    if (mOrientation == HORIZONTAL) {

      if (mPaddingSide == 0) {
        dX = longOffset + cx + ((threeRadius - mRadius * 2) / 2);
      } else if (mPaddingSide < 0) {
        dX = longOffset + cx + ((threeRadius - mRadius * 2) / 2) - (longOffset + mPaddingSide);
      } else {
        dX = longOffset + cx + ((threeRadius - mRadius * 2) / 2) + (longOffset - mPaddingSide);
      }

      dY = shortOffset;
    } else {
      dX = shortOffset;
      dY = longOffset + cx;
    }
    canvas.drawCircle(dX, dY, mRadius, mPaintFill);
  }

  public boolean onTouchEvent(MotionEvent ev) {
    if (super.onTouchEvent(ev)) {
      return true;
    }
    if ((mViewPager == null) || (mViewPager.getAdapter().getCount() == 0)) {
      return false;
    }

    final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
        mLastMotionX = ev.getX();
        break;

      case MotionEvent.ACTION_MOVE: {
        final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
        final float x = MotionEventCompat.getX(ev, activePointerIndex);
        final float deltaX = x - mLastMotionX;

        if (!mIsDragging) {
          if (Math.abs(deltaX) > mTouchSlop) {
            mIsDragging = true;
          }
        }

        if (mIsDragging) {
          mLastMotionX = x;
          if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()) {
            mViewPager.fakeDragBy(deltaX);
          }
        }

        break;
      }

      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        if (!mIsDragging) {
          final int count;

          if (mPointCount != -1) {
            count = mPointCount;
          } else {
            count = mViewPager.getAdapter().getCount();
          }

          final int width = getWidth();
          final float halfWidth = width / 2f;
          final float sixthWidth = width / 6f;

          if ((mCurrentPage > 0) && (ev.getX() < halfWidth - sixthWidth)) {
            if (action != MotionEvent.ACTION_CANCEL) {
              mViewPager.setCurrentItem(mCurrentPage - 1);
            }
            return true;
          } else {
            if (mPointCount != -1) {
              if ((mCurrentPage % mPointCount < count - 1) && (ev.getX()
                  > halfWidth + sixthWidth)) {
                if (action != MotionEvent.ACTION_CANCEL) {
                  mViewPager.setCurrentItem(mCurrentPage + 1);
                }
                return true;
              }
            } else {
              if ((mCurrentPage < count - 1) && (ev.getX() > halfWidth + sixthWidth)) {
                if (action != MotionEvent.ACTION_CANCEL) {
                  mViewPager.setCurrentItem(mCurrentPage + 1);
                }
                return true;
              }
            }
          }
        }

        mIsDragging = false;
        mActivePointerId = INVALID_POINTER;
        if (mViewPager.isFakeDragging()) mViewPager.endFakeDrag();
        break;

      case MotionEventCompat.ACTION_POINTER_DOWN: {
        final int index = MotionEventCompat.getActionIndex(ev);
        mLastMotionX = MotionEventCompat.getX(ev, index);
        mActivePointerId = MotionEventCompat.getPointerId(ev, index);
        break;
      }

      case MotionEventCompat.ACTION_POINTER_UP:
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
          final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
          mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
        mLastMotionX =
            MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
        break;
    }

    return true;
  }

  @Override public void setViewPager(ViewPager view) {
    if (mViewPager == view) {
      return;
    }
    if (mViewPager != null) {
      mViewPager.setOnPageChangeListener(null);
    }
    if (view.getAdapter() == null) {
      throw new IllegalStateException("ViewPager does not have adapter instance.");
    }
    mViewPager = view;
    mViewPager.setOnPageChangeListener(this);
    invalidate();
  }

  @Override public void setViewPager(ViewPager view, int initialPosition) {
    setViewPager(view);
    setCurrentItem(initialPosition);
  }

  @Override public void setCurrentItem(int item) {
    if (mViewPager == null) {
      throw new IllegalStateException("ViewPager has not been bound.");
    }
    mViewPager.setCurrentItem(item);
    mCurrentPage = item;
    invalidate();
  }

  @Override public void notifyDataSetChanged() {
    invalidate();
  }

  @Override public void onPageScrollStateChanged(int state) {
    mScrollState = state;

    if (mListener != null) {
      mListener.onPageScrollStateChanged(state);
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    mCurrentPage = position;
    mPageOffset = positionOffset;

    if (mPointCount != -1) {
      if (position % mPointCount == mPointCount - 1) {
        if (positionOffset < 0.5) {
          mCurrentPage = position;
          mPageOffset = positionOffset;
        } else {
          mCurrentPage = position + 1;
          mPageOffset = positionOffset - 1;
        }
      }
    }

    invalidate();

    if (mListener != null) {
      mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }
  }

  @Override public void onPageSelected(int position) {
    if (mSnap || mScrollState == ViewPager.SCROLL_STATE_IDLE) {
      mCurrentPage = position;
      mSnapPage = position;
      invalidate();
    }

    if (mListener != null) {
      mListener.onPageSelected(position);
    }
  }

  @Override public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
    mListener = listener;
  }

  /*
   * (non-Javadoc)
   *
   * @see android.com.xmchoice.yyxsjcontrol.view.View#onMeasure(int, int)
   */
  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (mOrientation == HORIZONTAL) {
      setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
    } else {
      setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec));
    }
  }

  /**
   * Determines the width of this com.xmchoice.yyxsjcontrol.view
   *
   * @param measureSpec A measureSpec packed into an int
   * @return The width of the com.xmchoice.yyxsjcontrol.view, honoring constraints from measureSpec
   */
  private int measureLong(int measureSpec) {
    int result;
    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);

    if ((specMode == MeasureSpec.EXACTLY) || (mViewPager == null)) {
      // We were told how big to be
      result = specSize;
    } else {
      // Calculate the width according the views count
      final int count;

      if (mPointCount != -1) {
        count = mPointCount;
      } else {
        count = mViewPager.getAdapter().getCount();
      }

      result = (int) (getPaddingLeft() + getPaddingRight() + (count * 2 * mRadius)
          + (count - 1) * mRadius + 1);
      // Respect AT_MOST value if that was what is called for by
      // measureSpec
      if (specMode == MeasureSpec.AT_MOST) {
        result = Math.min(result, specSize);
      }
    }
    return result;
  }

  /**
   * Determines the height of this com.xmchoice.yyxsjcontrol.view
   *
   * @param measureSpec A measureSpec packed into an int
   * @return The height of the com.xmchoice.yyxsjcontrol.view, honoring constraints from measureSpec
   */
  private int measureShort(int measureSpec) {
    int result;
    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);

    if (specMode == MeasureSpec.EXACTLY) {
      // We were told how big to be
      result = specSize;
    } else {
      // Measure the height
      result = (int) (2 * mRadius + getPaddingTop() + getPaddingBottom() + 1);
      // Respect AT_MOST value if that was what is called for by
      // measureSpec
      if (specMode == MeasureSpec.AT_MOST) {
        result = Math.min(result, specSize);
      }
    }
    return result;
  }

  @Override public void onRestoreInstanceState(Parcelable state) {
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    mCurrentPage = savedState.currentPage;
    mSnapPage = savedState.currentPage;
    requestLayout();
  }

  @Override public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.currentPage = mCurrentPage;
    return savedState;
  }

  static class SavedState extends BaseSavedState {
    int currentPage;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      currentPage = in.readInt();
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeInt(currentPage);
    }

    @SuppressWarnings("UnusedDeclaration") public static final Creator<SavedState> CREATOR =
        new Creator<SavedState>() {
          @Override public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
          }

          @Override public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }

  /**
   * 设置无线张（如100000张）时，可以设置点的个数,默认情况下 不用设置
   */
  @Override public void setPointCount(int pointCount) {
    this.mPointCount = pointCount;
  }
}
