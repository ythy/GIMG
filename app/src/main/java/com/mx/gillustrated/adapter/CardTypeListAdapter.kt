package com.mx.gillustrated.adapter

import com.mx.gillustrated.R
import com.mx.gillustrated.vo.CardTypeInfo
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageButton
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.activity.BaseActivity

class CardTypeListAdapter constructor(mContext: BaseActivity, private val list: List<CardTypeInfo>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var mListener: DespairTouchListener? = null

    fun setDespairTouchListener(listener: DespairTouchListener) {
        mListener = listener
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(arg0: Int): Any {
        return list[arg0]
    }

    override fun getItemId(arg0: Int): Long {
        return arg0.toLong()
    }

    override fun getView(arg0: Int, convertViews: View?, arg2: ViewGroup): View {
        var convertView = convertViews
        lateinit var component: Component

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.adapter_cardtype, arg2, false)
            component = Component(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as Component

        try {

            component.etName.setText(list[arg0].name)
            component.btnSave.setOnClickListener {
                val name = component.etName.text.toString()
                val id = list[arg0].id
                val gid = list[arg0].gameId
                val despairInfo = CardTypeInfo()
                despairInfo.id = id
                despairInfo.gameId = gid
                despairInfo.name = name
                mListener!!.onSaveBtnClickListener(despairInfo)
            }

            component.btnDel.setOnClickListener {
                val id = list[arg0].id
                val gid = list[arg0].gameId
                val cardTypeInfo = CardTypeInfo()
                cardTypeInfo.id = id
                cardTypeInfo.gameId = gid
                mListener!!.onDelBtnClickListener(cardTypeInfo)
            }

            component.etName.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    //   index = position;
                }
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return convertView
    }

    interface DespairTouchListener {
        fun onSaveBtnClickListener(info: CardTypeInfo)
        fun onDelBtnClickListener(info: CardTypeInfo)
    }

    internal class Component(view: View) {

        @BindView(R.id.etCardType)
        lateinit var etName: EditText

        @BindView(R.id.btnCardTypeModify)
        lateinit var btnSave: ImageButton

        @BindView(R.id.btnCardTypeDel)
        lateinit var btnDel: ImageButton

        init {
            ButterKnife.bind(this, view)
        }

    }
}
