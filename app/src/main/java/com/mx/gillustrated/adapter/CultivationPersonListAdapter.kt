package com.mx.gillustrated.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.component.CultivationSetting.PostColors
import com.mx.gillustrated.vo.cultivation.Person

class CultivationPersonListAdapter constructor(private val context: Context, private val list: MutableList<Person>, private val showStar:Boolean = true) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val nation = CultivationHelper.mConfig.nation

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
        val talentSymbol = if(CultivationHelper.isTalent(person) && showStar) "⭐" else ""
        val zhuSymbol = if(person.equipmentListPair.find { it.first == "7009001" } != null && showStar) "\uD83C\uDFC1" else ""
        val shaoSymbol = if(person.equipmentListPair.find { it.first == "7009002" } != null && showStar) "\uD83C\uDFF4\u200D☠️" else ""
        val gongSymbol = if(person.equipmentListPair.find { it.first == "7009003" } != null && showStar) "\uD83C\uDFF3️\u200D\uD83C\uDF08" else ""
        val neiSymbol = if(person.equipmentListPair.find { it.first == "7009009" } != null && showStar) "\uD83C\uDF8C" else ""

        component.name.text = "${CultivationHelper.showing(person.name)}$talentSymbol$zhuSymbol$shaoSymbol$gongSymbol$neiSymbol${CultivationHelper.showLifeTurn(person)}"
        component.age.text = CultivationHelper.showAgeRemained(person)
        component.jingjie.text = CultivationHelper.showing(person.jinJieName)
        //component.jingjie.setTextColor(Color.parseColor(CommonColors[person.jinJieColor]))
        component.xiuwei.text = "${person.xiuXei}/${person.jinJieMax}"
        component.lingGen.text = CultivationHelper.showing(person.lingGenName)
        component.lingGen.setTextColor(Color.parseColor(CommonColors[person.lingGenType.color]))
        component.alliance.text = CultivationHelper.showing("${nation.find { it.id == person.nationId }?.name}-${person.allianceName}")

        if(person.nationPost > 0){
            component.name.background = context.getDrawable(R.drawable.box_bottom)
            component.name.backgroundTintList = ColorStateList.valueOf(Color.parseColor(PostColors[person.nationPost - 1]))
        }else{
            component.name.background = null
            component.name.backgroundTintList = null
        }

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