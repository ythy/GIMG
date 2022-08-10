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
import butterknife.*
import com.google.gson.Gson
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.CultivationHistoryAdapter
import com.mx.gillustrated.component.CultivationBattleHelper
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationHelper.SpecPersonFirstName
import com.mx.gillustrated.component.CultivationHelper.SpecPersonFirstName2
import com.mx.gillustrated.component.CultivationHelper.SpecPersonFirstName3
import com.mx.gillustrated.component.CultivationHelper.SpecPersonFixedName
import com.mx.gillustrated.component.CultivationHelper.addPersonEvent
import com.mx.gillustrated.component.CultivationHelper.getPersonBasicString
import com.mx.gillustrated.component.CultivationHelper.mBattleRound
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.component.CultivationHelper.mCurrentXun
import com.mx.gillustrated.component.CultivationHelper.writeHistory
import com.mx.gillustrated.component.CultivationHelper.SpecPersonInfo
import com.mx.gillustrated.component.CultivationHelper.pinyinMode
import com.mx.gillustrated.component.CultivationHelper.maxFemaleProfile
import com.mx.gillustrated.component.CultivationHelper.maxMaleProfile
import com.mx.gillustrated.component.CultivationHelper.showing
import com.mx.gillustrated.dialog.*
import com.mx.gillustrated.util.CultivationBakUtil
import com.mx.gillustrated.util.JsonFileReader
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.*
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.*


@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.N)
class CultivationActivity : BaseActivity() {

    private var mThreadRunnable = true
    private var mHistoryThreadRunnable = true
    private var mSpeed = 10L//流失速度
    private val mInitPersonCount = 1000//初始化Person数量
    private var readRecord = true

    var mPersons:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var mDeadPersons:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var mAlliance:ConcurrentHashMap<String, Alliance> = ConcurrentHashMap()
    var mClans:ConcurrentHashMap<String, Clan> = ConcurrentHashMap()
    private var mEnemys:ConcurrentHashMap<String, Enemy> = ConcurrentHashMap()
    private var mBoss:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    private var mHistoryData = mutableListOf<CultivationHelper.HistoryInfo>()
    private val mTimeHandler:TimeHandler = TimeHandler(this)

    private val mExecutor:ExecutorService = Executors.newFixedThreadPool(20)

    @BindView(R.id.lv_history)
    lateinit var mHistory:ListView

    @BindView(R.id.tv_date)
    lateinit var mDate:TextView

    @BindView(R.id.tv_speed)
    lateinit var mSpeedText:TextView

    @BindView(R.id.btn_menu)
    lateinit var mBtnMenu:Button

    @BindView(R.id.btn_time)
    lateinit var mBtnTime:Button

    @OnClick(R.id.btn_save)
    fun onSaveClickHandler(){
        setTimeLooper(false)
        mProgressDialog.show()
        Thread(Runnable {
            Thread.sleep(500)
            val backupInfo = BakInfo()
            backupInfo.xun = mCurrentXun
            backupInfo.battleRound = mBattleRound
            backupInfo.alliance = mAlliance.mapValues { it.value.toConfig() }
            mPersons.forEach { p->
                val it = p.value
                it.lingGenName = if (it.lingGenId == "") it.lingGenName else CultivationHelper.getTianName(it.lingGenId)
                it.profile = CultivationHelper.getRandomProfile(it.gender, it.profile)
                it.HP = Math.min(it.HP, it.maxHP)
                it.children = it.children.filterNot { f-> getOnlinePersonDetail(f) == null && getOfflinePersonDetail(f) == null }.toMutableList()
            }
            backupInfo.persons = mPersons
            backupInfo.clans = mClans.mapValues { it.value.toClanBak() }
            CultivationBakUtil.saveDataToFiles(Gson().toJson(backupInfo))
            val message = Message.obtain()
            message.what = 2
            mTimeHandler.sendMessage(message)
        }).start()
    }

    @OnClick(R.id.btn_multi)
    fun onAdd100ClickHandler(){
        addMultiPerson(mInitPersonCount)
    }

