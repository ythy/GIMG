package com.mx.gillustrated.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.mx.gillustrated.component.CultivationBattleHelper
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.component.CultivationHelper.showing
import com.mx.gillustrated.databinding.AdapterCultivationBattleHisBinding
import com.mx.gillustrated.vo.cultivation.BattleInfoSeq

class CultivationBattleAdapter constructor(mContext: Context, private val list: List<BattleInfoSeq>) : BaseAdapter() {

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
        lateinit var component: AdapterCultivationBattleHisBinding

        if (convertView == null) {
            component = AdapterCultivationBattleHisBinding.inflate(layoutInflater, arg2, false)
            convertView = component.root
            convertView.tag = component
        } else {
            component = convertView.tag as AdapterCultivationBattleHisBinding
        }
        val round = if(list[arg0].round > 0)  "${showing("\u56de\u5408")}${list[arg0].round}-${list[arg0].seq}:" else ""
        val history = "$round${showing(list[arg0].content)}"
        val battle = list[arg0]

        val matchResultWinner = "<${showing(battle.winner).replace("(", "\\(").replace(")", "\\)")}(-[^>]+)?>".toRegex().find(history)
        val matchResultLooser = "<${showing(battle.looser).replace("(", "\\(").replace(")", "\\)")}(-[^>]+)?>".toRegex().find(history)
        var matchResultTeji: MatchResult? = null
        if (battle.teji != null) {
            matchResultTeji = showing(CultivationHelper.mConfig.teji.find { it.id == battle.teji }?.name!!).toRegex().find(history)
        }
        var inColor = false
        val spannable = SpannableString(history)
        if (matchResultWinner != null) {
            inColor = true
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#108A5E")), matchResultWinner.range.start, matchResultWinner.range.endInclusive + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (matchResultLooser != null) {
            inColor = true
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#BF7C92")), matchResultLooser.range.start, matchResultLooser.range.endInclusive + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (matchResultTeji != null) {
            inColor = true
            spannable.setSpan(ForegroundColorSpan(Color.parseColor(CommonColors[CultivationHelper.mConfig.teji.find { it.id == battle.teji }!!.rarity])), matchResultTeji.range.start, matchResultTeji.range.endInclusive + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val positiveMatchResults = showing(CultivationBattleHelper.SpecPositiveWords.joinToString("|")).toRegex().findAll(history)
        positiveMatchResults.forEach {
            inColor = true
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#FF0000")), it.range.start, it.range.endInclusive + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val negativeMatchResults = showing(CultivationBattleHelper.SpecNegativeWords.joinToString("|")).toRegex().findAll(history)
        negativeMatchResults.forEach {
            inColor = true
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#0000FF")), it.range.start, it.range.endInclusive + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (inColor) {
            component.tvRow.setText(spannable, TextView.BufferType.SPANNABLE)
        } else {
            component.tvRow.text = history
        }
        return convertView
    }


}