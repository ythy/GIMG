package com.mx.gillustrated.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.BaseInputConnection
import android.widget.*
import butterknife.*
import com.google.gson.Gson
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.CultivationHistoryAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationHelper.addPersonEvent
import com.mx.gillustrated.component.CultivationHelper.getPersonBasicString
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.component.CultivationHelper.mCurrentXun
import com.mx.gillustrated.component.CultivationHelper.writeHistory
import com.mx.gillustrated.dialog.*
import com.mx.gillustrated.util.CultivationBakUtil
import com.mx.gillustrated.util.JsonFileReader
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.N)
class CultivationActivity : BaseActivity() {

    private var mThreadRunnable = true
    private var mHistoryThreadRunnable = true
    private var mSpeed = 10L//流失速度
    var pinyinMode:Boolean = true //是否pinyin模式
    private val mInitPersonCount = 500//初始化Person数量
    var readRecord = true
    var maxFemaleProfile = 0 // 1号保留不用
    var maxMaleProfile = 0 // 默认0号
    var mPersons:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var mAlliance:ConcurrentHashMap<String, Alliance> = ConcurrentHashMap()
    var mClans:ConcurrentHashMap<String, Clan> = ConcurrentHashMap()
    private var mEnemys:ConcurrentHashMap<String, Enemy> = ConcurrentHashMap()
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
            backupInfo.alliance = mAlliance.mapValues { it.value.toConfig() }
            backupInfo.persons = mPersons.filter { !it.value.isDead }
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



