package com.mx.gillustrated.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationHelper.CommonColors
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.vo.cultivation.Follower

class CultivationFollowerAdapter constructor(mContext: Context, private val list: List<Follower>, private val callbacks: FollowerAdapterCallback) : BaseAdapter() {
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
                    R.layout.adapter_cultivation_follower, arg2, false)
            component = ViewHolder(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as ViewHolder

        val values = list[arg0]
        component.name.text = CultivationHelper.showing(values.name)
        component.name.setTextColor(Color.parseColor(CommonColors[values.rarity]))
        if(values.teji.isEmpty())
            component.teji.text = ""
        else
            component.teji.text = CultivationHelper.showing(mConfig.teji.filter { values.teji.contains(it.id) }.joinToString { it.name })

        component.props.text = values.property.take(4).joinToString()

        component.del.setOnClickListener{
            callbacks.onDeleteHandler(values)
        }
        return convertView
    }

    internal class ViewHolder(view: View) {

        @BindView(R.id.tv_name)
        lateinit var name: TextView

        @BindView(R.id.tv_props)
        lateinit var props: TextView

        @BindView(R.id.tv_teji)
        lateinit var teji: TextView

        @BindView(R.id.btnDel)
        lateinit var del: ImageButton

        init {
            ButterKnife.bind(this, view)
        }
    }

    interface FollowerAdapterCallback {
        fun onDeleteHandler(follower: Follower)
    }

}