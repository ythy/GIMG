package com.mx.gillustrated.adapter

import com.mx.gillustrated.R
import com.mx.gillustrated.vo.EventInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.activity.BaseActivity

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
        lateinit var component: ViewHolder

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.adapter_events, arg2, false)
            component = ViewHolder(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as ViewHolder

        component.tvName.text = list[arg0].name
        return convertView
    }

    internal class ViewHolder(view: View) {

        @BindView(R.id.tvName)
        lateinit var tvName: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }
}
