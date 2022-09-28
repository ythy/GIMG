package com.mx.gillustrated.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
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
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import butterknife.*
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.CultivationHistoryAdapter
import com.mx.gillustrated.component.CultivationBattleHelper
import com.mx.gillustrated.component.CultivationEnemyHelper
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.SpecPersonFirstName
import com.mx.gillustrated.component.CultivationSetting.SpecPersonFirstName3
import com.mx.gillustrated.component.CultivationHelper.addPersonEvent
import com.mx.gillustrated.component.CultivationHelper.getPersonBasicString
import com.mx.gillustrated.component.CultivationHelper.mBattleRound
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.component.CultivationHelper.mCurrentXun
import com.mx.gillustrated.component.CultivationHelper.writeHistory
import com.mx.gillustrated.component.CultivationHelper.isTrigger
import com.mx.gillustrated.component.CultivationHelper.pinyinMode
import com.mx.gillustrated.component.CultivationHelper.maxFemaleProfile
import com.mx.gillustrated.component.CultivationHelper.maxMaleProfile
import com.mx.gillustrated.component.CultivationHelper.showing
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.CultivationSetting.SpecPersonFirstName4
import com.mx.gillustrated.component.CultivationSetting.getIdentityGender
import com.mx.gillustrated.component.CultivationSetting.getIdentityIndex
import com.mx.gillustrated.component.CultivationSetting.createIdentitySeq
import com.mx.gillustrated.component.CultivationSetting.PresetInfo
import com.mx.gillustrated.component.CultivationSetting.SpecPersonFirstNameWeight
import com.mx.gillustrated.dialog.*
import com.mx.gillustrated.service.StopService
import com.mx.gillustrated.util.CultivationBakUtil
import com.mx.gillustrated.util.JsonFileReader
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.*
import java.io.IOException
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.*


@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.N)
class CultivationActivity : BaseActivity() {



    private var mThreadRunnable = true
    private var mHistoryThreadRunnable = true
    private var isStop = false//
    private var mSpeed = 10L//流失速度
    private val mInitPersonCount = 1000//初始化Person数量
    private var readRecord = true

    var mPersons:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var mDeadPersons:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var mAlliance:ConcurrentHashMap<String, Alliance> = ConcurrentHashMap()
    var mClans:ConcurrentHashMap<String, Clan> = ConcurrentHashMap()
    var mNations:ConcurrentHashMap<String, Nation> = ConcurrentHashMap()

    private var mEnemys:ConcurrentHashMap<String, Enemy> = ConcurrentHashMap()
    private var mBoss:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    private var mHistoryData = mutableListOf<CultivationSetting.HistoryInfo>()
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

    @BindView(R.id.navigation_cultivation)
    lateinit var mNavigation:NavigationView

    @BindView(R.id.navigation_cultivation_end)
    lateinit var mNavigationEnd:NavigationView

    @BindView(R.id.drawer_layout)
    lateinit var mDrawer:DrawerLayout

    @OnClick(R.id.btn_save)
    fun onSaveClickHandler(){
        saveAllData()
    }

