package com.mx.gillustrated.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.BaseInputConnection
import android.widget.*
import androidx.annotation.RequiresApi
import butterknife.*
import com.google.gson.Gson
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.CultivationHistoryAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationHelper.addPersonEvent
import com.mx.gillustrated.component.CultivationHelper.getPersonBasicString
import com.mx.gillustrated.component.CultivationHelper.writeHistory
import com.mx.gillustrated.dialog.*
import com.mx.gillustrated.util.CultivationBakUtil
import com.mx.gillustrated.util.JsonFileReader
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.*
import java.lang.ref.WeakReference
import java.util.*

@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.N)
class CultivationActivity : BaseActivity() {

    lateinit var mConfig:Config
    private var mThreadRunnable = true
    var mCurrentXun:Int = 0//当前时间
    var mSpeed = 100L//流失速度
    var mInitPersonCount = 200//初始化Person数量
    var pinyinMode:Boolean = false //是否pinyin模式
    var readRecord = true
    var maxFemaleProfile = 0 // 1号保留不用
    var maxMaleProfile = 0 // 默认0号
    var mPersons:MutableList<Person> = mutableListOf()
    var mDeadPersons:MutableList<Person> = mutableListOf()
    var mAlliance:MutableList<Alliance> = mutableListOf()
    var mClans:MutableList<Clan> = mutableListOf()
    private var mHistoryData = mutableListOf<CultivationHelper.HistoryInfo>()
    private val mTimeHandler:TimeHandler = TimeHandler(this)

    @BindView(R.id.lv_history)
    lateinit var mHistory:ListView

    @BindView(R.id.tv_date)
    lateinit var mDate:TextView

    @BindView(R.id.tv_speed)
    lateinit var mSpeedText:TextView

    @BindView(R.id.btn_menu)
    lateinit var mBtnMenu:Button


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
            backupInfo.clans = mClans
            CultivationBakUtil.saveDataToFiles(Gson().toJson(backupInfo))
            val message = Message.obtain()
            message.what = 2
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

