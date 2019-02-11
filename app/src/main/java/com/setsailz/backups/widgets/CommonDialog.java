package com.setsailz.backups.widgets;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.setsailz.backups.R;

/**
 * @author Setsail
 * @ClassName: CommonDialog
 * @Description: 通用的弹出框，支持左右两个按钮、最上面图片区域自定义、主文本和副文本、文本区域自定义
 * @date 2018/1/23 10:35
 */
public class CommonDialog extends Dialog {

    public CommonDialog(Context context) {
        super(context);
    }

    public CommonDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context mContext;
        private String mTitle;
        private String mMainText;
        private String mSubText;
        private String mPositiveButtonText;
        private String mNegativeButtonText;
        private View mContentView;
        private View mTitleView;
        private View.OnClickListener mPositiveButtonClickListener;
        private View.OnClickListener mNegativeButtonClickListener;

        public Builder(Context context) {
            this.mContext = context;
        }

        /**
         * 设置主文本，为空则不显示
         *
         * @param mainText
         * @return
         */
        public Builder setMainText(String mainText) {
            this.mMainText = mainText;
            return this;
        }

        /**
         * 设置主文本，为空则不显示
         *
         * @param mainText
         * @return
         */
        public Builder setMainText(@StringRes int mainText) {
            this.mMainText = mContext.getString(mainText);
            return this;
        }

        /**
         * 设置副文本，为空则不显示
         *
         * @param subText
         * @return
         */
        public Builder setSubText(String subText) {
            this.mSubText = subText;
            return this;
        }

        /**
         * 设置副文本，为空则不显示
         *
         * @param subText
         * @return
         */
        public Builder setSubText(@StringRes int subText) {
            this.mSubText = mContext.getString(subText);
            return this;
        }

        /**
         * 设置标题，为空则不显示
         *
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.mTitle = mContext.getString(title);
            return this;
        }

        /**
         * 设置标题，为空则不显示
         *
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        /**
         * 主内容区域可以用自定义的view
         *
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.mContentView = v;
            return this;
        }

        /**
         * 标题区域可以用自定义的view
         *
         * @param v
         * @return
         */
        public Builder setTitleView(View v) {
            this.mTitleView = v;
            return this;
        }

        /**
         * 设置右边按钮，文本为空则隐藏按钮
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(@StringRes int positiveButtonText,
                                         @Nullable View.OnClickListener listener) {
            this.mPositiveButtonText = mContext.getString(positiveButtonText);
            this.mPositiveButtonClickListener = listener;
            return this;
        }

        /**
         * 设置右边按钮，文本为空则隐藏按钮
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,
                                         @Nullable View.OnClickListener listener) {
            this.mPositiveButtonText = positiveButtonText;
            this.mPositiveButtonClickListener = listener;
            return this;
        }

        /**
         * 设置左边按钮，文本为空则隐藏按钮
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(@StringRes int negativeButtonText,
                                         @Nullable View.OnClickListener listener) {
            this.mNegativeButtonText = mContext.getString(negativeButtonText);
            this.mNegativeButtonClickListener = listener;
            return this;
        }

        /**
         * 设置左边按钮，文本为空则隐藏按钮
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText,
                                         @Nullable View.OnClickListener listener) {
            this.mNegativeButtonText = negativeButtonText;
            this.mNegativeButtonClickListener = listener;
            return this;
        }

        public CommonDialog build() {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CommonDialog dialog = new CommonDialog(mContext,
                    R.style.ConfirmDialog);
            View layout = inflater.inflate(R.layout.dialog_common_layout, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            TextView title = layout.findViewById(R.id.tv_title);
            if (!TextUtils.isEmpty(mTitle)) {
                title.setVisibility(View.VISIBLE);
                title.setText(mTitle);
            }

            if (mTitleView != null) {
                LinearLayout titleArea = layout.findViewById(R.id.lin_title);
                titleArea.removeAllViews();
                titleArea.addView(mTitleView);
            }

            View divider = layout.findViewById(R.id.divider);

            final Button positiveBtn = layout.findViewById(R.id.btn_positive);
            if (!TextUtils.isEmpty(mPositiveButtonText)) {
                positiveBtn.setText(mPositiveButtonText);

                positiveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (mPositiveButtonClickListener != null) {
                            mPositiveButtonClickListener.onClick(v);
                        }
                    }
                });
            } else {
                positiveBtn.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            }

            final Button negativeBtn = layout.findViewById(R.id.btn_negative);
            if (!TextUtils.isEmpty(mNegativeButtonText)) {
                negativeBtn.setText(mNegativeButtonText);

                negativeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (mNegativeButtonClickListener != null) {
                            mNegativeButtonClickListener.onClick(v);
                        }
                    }
                });
            } else {
                negativeBtn.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mMainText)) {
                TextView mainText = layout.findViewById(R.id.tv_main_text);
                mainText.setText(mMainText);
                mainText.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(mSubText)) {
                TextView subText = layout.findViewById(R.id.tv_sub_text);
                subText.setText(mSubText);
                subText.setVisibility(View.VISIBLE);
            }

            if (mContentView != null) {
                LinearLayout content = layout.findViewById(R.id.lin_content);
                content.removeAllViews();
                content.addView(mContentView);
            }

            dialog.setContentView(layout);
            dialog.setCancelable(false);

            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 75 / 100; // 设置宽度
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);

            return dialog;
        }
    }
}
