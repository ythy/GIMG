package com.mx.gillustrated.dialog

import android.annotation.TargetApi
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.activity.MainActivity
import com.mx.gillustrated.vo.cultivation.LingGen
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference

class FragmentDialogPerson : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogPerson {
            return FragmentDialogPerson()
        }
        class TimeHandler constructor(val context: FragmentDialogPerson): Handler(){

            private val reference: WeakReference<FragmentDialogPerson> = WeakReference(context)

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

    lateinit var mPerson:Person
    lateinit var mId:String
    lateinit var mContext:CultivationActivity
    lateinit var mDialogView:DialogView

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog!!.window.requestFeature(Window.FEATURE_NO_TITLE)
        val v = inflater.inflate(R.layout.fragment_dialog_persion, container, false)
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
        updateView()
        registerTimeLooper()
    }

    private fun registerTimeLooper(){
        Thread(Runnable {
            while (true){
                Thread.sleep(1100)
                if(mThreadRunnable){
                    val message = Message.obtain()
                    message.what = 1
                    mTimeHandler.sendMessage(message)
                }
            }
        }).start()
    }

    private fun updateView(){
        mPerson = mContext.mPersons.find { it.id == mId }!!
        mDialogView.name.text = "${mPerson.name}(${mPerson.gender.props})"
        mDialogView.age.text = "${mPerson.age}/${mPerson.lifetime}"
        mDialogView.jingjie.text = mPerson.jinJieName
        mDialogView.xiuwei.text = "${mPerson.xiuXei}/${mPerson.jinJieMax}"
        mDialogView.success.text = "${mPerson.jingJieSuccess}"
        mDialogView.lingGen.text = mPerson.lingGenName
        mDialogView.lingGen.setTextColor(Color.parseColor(mPerson.lingGenType.color))
        val events = mutableListOf<String>()
        mPerson.events.forEach {
            events.add(it.content)
        }
        mDialogView.events.adapter = ArrayAdapter(this.context!!,
                android.R.layout.simple_list_item_1, android.R.id.text1, events)
    }

    class DialogView constructor(view: View){

        @BindView(R.id.tv_name)
        lateinit var name:TextView

        @BindView(R.id.tv_age)
        lateinit var age:TextView

        @BindView(R.id.tv_jingjie)
        lateinit var jingjie:TextView

        @BindView(R.id.tv_xiuwei)
        lateinit var xiuwei:TextView

        @BindView(R.id.tv_success)
        lateinit var success:TextView

        @BindView(R.id.tv_lingGen)
        lateinit var lingGen:TextView

        @BindView(R.id.lv_events)
        lateinit var events:ListView

        init {
            ButterKnife.bind(this, view)
        }
    }
}