package com.mx.gillustrated.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.mx.gillustrated.vo.SpinnerInfo

/**
 * Created by maoxin on 2018/7/17.
 */

class SpinnerCommonAdapter<T : SpinnerInfo> constructor(mContext: Context, private val list: List<T>) : BaseAdapter() {

    private var layoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var mExtraParentheses = true
    private var mFontSize = 0

    constructor(context: Context, items: List<T>, extraParentheses: Boolean, fontSize: Int): this(context, items) {
        this.mExtraParentheses = extraParentheses
        this.mFontSize = fontSize
    }


    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): T {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //Dropdown Item Style
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mDropDownResource, false)
    }

    //Select Item Style
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mResource, true)
    }

    private fun createViewFromResource(position: Int, convertView: View?,
                                       parent: ViewGroup, resource: Int, defaultView: Boolean): View {

        val view: View = convertView ?: layoutInflater.inflate(resource, parent, false)
        val text: TextView

        text = view as TextView
        if (mFontSize > 0) {
            text.textSize = mFontSize.toFloat()
            if (defaultView)
                text.setPadding(0, 0, 0, 0)
        }

        val items = getItem(position)

        val textToShow = if (mExtraParentheses && items.nid > 0)
            items.name + " (" + items.nid + ")"
        else
            items.name
        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(textToShow)
        text.setText(spanned, TextView.BufferType.SPANNABLE)

        return view

    }

    companion object {
        private const val mResource = android.R.layout.simple_spinner_item
        private const val mDropDownResource = android.R.layout.simple_spinner_dropdown_item
    }
}
