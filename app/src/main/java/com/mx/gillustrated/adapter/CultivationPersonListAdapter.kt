package com.mx.gillustrated.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mx.gillustrated.R
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.databinding.AdapterCultivationPersonListBinding
import com.mx.gillustrated.vo.cultivation.Person

@SuppressLint("SetTextI18n")
class CultivationPersonListAdapter constructor(private val showStar:Boolean, private val showSpecEquipment:Boolean, private val callback: Callback) :
        ListAdapter<Person, CultivationPersonListAdapter.ViewHolder>(PersonDiffCallback) {

    private val nation = CultivationHelper.mConfig.nation

    object PersonDiffCallback : DiffUtil.ItemCallback<Person>() {
        override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
            return false
        }
    }

    class ViewHolder(val binding: AdapterCultivationPersonListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AdapterCultivationPersonListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val binding = viewHolder.binding
        val context = binding.root.context
        val person = getItem(position)
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


        binding.root.setOnClickListener {
            callback.onItemClick(person)
        }
    }

    interface Callback{
        fun onItemClick(item: Person)
    }




}