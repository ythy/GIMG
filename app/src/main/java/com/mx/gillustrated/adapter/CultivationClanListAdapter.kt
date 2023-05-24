package com.mx.gillustrated.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.AdapterClanListBinding
import com.mx.gillustrated.vo.cultivation.Clan

@SuppressLint("SetTextI18n")
class CultivationClanListAdapter  constructor(mContext: Context, private val list: List<Clan>) : BaseAdapter() {

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
        lateinit var binding: AdapterClanListBinding
        if (convertView == null) {
            binding = AdapterClanListBinding.inflate(layoutInflater, arg2, false)
            convertView = binding.root
            convertView.tag = binding
        } else
            binding = convertView.tag as AdapterClanListBinding

        val clan = list[arg0]
        val personList = clan.clanPersonList.map { it.value }

        binding.tvName.text = CultivationHelper.showing(clan.name)
        if(clan.zhu != null)
            binding.tvZhu.text = CultivationHelper.showing(clan.zhu!!.name)
        else
            binding.tvZhu.text = ""
        binding.tvPersons.text = "${personList.size}-${personList.count { it.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
        binding.tvTotal.text  = CultivationHelper.showLifeTurn(clan.totalXiuwei)
        binding.tvWinner.text = clan.battleWinner.toString()
        return convertView
    }

}
