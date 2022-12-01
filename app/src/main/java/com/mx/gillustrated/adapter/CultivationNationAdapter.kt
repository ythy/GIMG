package com.mx.gillustrated.adapter

import android.content.Context
import com.mx.gillustrated.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.vo.cultivation.Nation

class CultivationNationAdapter  constructor(mContext: Context, private val list: List<Nation>) : BaseAdapter() {

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
                    R.layout.adapter_cultivation_nation, arg2, false)
            component = ViewHolder(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as ViewHolder

        component.name.text =  CultivationHelper.showing(list[arg0].name)
        component.persons.text = "${ list[arg0].nationPersonList.size}-${ list[arg0].nationPersonList.count { it.value.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
        component.winner.text = list[arg0].battleWinner.toString()
        component.total.text  = list[arg0].totalTurn.toString()
        return convertView
    }

    internal class ViewHolder(view: View) {

        @BindView(R.id.tv_name)
        lateinit var name: TextView

        @BindView(R.id.tv_persons)
        lateinit var persons: TextView

        @BindView(R.id.tv_winner)
        lateinit var winner: TextView

        @BindView(R.id.tv_total)
        lateinit var total: TextView


        init {
            ButterKnife.bind(this, view)
        }
    }
}
