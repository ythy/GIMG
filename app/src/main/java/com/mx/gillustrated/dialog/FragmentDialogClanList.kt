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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnItemClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationClanListAdapter
import com.mx.gillustrated.vo.cultivation.Clan
import java.lang.ref.WeakReference

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogClanList  : DialogFragment() {

    companion object{

        fun newInstance(): FragmentDialogClanList {
            return FragmentDialogClanList()
        }

        class TimeHandler constructor(val context: FragmentDialogClanList): Handler(){

            private val reference: WeakReference<FragmentDialogClanList> = WeakReference(context)

            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg?.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }

    }

    @BindView(R.id.lv_clan)
    lateinit var mListView: ListView

    @BindView(R.id.tv_total)
    lateinit var mTotalText: TextView


    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
    }

    @OnItemClick(R.id.lv_clan)
    fun onItemClick(position:Int){
        if(mClanListData[position].clanPersonList.map { it.value }.count() == 0){
            Toast.makeText(mContext, "size 0", Toast.LENGTH_SHORT).show()
            return
        }
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogClan.newInstance()
        newFragment.isCancelable = false

        val bundle = Bundle()
        bundle.putString("id", mClanListData[position].id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_clan_info")
    }

    lateinit var mContext: CultivationActivity
    var mClanListData: MutableList<Clan> = mutableListOf()
    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_clan_list, container, false)
        mContext = activity as CultivationActivity
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListView.adapter = CultivationClanListAdapter(this.context!!, mClanListData)
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

    fun updateView(){
        mClanListData.clear()
        mContext.mClans.forEach {
            it.value.totalXiuwei = it.value.clanPersonList.map { it.value }.sumByDouble { s->s.maxXiuWei.toDouble() }.toLong()
        }
        mClanListData.addAll(mContext.mClans.map { it.value })
        mClanListData.sortByDescending { it.totalXiuwei }
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
        mTotalText.text = mClanListData.size.toString()
    }
}