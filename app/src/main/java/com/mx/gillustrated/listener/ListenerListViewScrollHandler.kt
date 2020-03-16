package com.mx.gillustrated.listener

import com.mx.gillustrated.R
import android.view.View
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView

class ListenerListViewScrollHandler constructor(lv: ListView, rl: RelativeLayout,
                                                private val mAddRowNum: Int, //增加了几个额外行 刷新行或者表头行都计入数量
                                                private val mScrollHandle: ScrollHandle?) : OnScrollListener {

    private var isLastRow = false
    private var lastCount = 0 //避免反复触发last事件
    private lateinit var listView: ListView
    private lateinit var pageVBoxLayout: RelativeLayout //计数用   例： 1/4
    private lateinit var pageText: TextView

    init {
        init(lv, rl)
    }

    constructor(lv: ListView, rl: RelativeLayout): this(lv, rl, 0, null)

    private fun init(lv: ListView, rl: RelativeLayout) {
        listView = lv
        pageVBoxLayout = rl
        pageText = pageVBoxLayout.findViewById<View>(R.id.pageText) as TextView
    }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int,
                          visibleItemCount: Int, totalItemCount: Int) {
        if (firstVisibleItem == 0)
            lastCount = 0

        if (pageVBoxLayout.visibility == View.VISIBLE) {
            setPageBox(true)
        }
        if (firstVisibleItem + visibleItemCount == totalItemCount
                && totalItemCount > 0 && lastCount < totalItemCount) {
            isLastRow = true
            lastCount = totalItemCount
        }
    }

    private fun setPageBox(isVisible: Boolean) {
        val totalCount = listView.count - mAddRowNum
        val visibleCount = listView.lastVisiblePosition + 1 - mAddRowNum
        pageText.text = if (isVisible) "$visibleCount/$totalCount" else ""
        pageVBoxLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {

        if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            setPageBox(true)
        }
        if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            setPageBox(true)
        }
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            setPageBox(false)
            if (isLastRow && mScrollHandle != null) {
                mScrollHandle.scrollLastRow(listView.count - mAddRowNum)
                isLastRow = false
            }
        }
    }

    interface ScrollHandle {
        fun scrollLastRow(totalCount: Int)
    }

}
