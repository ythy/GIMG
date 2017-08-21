package com.mx.gillustrated.adapter;

import java.util.List;

import com.mx.gillustrated.R;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsAdapter extends BaseAdapter {

    private Context mcontext;
    private LayoutInflater layoutInflator;
    private List<EventInfo> list;

    public EventsAdapter() {
    }

    public EventsAdapter(Context context, List<EventInfo> items) {
        mcontext = context;
        layoutInflator = LayoutInflater.from(mcontext);
        list = items;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        return list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int arg0, View convertView, ViewGroup arg2) {
        ViewHolder component = null;

        if (convertView == null) {
            convertView = layoutInflator.inflate(
                    R.layout.adapter_events, null);
            component = new ViewHolder(convertView);
            convertView.setTag(component);
        }
        else
            component = (ViewHolder) convertView.getTag();

        component.tvName.setText(list.get(arg0).getName());
        return convertView;
    }

     static class ViewHolder{
        @BindView(R.id.tvName)
        TextView tvName;

        public ViewHolder(View view){
            ButterKnife.bind(this, view);
        }
    }
}