    @OnClick(R.id.btn_time)
    fun onTimeClickHandler(){
        setTimeLooper(mBtnTime.tag == "OFF")
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
        val ft = supportFragmentManager.beginTransaction()
        if(row.type == 1){
            val person = row.person!!
            // Create and show the dialog.
            val newFragment = FragmentDialogPerson.newInstance()
            newFragment.isCancelable = false
            val bundle = Bundle()
            bundle.putString("id", person.id)
            newFragment.arguments = bundle
            newFragment.show(ft, "dialog_person_info")
        }else if(row.type == 2){
            val newFragment = FragmentDialogBattleInfo.newInstance()
            newFragment.isCancelable = false
            val bundle = Bundle()
            bundle.putString("id", row.battleId)
            newFragment.arguments = bundle
            newFragment.show(ft, "dialog_battle_info")
        }
    }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cultivation)
        ButterKnife.bind(this)
        initLayout()
        loadConfig()
        if(Build.getSerial().contains("EMULATOR")){ //判断是否是模拟器
            pinyinMode = true
        }
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

    override fun onPause() {
        super.onPause()
        setTimeLooper(false)
    }

    override fun onResume() {
        super.onResume()
        if(mCurrentXun > 0){
            setTimeLooper(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mThreadRunnable = false
        mHistoryThreadRunnable = false
    }


    private fun init(json:String?){
        val out:String? = if(readRecord) json else null
        if(out != null){
            val backup = Gson().fromJson(out, BakInfo::class.java)
            mCurrentXun = backup.xun
            mPersons.putAll(backup.persons)
            mPersons.forEach {
                if(it.value.children.isNotEmpty())
                    it.value.children = Collections.synchronizedList(it.value.children)
                it.value.birthDay = Collections.synchronizedList(it.value.birthDay)
            }
            mAlliance.putAll(backup.alliance.mapValues {
                it.value.toAlliance(mPersons)
            })
            mClans.putAll(backup.clans.mapValues {
                it.value.toClan(mPersons)
            })
            mBattleRound = backup.battleRound ?: BattleRound()
        }else{
            mBattleRound = BattleRound()
        }
        createAlliance()
        //更新Alliance属性
        mPersons.forEach {
            it.value.allianceSuccess = mAlliance[it.value.allianceId]!!.success
            it.value.allianceProperty =  mAlliance[it.value.allianceId]!!.property
            it.value.allianceName = mAlliance[it.value.allianceId]!!.name
            it.value.extraXuiweiMulti = CultivationHelper.getExtraXuiweiMulti(it.value,  mAlliance[it.value.allianceId]!!)
            it.value.equipment.removeIf { e-> e.split(",").size != 2}
            CultivationHelper.updatePersonEquipment(it.value)
        }
        if(out == null){
            startWorld()
        }else{
            writeHistory("返回世界...", null, 0)
        }
        registerTimeLooper()
        registerHistoryTimeLooper()
    }

    private fun startWorld(){
        writeHistory("进入世界...", null, 0)
        addMultiPerson(mInitPersonCount)
        val li = addPersion(Pair("李", "逍遥"), NameUtil.Gender.Male, 100, null, true)
        val nu = addPersion(Pair("阿", "奴"), NameUtil.Gender.Female, 100, null, true)
        CultivationHelper.createPartner(li, nu)
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
                mAlliance[it.id] = newAlliance(it)
            }
        }else{
            mConfig.alliance.forEach { configAlliance->
                val alliance = mAlliance[configAlliance.id]
                if(alliance != null){
                    alliance.name = configAlliance.name + "界"
                    alliance.type = configAlliance.type
                    alliance.level = configAlliance.level
                    alliance.lifetime = configAlliance.lifetime
                    alliance.maxPerson = configAlliance.maxPerson
                    alliance.xiuwei = configAlliance.xiuwei
                    alliance.xiuweiMulti = configAlliance.xiuweiMulti
                    alliance.success = configAlliance.success
                    alliance.tianfu = configAlliance.tianfu
                    alliance.speedG1 = configAlliance.speedG1
                    alliance.speedG2 = configAlliance.speedG2
                    alliance.property = configAlliance.property
                }else{
                    mAlliance[configAlliance.id] = newAlliance(configAlliance)
                }
            }

            mAlliance.forEach {
                if(mConfig.alliance.find { a-> a.id == it.value.id } == null){
                    mAlliance.remove(it.value.id)
                    mPersons.forEach { p->
                        if(p.value.allianceId == it.value.id)
                            mPersons.remove(p.value.id)
                    }
                }
            }
        }
    }

    private fun newAlliance(it:AllianceConfig):Alliance{
        val alliance = Alliance()
        alliance.name = it.name + "界"
        alliance.id = it.id
        alliance.type = it.type
        alliance.level = it.level
        alliance.lifetime = it.lifetime
        alliance.xiuwei = it.xiuwei
        alliance.maxPerson = it.maxPerson
        alliance.tianfu = it.tianfu
        alliance.success = it.success
        alliance.xiuweiMulti = it.xiuweiMulti
        alliance.lingGen = it.lingGen
        alliance.property = it.property
        alliance.speedG1 = it.speedG1
        alliance.speedG2 = it.speedG2
        return alliance
    }

    fun getPersonData(id: String):Person?{
        if(id == null)
            return null
        return  getOnlinePersonDetail(id) ?: getOfflinePersonDetail(id) ?: mBoss[id]
    }

    fun getOnlinePersonDetail(id:String?):Person?{
        if(id == null)
            return null
        return mPersons[id]
    }

    fun getOfflinePersonDetail(id:String?):Person?{
        if(id == null)
            return null
        return mDeadPersons[id]
    }

    fun setTimeLooper(flag:Boolean){
        if(flag){
            mBtnTime.tag = "ON"
            mBtnTime.text = resources.getString(R.string.cultivation_stop)
            mThreadRunnable = true
        }else{
            mBtnTime.tag = "OFF"
            mBtnTime.text = resources.getString(R.string.cultivation_start)
            mThreadRunnable = false
        }
    }

    private fun deadHandler(it:Person, currentXun:Long){
        mPersons.remove(it.id)
        mDeadPersons[it.id] = it
        addPersonEvent(it, "${getYearString()} ${getPersonBasicString(it, false)} ${personDataString[0]}")
        writeHistory("${getPersonBasicString(it)} ${personDataString[0]}", it)
        val alliance = mAlliance[it.allianceId]
        alliance?.personList?.remove(it.id)
        it.allianceId = ""
        it.allianceName = ""
        if(alliance != null && alliance.zhuPerson == it)
            alliance.zhuPerson = null
        if(mClans[it.ancestorId] != null){
            mClans[it.ancestorId]!!.clanPersonList.remove(it.id)
            if(mClans[it.ancestorId]!!.zhu?.id == it.id){
                mClans[it.ancestorId]!!.zhu = null
            }
        }
        synchronized(it.birthDay){
            val pair = Pair(it.birthDay.last().first, currentXun)
            it.birthDay.removeIf { it.second == 0L }
            it.birthDay.add(pair)
        }
    }

    private fun isDeadException(person:Person):Boolean{
        val matchName = "(李逍遥|阿奴)".toRegex()
        if(person.isFav){
            return true
        }else if(matchName.find(person.name) != null){
            return true
        }
        return false
    }

    private fun xunHandler(currentXun:Long) {
        val year = getYearString(currentXun)
        if(mDate.text != year) {
            mDate.text = year
        }
        mExecutor.execute {
            updateInfoByXun(currentXun)
        }
        for ((_: String, it: Person) in mPersons) {
            mExecutor.execute {
                updatePersonByXun(it, currentXun)
            }
        }
    }

    private fun updatePersonByXun(it:Person, currentXun:Long){
        it.age = (it.lastTotalXun + currentXun - it.lastBirthDay) / 12
        if (it.age > it.lifetime ) {
            if(isDeadException(it)){
                it.lifetime += 5000
            }else{
                if(getOnlinePersonDetail(it.id) != null)
                    deadHandler(it, currentXun)
                return
            }
        }
        val currentJinJie = CultivationHelper.getJingJie(it.jingJieId)
        val xiuweiGrow = CultivationHelper.getXiuweiGrow(it, mAlliance)
        it.maxXiuWei += xiuweiGrow
        it.xiuXei += xiuweiGrow
        if (it.xiuXei < currentJinJie.max) {
            return
        }
        val next = CultivationHelper.getNextJingJie(it.jingJieId)
        it.xiuXei = 0
        val totalSuccess = CultivationHelper.getTotalSuccess(it)
        val random = Random().nextInt(100)
        if (random <= totalSuccess) {//成功
            if (next != null) {
                val commonText = "${personDataString[1]} ${CultivationHelper.getJinJieName(next.name)}，${personDataString[2]} $random/$totalSuccess"
                val lastJingJieDigt = CultivationHelper.getJingJieLevel(it.jingJieId)
                if (it.isFav || (lastJingJieDigt.second >= 6 && lastJingJieDigt.third == 4)) {
                    writeHistory("${getPersonBasicString(it)} $commonText", it)
                }
                if (lastJingJieDigt.second >= 7 && lastJingJieDigt.third == 4) {
                    addPersonEvent(it, "${getYearString()} ${getPersonBasicString(it, false)} $commonText")
                }
                it.jingJieId = next.id
                it.jinJieName = CultivationHelper.getJinJieName(next.name)
                it.jingJieSuccess = next.success
                it.jinJieColor = next.color
                it.jinJieMax = next.max
                val allianceNow = mAlliance[it.allianceId]
                it.lifetime += next.lifetime * (100 + (allianceNow?.lifetime ?: 0)) / 100
            } else {
                val commonText = "转转成功，${personDataString[2]} $random/$totalSuccess"
                addPersonEvent(it, "${getYearString()} ${getPersonBasicString(it, false)} $commonText")
                writeHistory("${getPersonBasicString(it)} $commonText", it)
                it.jingJieId = mConfig.jingJieType[0].id
                it.jinJieName = CultivationHelper.getJinJieName(mConfig.jingJieType[0].name)
                it.jingJieSuccess = mConfig.jingJieType[0].success
                it.jinJieColor = mConfig.jingJieType[0].color
                it.jinJieMax = mConfig.jingJieType[0].max
                it.lifeTurn += 1
                it.lifetime = it.age + 100 * ( 100 + mAlliance[it.allianceId]!!.lifetime ) / 100 + it.lifeTurn * 5 + (it.tianfus.find { t-> t.type == 3 }?.bonus ?: 0)
            }
        } else {
            val commonText = if (next != null)
                "${personDataString[5]} ${CultivationHelper.getJinJieName(next.name)} ${personDataString[3]} $random/$totalSuccess，${personDataString[4]} ${totalSuccess + currentJinJie.fault}%"
            else
                "转转失败 ${personDataString[3]} $random/$totalSuccess，${personDataString[4]} ${totalSuccess + currentJinJie.fault}%"
            if (it.isFav) {
                writeHistory("${getPersonBasicString(it)} $commonText", it)
            }
            it.jingJieSuccess += currentJinJie.fault
        }

    }

    private fun updateInfoByXun(currentXun:Long){
        CultivationHelper.updateAllianceGain(mAlliance, currentXun % 120 == 0L)
        if(currentXun % 120 == 0L) {
            updatePartnerChildren()
            updateHP()
        }
        if(currentXun % 240 == 0L) {
            CultivationHelper.updatePartner(mPersons)
        }
        //randomEvent(fixedPerson) 暂时不启用
        updateClans(currentXun)
        updateEnemys(currentXun)
        updateBoss(currentXun)
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
                    message.obj = mCurrentXun
                    mTimeHandler.sendMessage(message)
                }
            }
        }).start()
    }


    //1旬一月
    private fun registerHistoryTimeLooper(){
        Thread(Runnable {
            while (true){
                Thread.sleep(1000)
                if(mHistoryThreadRunnable){
                    val message = Message.obtain()
                    message.what = 7
                    mTimeHandler.sendMessage(message)
                }
            }
        }).start()
    }

    private fun updateHistory(){
        if(mHistoryData.size > 500 && mThreadRunnable)
            mHistoryData.clear()
        val tempList = CultivationHelper.mHistoryTempData.toList()
        CultivationHelper.mHistoryTempData.clear()
        if(pinyinMode){
            tempList.forEach {
                it.content = PinyinUtil.convert(it.content)
            }
        }
        mHistoryData.addAll(0, tempList)
        (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
        mHistory.invalidateViews()
    }

    private fun getYearString(xun:Long = mCurrentXun):String{
       return "${xun / 12}${showing("年")}"
    }

    private fun addMultiPerson(count:Int){
        setTimeLooper(false)
        mProgressDialog.show()
        Thread(Runnable {
            var temp = count
            Thread.sleep(500)
            try {
                while(temp-- > 0){
                    mExecutor.execute(AddPersonRunnable(temp, mTimeHandler))
                }
            }catch (e:IOException) {
                mExecutor.shutdown()
            }
        }).start()
    }

    class AddPersonRunnable constructor(private val count:Int, val handler:TimeHandler):Runnable {
        override fun run() {
            val person = CultivationHelper.getPersonInfo(null, null)
            val message = Message.obtain()
            message.what = 5
            message.obj = person
            message.arg1 = count
            handler.sendMessage(message)
        }
    }

    private fun addPersion(fixedName:Pair<String, String?>?, fixedGender:NameUtil.Gender?,
                           lifetime: Long = 100, parent: Pair<Person, Person>? = null, fav:Boolean = false):Person{
        val person = CultivationHelper.getPersonInfo(fixedName, fixedGender, lifetime, parent, fav)
        combinedPersonRelationship(person)
        return person
    }

    fun combinedPersonRelationship(person: Person, log:Boolean = true){
        mPersons[person.id] = person
        if(mClans[person.ancestorId] != null){
            mClans[person.ancestorId]!!.clanPersonList[person.id] = person
        }
        CultivationHelper.joinAlliance(person, mAlliance)
        if(log){
            addPersonEvent(person,"${getYearString()} ${getPersonBasicString(person, false)} 加入")
            writeHistory("${getPersonBasicString(person)} 加入", person)
        }

    }


    fun bePerson(){
        setTimeLooper(false)
        Thread(Runnable {
            Thread.sleep(500)
            mDeadPersons.forEach {
                it.value.partnerName = null
                it.value.partner = null
            }
            mPersons.forEach {
                val person = it.value
                val partner = getOnlinePersonDetail(person.partner)
                if(person.partner != null && partner == null ){
                    person.partnerName = null
                    person.partner = null
                }
            }
            val message = Message.obtain()
            message.what = 6
            mTimeHandler.sendMessage(message)
        }).start()
    }

    fun revivePerson(id:String){
        setTimeLooper(false)
        Thread(Runnable {
            Thread.sleep(500)
            val person = mDeadPersons[id]
            if(person != null){
                person.lifetime += 5000L
                person.lastBirthDay = mCurrentXun
                person.lastTotalXun += person.birthDay.last().second -  person.birthDay.last().first
                person.birthDay.add(Pair(mCurrentXun, 0))
                mDeadPersons.remove(id)
                combinedPersonRelationship(person, false)
                val commonText = " 复活，寿命增加5000"
                addPersonEvent(person,"${getYearString()} ${getPersonBasicString(person, false)} $commonText")
                writeHistory("${getPersonBasicString(person)} $commonText", person)
            }
            val message = Message.obtain()
            message.what = 6
            mTimeHandler.sendMessage(message)
        }).start()
    }

    fun killPerson(id:String){
        setTimeLooper(false)
        Thread(Runnable {
            Thread.sleep(500)
            val person = mPersons[id]
            if(person != null){
                person.lifetime = person.age
                deadHandler(person, mCurrentXun)
            }
            val message = Message.obtain()
            message.what = 6
            mTimeHandler.sendMessage(message)
        }).start()
    }

    fun addPersonLifetime(id:String):Boolean{
        val person = mPersons[id]
        if(person != null){
            person.lifetime += 5000L
            val commonText = "天机，寿命增加5000"
            addPersonEvent(person,"${getYearString()} ${getPersonBasicString(person, false)} $commonText")
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

    // Every 10 Years
    private fun updatePartnerChildren(){
        val females = mPersons.filter { !it.value.dink && it.value.gender == NameUtil.Gender.Female }.map { it.value }.toMutableList()
        for(i in 0 until females.size) {
            val it = females[i]
            if(it.partner != null){
                val partner = getOnlinePersonDetail(it.partner)
                val children = it.children.filter { c-> getOnlinePersonDetail(c) != null }
                val baseNumber = if(children.isEmpty()) 10L else Math.pow((children.size * 2).toDouble(), 5.0).toLong()
                if(partner != null && baseNumber < Int.MAX_VALUE){
                    if(Random().nextInt(baseNumber.toInt()) == 0){
                        val child = if(mAlliance[partner.allianceId]!!.type == 2 && partner.ancestorLevel <= 1 && mPersons[partner.ancestorId] != null ){
                            if(Random().nextInt(2) == 0){
                                val allianceList = mutableListOf(mAlliance[partner.allianceId]!!)
                                fixedPersonGenerate(mutableListOf(SpecPersonInfo(Pair(partner.lastName, null), NameUtil.Gender.Male, 0,1,1)),
                                        allianceList, Pair(partner, it))[0]
                            }else{
                                addPersion(Pair(partner.lastName, null), NameUtil.Gender.Female, 100,
                                        Pair(partner, it))
                            }
                        }else{
                            addPersion(Pair(partner.lastName, null), null, 100,
                                    Pair(partner, it))
                        }
                        if(child != null){
                            synchronized(it.children){
                                it.children.add(child.id)
                            }
                            synchronized(partner.children){
                                partner.children.add(child.id)
                            }
                        }
                    }
                }
            }
        }
        mPersons.forEach {
            if(it.value.children.isNotEmpty()){
                synchronized(it.value.children){
                    it.value.children.removeIf { c-> getOnlinePersonDetail(c) == null }
                }
            }
        }
    }

    private fun updateEnemys(xun:Long){
        val random = Random()
        mEnemys.filter { !it.value.isDead }.forEach { e->
            val it = e.value
            if(random.nextInt(it.attackFrequency) == 0){
                it.remainHit--
                val persons = mPersons.map { it.value }.shuffled()
                val person = persons[0]
                val result = CultivationBattleHelper.battleEnemy(person, it, it.HP * 1000)
                if(result){
                    writeHistory("${it.name} 消失", null, 0)
                    it.isDead = true
                    CultivationHelper.gainJiEquipment(person, 14, it.type, it.seq)
                    if(it.type > 1){
                        gainTeji(person, 20 * it.type)
                    }
                }else if(it.remainHit <= 0){
                    writeHistory("${it.name} 消失", null, 0)
                    it.isDead = true
                }
            }
        }
    }

    private fun updateBoss(xun:Long){
        if(xun % 12 == 0L) {
            mBoss.map(Map.Entry<String, Person>::value).filter { (xun - it.lastBirthDay) / 12 < it.lifetime }.forEach { u ->
                val targets = mPersons.map { it.value }.shuffled()
                val person = targets[0]
                val result = CultivationBattleHelper.battlePerson(person, u, 10, 5000000 * Math.min(2, u.type))
                if (result) {
                    u.lifetime = 0
                    mAlliance[u.allianceId]?.personList?.remove(u.id)
                    writeHistory("${u.name} 倒", u, 0)
                    gainTeji(person, Math.min(1000, 500 * u.type))
                    CultivationHelper.gainJiEquipment(person, 15, u.type - 1, mBattleRound.boss[u.type - 1])
                }
            }
        }
    }

    private fun updateClans(xun:Long){
        if(xun % 480 == 0L) {
            mPersons.filter { it.value.ancestorId != null }.map { it.value }.toMutableList()
                    .groupBy { it.ancestorId }.forEach { (t, u) ->
                if (u.size >= 5) {
                    if (mClans[t] == null) {
                        val clan = Clan()
                        clan.id = t!!
                        clan.name = u[0].lastName
                        clan.createDate = xun
                        u.forEach { p ->
                            clan.clanPersonList[p.id] = p
                        }
                        mClans[t] = clan
                    }
                }
            }
        }
        if(xun % 12 == 0L) {
            mClans.forEach {
                if(it.value.clanPersonList.isEmpty()){
                    mClans.remove(it.key)
                }else if(it.value.zhu == null){
                    val temp = it.value.clanPersonList.map { c->c.value }.toMutableList()
                    temp.sortBy { s-> s.ancestorLevel }
                    it.value.zhu = temp[0]
                }
            }
        }
    }

    // update every 10 years
    private fun updateHP(){
        for ((_: String, it: Person) in mPersons) {
            it.HP += it.lingGenType.color + 1
            val currentHP = CultivationHelper.getProperty(it)[0]
            if(currentHP < -10){
                val supplement = Math.abs(currentHP)
                if(it.lifetime - it.age - supplement > 200){
                    it.lifetime -= supplement
                    it.HP += supplement
                }
            }
            if(it.HP > it.maxHP )
                it.HP = it.maxHP
        }
    }

    private fun eventEnemyHandler(){
        val type = Random().nextInt(5)
        mBattleRound.enemy[type]++
        val enemy = CultivationHelper.generateEnemy(type)
        mEnemys[enemy.id] = enemy
        writeHistory("====================================", null, 0)
        writeHistory("↑↑============================↑↑", null, 0)
        writeHistory("${enemy.name}天降 - (${enemy.HP}/${enemy.remainHit})${enemy.attack}-${enemy.defence}-${enemy.speed}", null, 0)
        writeHistory("↓↓============================↓↓", null, 0)
        writeHistory("====================================", null, 0)
    }

    private fun addFixedcPerson(){
        val ft = supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogAddPerson.newInstance()
        newFragment.isCancelable = true
        newFragment.show(ft, "dialog_add_fixed_person")
    }

    private fun addSpecPerson(){
        val allianceList = Collections.synchronizedList(mAlliance.map { it.value }.filter { it.type == 1 })
        val specPersonList = mutableListOf<SpecPersonInfo>()
        for ( i in 0 until 4){
            SpecPersonFirstName.forEach { first->
                specPersonList.add(SpecPersonInfo(Pair(allianceList[i].name.slice(0 until 1), first), NameUtil.Gender.Female, i, 50, 20))
            }
        }
        fixedPersonGenerate(specPersonList, allianceList)

        val allianceList2 = mAlliance.map { it.value }.filter { it.type == 2 }.toMutableList()
        val specPersonList2 = mutableListOf<SpecPersonInfo>()
        for ( i in 0 until 1){
            SpecPersonFirstName2.forEach { first->
                specPersonList2.add(SpecPersonInfo(Pair(allianceList2[i].name.slice(0 until 1), first), null, i, 20, 10))
            }
        }
        fixedPersonGenerate(specPersonList2, allianceList2)

        val allianceList3 = mAlliance.map { it.value }.filter { it.type == 3 }.sortedBy { it.id }.toMutableList()
        val specPersonList3 =  SpecPersonFirstName3.map { props->
            SpecPersonInfo(Pair(props.first.first, props.first.second), props.second, props.third, 50, 50)
        }.toMutableList()
        fixedPersonGenerate(specPersonList3, allianceList3)

        val personList = Collections.synchronizedList(mPersons.map { m-> m.value })
        SpecPersonFixedName.forEach {
            if(personList.find { p-> p.name == it.first.first + it.first.second } == null){
                val person = CultivationHelper.getPersonInfo(it.first, it.second, 100, null, false, it.third)
                combinedPersonRelationship(person)
            }
        }

    }

    private fun fixedPersonGenerate(specPersonList:MutableList<SpecPersonInfo>, allianceList:MutableList<Alliance>, parent: Pair<Person, Person>? = null):MutableList<Person?>{
        val personList = Collections.synchronizedList(mPersons.map { m-> m.value })
        val result = mutableListOf<Person?>()
        specPersonList.forEach {
            if(personList.find { p-> p.name == it.name.first + it.name.second } == null){
                val person = CultivationHelper.getPersonInfo(it.name, it.gender, 100, parent, false, CultivationHelper.PersonFixedInfoMix(null, null, it.TianFuWeight, it.LingGenWeight))
                result.add(person)
                mPersons[person.id] = person
                CultivationHelper.joinFixedAlliance(person, allianceList[it.allianceIndex])
                addPersonEvent(person,"${getYearString()} ${getPersonBasicString(person, false)} 加入")
                writeHistory("${getPersonBasicString(person)} 加入", person)
            }
        }
        return result
    }

    private fun resetHandler(){
        setTimeLooper(false)
        mProgressDialog.show()
        mHistoryData.clear()
        CultivationHelper.mHistoryTempData.clear()
        (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
        mHistory.invalidateViews()
        Thread(Runnable {
            Thread.sleep(1000)
            mCurrentXun = 0
            mClans.clear()
            mPersons.clear()
            mDeadPersons.clear()
            mAlliance.clear()
            mBattleRound = BattleRound()
            createAlliance()
            val message = Message.obtain()
            message.what = 4
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleSingleHandler(){
        val personsAll = mPersons.filter { CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()
        personsAll.shuffle()
        if(personsAll.size < 20){
            Toast.makeText(this, "persons less than 20", Toast.LENGTH_SHORT).show()
            return
        }
        val persons = personsAll.subList(0, personsAll.size/2)
        setTimeLooper(false)
        mHistoryData.clear()
        CultivationHelper.mHistoryTempData.clear()
        (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
        mHistory.invalidateViews()
        Thread(Runnable {
            Thread.sleep(500)
            mBattleRound.single++
            writeHistory("第${mBattleRound.single}届  Single Battle Start", null, 0)
            var roundNumber = 1
            while (true){
                writeHistory("Single Battle ${roundNumber}轮 Start", null, 0)
                roundNumber++
                val result = roundHandler(persons, 40, 200000)
                if(result)
                    break
            }
            persons[0].xiuXei += 200000
            persons[1].xiuXei += 100000
            addPersonEvent(persons[0],"${getYearString()} ${getPersonBasicString(persons[0], false)} Single Battle Winner")
            CultivationHelper.gainJiEquipment(persons[0], 13, 0, mBattleRound.single)
            addPersonEvent(persons[1],"${getYearString()} ${getPersonBasicString(persons[1], false)} Single Battle Runner")
            CultivationHelper.gainJiEquipment(persons[1], 13, 1, mBattleRound.single)
            writeHistory("第${mBattleRound.single}届 Single Battle Runner: ${persons[1].allianceName} - ${persons[1].name}", persons[1])
            writeHistory("第${mBattleRound.single}届 Single Battle Winner: ${persons[0].allianceName} - ${persons[0].name}", persons[0])
            val message = Message.obtain()
            message.what = 8
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleClanHandler(){
        val clans = mClans.filter { it.value.clanPersonList.size > 0 }.map { it.value }.toMutableList()
        if(clans.isEmpty() || clans.size < 4){
            Toast.makeText(this, "Clan less than 4", Toast.LENGTH_SHORT).show()
            return
        }
        setTimeLooper(false)
        mHistoryData.clear()
        CultivationHelper.mHistoryTempData.clear()
        (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
        mHistory.invalidateViews()
        Thread(Runnable {
            Thread.sleep(500)
            mBattleRound.clan++
            writeHistory("第${mBattleRound.clan}届 Clan Battle Start", null, 0)
            var roundNumber = 1
            while (true){
                writeHistory("Clan Battle ${roundNumber}轮 Start", null, 0)
                roundNumber++
                val result = roundClanHandler(clans, 10, 80000)
                if(result)
                    break
            }
            clans[0].clanPersonList.forEach {
                it.value.xiuXei += 200000
                CultivationHelper.gainJiEquipment(it.value, 12, 0, mBattleRound.clan)
            }
            writeHistory("Clan Battle Winner: ${clans[0].name}", null, 0)
            val message = Message.obtain()
            message.what = 8
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleBangHandler(){
        val alliances = mAlliance.filter { it.value.personList.isNotEmpty() }.map { it.value }.toMutableList()
        if(alliances.isEmpty() || alliances.size < 4){
            Toast.makeText(this, "Bang less than 4", Toast.LENGTH_SHORT).show()
            return
        }
        setTimeLooper(false)
        mHistoryData.clear()
        CultivationHelper.mHistoryTempData.clear()
        (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
        mHistory.invalidateViews()
        Thread(Runnable {
            Thread.sleep(500)
            mBattleRound.bang++
            writeHistory("第${mBattleRound.bang}届 Bang Battle Start", null, 0)
            var roundNumber = 1
            while (true){
                writeHistory("Bang Battle ${roundNumber}轮 Start", null, 0)
                roundNumber++
                val result = roundBangHandler(alliances, 20, 200000)
                if(result)
                    break
            }
            alliances[0].personList.forEach {
                CultivationHelper.gainJiEquipment(it.value, 11, 0, mBattleRound.bang)
            }
            alliances[1].personList.forEach {
                CultivationHelper.gainJiEquipment(it.value, 11, 1, mBattleRound.bang)
            }
            writeHistory("第${mBattleRound.bang}届 Bang Battle Runner: ${alliances[1].name}", null, 0)
            writeHistory("第${mBattleRound.bang}届 Bang Battle Winner: ${alliances[0].name}", null, 0)
            val message = Message.obtain()
            message.what = 8
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun roundHandler(persons: MutableList<Person>, round:Int, xiuWei:Int):Boolean{
        persons.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until persons.size step 2) {
            if (i + 1 >= persons.size) {
                break
            }
            val firstPerson = persons[i]
            val secondPerson = persons[i + 1]
            val result = CultivationBattleHelper.battlePerson(firstPerson, secondPerson, round, xiuWei)
            if(result){
                passIds.add(secondPerson.id)
            }else{
                passIds.add(firstPerson.id)
            }
        }
        return if(persons.size == 2){
            val looser = persons.find { it.id == passIds[0] }!!
            persons.removeIf { it.id == passIds[0] }
            persons.add(looser)
            true
        }else{
            persons.removeIf { passIds.contains(it.id) }
            false
        }
    }

    private fun roundBangHandler(alliance: MutableList<Alliance>, round:Int, xiuWei:Int):Boolean{
        alliance.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until alliance.size step 2){
            if( i + 1 >= alliance.size){
               break
            }
            val firstAlliance = alliance[i]
            val secondAlliance = alliance[i+1]
            val firstAlliancePersons = firstAlliance.personList.filter { it.value.type == 0 && CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()
            val secondAlliancePersons = secondAlliance.personList.filter { it.value.type == 0 && CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()

            if(firstAlliancePersons.size == 0){
                passIds.add(firstAlliance.id)
                continue
            }else if(secondAlliancePersons.size == 0){
                passIds.add(secondAlliance.id)
                continue
            }
            firstAlliancePersons.shuffle()
            secondAlliancePersons.shuffle()
            var firstIndex = 0
            var secondIndex = 0
            while (true){
                val result =  CultivationBattleHelper.battlePerson(firstAlliancePersons[firstIndex],
                        secondAlliancePersons[secondIndex], round, xiuWei)
                if(result){
                    secondIndex++
                    if(secondIndex == secondAlliancePersons.size || secondIndex == 100){
                        passIds.add(secondAlliance.id)
                        break
                    }
                }else{
                    firstIndex++
                    if(firstIndex == firstAlliancePersons.size || firstIndex == 100){
                        passIds.add(firstAlliance.id)
                        break
                    }
                }
            }
        }
        return if(alliance.size == 2){
            val looser = alliance.find { it.id == passIds[0] }!!
            alliance.removeIf { it.id == passIds[0] }
            alliance.add(looser)
            true
        }else{
            alliance.removeIf { passIds.contains(it.id) }
            false
        }
    }

    private fun roundClanHandler(clan: MutableList<Clan>, round:Int, xiuWei:Int):Boolean{
        clan.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until clan.size step 2){
            if( i + 1 >= clan.size){
                break
            }
            val firstClan = clan[i]
            val secondClan = clan[i+1]
            val firstClanPersons = firstClan.clanPersonList.filter { CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()
            val secondClanPersons = secondClan.clanPersonList.filter { CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()

            if(firstClanPersons.size == 0){
                passIds.add(firstClan.id)
                continue
            }else if(secondClanPersons.size == 0){
                passIds.add(secondClan.id)
                continue
            }
            firstClanPersons.shuffle()
            secondClanPersons.shuffle()
            var firstIndex = 0
            var secondIndex = 0
            while (true){
                val result =  CultivationBattleHelper.battlePerson(firstClanPersons[firstIndex],
                        secondClanPersons[secondIndex], round, xiuWei)
                if(result){
                    secondIndex++
                    if(secondIndex == secondClanPersons.size || secondIndex == 5){
                        passIds.add(secondClan.id)
                        break
                    }
                }else{
                    firstIndex++
                    if(firstIndex == firstClanPersons.size || firstIndex == 5){
                        passIds.add(firstClan.id)
                        break
                    }
                }
            }
        }
        return if(clan.size == 2){
            val looser = clan.find { it.id == passIds[0] }!!
            clan.removeIf { it.id == passIds[0] }
            clan.add(looser)
            true
        }else{
            clan.removeIf { passIds.contains(it.id) }
            false
        }
    }

    //暂时不用
    private fun disasterHandler(randomSize:Int = 0){
        val effectPersons = if(randomSize == 0)
            mPersons
        else
            mPersons.filter { Random().nextInt(randomSize) == 0 }

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
        if(randomSize == 0){
            writeHistory( "$description\ud83d\ude21\u706b\u0020\u6240\u6709\u4f19\u4f34\u5bff\u547d\u964d\u4f4e$level", null, 0 )
        }
        val text = "$description\ud83d\ude21\u706b\u0020\u5bff\u547d\u964d\u4f4e$level"
        effectPersons.forEach {
            it.value.lifetime -= level
            if(randomSize > 0){
                writeHistory( "${getPersonBasicString(it.value)} $text", it.value )
            }
            addPersonEvent(it.value,"${getYearString()} $text")
        }
    }

    private fun addBossHandler(){
        if(mBoss.map(Map.Entry<String, Person>::value).none { (mCurrentXun - it.lastBirthDay) / 12 < it.lifetime }){
            val boss = when (Random().nextInt(3)) {
                0 -> CultivationHelper.generateLiYuanBa(mAlliance["6000101"]!!)
                1 -> CultivationHelper.generateShadowMao(mAlliance["6000105"]!!)
                else -> CultivationHelper.generateShadowQiu(mAlliance["6000105"]!!)
            }
            mBoss[boss.id] = boss
            mBattleRound.boss[boss.type - 1]++
            writeHistory("====================================", null, 0)
            writeHistory("↑↑============================↑↑", null, 0)
            writeHistory("${boss.name}天降", boss, 0)
            writeHistory("↓↓============================↓↓", null, 0)
            writeHistory("====================================", null, 0)
        }
    }

    private fun gainTeji(person: Person, weight:Int = 1){
        CultivationHelper.createTeji(weight).forEach { t->
            if(!person.teji.contains(t)){
                person.teji.add(t)
                val commonText = "\u83b7\u5f97\u7279\u6280 : ${mConfig.teji.find { f-> f.id == t }?.name}"
                addPersonEvent(person, "${getYearString()} ${getPersonBasicString(person, false)} $commonText")
                writeHistory("${getPersonBasicString(person)} $commonText", person)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(pinyinMode)
            menuInflater.inflate(R.menu.menu_cultivation, menu)
        else
            menuInflater.inflate(R.menu.menu_cultivation_cn, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.menu_reset -> {
                resetHandler()
            }
            R.id.menu_battle_bang ->{
               battleBangHandler()
            }
            R.id.menu_battle_clan ->{
                battleClanHandler()
            }
            R.id.menu_battle_single ->{
                battleSingleHandler()
            }
            R.id.menu_event_enemy ->{
                eventEnemyHandler()
            }
            R.id.menu_add_spec ->{
                addSpecPerson()
            }
            R.id.menu_add_fixed ->{
                addFixedcPerson()
            }
            R.id.menu_enemy_list->{
               addBossHandler()
            }
            R.id.menu_grace->{
                mPersons.forEach { (_: String, u: Person) ->
                    u.xiuXei = Math.max(u.xiuXei, 0)
                }
                Toast.makeText(this, "不用谢", Toast.LENGTH_SHORT).show()
            }


        }
        return true
    }

    private val personDataString = arrayListOf("卒", "突破至", "成功率", "失败", "突破率提升至", "突破")

    companion object {

        class TimeHandler constructor(val context: CultivationActivity):Handler(){

            private val reference:WeakReference<CultivationActivity> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val activity = reference.get()!!
                if(msg.what == 1){
                    val xun = msg.obj.toString().toLong()
                    activity.xunHandler(xun)
                }else if(msg.what == 2){
                    activity.mProgressDialog.dismiss()
                    activity.setTimeLooper(true)
                    Toast.makeText(activity, "保存完成", Toast.LENGTH_SHORT).show()
                }else if(msg.what == 3){
                    activity.mProgressDialog.dismiss()
                    if(msg.obj != null){
                        Toast.makeText(activity, "读取完成", Toast.LENGTH_SHORT).show()
                        maxFemaleProfile = msg.arg1
                        maxMaleProfile = msg.arg2
                        activity.init(msg.obj.toString())
                    }else{
                        activity.init(null)
                    }
                }else if(msg.what == 4){
                    Toast.makeText(activity, "重启完成", Toast.LENGTH_SHORT).show()
                    activity.startWorld()
                }else if(msg.what == 5){
                    activity.combinedPersonRelationship(msg.obj as Person)
                    if(msg.arg1 == 0){
                        Toast.makeText(activity, "加入完成", Toast.LENGTH_SHORT).show()
                        activity.mProgressDialog.dismiss()
                        activity.setTimeLooper(true)
                    }
                }else if(msg.what == 6){
                    Toast.makeText(activity, "操作完成", Toast.LENGTH_SHORT).show()
                    activity.setTimeLooper(true)
                }else if(msg.what == 7){
                    activity.updateHistory()
                }else if(msg.what == 8){
                    Toast.makeText(activity, "Battle end", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}