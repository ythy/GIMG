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
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.PersonEvent
import java.lang.ref.WeakReference

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
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

    @OnClick(R.id.btn_revive)
    fun onReviveHandler(){
        val success = mContext.revivePerson(mId)
        if(success){
            mThreadRunnable = true
            Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
        }

    }

    @BindView(R.id.btn_revive)
    lateinit var mBtnRevive:Button

    @BindView(R.id.sch_fav)
    lateinit var mSwitchFav:Switch

    @OnCheckedChanged(R.id.sch_fav)
    fun onFavSwitch(checked:Boolean){
        mPerson.isFav = checked
    }

    lateinit var mPerson:Person
    lateinit var mId:String
    lateinit var mContext:CultivationActivity
    lateinit var mDialogView:DialogView

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var mEventDataString = mutableListOf<String>()
    private var mEventData = mutableListOf<PersonEvent>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
        mDialogView.events.adapter = ArrayAdapter(this.context!!,
                android.R.layout.simple_list_item_1, android.R.id.text1, mEventDataString)
        setTianfu()
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

    private fun getPerson():Person{
        val person = mContext.mPersons.find { it.id == mId }
        return person ?: mContext.mDeadPersons.find { it.id == mId }!!
    }

    private fun setTianfu(){
        val person = getPerson()
        val tianFus = person.tianfus
        if(tianFus.isNotEmpty()){
            tianFus.forEach {
                val data = it
                val textView = TextView(this.context)
                textView.text = data.name
                textView.setTextColor(Color.parseColor(TianFuColors[data.rarity]))
                textView.setOnClickListener {
                    var text = ""
                    when {
                        data.type == 1 -> text = "基础修为"
                        data.type == 2 -> text = "修为加速"
                        data.type == 3 -> text = "Life"
                        data.type == 4 -> text = "突破"
                    }
                    Toast.makeText(this.context, "${text}增加${data.bonus}", Toast.LENGTH_SHORT).show()
                }
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.marginEnd = 20
                textView.layoutParams = layoutParams
                mDialogView.tianfu.addView(textView)
            }

        }
    }

    private fun updateView(){
        mPerson = getPerson()
        if(mPerson.isDead){
            mThreadRunnable = false
            mBtnRevive.visibility = View.VISIBLE
        }else{
            mBtnRevive.visibility = View.GONE
        }
        mSwitchFav.isChecked = mPerson.isFav
        mDialogView.name.text = "${mPerson.name}(${mPerson.gender.props})"
        mDialogView.alliance.text = mPerson.allianceName
        mDialogView.age.text = "${mPerson.age}/${mPerson.lifetime}"
        mDialogView.neigong.text = mPerson.maxXiuWei.toString()
        mDialogView.jingjie.text = mPerson.jinJieName
        mDialogView.xiuwei.text = "${mPerson.xiuXei}/${mPerson.jinJieMax}"
        mDialogView.xiuweiAdd.text = ((mPerson.lingGenType.qiBasic + mPerson.extraXiuwei + mPerson.allianceXiuwei) * mPerson.extraXuiweiMulti).toInt().toString() + "(${mPerson.allianceXiuwei})"
        val currentJinJie = mContext.getJingJie(mPerson.jingJieId)
        var bonus = 0
        if(currentJinJie.bonus > 0 && mPerson.lingGenType.jinBonus.isNotEmpty()){
            bonus = mPerson.lingGenType.jinBonus[currentJinJie.bonus - 1]
        }
        mDialogView.success.text = "${mPerson.jingJieSuccess}+${mPerson.extraTupo}+[$bonus]"
        mDialogView.lingGen.text = mPerson.lingGenName
        mDialogView.lingGen.setTextColor(Color.parseColor(mPerson.lingGenType.color))
        val eventChanged = mEventData.size != mPerson.events.size
        mPerson.events.forEach {
            if(mEventData.find { e-> e.nid == it.nid} == null){
                mEventData.add(it)
                mEventDataString.add(0, it.content)
            }
        }
        if(eventChanged){
            (mDialogView.events.adapter as BaseAdapter).notifyDataSetChanged()
            mDialogView.events.invalidateViews()
        }

    }

    class DialogView constructor(view: View){

        @BindView(R.id.tv_name)
        lateinit var name:TextView

        @BindView(R.id.tv_alliance)
        lateinit var alliance:TextView

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

        @BindView(R.id.ll_tianfu)
        lateinit var tianfu:LinearLayout

        @BindView(R.id.tv_xiuwei_add)
        lateinit var xiuweiAdd:TextView

        @BindView(R.id.tv_neigong)
        lateinit var neigong:TextView


        init {
            ButterKnife.bind(this, view)
        }
    }
}