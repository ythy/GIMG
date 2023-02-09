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
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.TextViewBox
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Alliance
import com.mx.gillustrated.vo.cultivation.Person
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

    @OnClick(R.id.tv_name)
    fun onNameClickHandler(){
        if (mAlliance.type != 5)
            return
        val ft = mContext.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogJinlong.newInstance(mAlliance.personList.map { it.value }.shuffled()[0].id)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_jinglong")
    }

    @OnClick(R.id.btn_insert)
    fun onInsertPersonHandler(){
        val name = mDialogView.insertPerson.text.toString()
        val person = mContext.mPersons.map { it.value }.find { it.name == name || PinyinUtil.convert(it.name) == name }
        if (person != null){
            CultivationHelper.changedToFixedAlliance(person, mContext.mAlliance, mAlliance)
            mDialogView.insertPerson.setText("")
            Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
        }
    }

    @OnClick(R.id.tv_winner)
    fun onWinnerClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogRank.newInstance(3, mAlliance.id)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_rank_info")
    }



    @OnClick(R.id.tv_xiuwei)
    fun onXiuweiClickHandler(){
        val prop = mAlliance.property.joinToString()
        Toast.makeText(mContext, prop, Toast.LENGTH_SHORT).show()
    }

    @OnItemClick(R.id.lv_person)
    fun onItemClick(position:Int){
        showPersonInfo(mPersonList[position].id)
    }

    lateinit var mAlliance: Alliance
    lateinit var mId:String
    lateinit var mContext:CultivationActivity
    lateinit var mDialogView:DialogView

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var mPersonList = mutableListOf<Person>()
    private val nation = CultivationHelper.mConfig.nation

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
        mAlliance = mContext.mAlliance[mId]!!
        mDialogView.name.text = CultivationHelper.showing("${nation.find { it.id == mAlliance.nation }?.name}-${mAlliance.name}")
        mDialogView.lifetime.text = "life: ${mAlliance.lifetime}"
        mDialogView.xiuwei.text = "xiuwei: ${mAlliance.xiuwei}(${mAlliance.xiuweiMulti})  ↑${mAlliance.success}"
        mDialogView.persons.adapter = CultivationPersonListAdapter(this.context!!, mPersonList, true, true)
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
        if(mAlliance.zhuPerson == null){
            mDialogView.zhu.text = ""
        }else{
            val zhuName = CultivationHelper.showing(mAlliance.zhuPerson!!.name)
            mDialogView.zhu.text = zhuName
        }
        mDialogView.winner.text = "${mAlliance.battleWinner}-${mAlliance.xiuweiBattle}↑"
        mDialogView.speeds.removeAllViews()
//        val list = mAlliance.speedG1PersonList.mapNotNull { mContext.getOnlinePersonDetail(it.value.id) }
//        if(list.isNotEmpty()){
//            mDialogView.measures.measure(0,0)
//            mDialogView.speeds.setConfig(TextViewBox.TextViewBoxConfig(mDialogView.measures.measuredWidth - 20))
//
//            mDialogView.speeds.setCallback(object : TextViewBox.Callback {
//                override fun onClick(index: Int) {
//                    showPersonInfo(list[index].id)
//                }
//            })
//            mDialogView.speeds.setDataProvider(list.map { CultivationHelper.showing(it.name) }, null)
//        }

        mPersonList.clear()
        mPersonList.addAll(mAlliance.personList.map { it.value }.toMutableList())
        mPersonList.sortWith(compareByDescending<Person>{ it.lifeTurn }.thenByDescending { it.jingJieId })
        (mDialogView.persons.adapter as BaseAdapter).notifyDataSetChanged()
        mDialogView.persons.invalidateViews()

        mDialogView.total.text = "${mPersonList.size}-${mPersonList.count { it.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
    }

    private fun showPersonInfo(id:String?){
        if(id == null)
            return
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPerson.newInstance()
        newFragment.isCancelable = false

        val bundle = Bundle()
        bundle.putString("id", id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_person_info")
    }

    class DialogView constructor(view: View){

        @BindView(R.id.tv_name)
        lateinit var name:TextView

        @BindView(R.id.tv_lifetime)
        lateinit var lifetime:TextView

        @BindView(R.id.tv_xiuwei)
        lateinit var xiuwei:TextView

        @BindView(R.id.tv_total)
        lateinit var total:TextView

        @BindView(R.id.tv_zhu)
        lateinit var zhu:TextView

        @BindView(R.id.tv_winner)
        lateinit var winner:TextView

        @BindView(R.id.lv_person)
        lateinit var persons:ListView

        @BindView(R.id.ll_speed)
        lateinit var speeds:TextViewBox

        @BindView(R.id.ll_parent_measure)
        lateinit var measures:LinearLayout

        @BindView(R.id.et_insert)
        lateinit var insertPerson:EditText


        init {
            ButterKnife.bind(this, view)
        }
    }
}