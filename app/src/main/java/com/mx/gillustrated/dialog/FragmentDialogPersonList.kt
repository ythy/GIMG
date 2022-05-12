package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
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
import com.mx.gillustrated.adapter.CultivationPersonListAdapter
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogPersonList  : DialogFragment() {

    companion object{

        fun newInstance(): FragmentDialogPersonList {
            return FragmentDialogPersonList()
        }

        class TimeHandler constructor(val context: FragmentDialogPersonList): Handler(){

            private val reference: WeakReference<FragmentDialogPersonList> = WeakReference(context)

            @TargetApi(Build.VERSION_CODES.N)
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg?.what == 1 && dialog != null ){
                    dialog.updateOnlineList()
                }
            }
        }
    }

    @BindView(R.id.lv_person)
    lateinit var mListView: ListView

    @BindView(R.id.tv_total)
    lateinit var mTotalText: TextView

    @BindView(R.id.btn_switch)
    lateinit var mSwitchBtn: Button

    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
    }


    @OnClick(R.id.btn_switch)
    fun onSwitchClickHandler(){
        val tag = mSwitchBtn.tag
        if(tag == "ON"){
            mThreadRunnable = false
            mSwitchBtn.text = "offline"
            mSwitchBtn.tag = "OFF"
            setOfflineList()
        }else{
            setOnlineList()
            mThreadRunnable = true
            mSwitchBtn.text = "online"
            mSwitchBtn.tag = "ON"
        }
    }



    @OnItemClick(R.id.lv_person)
    fun onItemClick(position:Int){
        val ft = mContext.supportFragmentManager.beginTransaction()
        val prev = mContext.supportFragmentManager.findFragmentByTag("dialog_person_info")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        // Create and show the dialog.
        val newFragment = FragmentDialogPerson.newInstance()
        newFragment.isCancelable = false
        val person = mPersonData[position]
        val bundle = Bundle()
        bundle.putString("id", person.id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_person_info")
    }

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    lateinit var mContext:CultivationActivity
    private var mThreadRunnable:Boolean = true
    private var mPersonData = mutableListOf<Person>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_persion_list, container, false)
        mContext = activity as CultivationActivity
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        setOnlineList()
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

    private fun updateOnlineList(){
        val dead = mutableListOf<String>()
        mPersonData.forEach {
            if(it.isDead ){
                dead.add(it.id)
            }
        }
        mPersonData.removeIf { dead.contains(it.id) }
        mPersonData.sortByDescending { it.jingJieId.toInt() * 1000000 + it.xiuXei }
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
        mTotalText.text = mPersonData.size.toString()
    }

    private fun setOfflineList(){
        mPersonData = mutableListOf()
        val dead = mContext.mDeadPersons
        dead.sortByDescending { it.birthDay.last().second }
        mPersonData.addAll(dead)
        mListView.adapter =  CultivationPersonListAdapter(this.context!!, mPersonData)
        mTotalText.text = mPersonData.size.toString()
    }

    private fun setOnlineList(){
        mPersonData = mutableListOf()
        mPersonData.addAll(mContext.mPersons)
        mPersonData.sortByDescending {  it.jingJieId.toInt() * 1000000 + it.xiuXei }
        mListView.adapter = CultivationPersonListAdapter(this.context!!, mPersonData)
        mTotalText.text = mPersonData.size.toString()
    }

}