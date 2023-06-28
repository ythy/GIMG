package com.mx.gillustrated.adapter


import com.mx.gillustrated.vo.GameInfo
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mx.gillustrated.databinding.AdapterGameBinding

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
        lateinit var component: AdapterGameBinding

        if (convertView == null) {
            component = AdapterGameBinding.inflate(layoutInflater, arg2, false)
            convertView = component.root
            convertView.tag = component
        } else
            component = convertView.tag as AdapterGameBinding

        try {
            component.etGameName.setText(list[arg0].name)
            component.btnDespairModify.setOnClickListener {
                val name = component.etGameName.text.toString()
                val id = list[arg0].id
                val despairInfo = GameInfo()
                despairInfo.id = id
                despairInfo.name = name
                despairInfo.detail = list[arg0].detail
                mListener!!.onSaveBtnClickListener(despairInfo)
            }
            component.btnGameDetail.setOnClickListener {
                val name = component.etGameName.text.toString()
                val id = list[arg0].id
                val despairInfo = GameInfo()
                despairInfo.id = id
                despairInfo.name = name
                mListener!!.onDetailBtnClickListener(despairInfo)
            }

            component.etGameName.setOnTouchListener { _, event ->
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
        fun onSaveBtnClickListener(info: GameInfo)
        fun onDetailBtnClickListener(info: GameInfo)
    }

}
