package com.mx.gillustrated.component

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.MainActivity
import com.mx.gillustrated.vo.CardInfo
import java.util.HashMap
import com.mx.gillustrated.activity.BaseActivity.Companion.SHARE_SHOW_COST_COLUMN
import com.mx.gillustrated.databinding.AdapterMainlistHeaderBinding

/**
 * Created by maoxin on 2018/7/31.
 */

class MainActivityHeader(private val mContext: MainActivity, private val mHeaderHandle: HeaderHandle, gameId: Int) {

    private lateinit var mTextViewMap: Map<String, TextView>
    private lateinit var mResourceController: ResourceController
    private var mHeaderPressColor:Int = Color.TRANSPARENT
    private var mHeaderDefaultColor:Int = Color.WHITE
    private lateinit var binding:AdapterMainlistHeaderBinding

    init {
        initialize()
        setResourceController(gameId)
    }

    fun setResourceController(gameId: Int) {
        mResourceController = ResourceController(mContext, gameId)
        binding.tvHeaderHP.text = mResourceController.number1
        binding.tvHeaderAttack.text = mResourceController.number2
        binding.tvHeaderDefense.text = mResourceController.number3
        binding.tvHeaderExtra1.text = mResourceController.number4
        binding.tvHeaderExtra2.text = mResourceController.number5
        if ("E1" == mResourceController.number4) {
            binding.ivExtra1Gap.visibility = View.GONE
            binding.tvHeaderExtra1.visibility = View.GONE
        } else {
            binding.ivExtra1Gap.visibility = View.VISIBLE
            binding.tvHeaderExtra1.visibility = View.VISIBLE
        }
        if ("E2" == mResourceController.number5) {
            binding.ivExtra2Gap.visibility = View.GONE
            binding.tvHeaderExtra2.visibility = View.GONE
        } else {
            binding.ivExtra2Gap.visibility = View.VISIBLE
            binding.tvHeaderExtra2.visibility = View.VISIBLE
        }
        if (!mContext.mSP.getBoolean(SHARE_SHOW_COST_COLUMN + gameId, false)) {
            binding.ivCostGap.visibility = View.GONE
            binding.tvHeaderCost.visibility = View.GONE
        } else {
            binding.ivCostGap.visibility = View.VISIBLE
            binding.tvHeaderCost.visibility = View.VISIBLE
        }
    }
    @SuppressLint("ResourceType")
    private fun initialize() {
        val attribute = intArrayOf(R.attr.colorGridPrimary, R.attr.colorBackPrimary)
        val array = mContext.theme.obtainStyledAttributes(attribute)
        mHeaderPressColor = array.getColor(0, Color.TRANSPARENT)
        mHeaderDefaultColor = array.getColor(1, Color.WHITE)
        array.recycle()

        binding =  mContext.binding.llHeaderInclude
        setHeaderClickHandler(binding.tvHeaderHP, CardInfo.COLUMN_MAXHP)
        setHeaderClickHandler(binding.tvHeaderAttack, CardInfo.COLUMN_MAXATTACK)
        setHeaderClickHandler(binding.tvHeaderDefense, CardInfo.COLUMN_MAXDEFENSE)
        setHeaderClickHandler(binding.tvHeaderExtra1, CardInfo.COLUMN_EXTRA_VALUE1)
        setHeaderClickHandler(binding.tvHeaderExtra2, CardInfo.COLUMN_EXTRA_VALUE2)
        setHeaderClickHandler(binding.tvHeaderName, CardInfo.COLUMN_NAME)
        setHeaderClickHandler(binding.tvHeaderAttr, CardInfo.COLUMN_ATTR)
        setHeaderClickHandler(binding.tvHeaderCost, CardInfo.COLUMN_COST)
        setHeaderClickHandler(binding.tvHeaderImg, CardInfo.COLUMN_NID)

        mTextViewMap = object : HashMap<String, TextView>() {
            init {
                put(CardInfo.COLUMN_NID, binding.tvHeaderImg)
                put(CardInfo.COLUMN_MAXHP, binding.tvHeaderHP)
                put(CardInfo.COLUMN_MAXATTACK, binding.tvHeaderAttack)
                put(CardInfo.COLUMN_MAXDEFENSE, binding.tvHeaderDefense)
                put(CardInfo.COLUMN_EXTRA_VALUE1, binding.tvHeaderExtra1)
                put(CardInfo.COLUMN_EXTRA_VALUE2, binding.tvHeaderExtra2)
                put(CardInfo.COLUMN_NAME, binding.tvHeaderName)
                put(CardInfo.COLUMN_ATTR, binding.tvHeaderAttr)
                put(CardInfo.COLUMN_COST, binding.tvHeaderCost)
            }
        }
    }

    private fun setHeaderClickHandler(tv: TextView, column: String) {
        tv.setOnClickListener {
            setHeaderColor(column)
            mHeaderHandle.onHeaderClick(column)
        }
    }

    fun setHeaderColor(orderby: String) {
        for ((_, value) in mTextViewMap) {
            value.setBackgroundColor(mHeaderPressColor)
        }
        if (mTextViewMap[orderby] != null)
            mTextViewMap[orderby]?.setBackgroundColor(mHeaderDefaultColor)
    }

    interface HeaderHandle {
        fun onHeaderClick(c: String)
    }
}
