package com.mx.gillustrated.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.AdapterClanListBinding
import com.mx.gillustrated.vo.cultivation.Clan

@SuppressLint("SetTextI18n")
class CultivationClanListAdapter(private val callback: Callback): ListAdapter<Clan, CultivationClanListAdapter.ViewHolder>(ClanDiffCallback) {

    object ClanDiffCallback : DiffUtil.ItemCallback<Clan>() {
        override fun areItemsTheSame(oldItem: Clan, newItem: Clan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Clan, newItem: Clan): Boolean {
            return newItem == oldItem
        }
    }

    class ViewHolder(val binding: AdapterClanListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AdapterClanListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val binding = viewHolder.binding
        val clan = getItem(position)

        binding.tvName.text = CultivationHelper.showing(clan.name)
        if(clan.zhu != null)
            binding.tvZhu.text = CultivationHelper.showing(clan.zhu!!.name)
        else
            binding.tvZhu.text = ""
        binding.tvPersons.text = clan.totalPerson
        binding.tvTotal.text  = CultivationHelper.showLifeTurn(clan.totalXiuwei)
        binding.tvWinner.text = clan.battleWinner.toString()

        binding.root.setOnClickListener {
            callback.onItemClick(clan)
        }
    }

    interface Callback{
        fun onItemClick(item:Clan)
    }
}
