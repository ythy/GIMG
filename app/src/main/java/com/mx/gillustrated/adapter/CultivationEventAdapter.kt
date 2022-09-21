package com.mx.gillustrated.adapter

import android.content.Context
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
import com.mx.gillustrated.vo.cultivation.PersonEvent

class CultivationEventAdapter constructor(mContext: Context, private val list: List<PersonEvent>, private val callbacks: EventAdapterCallback) : BaseAdapter() {
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
                    R.layout.adapter_cultivation_event, arg2, false)
            component = ViewHolder(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as ViewHolder

        val values = list[arg0]
        component.content.text = CultivationHelper.showing("${values.happenTime/12}年 ${values.content}")

        component.del.setOnClickListener{
            callbacks.onDeleteHandler(values)
        }
        return convertView
    }

    internal class ViewHolder(view: View) {

        @BindView(R.id.tv_content)
        lateinit var content: TextView

        @BindView(R.id.btnDel)
        lateinit var del: ImageButton

        init {
            ButterKnife.bind(this, view)
        }
    }

    interface EventAdapterCallback {
        fun onDeleteHandler(event: PersonEvent)
    }

}