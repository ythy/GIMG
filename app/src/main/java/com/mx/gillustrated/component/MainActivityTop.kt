package com.mx.gillustrated.component

import android.widget.Spinner

import com.mx.gillustrated.R
import com.mx.gillustrated.activity.MainActivity
import com.mx.gillustrated.adapter.SpinnerCommonAdapter
import com.mx.gillustrated.database.DataBaseHelper
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.util.UIUtils
import com.mx.gillustrated.vo.CardInfo
import com.mx.gillustrated.vo.EventInfo
import com.mx.gillustrated.vo.GameInfo

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnItemSelected

import com.mx.gillustrated.util.CommonUtil.setSpinnerItemSelectedByValue2

/**
 * Created by maoxin on 2018/7/31.
 */

class MainActivityTop(private val mContext: MainActivity, private val mOrmHelper: DataBaseHelper, private val mTopHandle: TopHandle) {
    private val mTop: Top
    private var mSpinnerGameData: List<GameInfo>? = null
    private var mSpinnerChangedCount = 0 //控制 select变更后是否参与检索
    private var mSpinnerLastSelect: String? = null //保存最后一次本页面Spinner检索条件

    val spinnerSelectedIndexes: String
        get() = mTop.spinnerName.selectedItemPosition.toString() + "," +
                mTop.spinnerCost.selectedItemPosition + "," +
                mTop.spinnerAttr.selectedItemPosition + "," +
                mTop.spinnerEvent.selectedItemPosition + "," +
                mTop.spinnerFrontName.selectedItemPosition + "," +
                mTop.spinnerLevel.selectedItemPosition + ","

    val spinnerInfo: CardInfo?
        get() {
            if (mTop.spinnerName.selectedItem == null || mTop.spinnerCost.selectedItem == null ||
                    mTop.spinnerAttr.selectedItem == null || mTop.spinnerEvent.selectedItem == null ||
                    mTop.spinnerFrontName.selectedItem == null || mTop.spinnerLevel.selectedItem == null)
                return null

            val card = CardInfo()
            var spinnerSelected: CardInfo = mTop.spinnerName.selectedItem as CardInfo

            if (spinnerSelected.name != DEFAULT_NAME) {
                card.name = spinnerSelected.name
            }
            val spinnerSelected2 = mTop.spinnerEvent.selectedItem as EventInfo
            if (spinnerSelected2.name != DEFAULT_EVENT) {
                card.eventId = spinnerSelected2.id
            }
            spinnerSelected = mTop.spinnerCost.selectedItem as CardInfo
            if (spinnerSelected.name != DEFAULT_COST) {
                card.cost = Integer.parseInt(spinnerSelected.name)
            }
            spinnerSelected = mTop.spinnerAttr.selectedItem as CardInfo
            if (spinnerSelected.name != DEFAULT_ATTR) {
                card.attrId = spinnerSelected.attrId
            }
            spinnerSelected = mTop.spinnerFrontName.selectedItem as CardInfo
            if (spinnerSelected.name != DEFAULT_FRONT_NAME) {
                card.frontName = spinnerSelected.name
            }
            spinnerSelected = mTop.spinnerLevel.selectedItem as CardInfo
            if (spinnerSelected.name != DEFAULT_LEVEL) {
                card.level = spinnerSelected.name
            }
            return card
        }

    init {
        mTop = Top()
        initialize()
    }

    private fun initialize() {
        mSpinnerLastSelect = mContext.intent.getStringExtra("spinnerIndexs")
        UIUtils.setSpinnerClick(object : ArrayList<Spinner>() {
            init {
                add(mTop.spinnerName)
                add(mTop.spinnerCost)
                add(mTop.spinnerAttr)
                add(mTop.spinnerEvent)
                add(mTop.spinnerFrontName)
                add(mTop.spinnerLevel)
                add(mTop.spinnerGameList)
            }
        })
    }

