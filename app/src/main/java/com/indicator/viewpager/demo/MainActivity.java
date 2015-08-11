package com.indicator.viewpager.demo;

import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.indicator.viewpager.IndicatorViewPager;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private List<Integer> ids = new ArrayList<>();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ids.add(R.mipmap.ic_launcher);
    ids.add(R.mipmap.ic_launcher);
    ids.add(R.mipmap.ic_launcher);
    ids.add(R.mipmap.ic_launcher);
    ids.add(R.mipmap.ic_launcher);

    IndicatorViewPager ivp_test1= (IndicatorViewPager) findViewById(R.id.ivp_test1);
    IndicatorViewPager ivp_test2= (IndicatorViewPager) findViewById(R.id.ivp_test2);


    ivp_test1.setAdapter(new IvpAdapter(), ids.size(), 2000);
    ivp_test2.setAdapter(new IvpAdapter1());
  }


  class IvpAdapter extends PagerAdapter
  {

    @Override public View instantiateItem(ViewGroup container,final int position) {

      ImageView imageView = new ImageView(container.getContext());

      imageView.setImageDrawable(getResources().getDrawable(ids.get(position%ids.size())));

      container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT);

      imageView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          Toast.makeText(MainActivity.this, "点击--" + position % ids.size(), Toast.LENGTH_LONG).show();
        }
      });

      return imageView;
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView((View) object);
    }

    @Override
    public int getCount()
    {
      return 100000;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1)
    {
      return arg0==arg1;
    }

  }

  class IvpAdapter1 extends PagerAdapter
  {

    @Override public View instantiateItem(ViewGroup container,final int position) {

      ImageView imageView = new ImageView(container.getContext());

      imageView.setImageDrawable(getResources().getDrawable(ids.get(position)));

      container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT);

      imageView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          Toast.makeText(MainActivity.this, "点击--" + position , Toast.LENGTH_LONG).show();
        }
      });

      return imageView;
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView((View) object);
    }

    @Override
    public int getCount()
    {
      return ids.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1)
    {
      return arg0==arg1;
    }

  }


  @Override public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
