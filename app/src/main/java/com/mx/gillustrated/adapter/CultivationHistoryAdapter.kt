package com.mx.gillustrated.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.component.CultivationSetting.EnemyNames
import com.mx.gillustrated.util.PinyinUtil
import  com.mx.gillustrated.component.CultivationSetting.HistoryInfo
import com.mx.gillustrated.databinding.AdapterCultivationHistoryBinding

@SuppressLint("SetTextI18n")
class CultivationHistoryAdapter (private val callback: Callback): ListAdapter<HistoryInfo, CultivationHistoryAdapter.ViewHolder>(HistoryDiffCallback) {


    object HistoryDiffCallback : DiffUtil.ItemCallback<HistoryInfo>() {
        override fun areItemsTheSame(oldItem: HistoryInfo, newItem: HistoryInfo): Boolean {
            return oldItem.xun == newItem.xun && oldItem.content == newItem.content
        }

        override fun areContentsTheSame(oldItem: HistoryInfo, newItem: HistoryInfo): Boolean {
            return newItem == oldItem
        }
    }

    class ViewHolder(val binding: AdapterCultivationHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AdapterCultivationHistoryBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val binding = viewHolder.binding
        val data = getItem(position)

        val history = data.content
        val person = data.person

        val spannable = SpannableString(history)
        repeat(6) { index->
            val matchResult = "(${EnemyNames[index]}|${PinyinUtil.convert(EnemyNames[index])})[0-9]+(hao|号)".toRegex().find(history)
            if(matchResult != null && !CultivationHelper.pinyinMode) {
                spannable.setSpan(ForegroundColorSpan(Color.parseColor(CommonColors[index + 2])), matchResult.range.first, matchResult.range.last + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        if( person != null && person.jinJieName.indexOf("-") == -1 ){
            var lingGen = person.lingGenName
            if(person.lingGenDetail.type > 0){
                lingGen = CultivationHelper.getTianName(person.lingGenSpecId)
            }
            val lingGenColor = CommonColors[person.lingGenDetail.color]
            val index = history.indexOf(" $lingGen ")
            if(index > -1){
                spannable.setSpan(ForegroundColorSpan(Color.parseColor(lingGenColor)), index, index + lingGen.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        binding.tvRow.setText(spannable, TextView.BufferType.SPANNABLE)

        binding.root.setOnClickListener {
            callback.onItemClick(data)
        }
    }


    fun getData():List<HistoryInfo>{
        return currentList
    }

    interface Callback{
        fun onItemClick(item:HistoryInfo)
    }





}