    //此方法触发检索
    private fun initializeSppinersByGame(gametype: Int) {
        mSpinnerChangedCount = 6
        setSpinner(mTop.spinnerName, CardInfo.COLUMN_NAME, DEFAULT_NAME, gametype)
        setSpinner(mTop.spinnerCost, CardInfo.COLUMN_COST, DEFAULT_COST, gametype)
        setSpinner(mTop.spinnerAttr, CardInfo.COLUMN_ATTR, DEFAULT_ATTR, gametype)
        setSpinner(mTop.spinnerFrontName, CardInfo.COLUMN_FRONT_NAME, DEFAULT_FRONT_NAME, gametype)
        setSpinner(mTop.spinnerLevel, CardInfo.COLUMN_LEVEL, DEFAULT_LEVEL, gametype)
        //设置活动下拉列表
        val mEventList = mOrmHelper.eventInfoDao.getListByGameId(gametype, "Y")
        mEventList!!.add(0, EventInfo(DEFAULT_EVENT))
        val adapterEvent = SpinnerCommonAdapter(mContext, mEventList)
        mTop.spinnerEvent.adapter = adapterEvent

        if (mSpinnerLastSelect != null) {
            val temp = mSpinnerLastSelect!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            mTop.spinnerName.setSelection(Integer.parseInt(temp[0]))
            mTop.spinnerCost.setSelection(Integer.parseInt(temp[1]))
            mTop.spinnerAttr.setSelection(Integer.parseInt(temp[2]))
            mTop.spinnerEvent.setSelection(Integer.parseInt(temp[3]))
            mTop.spinnerFrontName.setSelection(Integer.parseInt(temp[4]))
            mTop.spinnerLevel.setSelection(Integer.parseInt(temp[5]))
            mSpinnerLastSelect = null
        }
    }

    private fun setSpinner(spinner: Spinner, columnType: String, defaultStr: String, gametype: Int) {
        val cardArray = mOrmHelper.cardInfoDao.queryCardDropList(columnType, gametype)
        Collections.sort(cardArray, dropListComparator)
        cardArray.add(0, CardInfo(defaultStr))
        val adapterName = SpinnerCommonAdapter(mContext, cardArray)
        spinner.adapter = adapterName
    }

    fun setGameList(gameType: Int, list: List<GameInfo>) {
        mSpinnerGameData = list
        val adapter: SpinnerCommonAdapter<*>
        adapter = SpinnerCommonAdapter(mContext, list)
        mTop.spinnerGameList.adapter = adapter
        setSpinnerItemSelectedByValue2(mTop.spinnerGameList, gameType.toString())
    }

    internal inner class Top {

        @BindView(R.id.spinnerName)
        lateinit var spinnerName: Spinner

        @BindView(R.id.spinnerCost)
        lateinit var spinnerCost: Spinner

        @BindView(R.id.spinnerAttr)
        lateinit var spinnerAttr: Spinner

        @BindView(R.id.spinnerEvent)
        lateinit var spinnerEvent: Spinner

        @BindView(R.id.spinnerGame)
        lateinit var spinnerGameList: Spinner

        @BindView(R.id.spinnerFrontName)
        lateinit var spinnerFrontName: Spinner

        @BindView(R.id.spinnerLevel)
        lateinit var spinnerLevel: Spinner

        @OnClick(R.id.btnRefresh)
        fun btnRefreshClickListener() {
            mTopHandle.onRefresh()

            mSpinnerChangedCount = 0
            if (spinnerName.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (spinnerCost.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (spinnerAttr.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (spinnerEvent.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (spinnerFrontName.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (spinnerLevel.selectedItemPosition > 0)
                mSpinnerChangedCount++

            if (mSpinnerChangedCount == 0) { //没有变化
                mTopHandle.onSearchData() //默认检索
            } else {
                spinnerName.setSelection(0)
                spinnerCost.setSelection(0)
                spinnerAttr.setSelection(0)
                spinnerEvent.setSelection(0)
                spinnerFrontName.setSelection(0)
                spinnerLevel.setSelection(0)
            }

        }

        @OnItemSelected(R.id.spinnerName, R.id.spinnerCost, R.id.spinnerAttr, R.id.spinnerEvent, R.id.spinnerFrontName, R.id.spinnerLevel)
        fun onSelectlistener() {
            if (mSpinnerChangedCount > 0)
                mSpinnerChangedCount--
            if (mSpinnerChangedCount == 0)
                mTopHandle.onSearchData()
        }

        @OnItemSelected(R.id.spinnerGame)
        fun onGameTypeSelectListener(position: Int) {
            val gameType = mSpinnerGameData!![position].id
            CommonUtil.setGameType(mContext, gameType)
            mTopHandle.onGameTypeChanged(gameType)
            initializeSppinersByGame(gameType) // 检索入口 由setSelection触发
        }

        init {
            ButterKnife.bind(this, mContext)
        }

    }

    interface TopHandle {
        fun onSearchData()
        fun onRefresh()
        fun onGameTypeChanged(type: Int)
    }

    companion object {
        private const val DEFAULT_NAME = "名称"
        private const val DEFAULT_COST = "コスト"
        private const val DEFAULT_ATTR = "属性"
        private const val DEFAULT_EVENT = "活动"
        private const val DEFAULT_FRONT_NAME = "前缀"
        private const val DEFAULT_LEVEL = "星级"
        private val dropListComparator = Comparator<CardInfo> { o1, o2 -> o2.nid - o1.nid }
    }
}
