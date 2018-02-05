package com.setsailz.backups.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.setsailz.backups.R;

/**
 * 多选弹出框选项adapter
 * Created by Setsail on 2017/8/22.
 */

public class SelectionDialogAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private String[] listData;

    public SelectionDialogAdapter(Context context, String[] listData) {
        this.listData = listData;
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return listData.length;
    }

    @Override
    public Object getItem(int position) {
        return listData[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.item_selections,
                parent, false);
        final TextView txt = (TextView) convertView.findViewById(R.id.tv);
        txt.setText(listData[position]);
        return convertView;
    }

}
