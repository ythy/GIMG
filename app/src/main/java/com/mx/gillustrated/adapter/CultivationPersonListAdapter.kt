package com.mx.gillustrated.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.content.res.AppCompatResources
import com.mx.gillustrated.R
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.databinding.AdapterCultivationPersonListBinding
import com.mx.gillustrated.vo.cultivation.Person

class CultivationPersonListAdapter constructor(private val context: Context, private val list: MutableList<Person>, private val showStar:Boolean, private val showSpecEquipment:Boolean) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
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
        lateinit var binding: AdapterCultivationPersonListBinding
        if (convertView == null) {
            binding = AdapterCultivationPersonListBinding.inflate(layoutInflater, arg2, false)
            convertView = binding.root
            convertView.tag = binding
        } else {
            binding = convertView.tag as AdapterCultivationPersonListBinding
        }
        val person = list[arg0]
        val talentSymbol = if(CultivationHelper.isTalent(person) && showStar) "⭐" else ""
        val zhuSymbol = if(person.equipmentList.find { it.id == "7009001" } != null && showSpecEquipment) "\u4E3B" else ""
        val shaoSymbol = if(person.equipmentList.find { it.id == "7009002" } != null && showSpecEquipment) "\u5C11" else ""
        val gongSymbol = if(person.equipmentList.find { it.id == "7009003" } != null && showSpecEquipment) "\u90E1" else ""
        val neiSymbol = if(person.equipmentList.find { it.id == "7009009" } != null && showSpecEquipment) "\u5185" else ""

        binding.tvName.text = "${CultivationHelper.showing(person.name)}${CultivationHelper.showLifeTurn(person)}$talentSymbol"
        val lastRanking = person.battleRecord[CultivationHelper.mBattleRound.single] ?: 100
        if (lastRanking < 11){
            binding.ivRanking.visibility = View.VISIBLE
            when(lastRanking){
                1 -> binding.ivRanking.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.rank1))
                2 -> binding.ivRanking.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.rank2))
                3 -> binding.ivRanking.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.rank3))
                else -> binding.ivRanking.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.rank10))
            }
        }else{
            binding.ivRanking.visibility = View.GONE
        }

        binding.tvNameExtra.text = "$zhuSymbol$shaoSymbol$gongSymbol$neiSymbol"
        if( binding.tvNameExtra.text != ""){
            binding.tvNameExtra.setPadding(2,2,2,2)
        }else{
            binding.tvNameExtra.setPadding(0,0,0,0)
        }
        binding.tvAge.text = CultivationHelper.showAgeRemained(person)
        binding.tvJingjie.text = CultivationHelper.showing(person.jinJieName)
        binding.tvXiuwei.text = "${person.jinJieMax - person.xiuXei}"
        binding.tvLingGen.text = CultivationHelper.showing(person.lingGenName)
        binding.tvLingGen.setTextColor(Color.parseColor(CommonColors[person.lingGenDetail.color]))
        binding.tvAlliance.text = CultivationHelper.showing("${nation.find { it.id == person.nationId }?.name}-${person.allianceName}")

        binding.tvName.background = null
        binding.tvName.backgroundTintList = null

        return convertView
    }
}