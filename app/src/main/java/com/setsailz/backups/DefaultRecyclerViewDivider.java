package com.setsailz.backups;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Setsail on 2017/3/22.
 */
public class DefaultRecyclerViewDivider extends RecyclerView.ItemDecoration {
    /*
      * RecyclerView的布局方向，默认先赋值
      * 为纵向布局
      * RecyclerView 布局可横向，也可纵向
      * 横向和纵向对应的分割想画法不一样
      * */
    private int mOrientation = LinearLayoutManager.VERTICAL;

    /**
     * 分割线的颜色
     */
    private int mColor;

    /**
     * item之间分割线的size，默认为1
     */
    private int mItemSize = 1;

    /**
     * 分割线左边距
     */
    private int mPaddingLeft;

    /**
     * 分割线右边距
     */
    private int mPaddingRight;

    /**
     * 是否是虚线
     */
    private boolean mIsDashLine = false;

    /**
     * 绘制item分割线的画笔，和设置其属性
     * 来绘制个性分割线
     */
    private Paint mPaint;

    /**
     * 是否显示头部divider
     */
    private boolean enableHeaderDivider = true;

    /**
     * 是否显示尾部divider
     */
    private boolean enableFooterDivider;

    private Context mContext;

    public DefaultRecyclerViewDivider(Context context) {
        mContext = context;
    }

    public DefaultRecyclerViewDivider build() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(ContextCompat.getColor(mContext, mColor));
         /*设置填充*/
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (mIsDashLine) {
            mPaint.setStrokeWidth(mItemSize);
            PathEffect effects = new DashPathEffect(new float[]{15, 15}, 0);
            mPaint.setPathEffect(effects);
        }
        return this;
    }

    public DefaultRecyclerViewDivider enableHeaderDivider(boolean enable) {
        this.enableHeaderDivider = enable;
        return this;
    }

    public DefaultRecyclerViewDivider enableFooterDivider(boolean enable) {
        this.enableFooterDivider = enable;
        return this;
    }

    public DefaultRecyclerViewDivider setSize(int size) {
        this.mItemSize = size;
        return this;
    }

    public DefaultRecyclerViewDivider setDimenSize(int dimen) {
        this.mItemSize = (int) mContext.getResources().getDimension(dimen);
        return this;
    }

    public DefaultRecyclerViewDivider setColor(int color) {
        this.mColor = color;
        return this;
    }

    public DefaultRecyclerViewDivider setOrientation(int orientation) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请传入正确的参数");
        }
        this.mOrientation = orientation;
        return this;
    }

    public DefaultRecyclerViewDivider setPaddingLeft(int paddingLeft) {
        this.mPaddingLeft = paddingLeft;
        return this;
    }

    public DefaultRecyclerViewDivider setPaddingRight(int paddingRight) {
        this.mPaddingRight = paddingRight;
        return this;
    }

    public DefaultRecyclerViewDivider isDashLine(boolean boo) {
        this.mIsDashLine = boo;
        return this;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    /**
     * 绘制纵向 item 分割线
     *
     * @param canvas
     * @param parent
     */
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft() + mPaddingLeft;
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight() - mPaddingRight;
        final int childSize = parent.getChildCount();
        boolean skipFirst = false;
        boolean skipLast = false;
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition() == 0)
                skipFirst = true;
            if (((LinearLayoutManager) layoutManager).findLastVisibleItemPosition() + 1 == layoutManager.getItemCount()) {
                skipLast = true;
            }
        }
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            if (i == 0 && !enableHeaderDivider && skipFirst) {
                continue;
            }
            if (i == childSize - 1 && !enableFooterDivider && skipLast) {
                continue;
            }
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mItemSize;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    /**
     * 绘制横向 item 分割线
     *
     * @param canvas
     * @param parent
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mItemSize;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    /**
     * 设置item分割线的size
     *
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        final int itemPosition = parent.getChildAdapterPosition(view);
        final int itemCount = state.getItemCount();
        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }
        if (!enableHeaderDivider && itemPosition == 0) {
            outRect.setEmpty();
        } else if (!enableFooterDivider && itemCount > 0 && itemPosition == itemCount - 1) {
            outRect.setEmpty();
        } else {
            if (mOrientation == LinearLayoutManager.VERTICAL) {
                outRect.set(0, 0, 0, mItemSize);
            } else {
                outRect.set(0, 0, mItemSize, 0);
            }
        }
    }
}
