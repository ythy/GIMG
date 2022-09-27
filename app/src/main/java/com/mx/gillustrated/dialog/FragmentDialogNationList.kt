package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.mx.gillustrated.adapter.CultivationNationAdapter
import com.mx.gillustrated.vo.cultivation.Nation
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogNationList  : DialogFragment() {

    companion object{

        fun newInstance(): FragmentDialogNationList {
            return FragmentDialogNationList()
        }

        class TimeHandler constructor(val context: FragmentDialogNationList): Handler(){

            private val reference: WeakReference<FragmentDialogNationList> = WeakReference(context)

            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg?.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }

    }

    @BindView(R.id.lv_nation)
    lateinit var mListView: ListView

    @BindView(R.id.tv_total)
    lateinit var mTotalText: TextView


    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
    }

    @OnItemClick(R.id.lv_nation)
    fun onItemClick(position:Int){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogNation.newInstance()
        newFragment.isCancelable = false

        val bundle = Bundle()
        bundle.putString("id", mNationList[position].id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_nation_info")
    }

    lateinit var mContext: CultivationActivity
    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var mNationList = listOf<Nation>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_nation_list, container, false)
        mContext = activity as CultivationActivity
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateView()
        registerTimeLooper()
    }

    private fun registerTimeLooper(){
        Thread(Runnable {
            while (true){
                Thread.sleep(4000)
                if(mThreadRunnable){
                    val message = Message.obtain()
                    message.what = 1
                    mTimeHandler.sendMessage(message)
                }
            }
        }).start()
    }

    private fun updateView(){
        mNationList = mContext.mNations.map { c->
            val nation = c.value.copy()
            nation.nationPersonList = ConcurrentHashMap(mContext.mPersons.filter { it.value.nationId == nation.id })
            nation.totalTurn =  nation.nationPersonList.map { it.value }.sumBy { it.lifeTurn }
            nation
        }.sortedByDescending { it.totalTurn }
        mListView.adapter = CultivationNationAdapter(this.context!!, mNationList)
        mTotalText.text = mNationList.sumBy { it.nationPersonList.size }.toString()
    }

}