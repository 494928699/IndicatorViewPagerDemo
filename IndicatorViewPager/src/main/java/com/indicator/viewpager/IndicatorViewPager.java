package com.indicator.viewpager;/**
 * 作者：武凤 on 2015/8/11 00:05
 * 邮箱：ail36413@163.com
 */

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * User: 连武凤(ail36413@163.com)
 * Date: 2015-08-11
 * Time: 00:05
 *    FIXME
 *
 *
 *     app:iv_fillColor="#ff0000"    移动点的颜色
       app:iv_pageColor="#00ff00"    固定点的颜色
       app:iv_radius="5dp"           点的半径
       app:iv_pointMargin="10dp"     点之间的距离（不是圆心之间的距离）
      app:iv_strokeColor="#0000ff"   点的边框的颜色
      app:iv_strokeWidth="1dp"       点的边框的线宽
      app:iv_paddingBottom="40dp"    点距离viewpager 底部的距离
      app:iv_gravity="right"         点的位置(left、center、right)
      app:iv_paddingSide="40dp"      第一个点 或 最后一个点  距离 viewpager 边界的距离(app:iv_gravity="center"时 无效)

 *
 *
 */
public class IndicatorViewPager extends FrameLayout implements View.OnTouchListener {

  protected int mLayout;
  private int mFillColor;
  private int mPageColor;
  private int mStrokeColor;
  private float mStrokeWidth;
  private float mPointMargin;
  private float mRadius;
  private float mPaddingBottom;
  private ViewPager mVp_banner;
  private CirclePageIndicator mIndicator;

  private Runnable mRunnable = null;
  private int mGravity;
  private float mPaddingSide;

  public IndicatorViewPager(Context context) {
    super(context);
    initView();
  }

  public IndicatorViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    initAttrs(attrs);
    initView();
  }

  public IndicatorViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initAttrs(attrs);
    initView();
  }

  protected void initAttrs(AttributeSet attrs) {
    TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.indicator_viewpager);
    try {

      // Load defaults from resources
      final Resources res = getResources();
      final int defaultPageColor = res.getColor(R.color.default_circle_indicator_page_color);
      final int defaultFillColor = res.getColor(R.color.default_circle_indicator_fill_color);
      final int defaultStrokeColor = res.getColor(R.color.default_circle_indicator_stroke_color);
      final float defaultStrokeWidth =
          res.getDimension(R.dimen.default_circle_indicator_stroke_width);
      final float pointMargin = res.getDimension(R.dimen.default_circle_point_margin);
      final float defaultRadius = res.getDimension(R.dimen.default_circle_indicator_radius);

      mLayout = a.getResourceId(R.styleable.indicator_viewpager_iv_mainLayoutId,
          R.layout.layout_indicator_viewpager);

      mFillColor = a.getColor(R.styleable.indicator_viewpager_iv_fillColor, defaultFillColor);
      mPageColor = a.getColor(R.styleable.indicator_viewpager_iv_pageColor, defaultPageColor);
      mStrokeColor = a.getColor(R.styleable.indicator_viewpager_iv_strokeColor, defaultStrokeColor);
      mStrokeWidth =
          a.getDimension(R.styleable.indicator_viewpager_iv_strokeWidth, defaultStrokeWidth);
      mPointMargin = a.getDimension(R.styleable.indicator_viewpager_iv_pointMargin, pointMargin);

      mRadius = a.getDimension(R.styleable.indicator_viewpager_iv_radius, defaultRadius);

      mPaddingBottom =
          a.getDimension(R.styleable.indicator_viewpager_iv_paddingBottom, defaultRadius);

      mPaddingSide = a.getDimension(R.styleable.indicator_viewpager_iv_paddingSide, 0);

      mGravity = a.getInteger(R.styleable.indicator_viewpager_iv_gravity, 1);
    } finally {
      a.recycle();
    }
  }

  private void initView() {
    View view = LayoutInflater.from(getContext()).inflate(mLayout, this);
    mVp_banner = (ViewPager) view.findViewById(R.id.vp_banner);
    mIndicator = (CirclePageIndicator) view.findViewById(R.id.pager_indicator);

    mIndicator.setFillColor(mFillColor);
    mIndicator.setPageColor(mPageColor);
    mIndicator.setStrokeColor(mStrokeColor);
    mIndicator.setPointMargin(mPointMargin);
    mIndicator.setStrokeWidth(mStrokeWidth);
    mIndicator.setRadius(mRadius);

    if (mGravity == 0) {
      //左边
      mIndicator.setPaddingSide((int) -mPaddingSide);
    } else if (mGravity == 2) {
      //  右边
      mIndicator.setPaddingSide((int) mPaddingSide);
    } else {
      //  居中
      mIndicator.setPaddingSide(0);
    }
    mIndicator.setPadding(0, (int) mRadius, 0, (int) mPaddingBottom);
  }

  public void setAdapter(PagerAdapter adapter) {
    mVp_banner.setAdapter(adapter);
    mIndicator.setViewPager(mVp_banner);
  }

  public void setAdapter(PagerAdapter adapter, int pointCount, final int delayMillis) {

    mVp_banner.setAdapter(adapter);
    mIndicator.setPointCount(pointCount);// 无尽循环试，设置点的个数
    mIndicator.setViewPager(mVp_banner);// 必须先设置适配器
    //确保默认的时候处于第一个点
    mVp_banner.setCurrentItem(((adapter.getCount() / 2)/pointCount)*pointCount);

    if (mRunnable == null) {
      mRunnable = new Runnable() {
        @Override public void run() {
          mVp_banner.removeCallbacks(mRunnable);
          int NEXT = 0;
          if (mVp_banner.getCurrentItem() != mVp_banner.getAdapter().getCount() - 1) {
            NEXT = mVp_banner.getCurrentItem() + 1;
          }
          if (NEXT == 0) {
            mVp_banner.setCurrentItem(NEXT, false);
          } else {
            mVp_banner.setCurrentItem(NEXT, true);
          }
          mVp_banner.postDelayed(this, delayMillis);
        }
      };
    }
    mVp_banner.postDelayed(mRunnable, delayMillis);
    mVp_banner.setOnTouchListener(this);
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mVp_banner.removeCallbacks(mRunnable);
        break;
      case MotionEvent.ACTION_MOVE:
        mVp_banner.removeCallbacks(mRunnable);
        break;

      case MotionEvent.ACTION_UP:
        mVp_banner.removeCallbacks(mRunnable);
        mVp_banner.postDelayed(mRunnable, 2000);
        break;

      default:
        break;
    }
    return false;
  }
}
