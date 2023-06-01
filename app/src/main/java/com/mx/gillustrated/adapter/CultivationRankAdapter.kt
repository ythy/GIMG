package com.mx.gillustrated.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.databinding.AdapterCultivationRankBinding
import com.mx.gillustrated.vo.cultivation.SimpleData


@SuppressLint("SetTextI18n")
class CultivationRankAdapter(private val callback: Callback): ListAdapter<SimpleData, CultivationRankAdapter.ViewHolder>(RankDiffCallback) {

    object RankDiffCallback : DiffUtil.ItemCallback<SimpleData>() {
        override fun areItemsTheSame(oldItem: SimpleData, newItem: SimpleData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SimpleData, newItem: SimpleData): Boolean {
            return newItem == oldItem
        }
    }

    class ViewHolder(val binding: AdapterCultivationRankBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AdapterCultivationRankBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val binding = viewHolder.binding
        val data = getItem(position)
        binding.tvName.text = CultivationHelper.showing(data.name)
        binding.tvSeq.text =  if(data.remark == "" )  data.seq.toString() else CultivationHelper.showing(data.remark ?: "")
        binding.root.setOnClickListener {
            callback.onItemClick(data)
        }
    }

    interface Callback{
        fun onItemClick(item: SimpleData)
    }
}
