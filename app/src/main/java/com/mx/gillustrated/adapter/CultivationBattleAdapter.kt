package com.mx.gillustrated.adapter

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationHelper.CommonColors
import com.mx.gillustrated.component.CultivationHelper.EnemyNames
import com.mx.gillustrated.component.CultivationHelper.showing
import com.mx.gillustrated.util.PinyinUtil
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

    @TargetApi(Build.VERSION_CODES.N)
    override fun getView(arg0: Int, convertViews: View?, arg2: ViewGroup): View {
        var convertView = convertViews
        lateinit var component: Component

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.adapter_cultivation_battle_his, arg2, false)
            component = Component(convertView)
            convertView.tag = component
        } else {
            component = convertView.tag as Component
        }
        val round = if(list[arg0].round > 0)  "${showing("\u56de\u5408")}${list[arg0].round}-${list[arg0].seq}:" else ""
        val history = "$round${showing(list[arg0].content)}"
        val battle = list[arg0]

        val matchResultWinner = "<${showing(battle.winner).replace("(", "\\(").replace(")", "\\)")}>".toRegex().find(history)
        val matchResultLooser = "<${showing(battle.looser).replace("(", "\\(").replace(")", "\\)")}>".toRegex().find(history)
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
        if (inColor) {
            component.tvRow.setText(spannable, TextView.BufferType.SPANNABLE)
        } else {
            component.tvRow.text = history
        }
        return convertView!!
    }

    internal class Component(view: View) {

        @BindView(R.id.tv_row)
        lateinit var tvRow: TextView

        init {
            ButterKnife.bind(this, view)
        }


    }
}