# IndicatorViewPagerDemo
IndicatorViewPager




使用方法：
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    >
    <com.indicator.viewpager.IndicatorViewPager
        android:id="@+id/ivp_test1"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        app:iv_fillColor="#ff0000"
        app:iv_pageColor="#00ff00"
        app:iv_radius="5dp"
        app:iv_pointMargin="10dp"
        app:iv_strokeColor="#0000ff"
        app:iv_strokeWidth="1dp"
        app:iv_paddingBottom="40dp"
        app:iv_gravity="right"
        app:iv_paddingSide="40dp"
></com.indicator.viewpager.IndicatorViewPager>
</LinearLayout>

属性描述
app:iv_fillColor="#ff0000"    移动点的颜色
app:iv_pageColor="#00ff00"    固定点的颜色
app:iv_radius="5dp"           点的半径
app:iv_pointMargin="10dp"     点之间的距离（不是圆心之间的距离）
app:iv_strokeColor="#0000ff"   点的边框的颜色
app:iv_strokeWidth="1dp"       点的边框的线宽
app:iv_paddingBottom="40dp"    点距离viewpager 底部的距离
app:iv_gravity="right"         点的位置(left、center、right)
app:iv_paddingSide="40dp"      第一个点 或 最后一个点  距离 viewpager 边界的距离(app:iv_gravity="center"时 无效)

  IndicatorViewPager ivp_test1= (IndicatorViewPager) findViewById(R.id.ivp_test1);
    IndicatorViewPager ivp_test2= (IndicatorViewPager) findViewById(R.id.ivp_test2);

  设置适配器、点的数量、自动切换的时间（应用场景广告栏等）
 ivp_test1.setAdapter(new IvpAdapter(), ids.size(), 2000);
   设置适配器（引导页）
 ivp_test2.setAdapter(new IvpAdapter1());

