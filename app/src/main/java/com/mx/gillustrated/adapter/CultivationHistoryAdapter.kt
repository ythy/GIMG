package com.mx.gillustrated.adapter

import android.graphics.Color
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

class CultivationHistoryAdapter constructor(val mContext: CultivationActivity, private val list: List<CultivationActivity.HistoryInfo>) : BaseAdapter() {

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
        if(person != null){
            var lingGen = person.lingGenName
            if(person.lingGenType.id == "1000006"){
                lingGen = mContext.getTianName(person.lingGenId)
            }
            val lingGenColor = person.lingGenType.color
            val index = history.indexOf(lingGen)
            val spannable = SpannableString(history)
            spannable.setSpan(ForegroundColorSpan(Color.parseColor(lingGenColor)), index, index + lingGen.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            component.tvRow.setText(spannable, TextView.BufferType.SPANNABLE)

        }else{
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