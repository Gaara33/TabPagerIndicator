package xd.tabpagerindicator;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String[] title = {"鸣人", "佐助", "蝎", "迪达拉", "Orochimaru", "长门", "九喇嘛"};
    private ArrayList<View> views = new ArrayList<View>();
    //private ImageView hehe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < title.length; i++) {
            TextView textView = new TextView(this);
            textView.setGravity(Gravity.CENTER);
            textView.setText(title[i]);
            views.add(textView);
        }
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new TestPageAdapter());
        ViewPagerTabs viewPagerTabs = (ViewPagerTabs) findViewById(R.id.tabs);
        viewPagerTabs.setViewPager(viewPager);
        viewPagerTabs.setOnPageChangeListener(new ViewPagerTabs.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "-onPageScrolled->" + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "-onPageSelected->" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "-onPageScrollStateChanged->" + state);
            }
        });
    }

    private class TestPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }
}