    private fun init(json:String?){
        val out:String? = if(readRecord) json else null
        if(out != null){
            val backup = Gson().fromJson(out, BakInfo::class.java)
            mCurrentXun = backup.xun
            mPersons.putAll(backup.persons)
            mAlliance.putAll(backup.alliance.mapValues {
                it.value.toAlliance(mPersons)
            })
            mClans.putAll(backup.clans.mapValues {
                it.value.toClan(mPersons)
            })
        }
        createAlliance()
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
                alliance.property = it.property
                alliance.speedG1 = it.speedG1
                alliance.speedG2 = it.speedG2
                mAlliance[it.id] = alliance
            }
        }else{
            mAlliance.forEach {
                val configAllianc = mConfig.alliance.find { f-> f.id == it.key }!!
                it.value.level = configAllianc.level
                it.value.lifetime = configAllianc.lifetime
                it.value.maxPerson = configAllianc.maxPerson
                it.value.xiuwei = configAllianc.xiuwei
                it.value.xiuweiMulti = configAllianc.xiuweiMulti
                it.value.success = configAllianc.success
                it.value.tianfu = configAllianc.tianfu
                it.value.speedG1 = configAllianc.speedG1
                it.value.speedG2 = configAllianc.speedG2
                it.value.property = configAllianc.property
            }
        }
    }

    fun getOnlinePersonDetail(id:String?):Person?{
        if(id == null)
            return null
        return mPersons[id]
    }

    fun setTimeLooper(flag:Boolean){
        if(flag){
            mBtnTime.tag = "ON"
            mBtnTime.text = "Stop"
            mThreadRunnable = true
        }else{
            mBtnTime.tag = "OFF"
            mBtnTime.text = "Run"
            mThreadRunnable = false
        }
    }

    private fun deadHandler(it:Person, currentXun:Long){
        val yongyu = personDataString
        addPersonEvent(it, "${getYearString()} ${getPersonBasicString(it, false)} ${yongyu[0]}")
        writeHistory("${getPersonBasicString(it)} ${yongyu[0]}", it)
        val alliance = mAlliance[it.allianceId]
        alliance?.personList?.remove(it.id)
        it.allianceId = ""
        it.allianceName = ""
        it.isDead = true
        synchronized(it.birthDay){
            val pair = Pair(it.birthDay.last().first, currentXun)
            it.birthDay.removeIf { it.second == 0L }
            it.birthDay.add(pair)
        }
    }

    private fun isDeadException(person:Person):Boolean{
        val matchName = "(李逍遥|阿奴)".toRegex()
        if(person.lifeTurn > 1 || person.isFav){
            return true
        }else if(matchName.find(person.name) != null){
            return true
        }
        return false
    }

    private fun xunHandler(currentXun:Long) {
        //randomEvent(fixedPerson) 暂时不启用
        CultivationHelper.updateAllianceGain(mAlliance, currentXun % 120 == 0L)
        if(currentXun % 120 == 0L) {
            updatePartnerChildren()
            updateHP()
        }
        if(currentXun % 240 == 0L) {
           CultivationHelper.updatePartner(mPersons)
        }
        updateClans(currentXun)
        updateEnemys(currentXun)

        val yongyu = personDataString
        for ((_: String, it: Person) in mPersons) {
            if (it.isDead)
                continue
            var totalAgeXun = 0L
            synchronized(it.birthDay){
                it.birthDay.forEach {
                    totalAgeXun += if (it.second == 0L) {
                        currentXun - it.first
                    } else {
                        it.second - it.first
                    }
                }
            }
            it.age = totalAgeXun / 12
            if (it.age > it.lifetime ) {
                if(isDeadException(it)){
                    it.lifetime += 5000
                }else{
                    deadHandler(it, currentXun)
                    continue
                }
            }
            val currentJinJie = CultivationHelper.getJingJie(it.jingJieId)
            it.jinJieName = CultivationHelper.getJinJieName(currentJinJie.name, pinyinMode)
            it.lingGenName = if (it.lingGenId == "") it.lingGenName else CultivationHelper.getTianName(it.lingGenId)
            if (it.gender == NameUtil.Gender.Female && it.profile == 0 && maxFemaleProfile > 1) {
                it.profile = Random().nextInt(maxFemaleProfile - 1) + 2
            }
            if (it.gender == NameUtil.Gender.Male && it.profile == 0 && maxMaleProfile > 2) {
                it.profile = Random().nextInt(maxMaleProfile - 2) + 2
            }
            var currentXiuwei = it.xiuXei
            val xiuweiGrow = CultivationHelper.getXiuweiGrow(it)
            it.maxXiuWei += xiuweiGrow
            currentXiuwei += xiuweiGrow
            if (currentXiuwei < currentJinJie.max) {
                it.xiuXei = currentXiuwei
                continue
            }
            val next = CultivationHelper.getNextJingJie(it.jingJieId)
            it.xiuXei -= currentJinJie.max
            val totalSuccess = CultivationHelper.getTotalSuccess(it, currentJinJie.bonus)
            val random = Random().nextInt(100)
            if (random <= totalSuccess) {//成功
                if (next != null) {
                    val commonText = "${yongyu[1]} ${CultivationHelper.getJinJieName(next.name, pinyinMode)}，${yongyu[2]} $random/$totalSuccess"
                    val lastJingJieDigt = CultivationHelper.getJingJieLevel(it.jingJieId)
                    if (it.isFav || (lastJingJieDigt.first >= 0 && lastJingJieDigt.third == 4)) {
                        writeHistory("${getPersonBasicString(it)} $commonText", it)
                    }
                    if (lastJingJieDigt.first >= 0 && lastJingJieDigt.third == 4) {
                        addPersonEvent(it, "${getYearString()} ${getPersonBasicString(it, false)} $commonText")
                    }
                    it.jingJieId = next.id
                    it.jingJieSuccess = next.success
                    it.jinJieColor = next.color
                    it.jinJieMax = next.max
                    val allianceNow = mAlliance[it.allianceId]
                    it.lifetime += next.lifetime * (100 + (allianceNow?.lifetime ?: 0)) / 100
                } else {
                    val commonText = "转转成功，${yongyu[2]} $random/$totalSuccess"
                    addPersonEvent(it, "${getYearString()} ${getPersonBasicString(it, false)} $commonText")
                    writeHistory("${getPersonBasicString(it)} $commonText", it)
                    it.jingJieId = mConfig.jingJieType[0].id
                    it.jingJieSuccess = mConfig.jingJieType[0].success
                    it.jinJieColor = mConfig.jingJieType[0].color
                    it.jinJieMax = mConfig.jingJieType[0].max
                    it.lifeTurn += 1
                    it.lifetime = it.age + it.lifeTurn * 1000
                }
            } else {
                val commonText = if (next != null)
                    "${yongyu[5]} ${CultivationHelper.getJinJieName(next.name, pinyinMode)} ${yongyu[3]} $random/$totalSuccess，${yongyu[4]} ${totalSuccess + currentJinJie.fault}%"
                else
                    "转转失败 ${yongyu[3]} $random/$totalSuccess，${yongyu[4]} ${totalSuccess + currentJinJie.fault}%"
                if (it.isFav) {
                    writeHistory("${getPersonBasicString(it)} $commonText", it)
                }
                it.jingJieSuccess += currentJinJie.fault
            }
        }
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
        if(mHistoryData.size > 1000)
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

    fun getYearString(xun:Long = mCurrentXun):String{
       return "${xun / 12}年"
    }

    private fun addMultiPerson(count:Int){
        setTimeLooper(false)
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

//    private fun addMultiPerson(count:Int){
//        setTimeLooper(false)
//        mProgressDialog.show()
//        var tempCount = count
//        Thread(Runnable {
//            Log.d("TTTTTTTTTTTTY", "1" )
//            Thread.sleep(500)
//            Log.d("TTTTTTTTTTTTZ", "2" )
//            while(tempCount-- > 0){
//                Log.d("TTTTTTTTTTTT1", "$tempCount" )
//                Thread(Runnable {
//
//                }).start()
//                Log.d("TTTTTTTTTTTT4", "$tempCount" )
//            }
//        }).start()
//    }
//
//    class AddPersonRunnable constructor(val count:Int, val handler:TimeHandler):Runnable {
//
//        override fun run() {
//            Log.d("TTTTTTTTTTTT2", "$count" )
//            //addPersion(null, null)
//            val message = Message.obtain()
//            message.what = 5
//            message.arg1 = count
//            handler.sendMessage(message)
//            Log.d("TTTTTTTTTTTT3", "$count" )
//        }
//
//    }

    private fun addPersion(fixedName:Pair<String, String?>?, fixedGender:NameUtil.Gender?,
                           lifetime: Long = 100, parent: Pair<Person, Person>? = null, fav:Boolean = false):Person{
        val person = CultivationHelper.getPersonInfo(fixedName, fixedGender, lifetime, parent, fav)
        mPersons[person.id] = person
        CultivationHelper.joinAlliance(person, mAlliance)
        addPersonEvent(person,"${getYearString()} ${getPersonBasicString(person, false)} 加入")
        writeHistory("${getPersonBasicString(person)} 加入", person)
        return person
    }

    fun bePerson(){
        setTimeLooper(false)
        Thread(Runnable {
            Thread.sleep(500)
            mPersons.forEach {
                val person = it.value
                if(person.isDead){
                    person.partnerName = null
                    person.partner = null
                }else{
                    val partner = getOnlinePersonDetail(person.partner)
                    if(person.partner != null && (partner == null || partner.isDead)){
                        person.partnerName = null
                        person.partner = null
                    }
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
            val person = mPersons[id]
            if(person != null && !person.isDead){
                person.isDead = false
                person.lifetime += 5000L
                person.birthDay.add(Pair(mCurrentXun, 0))
                val commonText = " 复活，寿命增加5000"
                addPersonEvent(person,"${getYearString()} ${getPersonBasicString(person, false)} $commonText")
                writeHistory("${getPersonBasicString(person)} $commonText", person)
                CultivationHelper.joinAlliance(person, mAlliance)
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
        val females = mPersons.filter { !it.value.isDead && it.value.gender == NameUtil.Gender.Female }.map { it.value }.toMutableList()
        for(i in 0 until females.size) {
            val it = females[i]
            if(it.partner != null){
                val partner = getOnlinePersonDetail(it.partner)
                val children = it.children.filter { c-> getOnlinePersonDetail(c) != null }
                val baseNumber = if(children.isEmpty()) 10L else Math.pow((children.size * 2).toDouble(), 5.0).toLong()
                if(partner != null && baseNumber < Int.MAX_VALUE){
                    if(Random().nextInt(baseNumber.toInt()) == 0){
                        val child = addPersion(Pair(partner.lastName, null), null, 100,
                                Pair(partner, it))
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
            if(xun - it.birthDay >= it.lifetime){
                writeHistory("${it.name} 消失", null, 0)
                it.isDead = true
            }else{
                if(random.nextInt(it.attackFrequency) == 0){
                    val persons = mPersons.filter { f->!f.value.isDead }
                    val keys = persons.keys
                    val person = persons[keys.elementAt(random.nextInt(keys.size))]
                    if(person != null){
                        val result = CultivationHelper.battleEnemy(person, it, it.HP * 1000)
                        if(result){
                            writeHistory("${it.name} 消失", null, 0)
                            it.isDead = true
                        }
                    }
                }
            }
        }
    }

    private fun updateClans(xun:Long){
        if(xun % 480 == 0L) {
            mPersons.filter { !it.value.isDead && it.value.ancestorId != null }.map { it.value }.toMutableList()
                    .groupBy { it.ancestorId }.forEach { (t, u) ->
                if (u.size >= 5) {
                    val clanOld = mClans[t]
                    if (clanOld == null) {
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
                }
                it.value.clanPersonList.forEach { (t: String, u: Person) ->
                    if(u.isDead)
                        it.value.clanPersonList.remove(t)
                }
                mPersons.filter { p->p.value.ancestorId == it.value.id }.forEach { f->
                    val person = f.value
                    if(!it.value.clanPersonList.contains(person.id)){
                        it.value.clanPersonList[person.id] = person
                    }
                }
            }
        }
    }

    // update every 10 years
    private fun updateHP(){
        for ((_: String, it: Person) in mPersons) {
            if(it.isDead || it.HP < it.maxHP )
                continue
            if(CultivationHelper.getProperty(it)[0] < -10){
                val count = Math.abs(CultivationHelper.getProperty(it)[0])
                it.HP = Math.min(it.maxHP, it.HP + count)
                it.lifetime -= count
            }else{
                it.HP++
            }
        }
    }

    private fun eventEnemyHandler(){
        val enemy = Enemy()
        val random = Random()
        if(random.nextInt(2) == 0){
            enemy.id = UUID.randomUUID().toString()
            enemy.name = "${CultivationHelper.EnemyNames[0]}${random.nextInt(10001)}号"
            enemy.birthDay = mCurrentXun
            enemy.HP = 10 + 10 * random.nextInt(100)// max 1000
            enemy.maxHP = enemy.HP
            enemy.attack = 110 + 10 * random.nextInt(50) // max 600
            enemy.defence = 10 + 10 * random.nextInt(10) // max 100
            enemy.speed = 500 / enemy.defence
            enemy.attackFrequency = 10 + 10 * random.nextInt(10) // max 100
            enemy.lifetime = 1000L + 1000 * random.nextInt(10) // max 10000
            mEnemys[enemy.id] = enemy
            writeHistory("${enemy.name}天降 - (${enemy.HP}/${enemy.lifetime/12})${enemy.attack}-${enemy.defence}-${enemy.speed}", null, 0)
        }else{
            enemy.id = UUID.randomUUID().toString()
            enemy.name = "${CultivationHelper.EnemyNames[1]}${random.nextInt(10001)}号"
            enemy.birthDay = mCurrentXun
            enemy.HP = 10 + 10 * random.nextInt(50)// max 500
            enemy.maxHP = enemy.HP
            enemy.attack = 10 + 10 * random.nextInt(20) // max 200
            enemy.defence = 10 + 5 * random.nextInt(5) // max 30
            enemy.speed = 300 / enemy.defence
            enemy.attackFrequency = 10 + 10 * random.nextInt(10) // max 100
            enemy.lifetime = 1000L + 1000 * random.nextInt(10) // max 10000
            mEnemys[enemy.id] = enemy
            writeHistory("${enemy.name}天降 - (${enemy.HP}/${enemy.lifetime/12})${enemy.attack}-${enemy.defence}-${enemy.speed}", null, 0)
        }
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
            mPersons.clear()
            mAlliance.clear()
            createAlliance()
            val message = Message.obtain()
            message.what = 4
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleSingleHandler(){
        val random = Random()
        val persons = mPersons.filter { !it.value.isDead && random.nextInt(2) == 0 && CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()
        if(persons.isEmpty() || persons.size < 10){
            Toast.makeText(this, "persons less than 10", Toast.LENGTH_SHORT).show()
            return
        }
        setTimeLooper(false)
        mHistoryData.clear()
        CultivationHelper.mHistoryTempData.clear()
        (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
        mHistory.invalidateViews()
        Thread(Runnable {
            Thread.sleep(500)
            writeHistory("Single Battle Start", null, 0)
            var roundNumber = 1
            while (persons.size > 1){
                writeHistory("Single Battle ${roundNumber}轮 Start", null, 0)
                roundNumber++
                roundHandler(persons, 20, 200000)
            }
            persons[0].xiuXei += 200000
            writeHistory("Single Battle Winner: ${persons[0].allianceName} - ${persons[0].name}", persons[0])
            addPersonEvent(persons[0],"${getYearString()} ${getPersonBasicString(persons[0], false)} Single Battle Winner")
            val message = Message.obtain()
            message.what = 8
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleClanHandler(){
        val clans = mClans.filter { it.value.clanPersonList.size > 4 }.map { it.value }.toMutableList()
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
            writeHistory("Clan Battle Start", null, 0)
            var roundNumber = 1
            while (clans.size > 1){
                writeHistory("Clan Battle ${roundNumber}轮 Start", null, 0)
                roundNumber++
                roundClanHandler(clans, 10, 80000)
            }
            clans[0].clanPersonList.forEach {
                it.value.xiuXei += 200000
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
            writeHistory("Bang Battle Start", null, 0)
            var roundNumber = 1
            while (alliances.size > 1){
                writeHistory("Bang Battle ${roundNumber}轮 Start", null, 0)
                roundNumber++
                roundBangHandler(alliances, 20, 200000)
            }
            writeHistory("Bang Battle Winner: ${alliances[0].name}", null, 0)
            val message = Message.obtain()
            message.what = 8
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun roundHandler(persons: MutableList<Person>, round:Int, xiuWei:Int){
        persons.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until persons.size step 2) {
            if (i + 1 >= persons.size) {
                break
            }
            val firstPerson = persons[i]
            val secondPerson = persons[i + 1]
            val result = CultivationHelper.battle(firstPerson, secondPerson, round, xiuWei)
            if(result){
                passIds.add(secondPerson.id)
            }else{
                passIds.add(firstPerson.id)
            }
        }
        persons.removeIf { passIds.contains(it.id) }
    }

    private fun roundBangHandler(alliance: MutableList<Alliance>, round:Int, xiuWei:Int){
        alliance.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until alliance.size step 2){
            if( i + 1 >= alliance.size){
               break
            }
            val firstAlliance = alliance[i]
            val secondAlliance = alliance[i+1]
            val firstAlliancePersons = firstAlliance.personList.filter { !it.value.isDead && CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()
            val secondAlliancePersons = secondAlliance.personList.filter { !it.value.isDead && CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()

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
                val result = CultivationHelper.battle(firstAlliancePersons[firstIndex],
                        secondAlliancePersons[secondIndex], round, xiuWei)
                if(result){
                    secondIndex++
                    if(secondIndex == secondAlliancePersons.size || secondIndex == 20){
                        passIds.add(secondAlliance.id)
                        break
                    }
                }else{
                    firstIndex++
                    if(firstIndex == firstAlliancePersons.size || firstIndex == 20){
                        passIds.add(firstAlliance.id)
                        break
                    }
                }
            }
        }
        alliance.removeIf { passIds.contains(it.id) }
    }

    private fun roundClanHandler(clan: MutableList<Clan>, round:Int, xiuWei:Int){
        clan.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until clan.size step 2){
            if( i + 1 >= clan.size){
                break
            }
            val firstClan = clan[i]
            val secondClan = clan[i+1]
            val firstClanPersons = firstClan.clanPersonList.filter { !it.value.isDead && CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()
            val secondClanPersons = secondClan.clanPersonList.filter { !it.value.isDead && CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()

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
                val result = CultivationHelper.battle(firstClanPersons[firstIndex],
                        secondClanPersons[secondIndex], round, xiuWei)
                if(result){
                    secondIndex++
                    if(secondIndex == secondClanPersons.size || secondIndex == 20){
                        passIds.add(secondClan.id)
                        break
                    }
                }else{
                    firstIndex++
                    if(firstIndex == firstClanPersons.size || firstIndex == 20){
                        passIds.add(firstClan.id)
                        break
                    }
                }
            }
        }
        clan.removeIf { passIds.contains(it.id) }
    }

    private fun disasterHandler(randomSize:Int = 0){
        val effectPersons = if(randomSize == 0)
            mPersons.filter { !it.value.isDead }
        else
            mPersons.filter { !it.value.isDead && Random().nextInt(randomSize) == 0 }

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
            writeHistory( "${description}😡火 所有伙伴寿命降低$level", null, 0 )
        }
        val text = "${description}😡火 寿命降低$level"
        effectPersons.forEach {
            it.value.lifetime -= level
            if(randomSize > 0){
                writeHistory( "${getPersonBasicString(it.value)} $text", it.value )
            }
            addPersonEvent(it.value,"${getYearString()} $text")
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
                disasterHandler(10)
            }
            R.id.menu_disaster_all -> {
                disasterHandler()
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
                    activity.mDate.text = "$xun 旬 - ${activity.getYearString(xun)}"
                    activity.xunHandler(xun)
                }else if(msg.what == 2){
                    activity.mProgressDialog.dismiss()
                    activity.setTimeLooper(true)
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
                    //Log.d("TTTTTTTTTTTT5", "${msg.arg1}" )
                   // if(msg.arg1 == -1){
                        Toast.makeText(activity, "加入完成", Toast.LENGTH_SHORT).show()
                        activity.mProgressDialog.dismiss()
                        activity.setTimeLooper(true)
                  //  }
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