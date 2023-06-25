package com.mx.gillustrated.component

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
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
import com.mx.gillustrated.databinding.ActivityMainBinding
import com.mx.gillustrated.util.CommonUtil.setSpinnerItemSelectedByValue2

/**
 * Created by maoxin on 2018/7/31.
 */

class MainActivityTop(private val mContext: MainActivity, private val binding: ActivityMainBinding,
                      private val mOrmHelper: DataBaseHelper, private val mTopHandle: TopHandle) {
    private var mSpinnerGameData: List<GameInfo>? = null
    private var mSpinnerChangedCount = 0 //控制 select变更后是否参与检索
    private var mSpinnerLastSelect: String? = null //保存最后一次本页面Spinner检索条件

    val spinnerSelectedIndexes: String
        get() = binding.spinnerName.selectedItemPosition.toString() + "," +
                binding.spinnerCost.selectedItemPosition + "," +
                binding.spinnerAttr.selectedItemPosition + "," +
                binding.spinnerEvent.selectedItemPosition + "," +
                binding.spinnerFrontName.selectedItemPosition + "," +
                binding.spinnerLevel.selectedItemPosition + ","

    val spinnerInfo: CardInfo?
        get() {
            if (binding.spinnerName.selectedItem == null || binding.spinnerCost.selectedItem == null ||
                binding.spinnerAttr.selectedItem == null || binding.spinnerEvent.selectedItem == null ||
                binding.spinnerFrontName.selectedItem == null || binding.spinnerLevel.selectedItem == null)
                return null

            val card = CardInfo()
            var spinnerSelected: CardInfo = binding.spinnerName.selectedItem as CardInfo

            if (spinnerSelected.name != DEFAULT_NAME) {
                card.name = spinnerSelected.name
            }
            val spinnerSelected2 = binding.spinnerEvent.selectedItem as EventInfo
            if (spinnerSelected2.name != DEFAULT_EVENT) {
                card.eventId = spinnerSelected2.id
            }
            spinnerSelected = binding.spinnerCost.selectedItem as CardInfo
            if (spinnerSelected.name != DEFAULT_COST) {
                card.cost = Integer.parseInt(spinnerSelected.name ?: "")
            }
            spinnerSelected = binding.spinnerAttr.selectedItem as CardInfo
            if (spinnerSelected.name != DEFAULT_ATTR) {
                card.attrId = spinnerSelected.attrId
            }
            spinnerSelected = binding.spinnerFrontName.selectedItem as CardInfo
            if (spinnerSelected.name != DEFAULT_FRONT_NAME) {
                card.frontName = spinnerSelected.name
            }
            spinnerSelected = binding.spinnerLevel.selectedItem as CardInfo
            if (spinnerSelected.name != DEFAULT_LEVEL) {
                card.level = spinnerSelected.name
            }
            return card
        }

    init {
        initialize()
    }

    private fun initialize() {
        mSpinnerLastSelect = mContext.intent.getStringExtra("spinnerIndexs")
        UIUtils.setSpinnerClick(object : ArrayList<Spinner>() {
            init {
                add(binding.spinnerName)
                add(binding.spinnerCost)
                add(binding.spinnerAttr)
                add(binding.spinnerEvent)
                add(binding.spinnerFrontName)
                add(binding.spinnerLevel)
                add(binding.spinnerGame)
            }
        })
        binding.btnRefresh.setOnClickListener {
            mTopHandle.onRefresh()

            mSpinnerChangedCount = 0
            if (binding.spinnerName.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (binding.spinnerCost.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (binding.spinnerAttr.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (binding.spinnerEvent.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (binding.spinnerFrontName.selectedItemPosition > 0)
                mSpinnerChangedCount++
            if (binding.spinnerLevel.selectedItemPosition > 0)
                mSpinnerChangedCount++

            if (mSpinnerChangedCount == 0) { //没有变化
                mTopHandle.onSearchData() //默认检索
            } else {
                binding.spinnerName.setSelection(0)
                binding.spinnerCost.setSelection(0)
                binding.spinnerAttr.setSelection(0)
                binding.spinnerEvent.setSelection(0)
                binding.spinnerFrontName.setSelection(0)
                binding.spinnerLevel.setSelection(0)
            }
        }

        addSpinnerSelectListener(binding.spinnerName)
        addSpinnerSelectListener(binding.spinnerCost)
        addSpinnerSelectListener(binding.spinnerAttr)
        addSpinnerSelectListener(binding.spinnerEvent)
        addSpinnerSelectListener(binding.spinnerFrontName)
        addSpinnerSelectListener(binding.spinnerLevel)

        binding.spinnerGame.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val gameType = mSpinnerGameData!![position].id
                CommonUtil.setGameType(mContext, gameType)
                mTopHandle.onGameTypeChanged(gameType)
                initializeSppinersByGame(gameType) // 检索入口 由setSelection触发
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun addSpinnerSelectListener(spinner: Spinner){
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (mSpinnerChangedCount > 0)
                    mSpinnerChangedCount--
                if (mSpinnerChangedCount == 0)
                    mTopHandle.onSearchData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    //此方法触发检索
    private fun initializeSppinersByGame(gametype: Int) {
        mSpinnerChangedCount = 6
        setSpinner(binding.spinnerName, CardInfo.COLUMN_NAME, DEFAULT_NAME, gametype)
        setSpinner(binding.spinnerCost, CardInfo.COLUMN_COST, DEFAULT_COST, gametype)
        setSpinner(binding.spinnerAttr, CardInfo.COLUMN_ATTR, DEFAULT_ATTR, gametype)
        setSpinner(binding.spinnerFrontName, CardInfo.COLUMN_FRONT_NAME, DEFAULT_FRONT_NAME, gametype)
        setSpinner(binding.spinnerLevel, CardInfo.COLUMN_LEVEL, DEFAULT_LEVEL, gametype)
        //设置活动下拉列表
        val mEventList = mOrmHelper.eventInfoDao.getListByGameId(gametype, "Y")
        mEventList!!.add(0, EventInfo(DEFAULT_EVENT))
        val adapterEvent = SpinnerCommonAdapter(mContext, mEventList)
        binding.spinnerEvent.adapter = adapterEvent

        if (mSpinnerLastSelect != null) {
            val temp = mSpinnerLastSelect!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            binding.spinnerName.setSelection(Integer.parseInt(temp[0]))
            binding.spinnerCost.setSelection(Integer.parseInt(temp[1]))
            binding.spinnerAttr.setSelection(Integer.parseInt(temp[2]))
            binding.spinnerEvent.setSelection(Integer.parseInt(temp[3]))
            binding.spinnerFrontName.setSelection(Integer.parseInt(temp[4]))
            binding.spinnerLevel.setSelection(Integer.parseInt(temp[5]))
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
        binding.spinnerGame.adapter = adapter
        setSpinnerItemSelectedByValue2(binding.spinnerGame, gameType.toString())
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
