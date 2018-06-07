package com.setsailz.backups.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.setsailz.backups.R;

/**
 * 带清空按钮的输入框，另外，drawable尺寸可配置
 */
public class ClearEditText extends android.support.v7.widget.AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {
    /**
     * 删除按钮的引用
     */
    private Drawable mClearDrawable;
    /**
     * 控件是否有焦点
     */
    private boolean hasFoucs;

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        // 这里构造方法也很重要，不加这个很多属性不能再XML里面定义
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        /**
         * 取得自定义属性值
         */
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ClearEditText);
        int drawableWidth = ta.getDimensionPixelSize(R.styleable.ClearEditText_drawableWidth, -1);
        int drawableHeight = ta.getDimensionPixelSize(R.styleable.ClearEditText_drawableHeight, -1);
        if (drawableWidth != -1 && drawableHeight != -1) {
            /**
             * 取得TextView的Drawable(左上右下四个组成的数组值)
             */
            Drawable[] drawables = getCompoundDrawables();
            Drawable textDrawable = null;
            for (Drawable drawable : drawables) {
                if (drawable != null) {
                    textDrawable = drawable;
                }
            }
            /**
             * 设置宽高
             */
            if (textDrawable != null && drawableWidth != -1 && drawableHeight != -1) {
                textDrawable.setBounds(0, 0, drawableWidth, drawableHeight);
            }
            /**
             * 设置给TextView
             */
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
            /**
             * 回收ta
             */
            ta.recycle();
        }

        // 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
            // throw new
            // NullPointerException("You can add drawableRight attribute in XML");
            mClearDrawable = getResources().getDrawable(R.drawable.icon_input_clear);
        }

        if (drawableWidth != -1 && drawableHeight != -1) {
            mClearDrawable.setBounds(0, 0, drawableWidth, drawableHeight);
        } else {
            mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        }

        // 默认设置隐藏图标
        setClearIconVisible(false);
        // 设置焦点改变的监听
        setOnFocusChangeListener(this);
        // 设置输入框里面内容发生改变的监听
        addTextChangedListener(this);
    }

    /**
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件 当我们按下的位置 在 EditText的宽度 -
     * 图标到控件右边的间距 - 图标的宽度 和 EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向就没有考虑
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {

                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight()) && (event.getX() < ((getWidth() - getPaddingRight())));

                if (touchable) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFoucs = hasFocus;
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        if (hasFoucs) {
            setClearIconVisible(s.length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}
