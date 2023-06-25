package com.mx.gillustrated.component

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.AdapterView
import com.mx.gillustrated.activity.MainActivity
import com.mx.gillustrated.adapter.DataListAdapter
import com.mx.gillustrated.database.DataBaseHelper
import com.mx.gillustrated.listener.ListenerListViewScrollHandler
import com.mx.gillustrated.vo.CardInfo
import java.util.ArrayList
import com.mx.gillustrated.activity.BaseActivity.Companion.SHARE_PAGE_SIZE
import com.mx.gillustrated.databinding.ActivityMainBinding
import java.lang.ref.WeakReference

/**
 * Created by maoxin on 2018/8/2.
 */

class MainActivityListView(private val mContext: MainActivity, private val binding: ActivityMainBinding,
                           private val mOrmHelper: DataBaseHelper, private val mDataViewHandle: DataViewHandle) {

    private var mList: MutableList<CardInfo> = ArrayList()
    private var mAdapter: DataListAdapter? = null
    private var mListViewLastPosition: Int = 0 //保存最后一次本页面滚动位置
    private var currentPage: Int = 0
    private var initPage: Int = 0 //初始页数，默认是1  如果从详细页面返回，可能为1+
    private var totalItemCount: Int = 0
    private val listHandler:ListHandler = ListHandler(this)
    var searchCondition: CardInfo = CardInfo()
        private set
    private lateinit var mOrderBy: String
    private lateinit var mIsAsc: String
    val orderBy: String
        get() = "$mOrderBy*$mIsAsc"

    val idListWithProfile: IntArray
        get() {
            val temp =  mutableListOf<Int>()
            for (i in mList.indices) {
                if ("Y" == mList[i].profile)
                    temp.add(mList[i].id)
            }
            val result = IntArray(temp.size)
            for (i in temp.indices) {
                result[i] = temp[i]
            }
            return result
        }


    val dataList: List<CardInfo>
        get() = mList

    private val itemClickListener = AdapterView.OnItemClickListener { arg0, _, position, _ ->
        if (position >= 0) {
            val info = arg0.getItemAtPosition(position) as CardInfo
            mDataViewHandle.onListItemClick(info, position, arg0.count, currentPage)
        }
    }

    private class ListHandler constructor(component: MainActivityListView ) : Handler(Looper.getMainLooper()) {

        private val weakReference: WeakReference<MainActivityListView> = WeakReference(component)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val component = weakReference.get()!!
            @Suppress("UNCHECKED_CAST") val list = msg.obj as List<CardInfo>
            component.totalItemCount = if (list.isEmpty()) 0 else list[0].totalCount

            if (component.currentPage == 1 || component.initPage > 1) {
                component.initPage = 1//恢复 只有初始第一次可能 >1
                component.mList.clear()
                component.mList.addAll(list)
                component.updateList(true)
            } else {
                component.mList.addAll(list)
                component.updateList(false)
            }
            component.mDataViewHandle.onSearchCompleted()
        }
    }

    init {
        initialize()
    }

    private fun initialize() {
        mListViewLastPosition = mContext.intent.getIntExtra("position", 0)
        initPage = mContext.intent.getIntExtra("currentPage", 1)
        currentPage = initPage
        val order = mContext.intent.getStringExtra("orderBy")
        mOrderBy = if (order == null) INIT_ORDER_BY else order.split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        mIsAsc = if (order == null) INIT_ORDER_TYPE else order.split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

        binding.pageVBox.visibility = android.view.View.GONE
        binding.lvMain.onItemClickListener = itemClickListener
        binding.lvMain.setOnScrollListener(ListenerListViewScrollHandler(binding.lvMain, binding.pageVBox, 0,
                object : ListenerListViewScrollHandler.ScrollHandle {
                    override fun scrollLastRow(totalCount: Int) {
                        if (totalCount == totalItemCount)
                            return
                        currentPage++
                        search()
                    }
                }))
        mAdapter = DataListAdapter(mContext, mList)
    }


    private fun search() {
        mDataViewHandle.onSearchStart()
        val pageSize = mContext.mSP.getInt(SHARE_PAGE_SIZE + mContext.mGameType, PAGE_SIZE)
        Thread{
            val list = mOrmHelper.cardInfoDao.queryCards(searchCondition, mOrderBy, mIsAsc == CardInfo.SORT_ASC,
                    (if (initPage == 1) (currentPage - 1) * pageSize else 0).toLong(), (initPage * pageSize).toLong())
            val msg = Message.obtain()
            msg.obj = list
            listHandler.sendMessage(msg)
        }.start()
    }

    fun searchData(info: CardInfo) {
        searchCondition = info
        currentPage = 1
        search()
    }

    fun searchData(pinyin: String) {
        searchCondition.pinyinName = pinyin
        currentPage = 1
        search()
    }

    fun searchDataOrderBy(order: String) {
        mIsAsc = if (mOrderBy == order)
            if (mIsAsc == CardInfo.SORT_ASC) CardInfo.SORT_DESC else CardInfo.SORT_ASC
        else
            CardInfo.SORT_DESC
        mOrderBy = order
        currentPage = 1
        search()
    }

    fun setOrderBy(orderBy: String?, isAsc: String?) {
        mOrderBy = orderBy ?: INIT_ORDER_BY
        mIsAsc = isAsc ?: INIT_ORDER_TYPE
    }


    private fun updateList(flag: Boolean) {
        if (flag)
            binding.lvMain.adapter = mAdapter
        else
            mAdapter!!.notifyDataSetChanged()

        if (mListViewLastPosition > 0) {
            binding.lvMain.setSelection(mListViewLastPosition)
            mListViewLastPosition = 0
        }

    }


    interface DataViewHandle {
        fun onListItemClick(info: CardInfo, position: Int, totalCount: Int, currentPage: Int)
        fun onSearchCompleted()
        fun onSearchStart()
    }

    companion object {
        private const val INIT_ORDER_BY = CardInfo.ID
        private const val INIT_ORDER_TYPE = CardInfo.SORT_DESC
        private const val PAGE_SIZE = 50
    }

}
