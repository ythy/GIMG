package com.mx.gillustrated.adapter

import com.mx.gillustrated.R
import com.mx.gillustrated.vo.GameInfo
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageButton
import butterknife.BindView
import butterknife.ButterKnife

class GameListAdapter constructor(mContext: Context, private val list: List<GameInfo>) : BaseAdapter() {

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
                    R.layout.adapter_game, arg2, false)
            component = Component(convertView)
            convertView.tag = component
        } else
            component = convertView.tag as Component

        try {
            component.tvName.setText(list[arg0].name)
            component.btnSave.setOnClickListener {
                val name = component.tvName.text.toString()
                val id = list[arg0].id
                val despairInfo = GameInfo()
                despairInfo.id = id
                despairInfo.name = name
                despairInfo.detail = list[arg0].detail
                mListener!!.onSaveBtnClickListener(despairInfo)
            }
            component.btnDetail.setOnClickListener {
                val name = component.tvName.text.toString()
                val id = list[arg0].id
                val despairInfo = GameInfo()
                despairInfo.id = id
                despairInfo.name = name
                mListener!!.onDetailBtnClickListener(despairInfo)
            }

            component.tvName.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    //   index = position;
                }
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return convertView!!
    }

    interface DespairTouchListener {
        fun onSaveBtnClickListener(info: GameInfo)
        fun onDetailBtnClickListener(info: GameInfo)
    }

    internal class Component(view:View) {

        @BindView(R.id.etGameName)
        lateinit var tvName: EditText

        @BindView(R.id.btnDespairModify)
        lateinit var btnSave: ImageButton

        @BindView(R.id.btnGameDetail)
        lateinit var btnDetail: ImageButton

        init {
            ButterKnife.bind(this, view)
        }
    }
}
