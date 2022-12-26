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
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Alliance

class AllianceListAdapter  constructor(mContext: Context, private val list: List<Alliance>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private val nation = CultivationHelper.mConfig.nation

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
                    R.layout.adapter_alliance_list, arg2, false)
            component = ViewHolder(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as ViewHolder

        component.name.text =  CultivationHelper.showing("${nation.find { it.id == list[arg0].nation }?.name}-${list[arg0].name}")
        component.persons.text =  "${list[arg0].personList.size}-${list[arg0].personList.count { it.value.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
        component.total.text  = CultivationHelper.showLifeTurn(list[arg0].totalXiuwei)
        component.winner.text  = list[arg0].battleWinner.toString()
        return convertView
    }

    internal class ViewHolder(view: View) {

        @BindView(R.id.tv_name)
        lateinit var name: TextView

        @BindView(R.id.tv_persons)
        lateinit var persons: TextView

        @BindView(R.id.tv_total)
        lateinit var total: TextView

        @BindView(R.id.tv_winner)
        lateinit var winner: TextView


        init {
            ButterKnife.bind(this, view)
        }
    }
}
