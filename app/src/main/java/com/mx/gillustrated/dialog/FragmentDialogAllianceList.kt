package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnItemClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.AllianceListAdapter
import com.mx.gillustrated.adapter.CultivationPersonListAdapter
import com.mx.gillustrated.vo.cultivation.Alliance
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogAllianceList  : DialogFragment() {

    companion object{

        fun newInstance(): FragmentDialogAllianceList {
            return FragmentDialogAllianceList()
        }

        class TimeHandler constructor(val context: FragmentDialogAllianceList): Handler(){

            private val reference: WeakReference<FragmentDialogAllianceList> = WeakReference(context)

            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg?.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }

    }

    @BindView(R.id.lv_alliance)
    lateinit var mListView: ListView

    @BindView(R.id.tv_total)
    lateinit var mTotalText: TextView


    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
    }

    @OnItemClick(R.id.lv_alliance)
    fun onItemClick(position:Int){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogAlliance.newInstance()
        newFragment.isCancelable = false

        val bundle = Bundle()
        bundle.putString("id", mAllianceListData[position].id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_alliance_info")
    }

    lateinit var mContext: CultivationActivity
    var mAllianceListData: MutableList<Alliance> = mutableListOf()
    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_alliance_list, container, false)
        mContext = activity as CultivationActivity
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListView.adapter = AllianceListAdapter(this.context!!, mAllianceListData)
        updateView()
        registerTimeLooper()
    }

    private fun registerTimeLooper(){
        Thread(Runnable {
            while (true){
                Thread.sleep(2000)
                if(mThreadRunnable){
                    val message = Message.obtain()
                    message.what = 1
                    mTimeHandler.sendMessage(message)
                }
            }
        }).start()
    }

    private fun updateView(){
        mAllianceListData.clear()
        mAllianceListData.addAll(mContext.mAlliance.map { it.value })
        mAllianceListData.forEach {
            it.isPinyinMode = mContext.pinyinMode
            it.totalXiuwei = it.personList.reduceValuesToLong(1000,
                    { p: Person -> p.maxXiuWei }, 0, { left, right -> left + right })
        }
        mAllianceListData.sortByDescending { it.totalXiuwei }
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
        mTotalText.text = mAllianceListData.sumBy { it.personList.size }.toString()
    }

}