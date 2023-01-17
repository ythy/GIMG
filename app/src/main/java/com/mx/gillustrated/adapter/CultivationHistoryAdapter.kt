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
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.component.CultivationSetting.EnemyNames
import com.mx.gillustrated.util.PinyinUtil

class CultivationHistoryAdapter constructor(val mContext: CultivationActivity, private val list: List<CultivationSetting.HistoryInfo>) : BaseAdapter() {

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

        val spannable = SpannableString(history)
        repeat(6) { index->
            val matchResult = "(${EnemyNames[index]}|${PinyinUtil.convert(EnemyNames[index])})[0-9]+(hao|号)".toRegex().find(history)
            if(matchResult != null && !CultivationHelper.pinyinMode) {
                spannable.setSpan(ForegroundColorSpan(Color.parseColor(CommonColors[index + 2])), matchResult.range.start, matchResult.range.endInclusive + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        component.tvRow.setText(spannable, TextView.BufferType.SPANNABLE)
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