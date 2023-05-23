package com.mx.gillustrated.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.AdapterAllianceListBinding
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

    @SuppressLint("SetTextI18n")
    override fun getView(arg0: Int, convertViews: View?, arg2: ViewGroup): View {
        var convertView = convertViews
        lateinit var binding: AdapterAllianceListBinding

        if (convertView == null) {
            binding = AdapterAllianceListBinding.inflate(layoutInflater, arg2, false)
            convertView = binding.root
            convertView.tag = binding
        } else
            binding = convertView.tag as AdapterAllianceListBinding

        val abridgeName = if (list[arg0].abridgeName != "") "(${list[arg0].abridgeName})" else ""
        binding.tvName.text =  CultivationHelper.showing("${nation.find { it.id == list[arg0].nation }?.name}-${list[arg0].name}$abridgeName")
        binding.tvPersons.text =  "${list[arg0].personList.size}-${list[arg0].personList.count { it.value.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
        binding.tvTotal.text  = CultivationHelper.showLifeTurn(list[arg0].totalXiuwei)
        binding.tvWinner.text  = list[arg0].battleWinner.toString()
        return convertView
    }

}
