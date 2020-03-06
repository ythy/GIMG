package com.mx.gillustrated.component

import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.mx.gillustrated.R
import com.mx.gillustrated.activity.MainActivity
import com.mx.gillustrated.vo.CardInfo

import java.util.HashMap

import butterknife.BindColor
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.activity.BaseActivity.SHARE_SHOW_COST_COLUMN

/**
 * Created by maoxin on 2018/7/31.
 */

class MainActivityHeader(private val mContext: MainActivity, private val mHeaderHandle: HeaderHandle, gameId: Int) {

    private lateinit var mTextViewMap: Map<String, TextView>
    private lateinit var mListHeaderView: ListHeaderView
    private lateinit var mResourceController: ResourceController

    init {
        initialize()
        setResourceController(gameId)
    }

    fun setResourceController(gameId: Int) {
        mResourceController = ResourceController(mContext, gameId)
        mListHeaderView.tvHP.text = mResourceController.number1
        mListHeaderView.tvAttack.text = mResourceController.number2
        mListHeaderView.tvDefense.text = mResourceController.number3
        mListHeaderView.tvExtra1.text = mResourceController.number4
        mListHeaderView.tvExtra2.text = mResourceController.number5
        if ("E1" == mResourceController.number4) {
            mListHeaderView.ivExtraGap1.visibility = View.GONE
            mListHeaderView.tvExtra1.visibility = View.GONE
        } else {
            mListHeaderView.ivExtraGap1.visibility = View.VISIBLE
            mListHeaderView.tvExtra1.visibility = View.VISIBLE
        }
        if ("E2" == mResourceController.number5) {
            mListHeaderView.ivExtraGap2.visibility = View.GONE
            mListHeaderView.tvExtra2.visibility = View.GONE
        } else {
            mListHeaderView.ivExtraGap2.visibility = View.VISIBLE
            mListHeaderView.tvExtra2.visibility = View.VISIBLE
        }
        if (!mContext.mSP.getBoolean(SHARE_SHOW_COST_COLUMN + gameId, false)) {
            mListHeaderView.ivCostGap.visibility = View.GONE
            mListHeaderView.tvCost.visibility = View.GONE
        } else {
            mListHeaderView.ivCostGap.visibility = View.VISIBLE
            mListHeaderView.tvCost.visibility = View.VISIBLE
        }
    }

    private fun initialize() {
        mListHeaderView = ListHeaderView()
        setHeaderClickHandler(mListHeaderView.tvHP, CardInfo.COLUMN_MAXHP)
        setHeaderClickHandler(mListHeaderView.tvAttack, CardInfo.COLUMN_MAXATTACK)
        setHeaderClickHandler(mListHeaderView.tvDefense, CardInfo.COLUMN_MAXDEFENSE)
        setHeaderClickHandler(mListHeaderView.tvExtra1, CardInfo.COLUMN_EXTRA_VALUE1)
        setHeaderClickHandler(mListHeaderView.tvExtra2, CardInfo.COLUMN_EXTRA_VALUE2)
        setHeaderClickHandler(mListHeaderView.tvName, CardInfo.COLUMN_NAME)
        setHeaderClickHandler(mListHeaderView.tvAttr, CardInfo.COLUMN_ATTR)
        setHeaderClickHandler(mListHeaderView.tvCost, CardInfo.COLUMN_COST)
        setHeaderClickHandler(mListHeaderView.tvImg, CardInfo.COLUMN_NID)



        mTextViewMap = object : HashMap<String, TextView>() {
            init {
                put(CardInfo.COLUMN_NID, mListHeaderView.tvImg)
                put(CardInfo.COLUMN_MAXHP, mListHeaderView.tvHP)
                put(CardInfo.COLUMN_MAXATTACK, mListHeaderView.tvAttack)
                put(CardInfo.COLUMN_MAXDEFENSE, mListHeaderView.tvDefense)
                put(CardInfo.COLUMN_EXTRA_VALUE1, mListHeaderView.tvExtra1)
                put(CardInfo.COLUMN_EXTRA_VALUE2, mListHeaderView.tvExtra2)
                put(CardInfo.COLUMN_NAME, mListHeaderView.tvName)
                put(CardInfo.COLUMN_ATTR, mListHeaderView.tvAttr)
                put(CardInfo.COLUMN_COST, mListHeaderView.tvCost)
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
            value.setBackgroundColor(mListHeaderView.colorWhite2)
        }
        if (mTextViewMap[orderby] != null)
            mTextViewMap[orderby]?.setBackgroundColor(mListHeaderView.colorWhite)
    }

    internal inner class ListHeaderView {

        @BindView(R.id.tvHeaderImg)
        lateinit var tvImg: TextView

        @BindView(R.id.tvHeaderName)
        lateinit var tvName: TextView

        @BindView(R.id.tvHeaderAttr)
        lateinit var tvAttr: TextView

        @BindView(R.id.tvHeaderCost)
        lateinit var tvCost: TextView

        @BindView(R.id.tvHeaderHP)
        lateinit var tvHP: TextView

        @BindView(R.id.tvHeaderAttack)
        lateinit var tvAttack: TextView

        @BindView(R.id.tvHeaderDefense)
        lateinit var tvDefense: TextView

        @BindView(R.id.tvHeaderExtra1)
        lateinit var tvExtra1: TextView

        @BindView(R.id.tvHeaderExtra2)
        lateinit var tvExtra2: TextView

        @BindView(R.id.ivExtra1Gap)
        lateinit var ivExtraGap1: ImageView

        @BindView(R.id.ivExtra2Gap)
        lateinit var ivExtraGap2: ImageView

        @BindView(R.id.ivCostGap)
        lateinit var ivCostGap: ImageView

        @JvmField
        @BindColor(R.color.color_white2)
        var colorWhite2: Int = 0

        @JvmField
        @BindColor(R.color.color_white)
        var colorWhite: Int = 0

        init {
            val view = mContext.findViewById<View>(R.id.ll_header)
            ButterKnife.bind(this, view)
        }
    }

    interface HeaderHandle {
        fun onHeaderClick(c: String)
    }
}
