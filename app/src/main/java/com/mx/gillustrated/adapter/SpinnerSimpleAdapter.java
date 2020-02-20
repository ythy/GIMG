package com.mx.gillustrated.adapter;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
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
    private List<String> list;


    public SpinnerSimpleAdapter(Context context, List<String> items) {
        mcontext = context;
        layoutInflator = LayoutInflater.from(mcontext);
        list = items;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            text.setText(Html.fromHtml(getItem(position),  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            text.setText(Html.fromHtml(getItem(position)), TextView.BufferType.SPANNABLE);
        }
        return view;

    }
}
