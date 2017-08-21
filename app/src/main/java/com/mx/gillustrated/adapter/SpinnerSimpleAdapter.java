package com.mx.gillustrated.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mx.gillustrated.provider.Providerdata;
import com.mx.gillustrated.vo.EventInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by maoxin on 2017/2/23.
 */

public class SpinnerSimpleAdapter extends BaseAdapter{

    private Context mcontext;
    private LayoutInflater layoutInflator;
    private static int mResource = android.R.layout.simple_gallery_item;
    private static int mDropDownResource = android.R.layout.simple_spinner_dropdown_item;
    private List<EventInfo> list;


    public SpinnerSimpleAdapter(Context context, List<EventInfo> items) {
        mcontext = context;
        layoutInflator = LayoutInflater.from(mcontext);
        list = items;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public EventInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getPosition(int id) {
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getId() == id)
                return i;
        }
        return 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    private View createViewFromResource(int position, View convertView,
                                        ViewGroup parent, int resource) {
        View view;
        TextView text;

        if (convertView == null) {
            view = layoutInflator.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        text = (TextView) view;

        EventInfo items = getItem(position);
        text.setText(items.getName());
        return view;

    }
}
