package com.mx.gillustrated.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/**
 * Created by maoxin on 2017/2/23.
 */

class SpinnerSimpleAdapter(mContext: Context, private val list: List<String>, private val mFontSize: Int = 0) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): String {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mDropDownResource, false)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mResource, true)
    }

    // defaultView : view or dropdown view
    private fun createViewFromResource(position: Int, convertView: View?,
                                       parent: ViewGroup, resource: Int, defaultView: Boolean): View {

        val view = convertView ?: layoutInflater.inflate(resource, parent, false)
        val text = view as TextView

        if (mFontSize > 0) {
            text.textSize = mFontSize.toFloat()
            if (defaultView)
                text.setPadding(0, 0, 0, 0)
        }

        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(getItem(position), Html.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(getItem(position))
        text.setText(spanned, TextView.BufferType.SPANNABLE)

        return view

    }

    companion object {
        private const val mResource = android.R.layout.simple_gallery_item
        private const val mDropDownResource = android.R.layout.simple_spinner_dropdown_item
    }
}
