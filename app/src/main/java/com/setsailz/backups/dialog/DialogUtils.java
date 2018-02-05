package com.setsailz.backups.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.setsailz.backups.R;

/**
 * 对话框
 * Created by Setsail on 2017/6/13.
 */

public class DialogUtils {

    /**
     * 创建提示 对话框
     *
     * @param context
     * @param title
     * @param msg
     * @param listerner
     */
    public static void alert(Context context, String title, String msg, DialogInterface.OnClickListener listerner) {
        if (context == null) {
            return;
        }

        ConfirmDialog.Builder builder = new ConfirmDialog.Builder(context);
        builder.setMessage(msg);
        builder.setTitle(title);
        builder.setPositiveButton(android.R.string.ok, listerner);

        builder.create().show();
    }

    /**
     * 创建确认对话框
     *
     * @param context
     * @param title
     * @param msg
     * @param positiveListener
     * @param negativeListener
     */
    public static void confirm(Context context, String title, String msg, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener... negativeListener) {
        if (context == null) {
            return;
        }
        confirm(context, title, msg, "是", "否", positiveListener, negativeListener);
    }

    /**
     * 创建确认对话框
     *
     * @param context
     * @param title
     * @param msg
     * @param textOK
     * @param textCancel
     * @param positiveListener
     * @param negativeListener
     */
    public static void confirm(Context context, String title, String msg, String textOK, String textCancel, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener... negativeListener) {
        ConfirmDialog.Builder builder = new ConfirmDialog.Builder(context);
        builder.setMessage(msg);
        builder.setTitle(title);

        if (positiveListener == null) {
            return;
        }

        builder.setPositiveButton(textOK, positiveListener);

        if (negativeListener.length > 0) {
            builder.setNegativeButton(textCancel, negativeListener[0]);
        } else {
            builder.setNegativeButton(textCancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        final Dialog dialog = builder.create();
        dialog.show();

        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = context.getResources().getDisplayMetrics().widthPixels * 75 / 100; // 设置宽度
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }

    /**
     * 单选弹出框
     *
     * @param context
     * @param title    如果为空，则不展示头部
     * @param items
     * @param cancel   如果为空，则默认文本为“取消”
     * @param callback
     */
    public static void singleSelect(Context context, String title, final String[] items, String cancel, final SingleSelectCallback callback) {
        if (context == null || callback == null) {
            return;
        }

        final Dialog mDialog = new Dialog(context, R.style.SelectionDialog);

        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();

        final WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mDialog.getWindow().setAttributes(lp);

        final LayoutInflater flater = LayoutInflater.from(context);
        final View view = flater.inflate(R.layout.dialog_selection, null);
        mDialog.setContentView(view);

        final ListView listview = (ListView) view.findViewById(R.id.lv_selections);
        final TextView tvCancel = (TextView) view.findViewById(R.id.tv);
        tvCancel.setTextColor(Color.parseColor("#999999"));
        final TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        final View divider = view.findViewById(R.id.divider);

        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        }

        final SelectionDialogAdapter adapter = new SelectionDialogAdapter(context, items);
        listview.setAdapter(adapter);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDialog.dismiss();
                callback.onSingleSelected(position, items[position]);
            }
        });

        if (TextUtils.isEmpty(cancel)) {
            tvCancel.setText("取消");
        } else {
            tvCancel.setText(cancel);
        }
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    /**
     * 单项选择弹出框的回调
     */
    public interface SingleSelectCallback {
        void onSingleSelected(int position, String item);
    }

    /**
     * 单选选择弹出框的点击事件
     */
    public class SelectionDialogOnItemClickListener implements OnItemClickListener {

        private Dialog mDialog;

        public void setmDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }
    }
}
