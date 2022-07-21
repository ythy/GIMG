package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

@SuppressLint("SetTextI18n")
class FragmentDialogPersonList  : DialogFragment() {

    companion object{

        fun newInstance(): FragmentDialogPersonList {
            return FragmentDialogPersonList()
        }

        class TimeHandler constructor(val context: FragmentDialogPersonList): Handler(){

            private val reference: WeakReference<FragmentDialogPersonList> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg.what == 1 && dialog != null ){
                    dialog.updateList()
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

    @BindView(R.id.btn_be)
    lateinit var mBe: Button


    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
    }

    @OnClick(R.id.btn_be)
    fun onBeClickHandler(){
       mContext.bePerson()
    }

    @OnClick(R.id.btn_switch)
    fun onSwitchClickHandler(){
        val tag = mSwitchBtn.tag
        if(tag == "ON"){
            mSwitchBtn.text = "offline"
            mSwitchBtn.tag = "OFF"
            mBe.visibility = View.VISIBLE
            setOfflineList()
        }else{
            mSwitchBtn.text = "online"
            mSwitchBtn.tag = "ON"
            mBe.visibility = View.GONE
            setOnlineList()
        }
    }



    @OnItemClick(R.id.lv_person)
    fun onItemClick(position:Int){
        val ft = mContext.supportFragmentManager.beginTransaction()
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
        mListView.adapter =  CultivationPersonListAdapter(this.requireContext(), mPersonData)
        updateList()
        registerTimeLooper()
    }

    private fun registerTimeLooper(){
        Thread {
            while (true){
                Thread.sleep(5000)
                if(mThreadRunnable){
                    val message = Message.obtain()
                    message.what = 1
                    mTimeHandler.sendMessage(message)
                }
            }
        }.start()
    }

    private fun updateList(){
        if(mSwitchBtn.tag == "ON"){
            setOnlineList()
        }else{
            setOfflineList()
        }
    }

    private fun setOfflineList(){
        mPersonData.clear()
        mPersonData.addAll(mContext.mDeadPersons.map { it.value })
        mPersonData.sortByDescending { it.lifeTurn * 1000000 + it.jinJieMax}
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
        mTotalText.text = mPersonData.size.toString()
    }

    private fun setOnlineList(){
        mPersonData.clear()
        mPersonData.addAll(mContext.mPersons.map { it.value })

        mPersonData.sortWith(compareByDescending<Person> {it.lifeTurn}
                .thenByDescending { it.jingJieId }
                .thenByDescending { it.xiuXei } )
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
        mTotalText.text = mPersonData.size.toString()
    }

}