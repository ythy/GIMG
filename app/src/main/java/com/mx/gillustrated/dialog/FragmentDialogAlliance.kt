package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.*
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.activity.CultivationActivity.Companion.TianFuColors
import com.mx.gillustrated.adapter.CultivationPersonListAdapter
import com.mx.gillustrated.vo.cultivation.Alliance
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.PersonEvent
import java.lang.ref.WeakReference

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogAlliance : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogAlliance {
            return FragmentDialogAlliance()
        }
        class TimeHandler constructor(val context: FragmentDialogAlliance): Handler(){

            private val reference: WeakReference<FragmentDialogAlliance> = WeakReference(context)

            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg?.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }
    }

    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
    }

    @OnClick(R.id.btn_speed)
    fun onSpeedHandler(){

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

        val bundle = Bundle()
        bundle.putString("id", mPersonList[position].id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_person_info")
    }

    lateinit var mAlliance: Alliance
    lateinit var mId:String
    lateinit var mContext:CultivationActivity
    lateinit var mDialogView:DialogView

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var mPersonList = mutableListOf<Person>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_alliance, container, false)
        ButterKnife.bind(this, v)
        mDialogView = DialogView(v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init(){
        mId = this.arguments!!.getString("id", "")
        mContext = activity as CultivationActivity
        mAlliance = mContext.mAlliance.find { it.id == mId }!!
        mDialogView.persons.adapter = CultivationPersonListAdapter(this.context!!, mPersonList)
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
        mDialogView.name.text = mAlliance.name
        mDialogView.total.text = mAlliance.persons.size.toString()
        mDialogView.zhu.text = mAlliance.zhu?.name
        var speedString = ""
        mAlliance.speedG1List.forEach {
            speedString += it.name + " "
        }
        mDialogView.speeds.text = speedString

        mPersonList.clear()
        mPersonList.addAll(mAlliance.persons)
        mPersonList.sortByDescending { it.maxXiuWei }
        (mDialogView.persons.adapter as BaseAdapter).notifyDataSetChanged()
        mDialogView.persons.invalidateViews()

    }

    class DialogView constructor(view: View){

        @BindView(R.id.tv_name)
        lateinit var name:TextView

        @BindView(R.id.tv_total)
        lateinit var total:TextView

        @BindView(R.id.tv_zhu)
        lateinit var zhu:TextView

        @BindView(R.id.lv_person)
        lateinit var persons:ListView

        @BindView(R.id.tv_speed_persons)
        lateinit var speeds:TextView

        init {
            ButterKnife.bind(this, view)
        }
    }
}