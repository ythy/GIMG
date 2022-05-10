package com.mx.gillustrated.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import butterknife.*
import com.google.gson.Gson
import com.j256.ormlite.stmt.query.In
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.CultivationHistoryAdapter
import com.mx.gillustrated.dialog.FragmentDialogPerson
import com.mx.gillustrated.dialog.FragmentDialogPersonList
import com.mx.gillustrated.util.JsonFileReader
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.*
import java.lang.ref.WeakReference
import java.util.*


class CultivationActivity : BaseActivity() {

    lateinit var mConfig:Config
    var mCurrentXun:Int = 0//当前时间
    var mPersons:MutableList<Person> = mutableListOf()
    private var mHistoryData = mutableListOf<HistoryInfo>()
    private val mTimeHandler:TimeHandler = TimeHandler(this)

    @BindView(R.id.lv_history)
    lateinit var mHistory:ListView

    @BindView(R.id.tv_date)
    lateinit var mDate:TextView


    @OnClick(R.id.btn_add)
    fun onAddClickHandler(){
        val gender = when (Random().nextInt(2)) {
            0 -> NameUtil.Gender.Male
            else -> NameUtil.Gender.Female
        }
        val name = NameUtil.getChineseName(null, gender)
        val person = getPersonInfo(name, gender)
        mPersons.add(person)
        writeHistory("${getPersonBasicString(person)} 加入", person)
    }

    @OnClick(R.id.btn_list)
    fun onListClickHandler(){
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog_person_list")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance()
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }

    @OnItemClick(R.id.lv_history)
    fun onListItemClick(position:Int){
        val row = mHistoryData[position]
        if(row.type == 1){
            val person = row.person!!
            if(mPersons.find { it.id == person.id } == null){
                return
            }
            val ft = supportFragmentManager.beginTransaction()
            val prev = supportFragmentManager.findFragmentByTag("dialog_person_info")
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            // Create and show the dialog.
            val newFragment = FragmentDialogPerson.newInstance()
            newFragment.isCancelable = false
            val bundle = Bundle()
            bundle.putString("id", person.id)
            newFragment.arguments = bundle
            newFragment.show(ft, "dialog_person_info")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cultivation)
        ButterKnife.bind(this)
        initLayout()
        loadConfig()
        registerTimeLooper()
        writeHistory("进入世界...", null, 0)
        var temp = 100
        while(temp-- > 0){
            onAddClickHandler()
        }
    }

    private fun initLayout(){
        mHistory.adapter = CultivationHistoryAdapter(this, mHistoryData)
    }

    private fun loadConfig(){
        mConfig = Gson().fromJson(JsonFileReader.getJsonFromAssets(this,"definition.json"), Config::class.java)
    }

    //1000毫秒一旬 1旬一月
    private fun registerTimeLooper(){
        var totalXun = 0
        Thread(Runnable {
            while (true){
                Thread.sleep(1000)
                totalXun++
                val message = Message.obtain()
                message.what = 1
                message.arg1 = totalXun
                mTimeHandler.sendMessage(message)
            }
        }).start()
    }

