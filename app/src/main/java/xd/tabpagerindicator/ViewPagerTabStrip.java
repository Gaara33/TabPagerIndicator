
package xd.tabpagerindicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewPagerTabStrip extends LinearLayout {
    private float mSliderHeightScale = 0.5f;
    private int mIndexForSelection;
    private float mSelectionOffset;
    private int mSliderColor = Color.BLACK;
    private int mTextColor = Color.BLACK;
    private Paint mPaint;
    private Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.XOR);

    public ViewPagerTabStrip(Context context) {
        this(context, null);
    }

    private ViewPagerTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        initPain();
    }

    private void initPain() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);// 抗锯齿
        mPaint.setFilterBitmap(false);
        mPaint.setDither(true);// 防抖动
        mPaint.setStyle(Paint.Style.FILL);// 设置画笔的填充方式为实心
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setTextSize(int size) {
        mPaint.setTextSize(size);
    }

    public void setTextColor(int color) {
        mTextColor = color;
    }

    public void setSliderColor(int sliderColor) {
        mSliderColor = sliderColor;
    }

    public void setSliderHeightScale(float scale) {
        mSliderHeightScale = scale;
    }

    /**
     * Notifies this view that view pager has been scrolled. We save the tab index
     * and selection offset for interpolating the position and width of selection
     * underline.
     */
    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mIndexForSelection = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    private boolean isRtl() {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int childCount = getChildCount();
        // Thick colored underline below the current selection
        if (childCount > 0) {
            int currentItem = mIndexForSelection;
            int nextItem = -1;
            View selectedTitle = getChildAt(mIndexForSelection);
            View nextTitle = null;
            int selectedLeft = selectedTitle.getLeft();
            int selectedRight = selectedTitle.getRight();
            final boolean isRtl = isRtl();
            final boolean hasNextTab = isRtl ? mIndexForSelection > 0 : (mIndexForSelection < (getChildCount() - 1));
            if ((mSelectionOffset > 0.0f) && hasNextTab) {
                // Draw the selection partway between the tabs
                nextItem = mIndexForSelection + (isRtl ? -1 : 1);
                nextTitle = getChildAt(mIndexForSelection + (isRtl ? -1 : 1));
                int nextLeft = nextTitle.getLeft();
                int nextRight = nextTitle.getRight();

                selectedLeft = (int) (mSelectionOffset * nextLeft + (1.0f - mSelectionOffset) * selectedLeft);
                selectedRight = (int) (mSelectionOffset * nextRight + (1.0f - mSelectionOffset) * selectedRight);
            }
            for (int i = 0; i < getChildCount(); i++) {
                TextView childAt = (TextView) getChildAt(i);
                if (i == currentItem || i == nextItem) {
                    childAt.setTextColor(Color.TRANSPARENT);
                } else {
                    childAt.setTextColor(mTextColor);
                }
            }
            int width = getWidth();
            int height = getHeight();
            int save = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
            drawSlidingBlock(canvas, selectedLeft, selectedRight, height);
            mPaint.setXfermode(xfermode);
            drawText(canvas, selectedTitle, nextTitle, height);
            mPaint.setXfermode(null);
            canvas.restoreToCount(save);
        }
        super.onDraw(canvas);
    }

    /**
     * 文字
     * @param canvas
     * @param selectedTitle
     * @param nextTitle
     * @param height
     */
    private void drawText(Canvas canvas, View selectedTitle, View nextTitle, int height) {
        mPaint.setColor(mTextColor);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        String content = (String) ((TextView) selectedTitle).getText();
        Rect targetRect = new Rect(selectedTitle.getLeft(), 0, selectedTitle.getRight(), height);
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(content, targetRect.centerX(), baseline, mPaint);
        if (nextTitle != null) {
            String content2 = (String) ((TextView) nextTitle).getText();
            Rect targetRect2 = new Rect(nextTitle.getLeft(), 0, nextTitle.getRight(), height);
            int baseline2 = (targetRect2.bottom + targetRect2.top - fontMetrics.bottom - fontMetrics.top) / 2;
            // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
            canvas.drawText(content2, targetRect2.centerX(), baseline2, mPaint);
        }
    }

    /**
     * 滑块
     * @param canvas
     * @param selectedLeft
     * @param selectedRight
     * @param height
     */
    private void drawSlidingBlock(Canvas canvas, int selectedLeft, int selectedRight, int height) {
        mPaint.setColor(mSliderColor);
        RectF t = new RectF(selectedLeft, height * (1 - mSliderHeightScale) / 2, selectedRight, height * (1 + mSliderHeightScale) / 2);
        canvas.drawRoundRect(t, height * mSliderHeightScale / 2, height * mSliderHeightScale / 2, mPaint);
    }

}