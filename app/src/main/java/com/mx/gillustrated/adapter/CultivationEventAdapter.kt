package com.mx.gillustrated.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.databinding.AdapterCultivationEventBinding
import com.mx.gillustrated.vo.cultivation.PersonEvent

class CultivationEventAdapter constructor(mContext: Context, private val list: List<PersonEvent>, private val callbacks: EventAdapterCallback) : BaseAdapter() {
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
        lateinit var component: AdapterCultivationEventBinding

        if (convertView == null) {
            component = AdapterCultivationEventBinding.inflate(layoutInflater, arg2, false)
            convertView = component.root
            convertView.tag = component
        } else
            component = convertView.tag as AdapterCultivationEventBinding

        val values = list[arg0]
        component.tvContent.text = CultivationHelper.showing("${CultivationHelper.getYearString(values.happenTime)} ${values.content}")

        component.btnDel.setOnClickListener{
            callbacks.onDeleteHandler(values)
        }
        return convertView
    }

    interface EventAdapterCallback {
        fun onDeleteHandler(event: PersonEvent)
    }

}