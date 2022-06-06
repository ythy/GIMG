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

@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.N)
class CultivationActivity : BaseActivity() {

    private var mThreadRunnable = true
    private var mHistoryThreadRunnable = true
    var mSpeed = 10L//流失速度
    var pinyinMode:Boolean = true //是否pinyin模式
    private val mInitPersonCount = 500//初始化Person数量
    var readRecord = true
    var maxFemaleProfile = 0 // 1号保留不用
    var maxMaleProfile = 0 // 默认0号
    var mPersons:MutableList<Person> = Collections.synchronizedList(mutableListOf())
    var mAlliance:MutableList<Alliance> =  Collections.synchronizedList(mutableListOf())
    var mClans:MutableList<Clan> =  Collections.synchronizedList(mutableListOf())
    var mEnemys:MutableList<Enemy> =  Collections.synchronizedList(mutableListOf())
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
            backupInfo.alliance = mAlliance.map { it.toConfig() }
            backupInfo.persons = mPersons.filter { !it.isDead }
            backupInfo.clans = mClans.map { it.toClanBak() }.filter { it.persons.size > 1 }
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
        Log.d("RRRRRRR", "${mCurrentXun} - ${mThreadRunnable}")
        if(mCurrentXun > 0){
            setTimeLooper(true)
        }
    }



    private fun init(json:String?){
        val out:String? = if(readRecord) json else null
        if(out != null){
            val backup = Gson().fromJson(out, BakInfo::class.java)
            mCurrentXun = backup.xun
            mPersons.addAll(backup.persons)
            mAlliance.addAll(backup.alliance.map {
                it.toAlliance(mPersons)
            })
            mClans.addAll(backup.clans.map {
                it.toClan(mPersons)
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
        val li = addPersion(Pair("李", "逍遥"), NameUtil.Gender.Male, 100000)
        val nu = addPersion(Pair("阿", "奴"), NameUtil.Gender.Female, 100000)
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
                it.property = configAllianc.property
            }
        }
    }

    fun getOnlinePersonDetail(id:String?):Person?{
        if(id == null)
            return null
        return mPersons.find { it.id == id }
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

    private fun xunHandler(currentXun:Int) {
        //randomEvent(fixedPerson) 暂时不启用
        CultivationHelper.updateAllianceGain(mAlliance, currentXun % 120 == 0)
        if(currentXun % 120 == 0) {
            updatePartnerChildren()
            updateHP()
        }
        if(currentXun % 240 == 0) {
           CultivationHelper.updatePartner(mPersons)
        }
        if(currentXun % 480 == 0) {
            updateClans(currentXun)
        }
        updateEnemys(currentXun)

        val yongyu = personDataString
        for (i in 0 until mPersons.size) {
            val it = mPersons[i]
            // executorService.execute {
            if (it.isDead)
                continue
            var totalAgeXun = 0
            it.birthDay.forEach {
                totalAgeXun += if (it.second == 0) {
                    currentXun - it.first
                } else {
                    it.second - it.first
                }
            }
            it.age = totalAgeXun / 12
            if (it.age > it.lifetime) {
                addPersonEvent(it, "${getYearString()} ${getPersonBasicString(it, false)} ${yongyu[0]}")
                writeHistory("${getPersonBasicString(it)} ${yongyu[0]}", it)
                it.allianceId = ""
                it.allianceName = ""
                it.isDead = true
                val pair = Pair(it.birthDay.last().first, currentXun)
                it.birthDay.removeIf { p -> p.second == 0 }
                it.birthDay.add(pair)
                continue
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
                    addPersonEvent(it, "${getYearString()} ${getPersonBasicString(it, false)} $commonText")
                    val lastJingJieDigt = CultivationHelper.getJingJieLevel(it.jingJieId)
                    if (it.isFav || (lastJingJieDigt.first >= 0 && lastJingJieDigt.third == 4)) {
                        writeHistory("${getPersonBasicString(it)} $commonText", it)
                    }
                    it.jingJieId = next.id
                    it.jingJieSuccess = next.success
                    it.jinJieColor = next.color
                    it.jinJieMax = next.max
                    val allianceNow = mAlliance.find { a -> a.id == it.allianceId }
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
                    message.arg1 = mCurrentXun
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

    fun getYearString(xun:Int = mCurrentXun):String{
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

    private fun addPersion(fixedName:Pair<String, String?>?, fixedGender:NameUtil.Gender?, lifetime: Int = 100, parent: Pair<Person, Person>? = null):Person{
        val person = CultivationHelper.getPersonInfo(fixedName, fixedGender, lifetime, parent)
        mPersons.add(person)
        joinClans(person)
        CultivationHelper.joinAlliance(person, mAlliance)
        addPersonEvent(person,"${getYearString()} ${getPersonBasicString(person, false)} 加入")
        writeHistory("${getPersonBasicString(person)} 加入", person)
        return person
    }

    fun bePerson(){
        setTimeLooper(false)
        Thread(Runnable {
            Thread.sleep(500)
            mPersons.forEach { person ->
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
            val person = mPersons.find { it.id == id && it.isDead }
            if(person != null){
                person.isDead = false
                person.lifetime += 5000
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

    fun killPerson(id:String):Boolean{
        val person = mPersons.find { !it.isDead && it.id == id }
        if(person != null){
            person.lifetime = person.age
            return true
        }
        return false
    }

    fun addPersonLifetime(id:String):Boolean{
        val person = mPersons.find { !it.isDead && it.id == id }
        if(person != null){
            person.lifetime += 5000
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
        val females = mPersons.filter { !it.isDead && it.gender == NameUtil.Gender.Female }
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
                        it.children.add(child.id)
                        partner.children.add(child.id)
                    }
                }
            }
        }
    }

    private fun updateEnemys(xun:Int){
        val random = Random()
        mEnemys.filter { !it.isDead }.forEach {
            if(xun - it.birthDay >= it.lifetime){
                writeHistory("${it.name} 消失", null, 0)
                it.isDead = true
            }else{
                if(random.nextInt(it.attackFrequency) == 0){
                    val persons = mPersons.filter { f->!f.isDead }
                    val person = persons[random.nextInt(persons.size)]
                    val result = CultivationHelper.battleEnemy(person, it, it.HP * 1000)
                    if(result){
                        writeHistory("${it.name} 消失", null, 0)
                        it.isDead = true
                    }
                }
            }
        }
    }

    private fun joinClans(person:Person){
        val myClan = mClans.find { it.id == person.ancestorId }
        myClan?.clanPersonList?.add(person)
    }

    private fun updateClans(xun:Int){
        mPersons.filter { !it.isDead && it.ancestorId != null }.groupBy { it.ancestorId }.forEach { (t, u) ->
            if(u.size >= 5){
                val clanOld = mClans.find { it.id == t }
                if(clanOld == null) {
                    val clan = Clan()
                    clan.id = t!!
                    clan.name = u[0].lastName
                    clan.createDate = xun
                    clan.clanPersonList.addAll(u)
                    mClans.add(clan)
                }
            }
        }
    }

    // update every 10 years
    private fun updateHP(){
        val persons = mPersons.filter { !it.isDead && it.HP < it.maxHP }
        persons.forEach {
            if(CultivationHelper.getProperty(it)[0] < -10){
                val count = Math.abs(CultivationHelper.getProperty(it)[0])
                it.HP += count
                it.lifetime -=  Math.min(100, count)
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
            enemy.name = "远古${random.nextInt(10001)}号"
            enemy.birthDay = mCurrentXun
            enemy.HP = 10 + 10 * random.nextInt(100)// max 1000
            enemy.maxHP = enemy.HP
            enemy.attack = 110 + 10 * random.nextInt(50) // max 600
            enemy.defence = 10 + 10 * random.nextInt(10) // max 100
            enemy.speed = 500 / enemy.defence
            enemy.attackFrequency = 10 + 10 * random.nextInt(10) // max 100
            enemy.lifetime = 1000 + 1000 * random.nextInt(10) // max 10000
            mEnemys.add(enemy)
            writeHistory("${enemy.name} 天降 - (${enemy.HP}/${enemy.lifetime/12})${enemy.attack}-${enemy.defence}-${enemy.speed}", null, 0)
        }else{
            enemy.id = UUID.randomUUID().toString()
            enemy.name = "菜菜${random.nextInt(10001)}号"
            enemy.birthDay = mCurrentXun
            enemy.HP = 10 + 10 * random.nextInt(50)// max 500
            enemy.maxHP = enemy.HP
            enemy.attack = 10 + 10 * random.nextInt(20) // max 200
            enemy.defence = 10 + 5 * random.nextInt(5) // max 30
            enemy.speed = 300 / enemy.defence
            enemy.attackFrequency = 10 + 10 * random.nextInt(10) // max 100
            enemy.lifetime = 1000 + 1000 * random.nextInt(10) // max 10000
            mEnemys.add(enemy)
            writeHistory("${enemy.name} 天降 - (${enemy.HP}/${enemy.lifetime/12})${enemy.attack}-${enemy.defence}-${enemy.speed}", null, 0)
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
        val persons = mutableListOf<Person>()
        val random = Random()
        persons.addAll(mPersons.filter { !it.isDead && random.nextInt(2) == 0 && CultivationHelper.getProperty(it)[0] > 0 })
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
            var restPersons = persons.toMutableList()
            var roundNumber = 1
            while (restPersons.size > 1){
                writeHistory("Single Battle ${roundNumber}轮 Start", null, 0)
                roundNumber++

                restPersons = roundHandler(restPersons, 20, 200000)
            }
            restPersons[0].xiuXei += 200000
            writeHistory("Single Battle Winner: ${restPersons[0].allianceName} - ${restPersons[0].name}", restPersons[0])
            addPersonEvent(restPersons[0],"${getYearString()} ${getPersonBasicString(restPersons[0], false)} Single Battle Winner")
            val message = Message.obtain()
            message.what = 8
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleClanHandler(){
        val clans = mClans.filter { it.clanPersonList.filter { c->!c.isDead }.size > 4 }
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
            var restClans = clans.toMutableList()
            var roundNumber = 1
            while (restClans.size > 1){
                writeHistory("Clan Battle ${roundNumber}轮 Start", null, 0)
                roundNumber++
                restClans = roundClanHandler(restClans, 10, 80000)
            }
            restClans[0].clanPersonList.forEach {
                if(!it.isDead){
                    it.xiuXei += 200000
                }
            }
            writeHistory("Clan Battle Winner: ${restClans[0].name}", null, 0)
            val message = Message.obtain()
            message.what = 8
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleBangHandler(){
        val alliances = mAlliance.filter { it.personList.isNotEmpty() }
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
            var restAlliances = alliances.toMutableList()
            var roundNumber = 1
            while (restAlliances.size > 1){
                writeHistory("Bang Battle ${roundNumber}轮 Start", null, 0)
                roundNumber++
                restAlliances = roundBangHandler(restAlliances, 20, 200000)
            }
            writeHistory("Bang Battle Winner: ${restAlliances[0].name}", null, 0)
            val message = Message.obtain()
            message.what = 8
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun roundHandler(persons: MutableList<Person>, round:Int, xiuWei:Int):MutableList<Person>{
        val restPersons = mutableListOf<Person>()
        val currentPersons = persons.toMutableList()
        while (true){
            if(currentPersons.size > 1){
                val first = Random().nextInt(currentPersons.size)
                val firstPerson = currentPersons[first]
                currentPersons.removeAt(first)
                val second = Random().nextInt(currentPersons.size)
                val secondPerson = currentPersons[second]
                currentPersons.removeAt(second)

                val result = CultivationHelper.battle(firstPerson, secondPerson, round, xiuWei)
                if(result){
                    restPersons.add(firstPerson)
                }else{
                    restPersons.add(secondPerson)
                }
            }else{
                restPersons.addAll(currentPersons)
                break
            }
        }
        return restPersons
    }

    private fun roundBangHandler(alliance: MutableList<Alliance>, round:Int, xiuWei:Int):MutableList<Alliance>{
        val restAlliance = mutableListOf<Alliance>()
        val currentAlliance = alliance.toMutableList()
        while (true){
            if(currentAlliance.size > 1){
                val first = Random().nextInt(currentAlliance.size)
                val firstAlliance = currentAlliance[first]
                currentAlliance.removeAt(first)
                val second = Random().nextInt(currentAlliance.size)
                val secondAlliance = currentAlliance[second]
                currentAlliance.removeAt(second)

                val firstAlliancePersons = firstAlliance.personList.filter { !it.isDead && CultivationHelper.getProperty(it)[0] > 0 }.toMutableList()
                val secondAlliancePersons = secondAlliance.personList.filter { !it.isDead && CultivationHelper.getProperty(it)[0] > 0 }.toMutableList()

                if(firstAlliancePersons.size == 0 || secondAlliancePersons.size == 0){
                    if(firstAlliancePersons.size > 0)
                        restAlliance.add(firstAlliance)
                    if(secondAlliancePersons.size > 0)
                        restAlliance.add(secondAlliance)
                    break
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
                        if(secondIndex == secondAlliancePersons.size || secondIndex == 10){
                            restAlliance.add(firstAlliance)
                            break
                        }
                    }else{
                        firstIndex++
                        if(firstIndex == firstAlliancePersons.size || firstIndex == 10){
                            restAlliance.add(secondAlliance)
                            break
                        }
                    }
                }
            }else{
                restAlliance.addAll(currentAlliance)
                break
            }
        }
        return restAlliance
    }

    private fun roundClanHandler(clan: MutableList<Clan>, round:Int, xiuWei:Int):MutableList<Clan>{
        val restClan = mutableListOf<Clan>()
        val currentClan = clan.toMutableList()
        while (true){
            if(currentClan.size > 1){
                val first = Random().nextInt(currentClan.size)
                val firstClan = currentClan[first]
                currentClan.removeAt(first)
                val second = Random().nextInt(currentClan.size)
                val secondClan = currentClan[second]
                currentClan.removeAt(second)

                val firstClanPersons = firstClan.clanPersonList.filter { !it.isDead && CultivationHelper.getProperty(it)[0] > 0 }.toMutableList()
                val secondClanPersons = secondClan.clanPersonList.filter { !it.isDead && CultivationHelper.getProperty(it)[0] > 0 }.toMutableList()

                if(firstClanPersons.size == 0 || secondClanPersons.size == 0){
                    if(firstClanPersons.size > 0)
                        restClan.add(firstClan)
                    if(secondClanPersons.size > 0)
                        restClan.add(secondClan)
                    break
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
                        if(secondIndex == secondClanPersons.size || secondIndex == 5){
                            restClan.add(firstClan)
                            break
                        }
                    }else{
                        firstIndex++
                        if(firstIndex == firstClanPersons.size || firstIndex == 5){
                            restClan.add(secondClan)
                            break
                        }
                    }
                }
            }else{
                restClan.addAll(currentClan)
                break
            }
        }
        return restClan
    }

    private fun disasterHandler(randomSize:Int = 0){
        val effectPersons = if(randomSize == 0)
            mPersons.filter { !it.isDead }
        else
            mPersons.filter { !it.isDead && Random().nextInt(randomSize) == 0 }

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
            it.lifetime -= level
            if(randomSize > 0){
                writeHistory( "${getPersonBasicString(it)} $text", it )
            }
            addPersonEvent(it,"${getYearString()} $text")
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
                    activity.mDate.text = "${msg.arg1} 旬 - ${activity.getYearString(msg.arg1)}"
                    activity.xunHandler(msg.arg1)
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
                    Toast.makeText(activity, "加入完成", Toast.LENGTH_SHORT).show()
                    activity.mProgressDialog.dismiss()
                    activity.setTimeLooper(true)
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