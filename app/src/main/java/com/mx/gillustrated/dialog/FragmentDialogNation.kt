package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
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
import com.mx.gillustrated.adapter.CultivationPersonListAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.vo.cultivation.Nation
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogNation : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogNation {
            return FragmentDialogNation()
        }
        class TimeHandler constructor(val context: FragmentDialogNation): Handler(){

            private val reference: WeakReference<FragmentDialogNation> = WeakReference(context)

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

    @OnItemClick(R.id.lv_person)
    fun onItemClick(position:Int){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPerson.newInstance()
        newFragment.isCancelable = false

        val bundle = Bundle()
        bundle.putString("id", mPersonList[position].id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_person_info")
    }

    lateinit var mNation: Nation
    private var mPersonList = listOf<Person>()
    lateinit var mContext:CultivationActivity
    lateinit var mDialogView:DialogView

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_nation, container, false)
        ButterKnife.bind(this, v)
        mDialogView = DialogView(v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init(){
        val nationId = this.arguments!!.getString("id", "")
        mContext = activity as CultivationActivity
        mNation = CultivationHelper.mConfig.nation.find { it.id == nationId }!!.copy()
        mDialogView.name.text = CultivationHelper.showing(mNation.name)
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
        mPersonList = mContext.mPersons.map { it.value }.filter { it.nationId == mNation.id }.
                sortedWith(compareByDescending<Person>{ it.lifeTurn }.thenByDescending { it.jingJieId })
        mDialogView.persons.adapter = CultivationPersonListAdapter(this.context!!, mPersonList.toMutableList())
        mDialogView.total.text = mPersonList.size.toString()
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

        init {
            ButterKnife.bind(this, view)
        }
    }
}