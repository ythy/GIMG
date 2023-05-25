package com.mx.gillustrated.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.AdapterAllianceListBinding
import com.mx.gillustrated.vo.cultivation.Alliance
import androidx.recyclerview.widget.ListAdapter

@SuppressLint("SetTextI18n")
class AllianceListAdapter(private val callback: Callback): ListAdapter<Alliance, AllianceListAdapter.ViewHolder>(AllianceDiffCallback) {

    private val nation = CultivationHelper.mConfig.nation

    object AllianceDiffCallback : DiffUtil.ItemCallback<Alliance>() {
        override fun areItemsTheSame(oldItem: Alliance, newItem: Alliance): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Alliance, newItem: Alliance): Boolean {
            return newItem == oldItem
        }
    }

    class ViewHolder(val binding: AdapterAllianceListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AdapterAllianceListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val binding = viewHolder.binding
        val data = getItem(position)
        val abridgeName = if (data.abridgeName != "") "(${data.abridgeName})" else ""
        binding.tvName.text =  CultivationHelper.showing("${nation.find { it.id == data.nation }?.name}-${data.name}$abridgeName")
        binding.tvPersons.text =  "${data.personList.size}-${data.personList.count { it.value.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
        binding.tvTotal.text  = CultivationHelper.showLifeTurn(data.totalXiuwei)
        binding.tvWinner.text  = data.battleWinner.toString()
        binding.root.setOnClickListener {
            callback.onItemClick(data)
        }
    }

    interface Callback{
        fun onItemClick(item:Alliance)
    }
}
