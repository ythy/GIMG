package com.mx.gillustrated.adapter

import android.content.Context
import android.os.Build
import com.mx.gillustrated.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Alliance
import com.mx.gillustrated.vo.cultivation.Clan
import com.mx.gillustrated.vo.cultivation.Person

class CultivationClanListAdapter  constructor(mContext: Context, private val list: List<Clan>) : BaseAdapter() {

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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getView(arg0: Int, convertViews: View?, arg2: ViewGroup): View {
        var convertView = convertViews
        lateinit var component: ViewHolder

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.adapter_clan_list, arg2, false)
            component = ViewHolder(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as ViewHolder

        val clan = list[arg0]
        val personList = clan.clanPersonList.map { it.value }

        component.name.text = CultivationHelper.showing(clan.name)
        if(clan.zhu != null)
            component.zhu.text = CultivationHelper.showing(clan.zhu!!.name)
        else
            component.zhu.text = ""
        component.persons.text = "${personList.size}-${personList.count { it.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
        component.total.text  = clan.totalXiuwei.toString()
        component.winner.text = clan.battleWinner.toString()
        return convertView
    }

    internal class ViewHolder(view: View) {

        @BindView(R.id.tv_name)
        lateinit var name: TextView

        @BindView(R.id.tv_zhu)
        lateinit var zhu: TextView

        @BindView(R.id.tv_persons)
        lateinit var persons: TextView

        @BindView(R.id.tv_winner)
        lateinit var winner: TextView

        @BindView(R.id.tv_total)
        lateinit var total: TextView


        init {
            ButterKnife.bind(this, view)
        }
    }
}
