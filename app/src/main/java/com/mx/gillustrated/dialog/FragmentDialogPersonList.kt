package com.mx.gillustrated.dialog

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.BaseAdapter
import android.widget.ListView
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
                    dialog.updateList()
                }
            }
        }
    }

    @BindView(R.id.lv_person)
    lateinit var mListView: ListView

    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
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
        dialog!!.window.requestFeature(Window.FEATURE_NO_TITLE)
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
        mPersonData.addAll(mContext.mPersons)
        mPersonData.sortByDescending {  it.jingJieId.toInt() * 1000000 + it.xiuXei }
        mListView.adapter = CultivationPersonListAdapter(this, mPersonData)
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateList(){
        val newDada = mContext.mPersons
        val dead = mutableListOf<String>()
        mPersonData.forEachIndexed { index, old ->
            val new = newDada.find { it.id ==  old.id}
            if(new != null){
                mPersonData[index] = new
            }else{
                dead.add(old.id)
            }
        }
        mPersonData.removeIf { dead.contains(it.id) }
        mPersonData.sortByDescending { it.jingJieId.toInt() * 1000000 + it.xiuXei }
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
    }
}