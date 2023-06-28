package com.mx.gillustrated.adapter

import com.mx.gillustrated.vo.EventInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mx.gillustrated.activity.BaseActivity
import com.mx.gillustrated.databinding.AdapterEventsBinding

class EventsAdapter  constructor(mContext: BaseActivity, private val list: List<EventInfo>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(arg0: Int): Any {
        return list[arg0]
    }

    override fun getItemId(arg0: Int): Long {
        return arg0.toLong()
    }

    override fun getView(arg0: Int, convertViews: View?, arg2: ViewGroup): View {
        var convertView = convertViews
        lateinit var component: AdapterEventsBinding

        if (convertView == null) {
            component = AdapterEventsBinding.inflate(layoutInflater, arg2, false)
            convertView = component.root
            convertView.tag = component
        } else
            component = convertView.tag as AdapterEventsBinding

        component.tvName.text = list[arg0].name
        return convertView
    }

}
