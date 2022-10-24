package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.annotation.TargetApi
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
import com.mx.gillustrated.adapter.CultivationPersonListAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference
import java.util.*

//type 0 all; 1 fav; 2 carrer rarity>=8 ; 3 lifeturn == 0 & lingGen type > 1 & tianFu rarity sum > 10;
//type 4  rank by xiuwei
@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class  FragmentDialogPersonList constructor(private val mType:Int)  : DialogFragment() {

    companion object{

        fun newInstance(type:Int = 0): FragmentDialogPersonList {
            return FragmentDialogPersonList(type)
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

    @BindView(R.id.tv_total)
    lateinit var mTotalText: TextView

    @BindView(R.id.btn_switch)
    lateinit var mSwitchBtn: Button

    @BindView(R.id.btn_be)
    lateinit var mBe: Button

    @BindView(R.id.btn_clear)
    lateinit var mClear: Button

    @BindView(R.id.btn_sort_t)
    lateinit var mBtnLifeturn: Button

    @BindView(R.id.btn_sort_x)
    lateinit var mBtnXiuwei: Button

    @BindView(R.id.btn_sort_b)
    lateinit var mBtnBattle: Button

    @OnClick(R.id.btn_sort_t, R.id.btn_sort_x, R.id.btn_sort_b)
    fun onSortHandler(btn:Button){
        mBtnLifeturn.setTextColor(mContext.getColor(R.color.color_blue))
        mBtnXiuwei.setTextColor(mContext.getColor(R.color.color_blue))
        mBtnBattle.setTextColor(mContext.getColor(R.color.color_blue))
        btn.setTextColor(Color.parseColor("white"))
        mSort = btn.tag.toString()
        setOnlineList()
    }


    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
    }

    @OnClick(R.id.btn_be)
    fun onBeClickHandler(){
       mContext.bePerson()
    }

    @OnClick(R.id.btn_clear)
    fun onClearClickHandler(){
        mContext.mDeadPersons.clear()
    }

    @OnClick(R.id.btn_switch)
    fun onSwitchClickHandler(){
        if (mType > 0)
            return
        val tag = mSwitchBtn.tag
        if(tag == "ON"){
            mSwitchBtn.text = "offline"
            mSwitchBtn.tag = "OFF"
            mBe.visibility = View.VISIBLE
            mClear.visibility = View.VISIBLE
            setOfflineList()
        }else{
            mSwitchBtn.text = "online"
            mSwitchBtn.tag = "ON"
            mBe.visibility = View.GONE
            mClear.visibility = View.GONE
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

    @BindView(R.id.et_name)
    lateinit var etName:EditText

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    lateinit var mContext:CultivationActivity
    private var mThreadRunnable:Boolean = true
    private var mPersonData =  Collections.synchronizedList(mutableListOf<Person>())
    private var mSort = "T"

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
        mListView.adapter =  CultivationPersonListAdapter(this.context!!, mPersonData)
        updateList()
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
        val persons = when (mType) {
            1 -> mContext.mPersons.map { it.value }.filter { it.isFav || it.neverDead }
            2 -> mContext.mPersons.map { it.value }.filter { p->
                val list = p.careerList.map { l-> CultivationHelper.mConfig.career.find{ c-> c.id == l.first} }
                        .sortedByDescending { it?.rarity }
                list.isNotEmpty() && list[0]?.rarity!! >= 8
            }
            3 -> mContext.mPersons.map { it.value }.filter { p->
                p.lifeTurn == 0 && p.ancestorLevel == 0 && ( p.lingGenType.type >= 3 || (p.lingGenType.type > 0 && p.tianfus.sumBy { s-> s.rarity } > 5))
            }
            else -> mContext.mPersons.map { it.value }
        }
        val filterString = etName.text.toString()
        if(filterString == "" )
            mPersonData.addAll(persons)
        else
            mPersonData.addAll(persons.filter {
                it.name.startsWith(filterString, true) || PinyinUtil.convert(it.name).startsWith(filterString, true)
            })


        when (mSort) {
            "X" -> mPersonData.sortWith(compareByDescending<Person> { CultivationHelper.getXiuweiGrow(it, mContext.mAlliance) }
                    .thenByDescending { it.lifeTurn })
            "T" -> mPersonData.sortWith(compareByDescending<Person> {it.lifeTurn}
                    .thenByDescending { it.jingJieId }
                    .thenByDescending { it.xiuXei } )
            "B" -> mPersonData.sortWith(compareByDescending<Person> {it.winner}
                    .thenByDescending { it.lifeTurn })
        }

        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
        val jie = mContext.mSP.getInt("cultivation_jie", CultivationSetting.SP_JIE_TURN)
        mTotalText.text = "${mPersonData.size}-${mPersonData.count { it.lifeTurn >= jie }}"
    }

}