    @OnClick(R.id.btn_menu)
    fun onAddClickHandler(){
        val mInputConnection = BaseInputConnection(mBtnMenu, true)
        val down = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU)
        mInputConnection.sendKeyEvent(down)
        val up = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU)
        mInputConnection.sendKeyEvent(up)
    }

    @OnClick(R.id.btn_alliance)
    fun onAllianceClickHandler(){
        val ft = supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogAllianceList.newInstance()
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_alliance_list")
    }

    @OnClick(R.id.btn_list)
    fun onListClickHandler(){
        val ft = supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance()
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }

    @OnClick(R.id.btn_clan)
    fun onClanClickHandler(){
        val ft = supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogClanList.newInstance()
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_clan_list")
    }

    @OnItemClick(R.id.lv_history)
    fun onListItemClick(position:Int){
        val row = mHistoryData[position]
        if(row.type == 1){
            val person = row.person!!
            val ft = supportFragmentManager.beginTransaction()
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
            message.arg1 = CultivationBakUtil.findFemaleHeaderSize()
            message.arg2 = CultivationBakUtil.findMaleHeaderSize()
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun init(json:String?){
        val out:String? = if(readRecord) json else null
        if(out != null){
            val backup = Gson().fromJson(out, BakInfo::class.java)
            mCurrentXun = backup.xun
            mPersons = backup.persons
            mAlliance = backup.alliance
            mClans = backup.clans
        }
        createAlliance()
        if(out == null){
            startWorld()
        }else{
            writeHistory("返回世界...", null, 0)
        }
        registerTimeLooper()
    }

    private fun startWorld(){
        writeHistory("进入世界...", null, 0)
        addMultiPerson(mInitPersonCount)
        val li = addPersion(Pair("李", "逍遥"), NameUtil.Gender.Male, 100000)
        val nu = addPersion(Pair("阿", "奴"), NameUtil.Gender.Female, 100000)
        CultivationHelper.createPartner(mCurrentXun, li, nu)
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
                alliance.tianfu = it.tianfu
                alliance.success = it.success
                alliance.xiuweiMulti = it.xiuweiMulti
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
                it.xiuweiMulti = configAllianc.xiuweiMulti
                it.success = configAllianc.success
                it.tianfu = configAllianc.tianfu
                it.speedG1 = configAllianc.speedG1
                it.speedG2 = configAllianc.speedG2
            }
            mAlliance.addAll(mConfig.alliance.filter { mAlliance.none { m -> m.id == it.id } })
        }
    }


    fun getPersonDetail(id:String, allPersons:List<Person> = mPersons.toList()):Person{
        val fixedDeadPersons = mDeadPersons.toList()
        val person = allPersons.find { it.id == id }
        return person ?: fixedDeadPersons.find { it.id == id }!!
    }

    fun getOnlinePersonDetail(id:String?):Person?{
        if(id == null)
            return null
        val fixedPersons = mPersons.toList()
        return fixedPersons.find { it.id == id }
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

    private fun updateHistory(){
        //1年更新一次
        if(mCurrentXun % 12 == 0) {
            if(mHistoryData.size > 1000)
                mHistoryData.clear()
            mHistoryData.addAll(0, CultivationHelper.mHistoryTempData)
            CultivationHelper.mHistoryTempData.clear()
            (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
            mHistory.invalidateViews()
        }
    }

    fun getYearString():String{
       return "${mCurrentXun / 12}年"
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

    private fun addPersion(fixedName:Pair<String, String?>?, fixedGender:NameUtil.Gender?, lifetime: Int = 100, parent: Pair<Person, Person>? = null):Person{
        val person = CultivationHelper.getPersonInfo(mConfig, mCurrentXun, fixedName, fixedGender, lifetime, parent)
        mPersons.add(person)
        CultivationHelper.joinAlliance(person, mAlliance)
        addPersonEvent(mCurrentXun, person,"${getYearString()} ${getPersonBasicString(person, false)} 加入")
        writeHistory("${getPersonBasicString(person)} 加入", person)
        return person
    }

    fun revivePerson(id:String):Boolean{
        val person = mDeadPersons.find { it.id == id }
        if(person != null){
            person.isDead = false
            person.lifetime += 1000
            person.birthDay.add(Pair(mCurrentXun, 0))
            val commonText = " 复活，寿命增加1000"
            addPersonEvent(mCurrentXun, person,"${getYearString()} ${getPersonBasicString(person, false)} $commonText")
            writeHistory("${getPersonBasicString(person)} $commonText", person)
            mDeadPersons.remove(person)
            mPersons.add(person)
            CultivationHelper.joinAlliance(person, mAlliance)
            return true
        }
        return false
    }

    fun killPerson(id:String):Boolean{
        val person = mPersons.find { it.id == id }
        if(person != null){
            person.lifetime = person.age
            return true
        }
        return false
    }

    fun addPersonLifetime(id:String):Boolean{
        val person = mPersons.find { it.id == id }
        if(person != null){
            person.lifetime += 1000
            val commonText = " 天机，寿命增加1000"
            addPersonEvent(mCurrentXun, person,"${getYearString()} ${getPersonBasicString(person, false)} $commonText")
            writeHistory("${getPersonBasicString(person)} $commonText", person)
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
                addPersonEvent(mCurrentXun, person,"${getYearString()} " + it.name.replace("P", getPersonBasicString(person, false)).replace("B", it.bonus.toString()), it)
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

    // Every 10 Years
    private fun updatePartnerChildren(){
        if(mCurrentXun % 120 == 0) {
            val females = mPersons.filter { it.gender == NameUtil.Gender.Female }
            for(i in 0 until females.size) {
                val it = females[i]
                if(it.partner != null){
                    val partner = getOnlinePersonDetail(it.partner)
                    val children = it.children.filter { c-> getOnlinePersonDetail(c) != null }
                    val baseNumber = if(children.isEmpty()) 10L else Math.pow((children.size * 2).toDouble(), 5.0).toLong()
                    if(partner != null && baseNumber < Int.MAX_VALUE){
                        if(Random().nextInt(baseNumber.toInt()) == 0){
                            val child = addPersion(Pair(partner.lastName, null), null, 100, Pair(partner, it))
                            it.children.add(child.id)
                            partner.children.add(child.id)
                        }
                    }
                }
            }
        }
    }

    // Every 50 Years
    private fun updateClans(){
        mClans.forEach { clan->
            val clanPersons = mPersons.filter { it.ancestorId == clan.id }.sortedBy { it.ancestorLevel }.toMutableList()
            val p1 = mPersons.find { it.id == clan.id }
            val p2 = mPersons.find { it.partner == clan.id }
            if(p2 != null){
                clanPersons.add(0, p2)
            }
            if(p1 != null){
                clanPersons.add(0, p1)
            }
            clan.persons = clanPersons.map { it.id }.toMutableList()
            clan.totalXiuwei = clanPersons.sumByDouble { it.maxXiuWei.toDouble() }.toLong()
        }
        if(mCurrentXun % 600 == 0) {
            mPersons.filter { it.ancestorId != null }.groupBy { it.ancestorId }.forEach { (t, u) ->
                val total = u.size + mPersons.filter { it.id == t || it.partner == t}.size
                if(mClans.find { it.id == t } == null){
                    if(total > 5){
                        val clan = Clan()
                        clan.id = t!!
                        clan.name = u[0].lastName
                        clan.createDate = mCurrentXun
                        mClans.add(clan)
                    }
                }
            }
        }
    }

    val personDataString = arrayListOf("卒", "突破至", "成功率", "失败", "突破率提升至", "突破")
    val personDataStringPinyin = arrayListOf("zu", "tupo", "success", "fail", "changeto", "tupo")
    @RequiresApi(Build.VERSION_CODES.N)
    fun personDataHandler(){
        val yongyu = if(pinyinMode) personDataStringPinyin else personDataString
        val fixedPerson = mPersons.toList()
        randomEvent(fixedPerson)
        CultivationHelper.updatePartner(mCurrentXun, mPersons)
        updatePartnerChildren()
        CultivationHelper.updateAllianceGain(mCurrentXun, mAlliance, mPersons)
        updateClans()
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

            if(it.jingJieId == "2000601" && it.age > it.lifetime){
                it.lifetime += 10000
            }
            if(it.age > it.lifetime){
                addPersonEvent(mCurrentXun, it,"${getYearString()} ${getPersonBasicString(it, false)} ${yongyu[0]}")
                writeHistory("${getPersonBasicString(it)} ${yongyu[0]}", it)
                CultivationHelper.exitAlliance(it, mAlliance)
                it.isDead = true
                val pair = Pair(it.birthDay.last().first, mCurrentXun)
                it.birthDay.removeIf { p-> p.second == 0 }
                it.birthDay.add(pair)
                dead.add(it)
                continue
            }
            val currentJinJie = getJingJie(it.jingJieId)
            it.jinJieName = CultivationHelper.getJinJieName(currentJinJie.name, pinyinMode)
            it.lingGenName =  if(it.lingGenId == "") it.lingGenName else getTianName(it.lingGenId)
            if(pinyinMode)
                it.lingGenName = PinyinUtil.convert(it.lingGenName)
            if(it.gender == NameUtil.Gender.Female && it.profile == 0 && maxFemaleProfile > 1){
                it.profile = Random().nextInt(maxFemaleProfile - 1) + 2
            }
            var currentXiuwei = it.xiuXei
            val xiuweiGrow = ((it.lingGenType.qiBasic + it.extraXiuwei + it.allianceXiuwei) * ((it.extraXuiweiMulti + 100).toDouble() / 100) ).toInt()
            it.maxXiuWei += xiuweiGrow
            currentXiuwei += xiuweiGrow
            if(currentXiuwei < currentJinJie.max){
                it.xiuXei = currentXiuwei
            }else{
                val next = getNextJingJie(it.jingJieId)
                if(next != null){
                    it.xiuXei = 0
                    val tianfuSuccess = it.extraTupo
                    val allianceSuccess = it.allianceSuccess
                    var currentSuccess = it.jingJieSuccess
                    var bonus = 0
                    if(currentJinJie.bonus > 0 && it.lingGenType.jinBonus.isNotEmpty()){
                        bonus = it.lingGenType.jinBonus[currentJinJie.bonus - 1]
                    }
                    val random = Random().nextInt(100)
                    if(random <= currentSuccess + bonus + tianfuSuccess + allianceSuccess){//成功
                        val commonText = "${yongyu[1]} ${CultivationHelper.getJinJieName(next.name, pinyinMode)}，${yongyu[2]} $random/${currentSuccess + bonus + tianfuSuccess + allianceSuccess}"
                        addPersonEvent(mCurrentXun, it,"${getYearString()} ${getPersonBasicString(it, false)} $commonText")
                        if(it.isFav || it.jingJieId.toInt() >= 2000009 ) {
                            writeHistory("${getPersonBasicString(it)} $commonText", it)
                        }
                        it.jingJieId = next.id
                        it.jingJieSuccess = next.success
                        it.jinJieColor = next.color
                        it.jinJieMax = next.max
                        it.lifetime += next.lifetime * (100 + mAlliance.find { a-> a.id == it.allianceId}!!.lifetime ) / 100
                    }else{
                        val commonText = "${yongyu[5]} ${CultivationHelper.getJinJieName(next.name, pinyinMode)} ${yongyu[3]} $random/${currentSuccess + bonus + tianfuSuccess + allianceSuccess}，${yongyu[4]} ${currentSuccess + bonus + tianfuSuccess + allianceSuccess + currentJinJie.fault}%"
                        addPersonEvent(mCurrentXun, it, "${getYearString()} ${getPersonBasicString(it, false)} $commonText")
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

    fun resetHandler(){
        mThreadRunnable = false
        mProgressDialog.show()
        mHistoryData.clear()
        CultivationHelper.mHistoryTempData.clear()
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

    private fun disasterHandler(){
        val effectPersons = mPersons.filter { it.age % 2 == 0 }
        val random = Random().nextInt(10)
        val level = when (random) {
            0 -> 500
            in 1..2 -> 200
            in 3..5 -> 100
            else -> 50
        }
        val description = when (random) {
            0 -> "超大"
            in 1..2 -> "大"
            in 3..5 -> "中"
            else -> "小"
        }
        val text = "${description}😡火 寿命降低$level"
        writeHistory( "${getYearString()} $text", null, 0)
        effectPersons.forEach {
            it.lifetime -= level
            addPersonEvent(mCurrentXun, it,"${getYearString()} $text")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cultivation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.menu_reset -> {
                resetHandler()
            }
            R.id.menu_disaster -> {
                disasterHandler()
            }
        }
        return true
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
                        activity.maxFemaleProfile = msg.arg1
                        activity.maxMaleProfile = msg.arg2
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
    }


}