    @SuppressLint("SetTextI18n")
    fun writeHistory(content:String, person: Person?, type:Int = 1){
        mHistoryData.add(0, HistoryInfo(content, person, type))
        (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
        mHistory.invalidateViews()
    }

    private fun getPersonInfo(name:String, gender: NameUtil.Gender): Person {
        val lingGenList = mConfig.lingGenType
        lingGenList.sortedBy { it.randomBasic }
        val sum = lingGenList.sumBy { it.randomBasic }
        val random = Random().nextInt(sum)
        var current = 0 //当前分布
        var lingGen:LingGen? = null
        for ( i in 0 until lingGenList.size){
            current += lingGenList[i].randomBasic
            if(random < current){
                lingGen = lingGenList[i]
                break
            }
        }
        var lingGenname = ""
        var lingGenId = ""
        if(lingGen!!.id == "1000006"){
            val tianIndex = Random().nextInt(mConfig.lingGenTian.size)
            lingGenId = mConfig.lingGenTian[tianIndex].id
            lingGenname = mConfig.lingGenTian[tianIndex].name
        }else{
            var type = mutableListOf("金", "水", "木", "火", "土")
            var typeNum = 5
            var total = lingGen.id.substring(6).toInt()
            while (total > 0){
                val index = Random().nextInt(typeNum)
                val selectType = type[index]
                lingGenname += selectType
                type = type.filter { it != selectType} as MutableList<String>
                total--
                typeNum--
            }
        }

        val result = Person()
        result.id =  UUID.randomUUID().toString()
        result.name = name
        result.gender = gender
        result.lingGenType = lingGen
        result.lingGenName = lingGenname
        result.lingGenId = lingGenId
        result.birthDay = mCurrentXun
        result.jingJieId = mConfig.jingJieType[0].id
        result.jinJieName = mConfig.jingJieType[0].name
        result.jingJieSuccess = mConfig.jingJieType[0].success
        result.jinJieMax = mConfig.jingJieType[0].max
        return result
    }

    private fun randomEvent(){
        mConfig.events.forEach {
            val random = Random().nextInt(it.weight)
            if(random == 0 && mPersons.isNotEmpty()){
                val personRandom = Random().nextInt(mPersons.size)
                val person = mPersons[personRandom]
                val personEvent = PersonEvent()
                personEvent.nid = UUID.randomUUID().toString()
                personEvent.happenTime = mCurrentXun
                personEvent.content = "${mCurrentXun/12}年 " + it.name.replace("P", getPersonBasicString(person)).replace("B", it.bonus.toString())
                personEvent.detail = it
                person.events.add(personEvent)
                if(it.type == 1){
                    person.xiuXei += it.bonus
                }else if(it.type == 2){
                    person.jingJieSuccess += it.bonus
                }
                writeHistory( personEvent.content, person)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun calculateXiuwei(){
        randomEvent()
        val dead = mutableListOf<String>()
        for (i in 0 until mPersons.size){
            val it = mPersons[i]
            if((mCurrentXun - it.birthDay)/12 > it.lifetime){
                writeHistory("${getPersonBasicString(it)} 卒", it)
                dead.add(it.id)
                continue
            }
            it.age = (mCurrentXun - it.birthDay) / 12
            it.jinJieName = getJingJie(it.jingJieId).name
            it.lingGenName =  if(it.lingGenId == "") it.lingGenName else getTianName(it.lingGenId)

            var currentXiuwei = it.xiuXei
            val currentJinJie = getJingJie(it.jingJieId)
            currentXiuwei += it.lingGenType.qiBasic
            if(currentXiuwei < currentJinJie.max){
                it.xiuXei = currentXiuwei
            }else{
                val next = getNextJingJie(it.jingJieId)
                if(next != null){
                    var currentSuccess = it.jingJieSuccess
                    var bonus = 0
                    if(currentJinJie.bonus > 0 && it.lingGenType.jinBonus.isNotEmpty()){
                        bonus = it.lingGenType.jinBonus[currentJinJie.bonus - 1]
                    }
                    val random = Random().nextInt(100)
                    if(random <= currentSuccess + bonus){//成功
                        writeHistory("${getPersonBasicString(it)} 突破至 ${next.name}，成功率 $random/${currentSuccess + bonus}", it)
                        it.jingJieId = next.id
                        it.jingJieSuccess = next.success
                        it.jinJieMax = next.max
                        it.xiuXei = 0
                        it.lifetime += next.lifetime
                    }else{
                        writeHistory("${getPersonBasicString(it)} 突破 ${next.name} 失败 $random/${currentSuccess + bonus}，突破率提升至${currentSuccess + currentJinJie.fault}%", it)
                        currentSuccess += currentJinJie.fault
                        it.jingJieSuccess = currentSuccess
                        it.xiuXei = 0
                    }
                }
            }
        }
        mPersons.removeIf { dead.contains(it.id)  }
    }

    fun getPersonBasicString(person:Person):String{
        return "${person.name} (${person.age}/${person.lifetime}岁:${person.jinJieName}) ${person.lingGenName} "
    }

    fun getTianName(id:String):String{
        return mConfig.lingGenTian.find { it.id == id }!!.name
    }

    fun getJingJie(id:String):JingJie{
        return mConfig.jingJieType.find { it.id == id }!!
    }

    fun getNextJingJie(id:String):JingJie?{
        val nextIndex = mConfig.jingJieType.indexOf(getJingJie(id)) + 1
        return if(nextIndex < mConfig.jingJieType.size)
            mConfig.jingJieType[nextIndex]
        else
            null
    }


    companion object {

        class TimeHandler constructor(val context: CultivationActivity):Handler(){

            private val reference:WeakReference<CultivationActivity> = WeakReference(context)

            @TargetApi(Build.VERSION_CODES.N)
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                val activity = reference.get()!!
                if(msg?.what == 1){
                    val xun = msg.arg1
                    activity.mCurrentXun = xun
                    activity.mDate.text = "$xun 旬 - ${(xun/ 12)}纪年"
                    activity.calculateXiuwei()
                }
            }
        }
    }

    // type 1 人物信息
    data class HistoryInfo(var content:String, var person:Person?, var type:Int = 0)
}