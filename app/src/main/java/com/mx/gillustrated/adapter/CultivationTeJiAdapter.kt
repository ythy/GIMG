package com.mx.gillustrated.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.databinding.AdatperCultivationTejiBinding
import com.mx.gillustrated.vo.cultivation.TeJi

class CultivationTeJiAdapter constructor(mContext: Context, private val list: List<TeJi>, private val callbacks: TeJiAdapterCallback) : BaseAdapter() {
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
        lateinit var component:AdatperCultivationTejiBinding

        if (convertView == null) {
            component = AdatperCultivationTejiBinding.inflate(layoutInflater, arg2, false)
            convertView = component.root
            convertView.tag = component
        } else
            component = convertView.tag as AdatperCultivationTejiBinding

        val values = list[arg0]
        component.tvName.text = CultivationHelper.showing(values.name)
        component.tvName.setTextColor(Color.parseColor(CommonColors[values.rarity]))
        component.tvDescription.text = CultivationHelper.showing(values.description)
        if (values.form == 0)
            component.btnDel.visibility = View.VISIBLE
        else
            component.btnDel.visibility = View.GONE

        component.btnDel.setOnClickListener{
            callbacks.onDeleteHandler(values)
        }
        return convertView
    }


    interface TeJiAdapterCallback {
        fun onDeleteHandler(item: TeJi)
    }

}