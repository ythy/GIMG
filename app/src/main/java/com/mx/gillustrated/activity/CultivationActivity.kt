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
import android.widget.Toast
import androidx.annotation.RequiresApi
import butterknife.*
import com.google.gson.Gson
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.CultivationHistoryAdapter
import com.mx.gillustrated.dialog.*
import com.mx.gillustrated.util.CultivationBakUtil
import com.mx.gillustrated.util.JsonFileReader
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.*
import java.lang.ref.WeakReference
import java.util.*

@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.N)
class CultivationActivity : BaseActivity() {

    lateinit var mConfig:Config
    private var mThreadRunnable = true
    var mCurrentXun:Int = 0//当前时间
    var mSpeed = 200L//流失速度
    var mPersons:MutableList<Person> = mutableListOf()
    var mDeadPersons:MutableList<Person> = mutableListOf()
    var mAlliance:MutableList<Alliance> = mutableListOf()
    private var mHistoryData = mutableListOf<HistoryInfo>()
    private val mTimeHandler:TimeHandler = TimeHandler(this)

    @BindView(R.id.lv_history)
    lateinit var mHistory:ListView

    @BindView(R.id.tv_date)
    lateinit var mDate:TextView

    @BindView(R.id.tv_speed)
    lateinit var mSpeedText:TextView

    @OnClick(R.id.btn_save)
    fun onSaveClickHandler(){
        mThreadRunnable = false
        mProgressDialog.show()
        Thread(Runnable {
            Thread.sleep(500)
            val backupInfo = BakInfo()
            backupInfo.xun = mCurrentXun
            backupInfo.alliance = mAlliance
            backupInfo.persons = mPersons
            CultivationBakUtil.saveDataToFiles(Gson().toJson(backupInfo))
            val message = Message.obtain()
            message.what = 2
            mTimeHandler.sendMessage(message)
        }).start()
    }

