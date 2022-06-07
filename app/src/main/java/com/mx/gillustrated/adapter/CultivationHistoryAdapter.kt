package com.mx.gillustrated.adapter

import android.annotation.TargetApi
import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import com.mx.gillustrated.util.PinyinUtil

class CultivationHistoryAdapter constructor(val mContext: CultivationActivity, private val list: List<CultivationHelper.HistoryInfo>) : BaseAdapter() {

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
                    R.layout.adapter_cultivation_history, arg2, false)
            component = Component(convertView)
            convertView.tag = component
        } else {
            component = convertView.tag as Component
        }
        val history = list[arg0].content
        val person = list[arg0].person
        if(person != null && person.jinJieName.indexOf("-") == -1){
            var lingGen = person.lingGenName
            if(person.lingGenType.id == "1000006" || person.lingGenType.id == "1000007"){
                lingGen = CultivationHelper.getTianName(person.lingGenId)
            }
            val lingGenColor = CommonColors[person.lingGenType.color]
            val index = history.indexOf(" $lingGen ")
            val spannable = SpannableString(history)
            if(index > -1){
                spannable.setSpan(ForegroundColorSpan(Color.parseColor(lingGenColor)), index, index + lingGen.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            component.tvRow.setText(spannable, TextView.BufferType.SPANNABLE)
        }else{
            val matchResult1 = "(${EnemyNames[0]}|${PinyinUtil.convert(EnemyNames[0])})[0-9]+(hao|号)".toRegex().find(history)
            val matchResult2 = "(${EnemyNames[1]}|${PinyinUtil.convert(EnemyNames[1])})[0-9]+(hao|号)".toRegex().find(history)
            when {
                matchResult1 != null -> {
                    val spannable = SpannableString(history)
                    spannable.setSpan(ForegroundColorSpan(Color.parseColor(CommonColors[2])), matchResult1.range.start, matchResult1.range.endInclusive + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    component.tvRow.setText(spannable, TextView.BufferType.SPANNABLE)
                }
                matchResult2 != null -> {
                    val spannable = SpannableString(history)
                    spannable.setSpan(ForegroundColorSpan(Color.parseColor(CommonColors[1])), matchResult2.range.start, matchResult2.range.endInclusive + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    component.tvRow.setText(spannable, TextView.BufferType.SPANNABLE)
                }
                else -> component.tvRow.text = history
            }

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