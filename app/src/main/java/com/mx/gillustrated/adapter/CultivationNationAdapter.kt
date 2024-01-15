package com.mx.gillustrated.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.AdapterCultivationNationBinding
import com.mx.gillustrated.vo.cultivation.Nation

@SuppressLint("SetTextI18n")
class CultivationNationAdapter(private val callback: Callback): ListAdapter<Nation, CultivationNationAdapter.ViewHolder>(NationDiffCallback) {

    object NationDiffCallback : DiffUtil.ItemCallback<Nation>() {
        override fun areItemsTheSame(oldItem: Nation, newItem: Nation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Nation, newItem: Nation): Boolean {
            return newItem == oldItem
        }
    }

    class ViewHolder(val binding: AdapterCultivationNationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AdapterCultivationNationBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val binding = viewHolder.binding
        val data = getItem(position)

        binding.tvName.text =  CultivationHelper.showing(data.name)
        binding.tvPersons.text =  data.totalPerson
        binding.tvWinner.text = data.battleWinner.toString()
        binding.tvTotal.text  = CultivationHelper.showLifeTurn(data.totalTurn.toLong())

        binding.root.setOnClickListener {
            callback.onItemClick(data)
        }
    }

    interface Callback{
        fun onItemClick(item: Nation)
    }
}
