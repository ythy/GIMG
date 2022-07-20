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
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationHelper.CommonColors
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Person

class CultivationPersonListAdapter constructor(context: Context, private val list: MutableList<Person>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

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
                    R.layout.adapter_cultivation_person_list, arg2, false)
            component = Component(convertView)
            convertView.tag = component
        } else {
            component = convertView.tag as Component
        }
        val person = list[arg0]
        val lifeTurn = if(person.lifeTurn == 0) "" else ".${person.lifeTurn}"
        component.name.text = "${CultivationHelper.showing(person.name)}$lifeTurn(${person.gender})"
        component.age.text = "${person.age}/${person.lifetime}-${person.ancestorLevel}"
        component.jingjie.text = CultivationHelper.showing(person.jinJieName)
        //component.jingjie.setTextColor(Color.parseColor(CommonColors[person.jinJieColor]))
        component.xiuwei.text = "${person.xiuXei}/${person.jinJieMax}"
        component.lingGen.text = CultivationHelper.showing(person.lingGenName)
        component.lingGen.setTextColor(Color.parseColor(CommonColors[person.lingGenType.color]))
        component.alliance.text = CultivationHelper.showing(person.allianceName)

        return convertView!!
    }


    internal class Component(view: View) {

        @BindView(R.id.tv_name)
        lateinit var name: TextView

        @BindView(R.id.tv_age)
        lateinit var age: TextView

        @BindView(R.id.tv_jingjie)
        lateinit var jingjie: TextView

        @BindView(R.id.tv_xiuwei)
        lateinit var xiuwei: TextView

        @BindView(R.id.tv_lingGen)
        lateinit var lingGen: TextView

        @BindView(R.id.tv_alliance)
        lateinit var alliance: TextView


        init {
            ButterKnife.bind(this, view)
        }


    }
}