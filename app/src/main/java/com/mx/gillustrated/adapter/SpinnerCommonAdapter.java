package com.mx.gillustrated.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.SpinnerInfo;

import java.util.List;

/**
 * Created by maoxin on 2018/7/17.
 */

public class SpinnerCommonAdapter<T extends SpinnerInfo> extends BaseAdapter {

    private Context mcontext;
    private LayoutInflater layoutInflater;
    private static int mResource = android.R.layout.simple_spinner_item;
    private static int mDropDownResource = android.R.layout.simple_spinner_dropdown_item;
    private List<T> list;


    public SpinnerCommonAdapter(Context context, List<T> items) {
        mcontext = context;
        layoutInflater = LayoutInflater.from(mcontext);
        list = items;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Dropdown Item Style
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }

    //Select Item Style
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    private View createViewFromResource(int position, View convertView,
                                        ViewGroup parent, int resource) {
        View view;
        TextView text;

        if (convertView == null) {
            view = layoutInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        text = (TextView) view;

        T items = getItem(position);
        if(items.getNid() > 0)
            text.setText(items.getName() + " (" + items.getNid() + ")");
        else
            text.setText(items.getName());
        return view;

    }
}
