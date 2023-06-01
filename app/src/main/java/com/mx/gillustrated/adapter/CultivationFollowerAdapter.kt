package com.mx.gillustrated.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.databinding.AdapterCultivationFollowerBinding
import com.mx.gillustrated.vo.cultivation.Follower

class CultivationFollowerAdapter constructor(mContext: Context, private val list: List<Follower>, private val callbacks: FollowerAdapterCallback) : BaseAdapter() {
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
        lateinit var component: AdapterCultivationFollowerBinding

        if (convertView == null) {
            component = AdapterCultivationFollowerBinding.inflate(layoutInflater, arg2, false)
            convertView = component.root
            convertView.tag = component
        } else
            component = convertView.tag as AdapterCultivationFollowerBinding

        val values = list[arg0].detail
        component.tvName.text = CultivationHelper.showing(values.name + list[arg0].uniqueName)
        component.tvName.setTextColor(Color.parseColor(CommonColors[values.rarity]))
        if(values.teji.isEmpty())
            component.tvTeji.text = ""
        else
            component.tvTeji.text = CultivationHelper.showing(mConfig.teji.filter { values.teji.contains(it.id) }.joinToString { it.name })

        component.tvProps.text = values.property.take(4).joinToString()

        if (values.type == 0){
            component.btnDel.visibility = View.VISIBLE
        }else{
            component.btnDel.visibility = View.GONE
        }

        component.btnDel.setOnClickListener{
            callbacks.onDeleteHandler(list[arg0])
        }
        return convertView
    }


    interface FollowerAdapterCallback {
        fun onDeleteHandler(follower: Follower)
    }

}