    @OnClick(R.id.btn_reset)
    fun onResetClickHandler(){
        mThreadRunnable = false
        mProgressDialog.show()
        mHistoryData.clear()
        mHistoryTempData.clear()
        (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
        mHistory.invalidateViews()
        Thread(Runnable {
            Thread.sleep(1000)
            mCurrentXun = 0
            mPersons.clear()
            mAlliance.clear()
            mDeadPersons.clear()
            createAlliance()
            val message = Message.obtain()
            message.what = 4
            mTimeHandler.sendMessage(message)
        }).start()
    }

    @OnClick(R.id.btn_multi)
    fun onAdd100ClickHandler(){
        addMultiPerson(100)
    }

    @OnClick(R.id.btn_clear)
    fun onClearClickHandler(){
        mHistoryData.clear()
        (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
        mHistory.invalidateViews()
    }

    @OnClick(R.id.btn_add_speed)
    fun onSpeedAddClickHandler(){
        mSpeed += if(mSpeed < 100L){
            10L
        }else{
            100L
        }
        mSpeedText.text = mSpeed.toString()
    }

    @OnClick(R.id.btn_reduce_speed)
    fun onSpeedReduceClickHandler(){
        if(mSpeed == 10L)
            return
        mSpeed -= if(mSpeed <= 100L){
            10L
        }else{
            100L
        }
        mSpeedText.text = mSpeed.toString()
    }

    @OnClick(R.id.btn_add)
    fun onAddClickHandler(){
        addPersion(null, null)
    }

    @OnClick(R.id.btn_alliance)
    fun onAllianceClickHandler(){
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog_alliance_list")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        // Create and show the dialog.
        val newFragment = FragmentDialogAllianceList.newInstance()
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_alliance_list")
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
        mProgressDialog.show()
        Thread(Runnable {
            Thread.sleep(100)
            val backup = CultivationBakUtil.getDataFromFiles()
            val message = Message.obtain()
            message.what = 3
            message.obj = backup
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun init(out:String?){
        if(out != null){
            val backup = Gson().fromJson(out, BakInfo::class.java)
            mCurrentXun = backup.xun
            mPersons = backup.persons
            mAlliance = backup.alliance
        }
        createAlliance()
        if(out == null){
            startWorld()
        }
        registerTimeLooper()
    }

    private fun startWorld(){
        writeHistory("进入世界...", null, 0)
        addMultiPerson(200)
        addPersion("男主", NameUtil.Gender.Male, 50000)
        addPersion("女主", NameUtil.Gender.Female, 50000)
    }

    private fun initLayout(){
        mHistory.adapter = CultivationHistoryAdapter(this, mHistoryData)
        mSpeedText.text = mSpeed.toString()
    }

    private fun loadConfig(){
        mConfig = Gson().fromJson(JsonFileReader.getJsonFromAssets(this,"definition.json"), Config::class.java)
    }

    private fun createAlliance() {
        if(mAlliance.isEmpty()){
            mConfig.alliance.forEach {
                val alliance = Alliance()
                alliance.name = it.name + "界"
                alliance.id = it.id
                alliance.level = it.level
                alliance.lifetime = it.lifetime
                alliance.xiuwei = it.xiuwei
                alliance.maxPerson = it.maxPerson
                alliance.lingGen = it.lingGen
                alliance.speedG1 = it.speedG1
                alliance.speedG2 = it.speedG2
                mAlliance.add(alliance)
            }
        }else{
            mAlliance.forEach {
                val configAllianc = mConfig.alliance.find { f-> f.id == it.id }!!
                it.level = configAllianc.level
                it.lifetime = configAllianc.lifetime
                it.maxPerson = configAllianc.maxPerson
                it.xiuwei = configAllianc.xiuwei
                it.speedG1 = configAllianc.speedG1
                it.speedG2 = configAllianc.speedG2
            }
            mAlliance.addAll(mConfig.alliance.filter { mAlliance.none { m -> m.id == it.id } })
        }
    }

    private fun joinAlliance(person:Person){
        var options = mAlliance.filter { it.level == 1 }.toMutableList()
        if(person.lingGenId == "") {
            options = options.filter { person.lingGenName.indexOf(it.lingGen!!) >= 0 }.toMutableList()
        }
        options.addAll(mAlliance.filter { it.level > 1 &&  it.persons.size < it.maxPerson })
        val random = Random().nextInt(options.map { 100 / it.level }.sum() )
        var count = 0
        for (i in 0 until options.size){
            val alliance = options[i]
            count += 100 / alliance.level
            if(random < count){
                alliance.persons.add(person.id)
                person.allianceId = alliance.id
                person.allianceName = alliance.name
                person.lifetime = person.lifetime * ( 100 + alliance.lifetime ) / 100
                break
            }
        }
    }

    private fun exitAlliance(person:Person){
        val alliance =  mAlliance.find { it.id == person.allianceId }
        if(alliance != null){
            alliance.persons.removeIf { it == person.id }
            alliance.speedG1List.remove(person.id)
        }
        person.allianceId = ""
        person.allianceName = ""
    }

    fun getPersonDetail(id:String, allPersons:List<Person> = mPersons.toList()):Person{
        val fixedDeadPersons = mDeadPersons.toList()
        val person = allPersons.find { it.id == id }
        return person ?: fixedDeadPersons.find { it.id == id }!!
    }

    //1旬一月
    private fun registerTimeLooper(){
        Thread(Runnable {
            while (true){
                Thread.sleep(mSpeed)
                if(mThreadRunnable){
                    mCurrentXun++
                    val message = Message.obtain()
                    message.what = 1
                    mTimeHandler.sendMessage(message)
                }
            }
        }).start()
    }

    var mHistoryTempData:MutableList<HistoryInfo> = mutableListOf()
    @SuppressLint("SetTextI18n")
    fun writeHistory(content:String, person: Person?, type:Int = 1){
        mHistoryTempData.add(0, HistoryInfo(content, person, type))
    }

    private fun updateHistory(){
        //1年更新一次
        if(mCurrentXun % 12 == 0) {
            if(mHistoryData.size > 1000)
                mHistoryData.clear()
            mHistoryData.addAll(0, mHistoryTempData)
            mHistoryTempData.clear()
            (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
            mHistory.invalidateViews()
        }
    }

    fun getYearString():String{
       return "${mCurrentXun / 12}纪年"
    }

    private fun addMultiPerson(count:Int){
        mThreadRunnable = false
        mProgressDialog.show()
        Thread(Runnable {
            Thread.sleep(500)
            var temp = count
            while(temp-- > 0){
                addPersion(null, null)
            }
            val message = Message.obtain()
            message.what = 5
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun addPersion(fixedName:String?, fixedGender:NameUtil.Gender?, lifetime: Int = 100){
        val gender = fixedGender ?: when (Random().nextInt(2)) {
            0 -> NameUtil.Gender.Male
            else -> NameUtil.Gender.Female
        }
        val name = fixedName ?: NameUtil.getChineseName(null, gender)
        val person = getPersonInfo(name, gender, lifetime)
        mPersons.add(person)
        joinAlliance(person)
        addPersonEvent(person,"${mCurrentXun / 12}年 ${getPersonBasicString(person, false)} 加入")
        writeHistory("${getPersonBasicString(person)} 加入", person)
    }

    private fun getPersonInfo(name:String, gender: NameUtil.Gender, lifetime:Int = 100): Person {
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
        val isFav = lifetime > 100
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
                type = type.filter { it != selectType}.toMutableList()
                total--
                typeNum--
            }
        }
        //tianfu
        val tianFus = mutableListOf<TianFu>()
        mConfig.tianFuType.groupBy { it.type }.forEach { (_, u) ->
            var data:TianFu? = null
            for (i in 0 until u.size){
                if(Random().nextInt(u[i].weight) == 0){
                    data = u[i]
                    break
                }
            }
            if(data != null){
                tianFus.add(data)
            }
        }
        val birthDay:Pair<Int, Int> = Pair(mCurrentXun, 0)
        val result = Person()
        result.id =  UUID.randomUUID().toString()
        result.name = name
        result.gender = gender
        result.lingGenType = lingGen
        result.lingGenName = lingGenname
        result.lingGenId = lingGenId
        result.birthDay.add(birthDay)
        result.jingJieId = mConfig.jingJieType[0].id
        result.jinJieName = mConfig.jingJieType[0].name
        result.jingJieSuccess = mConfig.jingJieType[0].success
        result.jinJieMax = mConfig.jingJieType[0].max
        result.isFav = isFav
        result.tianfus = tianFus
        result.lifetime = lifetime + (tianFus.find { it.type == 3 }?.bonus ?: 0)
        result.extraXiuwei = tianFus.find { it.type == 1 }?.bonus ?: 0
        result.extraTupo = tianFus.find { it.type == 4 }?.bonus ?: 0
        result.extraSpeed = tianFus.find { it.type == 5 }?.bonus ?: 0
        result.extraXuiweiMulti =  ((tianFus.find { it.type == 2 }?.bonus ?: 0) + 100).toDouble() / 100

        return result
    }

    fun revivePerson(id:String):Boolean{
        val person = mDeadPersons.find { it.id == id }
        if(person != null){
            person.isDead = false
            person.lifetime += 1000
            person.birthDay.add(Pair(mCurrentXun, 0))
            val commonText = " 复活，寿命增加1000"
            addPersonEvent(person,"${getYearString()} ${getPersonBasicString(person, false)} $commonText")
            writeHistory("${getPersonBasicString(person)} $commonText", person)
            mDeadPersons.remove(person)
            mPersons.add(person)
            joinAlliance(person)
            return true
        }
        return false
    }

    private fun randomEvent(persons:List<Person>){
        mConfig.events.forEach {
            val extraRate = 200/persons.size
            val random = Random().nextInt(it.weight * Math.max(1, extraRate))   //人少概率降低，200概率正常
            if(random == 0 && persons.isNotEmpty()){
                val personRandom = Random().nextInt(persons.size)
                val person = persons[personRandom]
                addPersonEvent(person,"${getYearString()} " + it.name.replace("P", getPersonBasicString(person, false)).replace("B", it.bonus.toString()), it)
                when {
                    it.type == 1 -> {
                        person.xiuXei += it.bonus
                        person.maxXiuWei += it.bonus
                    }
                    it.type == 2 -> person.jingJieSuccess += it.bonus
                    it.type == 3 -> person.lifetime += it.bonus
                }
                writeHistory( "${getYearString()} " + it.name.replace("P", getPersonBasicString(person)).replace("B", it.bonus.toString()), person)
            }
        }
    }

    private fun addPersonEvent(person:Person, content:String, event:Event? = null){
        val personEvent = PersonEvent()
        personEvent.nid = UUID.randomUUID().toString()
        personEvent.happenTime = mCurrentXun
        personEvent.content = content
        personEvent.detail = event
        person.events.add(personEvent)
    }

    private fun updateAllianceGain(){
        mAlliance.forEach { alliance->
            val fixedPersons = alliance.persons.toList()
            val persons = fixedPersons.map { getPersonDetail(it) }.toMutableList()
            if(persons.isNotEmpty()){
                persons.sortBy { it.birthDay.last().first }
                val zhu = persons.first()
                alliance.zhu = zhu
                //10 nian 一次，Fu 范围内必中
                if(mCurrentXun % 120 == 0) {
                    val base = persons.toMutableList()
                    base.sortByDescending { it.extraSpeed }
                    val result = mutableListOf<String>()
                    var total = Math.min(10, Math.max(1, persons.size / 4))
                    val specBase = base.filter { it.extraSpeed > 0 }.toMutableList()
                    while (total-- > 0) {
                        if(specBase.isNotEmpty()){
                            val totalRandom = specBase.map { it.extraSpeed }.sum()
                            val random = Random().nextInt(totalRandom)
                            var count = 0
                            for (p in 0 until specBase.size ){
                                count += base[p].extraSpeed
                                if(random < count){
                                    val selectPerson = specBase[p]
                                    specBase.remove(selectPerson)
                                    base.remove(selectPerson)
                                    result.add(selectPerson.id)
                                    break
                                }
                            }
                        }else{
                            val random = Random().nextInt(base.size)
                            val selectPerson = base[random]
                            base.remove(selectPerson)
                            result.add(selectPerson.id)
                        }
                    }
                    alliance.speedG1List = result
                }

                persons.forEach { p->
                    p.allianceXiuwei = alliance.xiuwei
                    if(alliance.speedG1List.find { it == p.id} != null){
                        p.allianceXiuwei += alliance.speedG1
                    }
                    if(p.id == alliance.zhu?.id){
                        p.allianceXiuwei += 20
                    }
                }

            }else{
                alliance.zhu = null
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun personDataHandler(){
        val fixedPerson = mPersons.toList()
        randomEvent(fixedPerson)
        updateAllianceGain()
        updateHistory()
        val dead = mutableListOf<Person>()
        for (i in 0 until fixedPerson.size){
            val it = fixedPerson[i]
            var totalAgeXun = 0
            it.birthDay.forEach {
                totalAgeXun += if(it.second == 0){
                    mCurrentXun - it.first
                }else{
                    it.second - it.first
                }
            }
            it.age = totalAgeXun / 12
            if(it.age > it.lifetime){
                addPersonEvent(it,"${getYearString()} ${getPersonBasicString(it, false)} 卒")
                writeHistory("${getPersonBasicString(it)} 卒", it)
                exitAlliance(it)
                it.isDead = true
                val pair = Pair(it.birthDay.last().first, mCurrentXun)
                it.birthDay.removeIf { p-> p.second == 0 }
                it.birthDay.add(pair)
                dead.add(it)
                continue
            }
            val currentJinJie = getJingJie(it.jingJieId)
            it.jinJieName = currentJinJie.name
            it.lingGenName =  if(it.lingGenId == "") it.lingGenName else getTianName(it.lingGenId)

            var currentXiuwei = it.xiuXei
            val xiuweiGrow = ((it.lingGenType.qiBasic + it.extraXiuwei + it.allianceXiuwei) * it.extraXuiweiMulti).toInt()
            it.maxXiuWei += xiuweiGrow
            currentXiuwei += xiuweiGrow
            if(currentXiuwei < currentJinJie.max){
                it.xiuXei = currentXiuwei
            }else{
                val next = getNextJingJie(it.jingJieId)
                if(next != null){
                    it.xiuXei = 0
                    val tianfuSuccess = it.extraTupo
                    var currentSuccess = it.jingJieSuccess
                    var bonus = 0
                    if(currentJinJie.bonus > 0 && it.lingGenType.jinBonus.isNotEmpty()){
                        bonus = it.lingGenType.jinBonus[currentJinJie.bonus - 1]
                    }
                    val random = Random().nextInt(100)
                    if(random <= currentSuccess + bonus + tianfuSuccess){//成功
                        val commonText = "突破至 ${next.name}，成功率 $random/${currentSuccess + bonus + tianfuSuccess}"
                        addPersonEvent(it,"${getYearString()} ${getPersonBasicString(it, false)} $commonText")
                        if(it.isFav || it.jingJieId.toInt() >= 2000009 ) {
                            writeHistory("${getPersonBasicString(it)} $commonText", it)
                        }
                        it.jingJieId = next.id
                        it.jingJieSuccess = next.success
                        it.jinJieMax = next.max
                        it.lifetime += next.lifetime * (100 + mAlliance.find { a-> a.id == it.allianceId}!!.lifetime ) / 100
                    }else{
                        val commonText = "突破 ${next.name} 失败 $random/${currentSuccess + bonus + tianfuSuccess}，突破率提升至${currentSuccess + bonus + tianfuSuccess + currentJinJie.fault}%"
                        addPersonEvent(it, "${getYearString()} ${getPersonBasicString(it, false)} $commonText")
                        if(it.isFav){
                            writeHistory("${getPersonBasicString(it)} $commonText", it)
                        }
                        currentSuccess += currentJinJie.fault
                        it.jingJieSuccess = currentSuccess
                    }
                }
            }
        }
        mPersons.removeIf { it.isDead }
        mDeadPersons.addAll(dead)
    }

    private fun getPersonBasicString(person:Person, lingGen:Boolean = true):String{
        return if(lingGen)
            "${person.name} (${person.age}/${person.lifetime}岁:${person.jinJieName}) ${person.lingGenName} "
        else
            "${person.name} (${person.age}/${person.lifetime}岁:${person.jinJieName}) "
    }

    fun getTianName(id:String):String{
        return mConfig.lingGenTian.find { it.id == id }!!.name
    }

    fun getJingJie(id:String):JingJie{
        return mConfig.jingJieType.find { it.id == id }!!
    }

    private fun getNextJingJie(id:String):JingJie?{
        val nextIndex = mConfig.jingJieType.indexOf(getJingJie(id)) + 1
        return if(nextIndex < mConfig.jingJieType.size)
            mConfig.jingJieType[nextIndex]
        else
            null
    }


    companion object {

        class TimeHandler constructor(val context: CultivationActivity):Handler(){

            private val reference:WeakReference<CultivationActivity> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val activity = reference.get()!!
                if(msg.what == 1){
                    activity.mDate.text = "${activity.mCurrentXun} 旬 - ${activity.getYearString()}"
                    activity.personDataHandler()
                }else if(msg.what == 2){
                    activity.mProgressDialog.dismiss()
                    activity.mThreadRunnable = true
                    Toast.makeText(activity, "保存完成", Toast.LENGTH_SHORT).show()
                }else if(msg.what == 3){
                    activity.mProgressDialog.dismiss()
                    if(msg.obj != null){
                        Toast.makeText(activity, "读取完成", Toast.LENGTH_SHORT).show()
                        activity.init(msg.obj.toString())
                    }else{
                        activity.init(null)
                    }
                }else if(msg.what == 4){
                    Toast.makeText(activity, "重启完成", Toast.LENGTH_SHORT).show()
                    activity.startWorld()
                }else if(msg.what == 5){
                    Toast.makeText(activity, "加入完成", Toast.LENGTH_SHORT).show()
                    activity.mProgressDialog.dismiss()
                    activity.mThreadRunnable = true
                }
            }
        }

        val TianFuColors = arrayOf("#EAEFE8", "#417B29", "#367CC4", "#7435C1", "#D22E59")
    }

    // type 1 人物信息
    data class HistoryInfo(var content:String, var person:Person?, var type:Int = 0)
}