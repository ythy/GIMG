package com.mx.gillustrated.adapter;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * Created by maoxin on 2017/2/23.
 */

public class SpinnerSimpleAdapter extends BaseAdapter{

    private Context mcontext;
    private LayoutInflater layoutInflator;
    private static int mResource = android.R.layout.simple_gallery_item;
    private static int mDropDownResource = android.R.layout.simple_spinner_dropdown_item;
    private List<String> list;
    private int mFontSize = 0;

    public SpinnerSimpleAdapter(Context context, List<String> items, int fontSize) {
        mcontext = context;
        layoutInflator = LayoutInflater.from(mcontext);
        list = items;
        mFontSize = fontSize;
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
        return createViewFromResource(position, convertView, parent, mDropDownResource, false);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource, true);
    }

    // defaultView : view or dropdown view
    private View createViewFromResource(int position, View convertView,
                                        ViewGroup parent, int resource, boolean defaultView) {
        View view;
        TextView text;

        if (convertView == null) {
            view = layoutInflator.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        text = (TextView) view;
        if(mFontSize > 0) {
            text.setTextSize(mFontSize);
            if(defaultView)
                text.setPadding(0,0,0,0);
        }

        Spanned spanned = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                ? Html.fromHtml(getItem(position), Html.FROM_HTML_MODE_LEGACY)
                : Html.fromHtml(getItem(position));
        text.setText(spanned, TextView.BufferType.SPANNABLE);

        return view;

    }
}
