
package xd.tabpagerindicator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
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
    private Bitmap mDst;
    private Bitmap text;
    private Shader mBG;
    private Paint basicPaint;
    private Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.XOR);

    public ViewPagerTabStrip(Context context) {
        this(context, null);
    }

    private ViewPagerTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        initData();
    }

    private void initData() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);// 抗锯齿
        mPaint.setFilterBitmap(false);
        mPaint.setDither(true);// 防抖动
        mPaint.setStyle(Paint.Style.FILL);// 设置画笔的填充方式为实心
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(Color.BLACK);

        Bitmap layer = Bitmap.createBitmap(new int[]{0xFFFFFFFF, 0xFFCCCCCC, 0xFFCCCCCC, 0xFFFFFFFF}, 2, 2, Bitmap.Config.RGB_565);
        mBG = new BitmapShader(layer, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        Matrix m = new Matrix();
        m.setScale(6, 6);
        mBG.setLocalMatrix(m);
        basicPaint = new Paint();
        basicPaint.setFilterBitmap(false);
        // draw the checker-board pattern
        basicPaint.setStyle(Paint.Style.FILL);
        basicPaint.setShader(mBG);
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
            final boolean hasNextTab = isRtl ? mIndexForSelection > 0
                    : (mIndexForSelection < (getChildCount() - 1));
            if ((mSelectionOffset > 0.0f) && hasNextTab) {
                // Draw the selection partway between the tabs
                nextItem = mIndexForSelection + (isRtl ? -1 : 1);
                nextTitle = getChildAt(mIndexForSelection + (isRtl ? -1 : 1));
                int nextLeft = nextTitle.getLeft();
                int nextRight = nextTitle.getRight();

                selectedLeft = (int) (mSelectionOffset * nextLeft +
                        (1.0f - mSelectionOffset) * selectedLeft);
                selectedRight = (int) (mSelectionOffset * nextRight +
                        (1.0f - mSelectionOffset) * selectedRight);
            }
            for (int i = 0; i < getChildCount(); i++) {
                TextView childAt = (TextView) getChildAt(i);
                if (i == currentItem || i == nextItem) {
                    childAt.setTextColor(Color.TRANSPARENT);
                } else {
                    childAt.setTextColor(mTextColor);
                }
            }
            int W = getWidth();
            int H = getHeight();
            // draw the src/dst example into our offscreen bitmap
            int sc = canvas.saveLayer(0, 0, W, H, null,
                    Canvas.MATRIX_SAVE_FLAG |
                            Canvas.CLIP_SAVE_FLAG |
                            Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                            Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                            Canvas.CLIP_TO_LAYER_SAVE_FLAG);
            mDst = makeSildingBlock(W, H, selectedLeft, selectedRight);
            canvas.drawBitmap(mDst, 0, 0, basicPaint);
            basicPaint.setXfermode(xfermode);
            text = drawText(W, H, selectedTitle, nextTitle);
            canvas.drawBitmap(text, 0, 0, basicPaint);
            basicPaint.setXfermode(null);
            canvas.restoreToCount(sc);
            if (mDst != null && !mDst.isRecycled()) {
                mDst.recycle();
            }
            if (text != null && !text.isRecycled()) {
                text.recycle();
            }
        }
    }

    private boolean isRtl() {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    /**
     * 滑块
     *
     * @param w
     * @param h
     * @param l
     * @param r
     * @return
     */
    private Bitmap makeSildingBlock(int w, int h, int l, int r) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        RectF t = new RectF(l, h * (1 - mSliderHeightScale) / 2, r, h * (1 + mSliderHeightScale) / 2);
        mPaint.setColor(mSliderColor);
        c.drawRoundRect(t, h * mSliderHeightScale / 2, h * mSliderHeightScale / 2, mPaint);
        return bm;
    }

    /**
     * 根据TextView画字
     *
     * @param w
     * @param h
     * @param v1
     * @param v2
     * @return
     */
    private Bitmap drawText(int w, int h, View v1, View v2) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        String content = (String) ((TextView) v1).getText();
        Rect targetRect = new Rect(v1.getLeft(), 0, v1.getRight(), h);
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        mPaint.setColor(mTextColor);
        c.drawText(content, targetRect.centerX(), baseline, mPaint);
        if (v2 != null) {
            String content2 = (String) ((TextView) v2).getText();
            Rect targetRect2 = new Rect(v2.getLeft(), 0, v2.getRight(), h);
            int baseline2 = (targetRect2.bottom + targetRect2.top - fontMetrics.bottom - fontMetrics.top) / 2;
            // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
            c.drawText(content2, targetRect2.centerX(), baseline2, mPaint);
        }
        return bm;
    }

}