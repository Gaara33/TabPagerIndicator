
package xd.tabpagerindicator;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Lightweight implementation of ViewPager tabs. This looks similar to traditional actionBar tabs,
 * but allows for the view containing the tabs to be placed anywhere on screen. Text-related
 * attributes can also be assigned in XML - these will get propogated to the child TextViews
 * automatically.
 */
public class ViewPagerTabs extends HorizontalScrollView implements ViewPager.OnPageChangeListener {

    ViewPager mPager;
    private ViewPagerTabStrip mTabStrip;

    /**
     * Linearlayout that will contain the TextViews serving as tabs. This is the only child
     * of the parent HorizontalScrollView.
     */
    final ColorStateList mSliderColor;
    final ColorStateList mTextColor;
    final int mTextSize;
    final boolean mTextAllCaps;
    int mPrevSelected = -1;
    int mSidePadding;
    float mSliderHeght;

    private static final int TAB_SIDE_PADDING_IN_DPS = 10;


    /**
     * Simulates actionbar tab behavior by showing a toast with the tab title when long clicked.
     */
    private class OnTabLongClickListener implements OnLongClickListener {
        final int mPosition;

        public OnTabLongClickListener(int position) {
            mPosition = position;
        }

        @Override
        public boolean onLongClick(View v) {
            final int[] screenPos = new int[2];
            getLocationOnScreen(screenPos);

            final Context context = getContext();
            final int width = getWidth();
            final int height = getHeight();
            final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

            Toast toast = Toast.makeText(context, mPager.getAdapter().getPageTitle(mPosition),
                    Toast.LENGTH_SHORT);

            // Show the toast under the tab
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    (screenPos[0] + width / 2) - screenWidth / 2, screenPos[1] + height);

            toast.show();
            return true;
        }
    }

    public ViewPagerTabs(Context context) {
        this(context, null);
    }

    public ViewPagerTabs(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewPagerTabs(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFillViewport(true);

        mSidePadding = (int) (getResources().getDisplayMetrics().density * TAB_SIDE_PADDING_IN_DPS);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerTabs, defStyle, 0);
        mTextSize = a.getDimensionPixelSize(R.styleable.ViewPagerTabs_textSize, sp2px(getContext(), 12));
        mTextColor = a.getColorStateList(R.styleable.ViewPagerTabs_textColor);
        mSliderColor = a.getColorStateList(R.styleable.ViewPagerTabs_sliderColor);
        mTextAllCaps = a.getBoolean(R.styleable.ViewPagerTabs_textAllCap, false);
        mSliderHeght = a.getFloat(R.styleable.ViewPagerTabs_sliderHeight, 0.5f);

        mTabStrip = new ViewPagerTabStrip(context);
        mTabStrip.setSliderHeightScale(mSliderHeght);
        mTabStrip.setTextSize(mTextSize);
        if (mSliderColor != null)
            mTabStrip.setSliderColor(mSliderColor.getDefaultColor());
        if (mTextColor != null)
            mTabStrip.setTextColor(mTextColor.getDefaultColor());

        addView(mTabStrip, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        a.recycle();
    }

    public void setViewPager(ViewPager viewPager) {
        mPager = viewPager;
        addTabs(mPager.getAdapter());
        mPager.addOnPageChangeListener(this);
    }

    private void addTabs(PagerAdapter adapter) {
        mTabStrip.removeAllViews();

        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            addTab(adapter.getPageTitle(i), i);
        }
    }

    private void addTab(CharSequence tabTitle, final int position) {
        final TextView textView = new TextView(getContext());
        textView.setText(tabTitle);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(getRtlPosition(position));
            }
        });

        textView.setOnLongClickListener(new OnTabLongClickListener(position));

        // Assign various text appearance related attributes to child views.
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        if (mTextColor != null) {
            textView.setTextColor(mTextColor);
        } else {
            textView.setTextColor(Color.BLACK);
        }
        textView.setAllCaps(mTextAllCaps);
        textView.setPadding(mSidePadding, 0, mSidePadding, 0);
        mTabStrip.addView(textView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
        // Default to the first child being selected
        if (position == 0) {
            mPrevSelected = 0;
            textView.setSelected(true);
        }
    }

    private int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        position = getRtlPosition(position);
        int tabStripChildCount = mTabStrip.getChildCount();
        if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
            return;
        }
        mTabStrip.onPageScrolled(position, positionOffset, positionOffsetPixels);
        if (null != onPageChangeListener) {
            onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        position = getRtlPosition(position);
        if (mPrevSelected >= 0) {
            mTabStrip.getChildAt(mPrevSelected).setSelected(false);
        }
        final View selectedChild = mTabStrip.getChildAt(position);
        selectedChild.setSelected(true);

        // Update scroll position
        final int scrollPos = selectedChild.getLeft() - (getWidth() - selectedChild.getWidth()) / 2;
        smoothScrollTo(scrollPos, 0);
        mPrevSelected = position;
        if (null != onPageChangeListener) {
            onPageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (null != onPageChangeListener) {
            onPageChangeListener.onPageSelected(state);
        }
    }

    private int getRtlPosition(int position) {
        if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            return mTabStrip.getChildCount() - 1 - position;
        }
        return position;
    }

    private OnPageChangeListener onPageChangeListener;

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public interface OnPageChangeListener {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);
    }

}