    private fun saveAllData(setBusyProgression:Boolean = true){
        if(setBusyProgression){
            mProgressDialog.show()
        }
        setTimeLooper(false)
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
            mClans.map { it.key }.toMutableList().forEach { id ->
                if(mPersons[id] == null){
                    mClans.remove(id)
                }
            }
            backupInfo.clans = mClans.mapValues { it.value.toClanBak() }
            backupInfo.nation = mNations.mapValues {
                it.value.toNationBak()
            }
            CultivationBakUtil.saveDataToFiles(Gson().toJson(backupInfo))
            val message = Message.obtain()
            message.what = 2
            message.arg1 = if(setBusyProgression) 0 else 1
            mTimeHandler.sendMessage(message)
        }).start()
    }

    @OnClick(R.id.btn_nation)
    fun onNationClickHandler(){
        val ft = supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogNationList.newInstance()
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_nation_list")
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
        }else if(row.type == 2 && CultivationBattleHelper.mBattles[row.battleId] != null){
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
        if(Build.getSerial().contains("EMULATOR")){ //判断是否是模拟器
            pinyinMode = true
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
        if(!isFinishing){
            isStop = true
            val serviceIntent = Intent(this, StopService::class.java)
            this.startForegroundService(serviceIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        isStop = false
        val serviceIntent = Intent(this, StopService::class.java)
        stopService(serviceIntent)
    }


    override fun onDestroy() {
        super.onDestroy()
        mThreadRunnable = false
        mHistoryThreadRunnable = false
        isStop = false
    }

    private fun temp(){
//        SpecPersonFirstName3.forEach { p->
//            val person = mPersons.map { it.value }.find { it.specIdentity == p.identity }
//            if(person != null){
//                CultivationHelper.updatePersonInborn(person, p.tianfuWeight, p.linggenWeight)
//            }
//        }
//        SpecPersonFirstName4.forEach { p->
//            val person = mPersons.map { it.value }.find { it.specIdentity == p.identity }
//            if(person != null){
//                CultivationHelper.updatePersonInborn(person, p.tianfuWeight, p.linggenWeight)
//            }
//        }
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
                it.value.equipmentList = Collections.synchronizedList(it.value.equipmentList)
                CultivationHelper.updatePersonEquipment(it.value)
                CultivationHelper.updatePersonExtraProperty(it.value)
                it.value.followerList = Collections.synchronizedList(it.value.followerList)
                if(it.value.careerList.isEmpty()){
                    it.value.careerList = Collections.synchronizedList(CultivationHelper.getCareer().map { c->Triple(c, 0, "") })
                    it.value.pointXiuWei = it.value.maxXiuWei
                }else{
                    it.value.careerList = Collections.synchronizedList(it.value.careerList)
                }
            }
            mAlliance.putAll(backup.alliance.mapValues {
                it.value.toAlliance(mPersons)
            })
            mClans.putAll(backup.clans.mapValues {
                it.value.toClan(mPersons)
            })
            mNations.putAll(backup.nation.mapValues {
                it.value.toNation()
            })
            mBattleRound = backup.battleRound ?: BattleRound()
        }else{
            mBattleRound = BattleRound()
        }
        createAlliance() //此处处理了删除alliance的情况
        createNation()
        //更新Alliance属性
        mPersons.forEach {
            it.value.allianceSuccess = mAlliance[it.value.allianceId]!!.success
            it.value.allianceProperty =  mAlliance[it.value.allianceId]!!.property
            it.value.allianceName = mAlliance[it.value.allianceId]!!.name
            it.value.extraXuiweiMulti = CultivationHelper.getExtraXuiweiMulti(it.value,  mAlliance[it.value.allianceId]!!)
            it.value.nationId = mAlliance[it.value.allianceId]!!.nation
            it.value.nationPost = 0
            CultivationHelper.updatePersonEquipment(it.value)
        }
        updateNationPost(0, true)
        if(out == null){
            startWorld()
        }else{
            writeHistory("返回世界...")
        }
        registerTimeLooper()
        registerHistoryTimeLooper()
    }


    private fun startWorld(){
        writeHistory("进入世界...")
        addMultiPerson(mInitPersonCount)
        val li = addPersion(Pair("李", "逍遥"), NameUtil.Gender.Male, 100, null, true)
        val nu = addPersion(Pair("阿", "奴"), NameUtil.Gender.Female, 100, null, true)
        CultivationHelper.createPartner(li, nu)
    }

    private fun initLayout(){
        if(pinyinMode)
            mNavigation.inflateMenu(R.menu.menu_nav_left_cultivation)
        else
            mNavigation.inflateMenu(R.menu.menu_nav_left_cultivation_cn)

        mNavigationEnd.setNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_reset -> {
                    resetHandler()
                }
                R.id.menu_reset_wtf->{
                    resetCustomBonus()
                }
                R.id.menu_temp->{
                    temp()
                }
            }
            mDrawer.closeDrawer(GravityCompat.END)
            false
        }

        mNavigation.setNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_grace->{
                    mPersons.forEach { (_: String, u: Person) ->
                        u.xiuXei = Math.max(u.xiuXei, 0)
                    }
                    showToast("不用谢")
                }
                R.id.menu_add_spec ->{
                    addSpecPerson()
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
                R.id.menu_battle_nation ->{
                    battleNationHandler()
                }
                R.id.menu_event_enemy ->{
                    eventEnemyHandler()
                }
                R.id.menu_enemy_list->{
                    addBossHandler()
                }
                R.id.menu_multi->{
                    addMultiPerson(mInitPersonCount)
                }
            }
            mDrawer.closeDrawer(GravityCompat.START)
            false
        }
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
                    alliance.nation = configAlliance.nation
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
                        if(p.value.allianceId == it.value.id){
                            mPersons.remove(p.value.id)
                            if(mClans[p.value.ancestorId] != null){
                                mClans[p.value.ancestorId]!!.clanPersonList.remove(p.value.id)
                                if(p.value.ancestorId == p.value.id){
                                    mClans.remove(p.value.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createNation(){
        if(mNations.isEmpty()){
            mConfig.nation.forEach {
                mNations[it.id] = it.copy()
            }
        }else{
            mConfig.nation.forEach { configNation->
                val nation = mNations[configNation.id]
                if(nation != null){
                    nation.name = configNation.name
                }else{
                    mNations[configNation.id] = configNation.copy()
                }
            }
            mNations.forEach {
                if(mConfig.nation.find { f-> f.id == it.key } == null ){ //不存在的nation
                    mNations.remove(it.key)
                }
            }
        }
    }


    private fun newAlliance(it:AllianceConfig):Alliance{
        val alliance = Alliance()
        alliance.name = it.name + "界"
        alliance.id = it.id
        alliance.type = it.type
        alliance.nation = it.nation
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

    fun getPersonData(id: String?):Person?{
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
            if(!isStop){
                mBtnTime.tag = "ON"
                mBtnTime.text = resources.getString(R.string.cultivation_stop)
            }
            mThreadRunnable = true
        }else{
            if(!isStop) {
                mBtnTime.tag = "OFF"
                mBtnTime.text = resources.getString(R.string.cultivation_start)
            }
            mThreadRunnable = false
        }
    }

    private fun deadHandler(it:Person, currentXun:Long){
        mPersons.remove(it.id)
        mDeadPersons[it.id] = it
        addPersonEvent(it, personDataString[0])
        writeHistory("${getPersonBasicString(it)} ${personDataString[0]}", it)
        val alliance = mAlliance[it.allianceId]
        alliance?.personList?.remove(it.id)
        it.allianceId = ""
        it.allianceName = ""
        if(alliance != null && alliance.zhuPerson == it)
            alliance.zhuPerson = null
        if(mClans[it.ancestorId] != null){
            mClans[it.ancestorId]!!.clanPersonList.remove(it.id)
            if(it.ancestorId == it.id){
                mClans.remove(it.id)
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
        val lifeTurn = mSP.getInt("cultivation_jie", CultivationSetting.SP_JIE_TURN)
        if(person.isFav){
            return true
        }else if(matchName.find(person.name) != null){
            return true
        }else if(person.lifeTurn >= lifeTurn){
            return true
        }
        return false
    }

    private fun xunHandler(currentXun:Long) {
        val year = getYearString(currentXun)
        if(!isStop && mDate.text != year) {
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
                it.lifeTurn = Math.max(0, it.lifeTurn - 1)
                addPersonEvent(it,"转转-1,残:${it.lifeTurn}")
            }else{
                if(getOnlinePersonDetail(it.id) != null)
                    deadHandler(it, currentXun)
                return
            }
        }
        val currentJinJie = CultivationHelper.getJingJie(it.jingJieId)
        val xiuweiGrow = CultivationHelper.getXiuweiGrow(it, mAlliance)
        it.maxXiuWei += xiuweiGrow
        it.pointXiuWei += xiuweiGrow
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
                if (it.isFav && lastJingJieDigt.third == 4) {
                    writeHistory("${getPersonBasicString(it)} $commonText", it)
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
            updateCareer()
        }
        if(currentXun % 240 == 0L) {
            CultivationHelper.updatePartner(mPersons)
        }
        //以下辅助操作
        when {
            currentXun % 100000 == 0L && isStop -> {
                mDeadPersons.clear()
                addSpecPerson()
                saveAllData(false)
            }
            currentXun % 44000 == 0L -> {
                if(mPersons.size > 400){
                    addBossHandler(true)
                }
            }
            currentXun % 12000 == 0L && isStop -> bePerson()
            else -> randomBattle(currentXun)
        }
        updateCareerEffect(currentXun)
        updateClans(currentXun)
        updateEnemys()
        updateBoss(currentXun)
        randomSpecialEvent(currentXun)
        updateNationPost(currentXun)
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
        if(isStop)
            return
        if(mHistoryData.size > 500 && mThreadRunnable){
            mHistoryData.clear()
            CultivationBattleHelper.mBattles.clear()
        }
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
            addPersonEvent(person,"加入")
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
                addPersonEvent(person,commonText)
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
            addPersonEvent(person,commonText)
            writeHistory("${getPersonBasicString(person)} $commonText", person)
            return true
        }
        return false
    }

    private fun randomBattle(xun: Long){
        val weight = CultivationSetting.SP_EVENT_WEIGHT.map {
            val temp = it.split("-")
            Pair(temp.first().trim().toInt(), temp.last().trim().toInt())
        }
        if(xun % weight[0].first == 0L && isTrigger(weight[0].second)) {
           eventEnemyHandler()
        }
        if(xun % weight[1].first == 0L && isTrigger(weight[1].second)) {
            addBossHandler()
        }
        if(xun % weight[2].first == 0L && isTrigger(weight[2].second)) {
            battleSingleHandler(false)
        }
        if(xun % weight[3].first == 0L && isTrigger(weight[3].second)) {
            battleBangHandler(false)
        }
        if(xun % weight[4].first == 0L && isTrigger(weight[4].second)) {
            battleClanHandler(false)
        }
    }

    private fun randomSpecialEvent(xun: Long){

    }

    private fun updateNationPost(xun:Long, force:Boolean = false){
        if(xun % 60000 == 0L || force) {
            mNations.forEach { (t: String, _: Nation) ->
                updateNationPost(t)
            }
        }
    }

    fun updateNationPost(id:String):Nation?{
        val mNation = mNations[id]!!
        val ps = ConcurrentHashMap(mPersons.filter { it.value.nationId == mNation.id }).map { it.value }.toMutableList()
        if (ps.size < 10)
            return null
        ps.forEach {
            it.nationPost = 0
        }
        val emperor = ps.sortedWith(compareByDescending<Person>{ it.lingGenType.color }
                .thenByDescending { it.tianfus.sumBy { s->s.weight } }
                .thenByDescending { it.jingJieId })[0]
        emperor.nationPost = 1
        mNation.emperor = emperor
        ps.remove(emperor)
        val taiwei = ps.sortedWith(compareByDescending<Person>{ it.tianfus.sumBy { s->s.weight } }
                .thenByDescending { it.jingJieId })[0]
        taiwei.nationPost = 2
        mNation.taiWei = taiwei
        ps.remove(taiwei)
        val shangshu = ps.sortedWith(compareByDescending<Person>{ it.tianfus.sumBy { s->s.rarity } }
                .thenByDescending { it.jingJieId })[0]
        shangshu.nationPost = 3
        mNation.shangShu = shangshu
        ps.remove(shangshu)
        mNation.ciShi = ps.shuffled().subList(0, 4).map {
            it.nationPost = 4
            it
        }.toMutableList()
        ps.removeIf { mNation.ciShi.find { f-> f.id == it.id } != null }

        val endIndex = Math.min(4, ps.size)
        mNation.duWei = ps.shuffled().subList(0, endIndex).map {
            it.nationPost = 5
            it
        }.toMutableList()

        return mNation
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
                    if(isTrigger(baseNumber.toInt())){
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

    private fun updateEnemys(){
        mEnemys.filter { !it.value.isDead }.forEach { e->
            val it = e.value
            if(isTrigger(it.attackFrequency)){
                it.remainHit--
                val persons = mPersons.map { it.value }.shuffled()
                val person = persons[0]
                val result = CultivationBattleHelper.battleEnemy(mPersons, person, it)
                if(result){
                    writeHistory("${it.name} 倒")
                    it.isDead = true
                    CultivationHelper.gainJiEquipment(person, 14, it.type, it.seq)
                    if(it.type > 1){
                        gainTeji(person, 10 * it.type)
                    }
                }else{
                    val punishWeight = mSP.getString("cultivation_punish_million", CultivationSetting.SP_PUNISH_MILLION.joinToString())!!.split(",")
                    person.xiuXei -= it.type * punishWeight[0].trim().toInt() * 10000
                    if(it.remainHit <= 0){
                        writeHistory("${it.name} 消失")
                        it.isDead = true
                    }
                }
            }
        }
    }

    private fun updateBoss(xun:Long){
        if(xun % 12 == 0L) {
            mBoss.map(Map.Entry<String, Person>::value).filter { (xun - it.lastBirthDay) / 12 < it.lifetime && it.remainHit > 0  }.forEach { u ->
                val targets = mPersons.map { it.value }.shuffled()
                val person = targets[0]
                val result = CultivationBattleHelper.battlePerson(mPersons, person, u, 10)
                if (result) {
                    u.lifetime = 0
                    mAlliance[u.allianceId]?.personList?.remove(u.id)
                    writeHistory("${u.name} 倒", u)
                    gainTeji(person, 50 * u.type)
                    CultivationHelper.gainJiEquipment(person, 15, u.type - 1, mBattleRound.boss[u.type - 1])
                }else{
                    u.remainHit --
                    val punishWeight = mSP.getString("cultivation_punish_million", CultivationSetting.SP_PUNISH_MILLION.joinToString())!!.split(",")
                    person.xiuXei -= u.type * punishWeight[1].trim().toInt() * 10000
                    if(u.remainHit <= 0){
                        mAlliance[u.allianceId]?.personList?.remove(u.id)
                        writeHistory("${u.name} 消失", u)
                    }
                }
            }
        }
    }

    private fun updateClans(xun:Long){
        if(xun % 480 == 0L) {
            mPersons.filter { it.value.ancestorId != null }.map { it.value }.toMutableList()
                    .groupBy { it.ancestorId }.forEach { (t, u) ->
                if (u.size >= 5 && mPersons[t] != null ) {
                    if (mClans[t] == null) {
                        val clan = Clan()
                        clan.id = t!!
                        clan.name = u[0].lastName
                        clan.zhu = mPersons[t]
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
                else if(it.value.zhu == null &&  mPersons[it.value.id] != null){
                    it.value.zhu = mPersons[it.value.id]
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

    private fun updateCareer(){
        for ((_: String, person: Person) in mPersons) {
            val list = person.careerList.map { t->
                val career = mConfig.career.find { c-> c.id == t.first }!!
                career.level = t.second
                career
            }
            var addonCareer:String? = null
            var changed = false
            if(list.all { it.level >= it.maxLevel }){
                if(person.pointXiuWei > 50000000) {
                    person.pointXiuWei -= 50000000
                    if(isTrigger(Math.pow(5.0, person.careerList.size.toDouble()).toInt())){
                        addonCareer = CultivationHelper.getCareer()[0]
                        if(person.careerList.find { f-> f.first == addonCareer } == null){
                            val commonText = "\u83b7\u5f97\u804c\u4e1a : ${mConfig.career.find { f-> f.id == addonCareer }?.name}"
                            addPersonEvent(person, commonText)
                            writeHistory("${getPersonBasicString(person)} $commonText", person)
                        }else{
                            addonCareer = null
                        }
                    }
                }
            }
            list.forEach {
                if(it.level < it.maxLevel && person.pointXiuWei > it.upgradeBasicXiuwei){
                    person.pointXiuWei -= it.upgradeBasicXiuwei
                    if(isTrigger(it.level)){
                        changed = true
                        it.level ++
                    }
                }
            }
            if(changed){
                person.careerList = Collections.synchronizedList(list.map { Triple(it.id, it.level, "") })
            }
            if(addonCareer != null){
                person.careerList.add(Triple(addonCareer, 0, ""))
            }
        }
    }

    private fun updateCareerEffect(xun:Long){
        if(xun % 120 == 0L) {
            for ((_: String, person: Person) in mPersons) {
                person.careerList.map { t->
                    val obj = mConfig.career.find { c-> c.id == t.first }!!
                    obj.level = t.second
                    obj
                }.forEach { career->
                    if(career.id == "6100001" || career.id == "6100002" || career.id == "6100003" || career.id == "6100006"){
                        val equipmentType = if (career.id == "6100001") 0 else if (career.id == "6100002") 1 else if (career.id == "6100003") 2 else 3
                        val equipment = CultivationHelper.makeEquipment(equipmentType, career.level)
                        if(equipment != null && person.equipmentList.find { it.first == equipment.id } == null){
                            person.equipmentList.add(Triple(equipment.id, 0, ""))
                            val commonText = "\u5236\u9020 : ${equipment.name}"
                            CultivationHelper.updatePersonEquipment(person)
                            addPersonEvent(person, commonText)
                            writeHistory("${getPersonBasicString(person)} $commonText", person)
                        }
                    }
                    if(career.id == "6100004"){
                        val follower = CultivationHelper.makeFollower(career.level)
                        if(follower != null && person.followerList.filter { it.first == follower.id }.size < follower.max){
                            val name = NameUtil.getChineseName(null, follower.gender)
                            person.followerList.add(Triple(follower.id, name.first + name.second, ""))
                            val commonText = "\u53ec\u5524\u968f\u4ece : ${follower.name + name.first + name.second}"
                            addPersonEvent(person, commonText)
                            writeHistory("${getPersonBasicString(person)} $commonText", person)
                        }
                    }
                    if(career.id == "6100005" && isTrigger(2)){
                        if(person.teji.size > Math.max(5, career.level / 10)  )
                            return
                        gainTeji(person, career.level)
                    }
                }
            }
        }
    }

    private fun eventEnemyHandler(){
        val type = Random().nextInt(6)
        mBattleRound.enemy[type]++
        val enemy = CultivationEnemyHelper.generateEnemy(type)
        mEnemys[enemy.id] = enemy
        writeHistory("====================================")
        writeHistory("↑↑============================↑↑")
        writeHistory("${enemy.name}天降 - (${enemy.HP}/${enemy.remainHit})${enemy.attack}-${enemy.defence}-${enemy.speed}")
        writeHistory("↓↓============================↓↓")
        writeHistory("====================================")
    }

    private fun addFixedcPerson(){
        val ft = supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogAddPerson.newInstance()
        newFragment.isCancelable = true
        newFragment.show(ft, "dialog_add_fixed_person")
    }

    private fun addSpecPerson(){
        val allianceList = Collections.synchronizedList(mAlliance.map { it.value }.filter { it.type == 1 })
        val specPersonList = mutableListOf<PresetInfo>()
        for ( i in 0 until 4){
            SpecPersonFirstName.forEachIndexed { index, first ->
                specPersonList.add(PresetInfo("110$i${createIdentitySeq(index)}1".toInt(), Pair(allianceList[i].name.slice(0 until 1), first),
                        0,SpecPersonFirstNameWeight.first, SpecPersonFirstNameWeight.second))
            }
        }
        fixedPersonGenerate(specPersonList, allianceList)

        fixedPersonGenerate(SpecPersonFirstName3,
                mAlliance.map { it.value }.filter { it.type == 3 }.sortedBy { it.id })
        fixedPersonGenerate(SpecPersonFirstName4,
                mAlliance.map { it.value }.filter { it.type == 4 }.sortedBy { it.id })


        val specPersons = mutableListOf<PresetInfo>()
        specPersons.addAll(SpecPersonFirstName3)
        specPersons.addAll(SpecPersonFirstName4)
        specPersons.forEach { p ->
            if(p.partner > 0){
                val current = mPersons.map { it.value }.find { it.specIdentity == p.identity }
                if(current != null && current.partner == null ){
                    val partner = mPersons.map { it.value }.find { it.specIdentity == p.partner }
                    if(partner != null){
                        current.partner = partner.id
                        current.partnerName = partner.name
                        if(partner.partner == null){
                            partner.partner = current.id
                            partner.partnerName = current.name
                        }
                        addPersonEvent(partner, "与${current.name}\u7ed3\u4f34")
                        addPersonEvent(current, "与${partner.name}\u7ed3\u4f34")
                        writeHistory("${getPersonBasicString(partner)} 与 ${getPersonBasicString(current)} \u7ed3\u4f34了")
                    }
                }
            }
        }

    }

    private fun fixedPersonGenerate(specPersonList:MutableList<PresetInfo>, allianceList:List<Alliance>):MutableList<Pair<Person, Int>>{
        val result = mutableListOf<Pair<Person, Int>>()
        specPersonList.forEach {
            val person = fixedSinglePersonGenerate(it.name, getIdentityGender(it.identity), allianceList[getIdentityIndex(it.identity)],
                    null, it.tianfuWeight, it.linggenWeight)
            if(person != null){
                person.specIdentity = it.identity
                if(it.profile > 0){
                    person.profile = it.profile
                }
                result.add(Pair(person, it.partner))
            }
        }
        return result
    }

    private fun fixedSinglePersonGenerate(name:Pair<String, String?>,gender: NameUtil.Gender, alliance:Alliance, parent: Pair<Person, Person>? = null, tinfu:Int = 1, linggen:Int = 1):Person?{
        if(mPersons.none { p-> p.value.allianceId == alliance.id && p.value.name == name.first + (name.second ?: "") }) {
            val person = CultivationHelper.getPersonInfo(name, gender, 100, parent, false, CultivationSetting.PersonFixedInfoMix(null, null, tinfu, linggen))
            mPersons[person.id] = person
            CultivationHelper.joinFixedAlliance(person, alliance)
            addPersonEvent(person,"加入")
            writeHistory("${getPersonBasicString(person)} 加入", person)
            return person
        }
        return null
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

    private fun battleSingleHandler(block:Boolean = true){
        val personsAll = mPersons.filter { CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()
        personsAll.shuffle()
        if(personsAll.size < 20){
            showToast("persons less than 20")
            return
        }
        val persons = personsAll.subList(0, personsAll.size/2)
        setTimeLooper(false)
        if(block){
            mHistoryData.clear()
            CultivationHelper.mHistoryTempData.clear()
            (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
            mHistory.invalidateViews()
        }
        Thread(Runnable {
            Thread.sleep(500)
            mBattleRound.single++
            writeHistory("第${mBattleRound.single}届  Single Battle Start")
            var roundNumber = 1
            while (true){
                writeHistory("Single Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = roundSingleHandler(persons, 40)
                if(result)
                    break
            }
            CultivationHelper.gainJiEquipment(persons[0], 13, 0, mBattleRound.single)
            CultivationHelper.gainJiEquipment(persons[1], 13, 1, mBattleRound.single)
            writeHistory("第${mBattleRound.single}届 Single Battle Runner: ${persons[1].allianceName} - ${persons[1].name}", persons[1])
            writeHistory("第${mBattleRound.single}届 Single Battle Winner: ${persons[0].allianceName} - ${persons[0].name}", persons[0])
            if(!block){
                Thread.sleep(5000)
            }
            val message = Message.obtain()
            message.what = 8
            message.arg1 = if (block) 0 else 1
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleClanHandler(block: Boolean = true){
        val clans = mClans.filter { it.value.clanPersonList.size > 0 }.map { it.value }.toMutableList()
        if(clans.isEmpty() || clans.size < 4){
            showToast("Clan less than 4")
            return
        }
        setTimeLooper(false)
        if(block){
            mHistoryData.clear()
            CultivationHelper.mHistoryTempData.clear()
            (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
            mHistory.invalidateViews()
        }
        Thread(Runnable {
            Thread.sleep(500)
            mBattleRound.clan++
            writeHistory("第${mBattleRound.clan}届 Clan Battle Start")
            var roundNumber = 1
            while (true){
                writeHistory("Clan Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = roundClanHandler(clans, 10)
                if(result)
                    break
            }
            clans[0].clanPersonList.forEach {
                CultivationHelper.gainJiEquipment(it.value, 12, 0, mBattleRound.clan)
            }
            writeHistory("Clan Battle Winner: ${clans[0].name}")
            if(!block){
                Thread.sleep(5000)
            }
            val message = Message.obtain()
            message.what = 8
            message.arg1 = if (block) 0 else 1
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleBangHandler(block: Boolean = true){
        val alliances = mAlliance.filter { it.value.personList.isNotEmpty() }.map { it.value }.toMutableList()
        if(alliances.isEmpty() || alliances.size < 4){
            showToast("Bang less than 4")
            return
        }
        setTimeLooper(false)
        if(block){
            mHistoryData.clear()
            CultivationHelper.mHistoryTempData.clear()
            (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
            mHistory.invalidateViews()
        }
        Thread(Runnable {
            Thread.sleep(500)
            mBattleRound.bang++
            writeHistory("第${mBattleRound.bang}届 Bang Battle Start")
            var roundNumber = 1
            while (true){
                writeHistory("Bang Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = roundBangHandler(alliances, 20)
                if(result)
                    break
            }
            alliances[0].personList.forEach {
                CultivationHelper.gainJiEquipment(it.value, 11, 0, mBattleRound.bang)
            }
            alliances[1].personList.forEach {
                CultivationHelper.gainJiEquipment(it.value, 11, 1, mBattleRound.bang)
            }
            writeHistory("第${mBattleRound.bang}届 Bang Battle Runner: ${alliances[1].name}")
            writeHistory("第${mBattleRound.bang}届 Bang Battle Winner: ${alliances[0].name}")
            if(!block){
                Thread.sleep(5000)
            }
            val message = Message.obtain()
            message.what = 8
            message.arg1 = if (block) 0 else 1
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleNationHandler(block: Boolean = true){
        setTimeLooper(false)
        if(block){
            mHistoryData.clear()
            CultivationHelper.mHistoryTempData.clear()
            (mHistory.adapter as BaseAdapter).notifyDataSetChanged()
            mHistory.invalidateViews()
        }
        val nations = mConfig.nation.map { n->
            val nation = Nation()
            val result: ConcurrentHashMap<String, Person> = ConcurrentHashMap()
            mAlliance.filter { it.value.nation == n.id }.forEach { (_, u) ->
                result.putAll(u.personList)
            }
            nation.id = n.id
            nation.name = n.name
            nation.nationPersonList = result
            nation
        }.toMutableList()
        Thread(Runnable {
            Thread.sleep(500)
            mBattleRound.nation++
            writeHistory("第${mBattleRound.nation}届 Nation Battle Start")
            var roundNumber = 1

            while (true){
                writeHistory("Nation Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = roundNationHandler(nations, 20)
                if(result)
                    break
            }
            nations[0].nationPersonList.forEach {
                CultivationHelper.gainJiEquipment(it.value, 16, 0, mBattleRound.nation)
            }

            writeHistory("第${mBattleRound.nation}届 Nation Battle Winner: ${nations[0].name}")
            if(!block){
                Thread.sleep(5000)
            }
            val message = Message.obtain()
            message.what = 8
            message.arg1 = if (block) 0 else 1
            mTimeHandler.sendMessage(message)
        }).start()
    }


    private fun roundSingleHandler(persons: MutableList<Person>, round:Int):Boolean{
        persons.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until persons.size step 2) {
            if (i + 1 >= persons.size) {
                break
            }
            val firstPerson = persons[i]
            val secondPerson = persons[i + 1]
            val result = CultivationBattleHelper.battlePerson(null, firstPerson, secondPerson, round)
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

    private fun roundNationHandler(nation: MutableList<Nation>, round:Int):Boolean{
        nation.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until nation.size step 2){
            if( i + 1 >= nation.size){
                break
            }
            val firstNation = nation[i]
            val secondNation = nation[i+1]
            val result = roundMultiBattle(firstNation.nationPersonList, secondNation.nationPersonList, round, 200)
            passIds.add(if(result) secondNation.id else firstNation.id)
        }
        return if(nation.size == 2){
            val looser = nation.find { it.id == passIds[0] }!!
            nation.removeIf { it.id == passIds[0] }
            nation.add(looser)
            true
        }else{
            nation.removeIf { passIds.contains(it.id) }
            false
        }
    }

    private fun roundBangHandler(alliance: MutableList<Alliance>, round:Int):Boolean{
        alliance.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until alliance.size step 2){
            if( i + 1 >= alliance.size){
               break
            }
            val firstAlliance = alliance[i]
            val secondAlliance = alliance[i+1]
            val result = roundMultiBattle(firstAlliance.personList, secondAlliance.personList, round, 100)
            passIds.add(if(result) secondAlliance.id else firstAlliance.id)
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

    private fun roundClanHandler(clan: MutableList<Clan>, round:Int):Boolean{
        clan.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until clan.size step 2){
            if( i + 1 >= clan.size){
                break
            }
            val firstClan = clan[i]
            val secondClan = clan[i+1]
            val result = roundMultiBattle(firstClan.clanPersonList, secondClan.clanPersonList, round, 5)
            passIds.add(if(result) secondClan.id else firstClan.id)
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

    //true: fiest win
    private fun roundMultiBattle(firstPersonList:ConcurrentHashMap<String, Person>, secondPersonList:ConcurrentHashMap<String, Person>, round:Int, count:Int):Boolean{
        val firstPersons = firstPersonList.filter { CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.take(count).shuffled()
        val secondPersons = secondPersonList.filter { CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.take(count).shuffled()

        if(firstPersons.isEmpty()){
            return false
        }else if(secondPersons.isEmpty()){
            return true
        }
        var firstIndex = 0
        var secondIndex = 0
        while (true) {
            val result = CultivationBattleHelper.battlePerson(mPersons, firstPersons[firstIndex],
                    secondPersons[secondIndex], round)
            if (result) {
                secondIndex++
                if (secondIndex == secondPersons.size || secondIndex == count) {
                    return true
                }
            } else {
                firstIndex++
                if (firstIndex == firstPersons.size || firstIndex == count) {
                   return false
                }
            }
        }
    }

    private fun addBossHandler(ss:Boolean = false){
        val boss = if(ss)  CultivationEnemyHelper.generateYaoWang(mAlliance["6000105"]!!)
            else when (Random().nextInt(3)) {
                0 -> CultivationEnemyHelper.generateLiYuanBa(mAlliance["6000101"]!!)
                1 -> CultivationEnemyHelper.generateShadowMao(mAlliance["6000105"]!!)
                else -> CultivationEnemyHelper.generateShadowQiu(mAlliance["6000105"]!!)
            }
        mBoss[boss.id] = boss
        mBattleRound.boss[boss.type - 1]++
        writeHistory("====================================")
        writeHistory("↑↑============================↑↑")
        writeHistory("${boss.name}天降", boss)
        writeHistory("↓↓============================↓↓")
        writeHistory("====================================")
    }

    private fun gainTeji(person: Person, weight:Int = 1){
        CultivationHelper.getTeji(weight, false).forEach { t->
            if(!person.teji.contains(t)){
                person.teji.add(t)
                val commonText = "\u83b7\u5f97\u7279\u6280 : ${mConfig.teji.find { f-> f.id == t }?.name}"
                addPersonEvent(person, commonText)
                writeHistory("${getPersonBasicString(person)} $commonText", person)
            }
        }
    }

    private fun resetCustomBonus(){
        mPersons.forEach { (_: String, u: Person) ->
            u.followerList.clear()
            u.careerList = Collections.synchronizedList(CultivationHelper.getCareer().map { c->Triple(c, 0, "") })
            u.pointXiuWei = u.maxXiuWei
            u.teji.clear()
            u.equipmentList.removeIf {
                val equipment = mConfig.equipment.find { e-> it.first == e.id }
                equipment?.type ?: 0 <= 10
            }
            u.events.removeIf {
                it.content.contains("\u83b7\u5f97") || it.content.contains("\u5236\u9020")
                        || it.content.contains("\u53ec\u5524") || it.content.contains("Battle")
            }
            CultivationHelper.updatePersonEquipment(u)
        }
        showToast("重置完成")
    }

    fun showToast(content:String){
        if(isStop)
            return
        try {
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            Log.e("CultivationActivity", e.message)
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
            R.id.menu_add_fixed ->{
                addFixedcPerson()
            }
            R.id.menu_setting->{
                val ft = supportFragmentManager.beginTransaction()
                val newFragment = FragmentDialogSetting.newInstance()
                newFragment.isCancelable = false
                newFragment.show(ft, "dialog_setting")
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
                val activity = reference.get() ?: return
                if(msg.what == 1){
                    val xun = msg.obj.toString().toLong()
                    activity.xunHandler(xun)
                }else if(msg.what == 2){
                    if(msg.arg1 == 0){
                        activity.mProgressDialog.dismiss()
                    }
                    activity.showToast("保存完成")
                    activity.setTimeLooper(true)
                }else if(msg.what == 3){
                    activity.mProgressDialog.dismiss()
                    if(msg.obj != null){
                        activity.showToast("读取完成")
                        maxFemaleProfile = msg.arg1
                        maxMaleProfile = msg.arg2
                        activity.init(msg.obj.toString())
                    }else{
                        activity.init(null)
                    }
                }else if(msg.what == 4){
                    activity.showToast("重启完成")
                    activity.startWorld()
                }else if(msg.what == 5){
                    activity.combinedPersonRelationship(msg.obj as Person)
                    if(msg.arg1 == 0){
                        activity.showToast("加入完成")
                        activity.mProgressDialog.dismiss()
                        activity.setTimeLooper(true)
                    }
                }else if(msg.what == 6){
                    activity.showToast("BE操作完成")
                    activity.setTimeLooper(true)
                }else if(msg.what == 7){
                    activity.updateHistory()
                }else if(msg.what == 8){
                    activity.showToast("Battle end")
                    if(msg.arg1 == 1)
                        activity.setTimeLooper(true)
                }
            }
        }
    }



}