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
    private boolean mExtraParentheses = true;
    private int mFontSize = 0;

    public SpinnerCommonAdapter(Context context, List<T> items) {
        mcontext = context;
        layoutInflater = LayoutInflater.from(mcontext);
        list = items;
    }

    public SpinnerCommonAdapter(Context context, List<T> items, boolean extraParentheses, int fontSize) {
        mcontext = context;
        layoutInflater = LayoutInflater.from(mcontext);
        list = items;
        this.mExtraParentheses = extraParentheses;
        this.mFontSize = fontSize;
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
        return createViewFromResource(position, convertView, parent, mDropDownResource, false );
    }

    //Select Item Style
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource, true);
    }

    private View createViewFromResource(int position, View convertView,
                                        ViewGroup parent, int resource, boolean defaultView) {
        View view;
        TextView text;

        if (convertView == null) {
            view = layoutInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        text = (TextView) view;
        if(mFontSize > 0){
            text.setTextSize(mFontSize);
            if(defaultView)
                text.setPadding(0,0,0,0);
        }

        T items = getItem(position);

        String textToShow = mExtraParentheses && items.getNid() > 0
                ? items.getName() + " (" + items.getNid() + ")" : items.getName();
        Spanned spanned = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                ? Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY)
                : Html.fromHtml(textToShow);
        text.setText(spanned, TextView.BufferType.SPANNABLE);

        return view;

    }
}
