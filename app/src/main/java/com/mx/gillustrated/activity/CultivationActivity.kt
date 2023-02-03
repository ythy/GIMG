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
import com.mx.gillustrated.component.CultivationSetting.SpecPersonFirstName2
import com.mx.gillustrated.component.CultivationHelper.addPersonEvent
import com.mx.gillustrated.component.CultivationHelper.getPersonBasicString
import com.mx.gillustrated.component.CultivationHelper.getXiuweiGrow
import com.mx.gillustrated.component.CultivationHelper.mBattleRound
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.component.CultivationHelper.mCurrentXun
import com.mx.gillustrated.component.CultivationHelper.writeHistory
import com.mx.gillustrated.component.CultivationHelper.isTrigger
import com.mx.gillustrated.component.CultivationHelper.inDurationByXun
import com.mx.gillustrated.component.CultivationHelper.isTalent
import com.mx.gillustrated.component.CultivationHelper.mBossRecord
import com.mx.gillustrated.component.CultivationHelper.mXunDuration
import com.mx.gillustrated.component.CultivationHelper.pinyinMode
import com.mx.gillustrated.component.CultivationHelper.maxFemaleProfile
import com.mx.gillustrated.component.CultivationHelper.maxMaleProfile
import com.mx.gillustrated.component.CultivationHelper.talentValue
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.CultivationSetting.getIdentityGender
import com.mx.gillustrated.component.CultivationSetting.getIdentityIndex
import com.mx.gillustrated.component.CultivationSetting.createIdentitySeq
import com.mx.gillustrated.component.CultivationSetting.PresetInfo
import com.mx.gillustrated.component.CultivationSetting.SpecPersonFirstNameWeight
import com.mx.gillustrated.component.CultivationSetting.getAllSpecPersons
import com.mx.gillustrated.component.CultivationSetting.getIdentityType
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
    private var isHidden = false// Activity 是否不可见
    private var mSpeed = 10L//流失速度
    private val mInitPersonCount = 1000//初始化Person数量
    private var readRecord = true

    var mPersons:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var mDeadPersons:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var mAlliance:ConcurrentHashMap<String, Alliance> = ConcurrentHashMap()
    var mClans:ConcurrentHashMap<String, Clan> = ConcurrentHashMap()
    var mNations:ConcurrentHashMap<String, Nation> = ConcurrentHashMap()

    private var mBoss:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    private var mHistoryData = mutableListOf<CultivationSetting.HistoryInfo>()
    private val mTimeHandler:TimeHandler = TimeHandler(this)

    private val mExecutor:ExecutorService = Executors.newFixedThreadPool(20)
    private val mMainExecutor:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor() // 1s 为单位
    private val mHistoryExecutor:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val mMicroMainExecutor:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor() //12xun 为单位

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

    @BindView(R.id.ll_skin)
    lateinit var mSkinContainer:LinearLayout


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
            backupInfo.xunDuration = mXunDuration.mapKeys { m-> "${m.key.first}-${m.key.second}" }
            backupInfo.bossRecord = mBossRecord
            backupInfo.alliance = mAlliance.mapValues { it.value.toBak() }
            mPersons.forEach { p->
                val it = p.value
                it.profile = CultivationHelper.getRandomProfile(it.gender, it.profile)
                it.HP = Math.min(it.HP, it.maxHP)
                it.children = it.children.filterNot { f-> getOnlinePersonDetail(f) == null && getOfflinePersonDetail(f) == null }.toMutableList()
            }
            backupInfo.persons = mPersons.mapValues { it.value.toPersonBak() }
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
        if(mBtnTime.tag == "OFF")
            setTimeLooper(true)
        else
            setTimeLooper(false)
    }

//    @OnClick(R.id.btn_add_speed)
//    fun onSpeedAddClickHandler(){
//        mSpeed += if(mSpeed < 100L){
//            10L
//        }else{
//            100L
//        }
//        mSpeedText.text = mSpeed.toString()
//    }
//
//    @OnClick(R.id.btn_reduce_speed)
//    fun onSpeedReduceClickHandler(){
//        if(mSpeed == 10L)
//            return
//        mSpeed -= if(mSpeed <= 100L){
//            10L
//        }else{
//            100L
//        }
//        mSpeedText.text = mSpeed.toString()
//    }

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
    override fun onPause() {
        super.onPause()
        if(!isFinishing){
            isHidden = true
            val serviceIntent = Intent(this, StopService::class.java)
            this.startForegroundService(serviceIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        isHidden = false
        val serviceIntent = Intent(this, StopService::class.java)
        stopService(serviceIntent)
    }


    override fun onDestroy() {
        super.onDestroy()
        mThreadRunnable = false
        mHistoryThreadRunnable = false
        isHidden = false
    }

    private fun tempConvert(from:Int, to:Int, alliance:Alliance){
        val person = mPersons.mapNotNull { it.value }.find { it.specIdentity == from }
        if(person != null){
            CultivationHelper.changedToFixedAlliance(person, mAlliance, alliance)
            person.specIdentity = to
        }
    }

    private fun temp(){
        val specConfig = getAllSpecPersons()
        mPersons.filterValues { it.specIdentity > 0 }.forEach { (_, u) ->
            val config = specConfig.find { c-> c.identity == u.specIdentity }
            if (config != null){
                u.name = config.name.first + config.name.second + CultivationSetting.createLifeTurnName(u.specIdentityTurn)
                u.lastName = config.name.first
                if (config.profile > 0)
                    u.profile = config.profile
                if (u.partner != null && mPersons[u.partner ?: ""]?.partner == u.id){
                    mPersons[u.partner ?: ""]?.partnerName = u.name
                }
            }else if(getIdentityType(u.specIdentity) != 1) {
                deadHandler(u, true)
            }
        }
//            tempConvert(13010031, 13021011, mAlliance["6000403"]!!)
//            tempConvert(13010041, 13021021, mAlliance["6000403"]!!)
//            tempConvert(13010070, 13021030, mAlliance["6000403"]!!)



//         mPersons.forEach { (_: String, u: Person) ->
//             u.label = CultivationHelper.getLabel()
//             CultivationHelper.updatePersonExtraProperty(u)
//         }

//        mPersons.forEach { (_: String, u: Person) ->
//            u.equipmentListPair.removeIf { it.second > 10000 }
//            u.events.removeIf { it.content.indexOf("\u5929\u5b98\u8d50\u798f") > -1 }
//        }

//        mutableListOf(205, 206, 207, 208).forEach { type->
//                val lucky = mPersons.map { it.value }.shuffled().first()
//                val spec = CultivationSetting.createEquipmentCustom(type)
//                if(lucky.equipmentListPair.find { it.first == spec.first && it.second == spec.second } != null){
//                    return
//                }
//                lucky.equipmentListPair.add(spec)
//                CultivationHelper.updatePersonEquipment(lucky)
//                val equipment = CultivationSetting.getEquipmentCustom(spec)
//                val commonText = "SuperLucky \u5929\u5b98\u8d50\u798f \u83b7\u5f97${equipment.uniqueName}"
//                addPersonEvent(lucky, commonText)
//                writeHistory("${getPersonBasicString(lucky)} $commonText", lucky)
//            }


//        val allianceList = Collections.synchronizedList(mAlliance.map { it.value }.filter { it.type == 1 }.sortedBy { it.id })
//        for ( i in 0 until allianceList.size){
//            SpecPersonFirstName.forEachIndexed { index, first ->
//                val person = mPersons.map { it.value }.find { it.name ==  allianceList[i].name.slice(0 until 1) + first}
//                val id = "110$i${createIdentitySeq(index)}1".toInt()
//                if(person != null && person.specIdentity != id){
//                    person.specIdentity = id
//                }
//            }
//        }
//        val specConfig = getAllSpecPersons()
//        mPersons.map { it.value }.filter { mAlliance[it.allianceId]?.type != 1 && it.specIdentity > 0 }.forEach { p->
//            val spec = specConfig.find { it.identity == p.specIdentity }
//            if(spec == null){
//                p.specIdentity = 0
//            }
//        }

//        SpecPersonFirstName2.forEach { spec->
//            val person = mPersons.map { it.value }.find { it.name == spec.name.first + spec.name.second }
//            if (person != null && person.specIdentity == 0){
//                person.specIdentity = spec.identity
//            }
//        }
//

//       CultivationHelper.updateSingleBattleBonus(mPersons)
//        mBattleRound.nation = 0


//        for ((_: String, person: Person) in mPersons) {
//            val list = person.careerList.map { t ->
//                val career = mConfig.career.find { c -> c.id == t.first }!!.copy()
//                career.level = t.second
//                career
//            }
//            if (list.size > 1){
//                person.careerList = Collections.synchronizedList(list.subList(0, list.size - 1).map { Triple(it.id, it.maxLevel, "") })
//                val last = list.last()
//                person.careerList.add(Triple(last.id, last.level, ""))
//            }
//        }


    }

    private fun init(json:String?){
        CultivationSetting.TEMP_SP_JIE_TURN = mSP.getInt("cultivation_jie", CultivationSetting.SP_JIE_TURN)
        CultivationSetting.TEMP_REDUCE_TURN = mSP.getInt("cultivation_dead_reduce", CultivationSetting.SP_REDUCE_TURN)
        CultivationSetting.TEMP_TALENT_PROTECT = mSP.getInt("cultivation_talent_protect", CultivationSetting.SP_TALENT_PROTECT)
        CultivationSetting.TEMP_TALENT_EXP = mSP.getInt("cultivation_talent_exception", CultivationSetting.SP_TALENT_EXP)
        val out:String? = if(readRecord) json else null
        if(out != null && out.trim() != ""){
            val backup = Gson().fromJson(out, BakInfo::class.java)
            mCurrentXun = backup.xun
            mPersons.putAll(backup.persons.filter { mConfig.alliance.find { a-> a.id == it.value.allianceId } != null }.mapValues { it.value.toPerson()})
            mPersons.forEach {
                if(it.value.children.isNotEmpty())
                    it.value.children = Collections.synchronizedList(it.value.children)
                it.value.equipmentList= Collections.synchronizedList(it.value.equipmentList)
                it.value.lingGenName = if (it.value.lingGenSpecId == "") it.value.lingGenName else CultivationHelper.getTianName(it.value.lingGenSpecId)
                CultivationHelper.updatePersonEquipment(it.value)
                CultivationHelper.updatePersonExtraProperty(it.value)
                it.value.followerList = Collections.synchronizedList(it.value.followerList)
                it.value.careerList = Collections.synchronizedList(it.value.careerList)
            }
            mClans.putAll(backup.clans.mapValues {
                it.value.toClan(mPersons)
            })
            mBattleRound = backup.battleRound ?: BattleRound()
            mXunDuration = ConcurrentHashMap(backup.xunDuration.mapKeys { m->
                val arr = m.key.split("-")
                Pair(arr[0], arr[1].toInt())
            })
            mBossRecord = backup.bossRecord
            if(mBossRecord.isEmpty()){
                repeat(CultivationEnemyHelper.bossSettings.size) {
                    mBossRecord.add(mutableMapOf())
                }
            }

            CultivationHelper.mConfig.nation.forEach { nationConfig ->
                val nationBak = backup.nation[nationConfig.id]
                if(nationBak == null){
                    mNations[nationConfig.id] = NationBak().toNation(nationConfig)
                }else{
                    mNations[nationConfig.id] = nationBak.toNation(nationConfig)
                }
            }
            CultivationHelper.mConfig.alliance.forEach { allianceConfig ->
                val allianceBak = backup.alliance[allianceConfig.id]
                if(allianceBak == null){
                    mAlliance[allianceConfig.id] = AllianceBak().toAlliance(ConcurrentHashMap(), allianceConfig)
                }else{
                    mAlliance[allianceConfig.id] = allianceBak.toAlliance(mPersons, allianceConfig)
                }
            }
        }else{
            mBattleRound = BattleRound()
            mXunDuration = ConcurrentHashMap()
            mBossRecord = mutableListOf()
            repeat(CultivationEnemyHelper.bossSettings.size) {
                mBossRecord.add(mutableMapOf())
            }
            createAlliance()
            createNation()
        }
        //更新人物Alliance属性
        mPersons.forEach {
            it.value.allianceSuccess = mAlliance[it.value.allianceId]!!.success
            it.value.allianceProperty =  mAlliance[it.value.allianceId]!!.property
            it.value.allianceName = mAlliance[it.value.allianceId]!!.name
            it.value.extraXuiweiMulti = CultivationHelper.getExtraXuiweiMulti(it.value,  mAlliance[it.value.allianceId]!!)
            it.value.nationId = mAlliance[it.value.allianceId]!!.nation
            it.value.nationPost = 0
            if(it.value.label.contains("4100302")){
                CultivationHelper.resumeLife(it.value, mAlliance)
            }
            CultivationHelper.updatePersonEquipment(it.value)
        }
        updateNationPost(0, true)
        CultivationHelper.updateAllianceBattleBonus(mAlliance)
        CultivationHelper.updateClanBattleBonus(mClans)
        CultivationHelper.updateNationBattleBonus(mNations, mPersons)
        CultivationHelper.updateSingleBattleBonus(mPersons)
        CultivationHelper.updateBossBattleBonus(mPersons)
        mPersons.forEach {
           it.value.skin = CultivationHelper.generateSkinValue(it.value)
           CultivationHelper.updatePersonExtraProperty(it.value)
        }
        if(out == null || out.trim() == ""){
            startWorld()
        }else{
            writeHistory("返回世界...")
        }
        registerTimeLooper()
        registerHistoryTimeLooper()
    }


    private fun startWorld(){
        writeHistory("进入世界...")//
        addSpecPerson()
        addMultiPerson(mInitPersonCount)
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
                R.id.menu_shuffle->{
                    killPersonShuffle()
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
                R.id.menu_lucky ->{
                    addSpecialEquipmentEvent(null, "Lucky")
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

        loadSkin()
    }

    fun loadSkin(){
        val skin =  mSP.getString("cultivation_skin", "spring")
        mSkinContainer.background = when(skin){
            "spring" -> getDrawable(R.drawable.skin_bg_spring)
            "rain" -> getDrawable(R.drawable.skin_bg_rain)
            "equinox" -> getDrawable(R.drawable.skin_bg_spring_equinox)
            else -> getDrawable(R.drawable.skin_bg_spring)
        }
    }

    private fun loadConfig(){
        mConfig = Gson().fromJson(JsonFileReader.getJsonFromAssets(this,"definition.json"), Config::class.java)
    }

    private fun createAlliance() {
        CultivationHelper.mConfig.alliance.forEach { allianceConfig ->
            mAlliance[allianceConfig.id] = AllianceBak().toAlliance(ConcurrentHashMap(), allianceConfig)
        }
    }

    private fun createNation(){
        CultivationHelper.mConfig.nation.forEach { nationConfig ->
            mNations[nationConfig.id] = NationBak().toNation(nationConfig)
        }
    }

    fun getPersonData(id: String?):Person?{
        if(id == null)
            return null
        return  getOnlinePersonDetail(id) ?: getOfflinePersonDetail(id) ?: mBoss[id]
    }

    private fun getSpecOnlinePerson(id:Int?):Person?{
        return  mPersons.map { it.value }.find { it.specIdentity == id}
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
            if(!isHidden){
                mBtnTime.tag = "ON"
                mBtnTime.text = resources.getString(R.string.cultivation_stop)
            }
            mThreadRunnable = true
        }else{
            if(!isHidden) {
                mBtnTime.tag = "OFF"
                mBtnTime.text = resources.getString(R.string.cultivation_start)
            }
            mThreadRunnable = false
        }
    }

    private fun deadHandler(it:Person, force:Boolean = false){
        mPersons.remove(it.id)
        mDeadPersons[it.id] = it
        it.nationPost = 0
        addPersonEvent(it, personDataString[0])
        writeHistory("${getPersonBasicString(it)} ${personDataString[0]}", it)
        val alliance = mAlliance[it.allianceId]
        alliance?.personList?.remove(it.id)
        it.allianceId = ""
        it.allianceName = ""
        if(alliance != null && alliance.zhuPerson == it)
            alliance.zhuPerson = null
        if(mClans[it.ancestorId] != null){
            mClans[it.ancestorId]?.clanPersonList?.remove(it.id)
        }
        if(it.specIdentity > 0 && alliance != null && !force){ //特殊处理SpecName
            val config = getAllSpecPersons().find { p-> p.identity == it.specIdentity } //maybe null
            if(config != null || alliance.type == 1){
                val person = when {
                    alliance.type == 0 -> //Name2
                        addSingleSpecPerson(config!!)
                    alliance.type == 1 -> {
                        val firstName = SpecPersonFirstName[CultivationSetting.getIdentitySeq(it.specIdentity) - 1]
                        addSingleSpecPerson(PresetInfo(it.specIdentity, Pair(it.lastName, firstName),
                                0,SpecPersonFirstNameWeight.first, SpecPersonFirstNameWeight.second),
                                alliance)
                    }
                    else -> addSingleSpecPerson(config!!, alliance)
                }

                if(person != null){
                    person.specIdentityTurn = it.specIdentityTurn + 1
                    person.name = person.name + CultivationSetting.createLifeTurnName(person.specIdentityTurn)
                }
            }
        }
    }

    // 0 dead; 1 cost 1; 2 cost reduce turn
    private fun isDeadException(person:Person):Int{
        if(CultivationHelper.isNeverDead(person)){
            return 1
        }else if(person.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN){
            return 2
        }else if(person.specIdentity == 0 && isTalent(person)){
            return 1
        }
        return 0
    }


    private fun xunMicroHandler(currentXun:Long){
        mExecutor.execute {
            updateInfoByXun(currentXun)
        }
    }

    private fun xunHandler(currentXun:Long, step:Int) {
        val year = CultivationHelper.getYearString(currentXun)
        if(!isHidden && mDate.text != year) {
            mDate.text = year
        }
        for ((_: String, it: Person) in mPersons) {
            mExecutor.execute {
                updatePersonByXun(it, currentXun, step)
            }
        }
    }

    private fun updatePersonByXun(it:Person, currentXun:Long, step: Int){
        if (it.lifetime < currentXun ) {
            when(isDeadException(it)){
                0 -> {
                    if(getOnlinePersonDetail(it.id) != null)
                        deadHandler(it)
                    return
                }
                1 -> {
                    it.lifetime += 5000
                    it.lifeTurn = Math.max(0, it.lifeTurn - 1)
                    it.deadExceptTimes++
                    addPersonEvent(it,"转转-1,残:${it.lifeTurn}")
                }
                2 -> {
                    it.lifetime += 5000
                    it.lifeTurn -= CultivationSetting.TEMP_REDUCE_TURN
                    it.deadExceptTimes++
                    addPersonEvent(it,"转转-${CultivationSetting.TEMP_REDUCE_TURN},残:${it.lifeTurn}")
                }
            }
        }

        var remainingStep = step
        loop@ while (remainingStep-- > 0){
            val currentJinJie = CultivationHelper.getJingJie(it.jingJieId)
            val xiuweiGrow = getXiuweiGrow(it, mAlliance)
            it.maxXiuWei += xiuweiGrow
            it.pointXiuWei += xiuweiGrow
            it.xiuXei += xiuweiGrow
            if (it.xiuXei < currentJinJie.max) {
                continue@loop
            }
            val next = CultivationHelper.getNextJingJie(it.jingJieId)
            it.xiuXei = 0
            val totalSuccess = CultivationHelper.getTotalSuccess(it)
            // 9zhuan↑  81zhuan↑↑ ← decrease  random
            var difficulty = 1
            if(next == null && it.lifeTurn > 0){
                when {
                    it.lifeTurn % 81 == 0 -> difficulty = mSP.getInt("cultivation_nan_81", CultivationSetting.SP_NAN_81)
                    it.lifeTurn % 9 == 0 -> difficulty = mSP.getInt("cultivation_nan_9", CultivationSetting.SP_NAN_9)
                }
            }
            val random = Random().nextInt(100 * difficulty)
            if (random <= totalSuccess) {//成功
                val allianceNow = mAlliance[it.allianceId]
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
                    it.lifetime +=  CultivationHelper.getLifetimeBonusRealm(it, allianceNow)
                } else {
                    val commonText = "转转成功$difficulty，${personDataString[2]} $random/$totalSuccess"
                    if(difficulty > 1 || it.isFav){
                        writeHistory("${getPersonBasicString(it)} $commonText", it)
                    }
                    it.jingJieId = mConfig.jingJieType[0].id
                    it.jinJieName = CultivationHelper.getJinJieName(mConfig.jingJieType[0].name)
                    it.jingJieSuccess = mConfig.jingJieType[0].success
                    it.jinJieColor = mConfig.jingJieType[0].color
                    it.jinJieMax = mConfig.jingJieType[0].max
                    it.lifeTurn += 1
                    it.lifetime =  currentXun + CultivationHelper.getLifetimeBonusInitial(it, allianceNow)
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
    }


    private fun updateInfoByXun(currentXun:Long){
        CultivationHelper.updateAllianceGain(mAlliance, inDurationByXun("XunAlliance", 120))
        if(inDurationByXun("Xun120", 120)) {
            updatePartnerChildren()
            updateHP()
            updateCareer()
            addPersion(null, null)
        }
        if(inDurationByXun("Xun240",240)) {
            CultivationHelper.updatePartner(mPersons)
        }
        //以下辅助操作
        when {
            inDurationByXun("Xun100000", 100000) && isHidden -> {
                mDeadPersons.clear()
                addSpecPerson()
                saveAllData(false)
            }
            inDurationByXun("Xun12000", 12000) -> {
                bePerson()
            }
            else -> randomBattle(currentXun)
        }
        updateCareerEffect(currentXun)
        updateClans(currentXun)
        updateBoss(currentXun)
        randomSpecialEquipmentEvent(currentXun)
        updateNationPost(currentXun)
    }

    private fun setMicroMainExecutor(){
        mMicroMainExecutor.scheduleAtFixedRate({
            if(mThreadRunnable){
                mCurrentXun += 10
                val message = Message.obtain()
                message.what = 9
                message.obj = mCurrentXun
                mTimeHandler.sendMessage(message)
            }
        }, 0, mSpeed * 10, TimeUnit.MILLISECONDS)
    }

    //1旬一月
    private fun registerTimeLooper(){
        setMicroMainExecutor()
        mMainExecutor.scheduleAtFixedRate({
            if(mThreadRunnable){
                isHidden = CultivationHelper.isServiceRunning(this, StopService::class.java)
                val step = 1000 / mSpeed // 旬增量，mSpeed == 1000 时, 基础增量； mSpeed == 10 时， 100倍增量
                val message = Message.obtain()
                message.what = 1
                message.arg1 = step.toInt()
                message.obj = mCurrentXun
                mTimeHandler.sendMessage(message)
            }
        }, 500, 1000, TimeUnit.MILLISECONDS)
    }

    private fun registerHistoryTimeLooper(){
        mHistoryExecutor.scheduleAtFixedRate({
            if(mHistoryThreadRunnable){
                val message = Message.obtain()
                message.what = 7
                mTimeHandler.sendMessage(message)
            }
        }, 100, 1000, TimeUnit.MILLISECONDS)
    }

    private fun updateHistory(){
        if(isHidden)
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

    private fun addPersion(fixedName:Pair<String, String?>?, fixedGender:NameUtil.Gender?, parent: Pair<Person, Person>? = null, mix: CultivationSetting.PersonFixedInfoMix? = null):Person{
        val person = CultivationHelper.getPersonInfo(fixedName, fixedGender, parent, mix)
        combinedPersonRelationship(person)
        return person
    }

    fun combinedPersonRelationship(person: Person, log:Boolean = true){
        CultivationHelper.joinAlliance(person, mAlliance)
        mPersons[person.id] = person
        if(mClans[person.ancestorId] != null){
            mClans[person.ancestorId]!!.clanPersonList[person.id] = person
        }
        if(log){
            val extra = person.parentName
            addPersonEvent(person,"加入")
            writeHistory("${getPersonBasicString(person)} 加入${ if (extra == null) "" else " ←$extra"}", person)
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
                mDeadPersons.remove(id)
                person.id =  UUID.randomUUID().toString()
                if(person.specIdentityTurn == 0)
                    person.fullName = person.name
                person.specIdentityTurn = person.specIdentityTurn + 1
                person.name = person.fullName + CultivationSetting.createLifeTurnName(person.specIdentityTurn)
                resetCustomBonusSingle(person)
                person.xiuXei = 0
                person.jingJieId = mConfig.jingJieType[0].id
                person.jinJieName = CultivationHelper.getJinJieName(mConfig.jingJieType[0].name)
                person.jingJieSuccess = mConfig.jingJieType[0].success
                person.jinJieColor = mConfig.jingJieType[0].color
                person.jinJieMax = mConfig.jingJieType[0].max
                combinedPersonRelationship(person, false)
                val commonText = "\u590d\u6d3b"
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
                person.lifetime = mCurrentXun
                deadHandler(person)
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
        val weight = CultivationSetting.EVENT_WEIGHT.map {
            val temp = it.split("-")
            Pair(temp.first().trim().toInt(), temp.last().trim().toInt())
        }
        if(inDurationByXun("BattleBoss", weight[0].first, xun) && isTrigger(weight[0].second)) {
            addBossHandler()
        }
        if(inDurationByXun("BattleSingle", weight[1].first, xun) && isTrigger(weight[1].second)) {
            battleSingleHandler(false)
        }
        if(inDurationByXun("BattleBang", weight[2].first, xun) && isTrigger(weight[2].second)) {
            battleBangHandler(false)
        }
        if(inDurationByXun("BattleClan", weight[3].first, xun) && isTrigger(weight[3].second)) {
            battleClanHandler(false)
        }
    }

    private fun randomSpecialEquipmentEvent(xun: Long){
        if(inDurationByXun("SpecialEvent", 121212, xun)) {
            if(isTrigger(100)) {
                val lucky = mPersons.map { it.value }.shuffled().first()
                addSpecialEquipmentEvent(lucky, "Special")
            }
        }
    }

    private fun addSpecialEquipmentEvent(person:Person? = null, tag:String = ""){
        val lucky = person ?: mPersons.map { it.value }.shuffled().first()
        val spec = CultivationSetting.createEquipmentCustom()
        if(spec == null || lucky.equipmentList.find { it.id == spec.first && it.seq == spec.second } != null){
            return
        }
        lucky.equipmentList.add(Equipment(spec.first, spec.second))
        CultivationHelper.updatePersonEquipment(lucky)
        val equipment = CultivationSetting.getEquipmentCustom(spec.first, spec.second)
        val commonText = "$tag \u5929\u5b98\u8d50\u798f \u83b7\u5f97${equipment.second}"
        addPersonEvent(lucky, commonText)
        writeHistory("${getPersonBasicString(lucky)} $commonText", lucky)
    }

    private fun updateNationPost(xun:Long, force:Boolean = false){
        if(inDurationByXun("NationPost", 60000, xun) || force) {
            mNations.forEach { (t: String, _: Nation) ->
                updateNationPost(t)
            }
        }
    }

    fun updateNationPost(id:String):Nation?{
        val mNation = mNations[id]!!
        val ps = ConcurrentHashMap(mPersons.filter {
            it.value.nationId == mNation.id })
                .map { it.value }.toMutableList()
        if (ps.size < 11)
            return null
        ps.forEach {
            it.nationPost = 0
        }
        val emperor = ps.sortedWith(compareByDescending<Person>{ talentValue(it) }
                .thenByDescending { it.jingJieId })[0]
        emperor.nationPost = 1
        mNation.emperor = emperor.id
        ps.remove(emperor)
        val taiwei = ps.sortedWith(compareByDescending<Person>{ it.tianfuList.sumBy { s->s.weight } }
                .thenByDescending { it.jingJieId })[0]
        taiwei.nationPost = 2
        mNation.taiWei = taiwei.id
        ps.remove(taiwei)
        val shangshu = ps.sortedWith(compareByDescending<Person>{
            val property = CultivationHelper.getProperty(it)
            property[1]/5 + property[2] +  property[3] +  property[4]
        }.thenByDescending { it.jingJieId })[0]
        shangshu.nationPost = 3
        mNation.shangShu = shangshu.id
        ps.remove(shangshu)

        mNation.ciShi = ps.sortedWith(compareByDescending<Person>{ talentValue(it) }
                .thenByDescending { it.jingJieId }).subList(0, 4).map {
            it.nationPost = 4
            it.id
        }.toMutableList()
        ps.removeIf { mNation.ciShi.find { f-> f == it.id } != null }

        val endIndex = Math.max(4, ps.size / 10)
        mNation.duWei = ps.shuffled().subList(0, endIndex).map {
            it.nationPost = 5
            it.id
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
                if(partner != null && partner.children.size < 5 && !partner.dink && baseNumber < Int.MAX_VALUE){
                    if(isTrigger(baseNumber.toInt())){
                        val child = addPersion(Pair(partner.lastName, null), null,
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


    private fun updateBoss(xun:Long){
        if(inDurationByXun("BossUpdated", 12, xun)) {
            mBoss.map(Map.Entry<String, Person>::value).filter { it.lifetime > xun && it.remainHit > 0  }.forEach { u ->
                val targets = mPersons.map { it.value }.shuffled()
                val person = targets[0]
                if (CultivationHelper.getProperty(person)[0] > 0){
                    val result = CultivationBattleHelper.battlePerson(mPersons, person, u, 50)
                    if (result) {
                        u.lifetime = 0
                        mAlliance[u.allianceId]?.personList?.remove(u.id)
                        writeHistory("${u.name} 倒", u)
                        if(isTrigger(500 / u.type)) {
                            addSpecialEquipmentEvent(person, "Boss")
                        }
                        mBossRecord[u.type - 1][mBattleRound.boss[u.type - 1]] = person.id
                        CultivationHelper.updateBossBattleBonus(mPersons)
                    }else{
                        u.remainHit --
                        val punishWeight = mSP.getInt("cultivation_punish_boss_million", CultivationSetting.SP_PUNISH_BOSS_MILLION)
                        person.xiuXei -= u.type * punishWeight * 10000
                        if(u.remainHit <= 0){
                            mAlliance[u.allianceId]?.personList?.remove(u.id)
                            writeHistory("${u.name} 消失", u)
                        }
                    }
                }
            }
        }
    }

    private fun updateClans(xun:Long){
        if(inDurationByXun("ClanCreated", 480, xun)) {
            mPersons.filter { it.value.ancestorId != null }.map { it.value }.toMutableList()
                    .groupBy { it.ancestorId }.forEach { (t, u) ->
                if (u.size >= 5 && mPersons[t] != null ) {
                    if (mClans[t] == null) {
                        CultivationHelper.createClan(mPersons[t]!!, mClans, mPersons)
                    }
                }
            }
            mClans.forEach {
                it.value.clanPersonList = ConcurrentHashMap(mPersons.filter { m ->
                    m.value.ancestorId == it.key
                })
            }
        }
        if(inDurationByXun("ClanUpdated", 12, xun)) {
            mClans.forEach {
                if(getOnlinePersonDetail(it.key) == null){
                    it.value.clanPersonList.filter { c-> isDeadException(c.value) == 0 }.forEach { p->
                        deadHandler(p.value)
                    }
                    it.value.clanPersonList.clear()
                }
                if(it.value.clanPersonList.isEmpty()){
                    mClans.remove(it.key)
                }
            }
        }
    }

    // update every 10 years
    private fun updateHP(){
        for ((_: String, it: Person) in mPersons) {
            it.HP += it.lingGenDetail.color + 1
            val currentHP = CultivationHelper.getProperty(it)[0]
            if(currentHP < -10){
                val supplement = Math.abs(currentHP)
                if(it.lifetime - mCurrentXun > supplement * 12 + 2000){
                    it.lifetime -= supplement * 12
                    it.HP += supplement
                }
            }
            if(it.HP > it.maxHP )
                it.HP = it.maxHP
        }
    }

    private fun updateCareer(){
        for ((_: String, person: Person) in mPersons) {
            val list = person.careerList
            var addonCareer:CareerConfig? = null
            if(list.size == 0 || list.all { it.level >= it.detail.maxLevel }){
                if(person.pointXiuWei > 50000000) {
                    person.pointXiuWei -= 50000000
                    if(isTrigger(Math.pow(5.0, list.size.toDouble()).toInt())){
                        addonCareer = CultivationHelper.getCareer()
                        if(list.find { f-> f.id == addonCareer?.id } == null){
                            val commonText = "\u83b7\u5f97\u804c\u4e1a : ${addonCareer.name}"
                            addPersonEvent(person, commonText)
                            writeHistory("${getPersonBasicString(person)} $commonText", person)
                        }else{
                            addonCareer = null
                        }
                    }
                }
            }else{
                list.forEach {
                    if(it.level < it.detail.maxLevel && person.pointXiuWei > it.detail.upgradeBasicXiuwei){
                        person.pointXiuWei -= it.detail.upgradeBasicXiuwei
                        if(isTrigger(it.level)){
                            it.level ++
                        }else{
                            if(person.isFav){
                                val commonText =  "${it.detail.name} LEVEL${it.level}\u5347\u7EA7\u5931\u8D25"
                                writeHistory("${getPersonBasicString(person)} $commonText", person)
                            }
                        }
                    }
                }
            }
            if(addonCareer != null){
                person.careerList.add(Career(addonCareer.id, 1))
            }
        }
    }

    private fun updateCareerEffect(xun:Long){
        if(inDurationByXun("CareerEffect", 120, xun)) {
            for ((_: String, person: Person) in mPersons) {
                person.careerList.forEach { career->
                    if(career.id == "6100001" || career.id == "6100002" || career.id == "6100003" || career.id == "6100006"){
                        val equipmentType = if (career.id == "6100001") 0 else if (career.id == "6100002") 1 else if (career.id == "6100003") 2 else 3
                        val equipment = CultivationHelper.makeEquipment(equipmentType, career.level)
                        if(equipment != null && equipment.detail.rarity > person.equipmentList.filter { it.detail.type == equipmentType }.maxBy { it.detail.rarity }?.detail?.rarity ?: 0){
                            person.equipmentList.removeIf { it.detail.type == equipmentType }
                            person.equipmentList.add(equipment)
                            val commonText = "\u5236\u9020 : ${equipment.detail.name}"
                            CultivationHelper.updatePersonEquipment(person)
                            addPersonEvent(person, commonText)
                            writeHistory("${getPersonBasicString(person)} $commonText", person)
                        }
                    }
                    if(career.id == "6100004"){
                        val follower = CultivationHelper.makeFollower(career.level)
                        if(follower != null && person.followerList.filter { it.id == follower.id }.size < follower.max){
                            val name = NameUtil.getChineseName(null, follower.gender)
                            person.followerList.add(Follower(follower.id, name.first + name.second))
                            val commonText = "\u53ec\u5524\u968f\u4ece : ${follower.name + name.first + name.second}"
                            addPersonEvent(person, commonText)
                            writeHistory("${getPersonBasicString(person)} $commonText", person)
                        }
                    }
                    if(career.id == "6100005" && isTrigger(2)){
                        if(person.teji.mapNotNull { m-> mConfig.teji.find { it.id == m } }.filter { it.type < 4 }.size >= Math.max(3, career.level / 20)  )
                            return
                        gainTeji(person, career.level)
                    }
                }
            }
        }
    }

    private fun addFixedcPerson(){
        val ft = supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogAddPerson.newInstance()
        newFragment.isCancelable = true
        newFragment.show(ft, "dialog_add_fixed_person")
    }

    private fun addSpecPerson(){
        Collections.synchronizedList(mAlliance.map { it.value }.filter { it.type == 1 }.sortedBy { it.id }).forEachIndexed { i, alliance ->
            SpecPersonFirstName.forEachIndexed { index, first ->
                val nid = "110$i${createIdentitySeq(index)}1".toInt()
                addSingleSpecPerson(PresetInfo(nid, Pair(alliance.name.slice(0 until 1), first),
                            0,SpecPersonFirstNameWeight.first, SpecPersonFirstNameWeight.second), alliance)
            }
        }

        SpecPersonFirstName2.forEach { spec->
            addSingleSpecPerson(spec)
        }

        CultivationSetting.getSpecPersonsByType().forEach { (t, u) ->
            val alliances = mAlliance.map { it.value }.filter { it.type == t }
            u.forEach { spec->
                addSingleSpecPerson(spec, alliances.find { alliance -> alliance.id.toInt() % 10 == getIdentityIndex(spec.identity) + 1 })
            }
        }

        getAllSpecPersons().forEach spec@{ p ->
            val current = mPersons.map { it.value }.find { it.specIdentity == p.identity }
                    ?: return@spec

            if(p.partner > 0){
                val validPartner = mPersons.map { it.value }.find { it.specIdentity == p.partner }
                val currentPartner = getOnlinePersonDetail(current.partner)
                if(currentPartner?.specIdentity ?: 0 > 0 && currentPartner?.specIdentity != p.partner){
                    current.partner = null
                    current.partnerName = null
                    current.children.clear()
                    currentPartner!!.children.clear()
                }
                if(validPartner != null && current.partner == null){
                    current.partner = validPartner.id
                    current.partnerName = validPartner.name
                    val specPartnerSetting = getAllSpecPersons().find { f->f.identity == p.partner }!!
                    if(validPartner.partner == null && specPartnerSetting.partner == current.specIdentity){
                        validPartner.partner = current.id
                        validPartner.partnerName = current.name
                    }
                    addPersonEvent(validPartner, "与${current.name}\u7ed3\u4f34")
                    addPersonEvent(current, "与${validPartner.name}\u7ed3\u4f34")
                    writeHistory("${getPersonBasicString(validPartner)} 与 ${getPersonBasicString(current)} \u7ed3\u4f34了")
                }
            }

            if(p.parent != null){
                val dad = mPersons.map { it.value }.find { it.specIdentity == p.parent?.first }
                val mum = mPersons.map { it.value }.find { it.specIdentity == p.parent?.second }
                if(current.parent?.first != dad?.id || current.parent?.second != mum?.id){
                    if(dad != null && mum != null){
                        current.parent = Pair(dad.id, mum.id)
                        current.parentName = Pair(dad.name, mum.name)
                        synchronized(dad.children) {
                            dad.children.removeIf { r -> current.id == r }
                        }
                        synchronized(mum.children) {
                            mum.children.removeIf { r -> current.id == r }
                        }
                        dad.children.add(current.id)
                        mum.children.add(current.id)
                    }
                }
            }
        }
    }

    private fun addSingleSpecPerson(spec:PresetInfo, alliance:Alliance? = null):Person?{
        if(getSpecOnlinePerson(spec.identity) != null)
            return null
        var parent:Pair<Person, Person>? = null
        if(spec.parent != null){
            val dad = getSpecOnlinePerson(spec.parent?.first)
            val mum = getSpecOnlinePerson(spec.parent?.second)
            if (dad != null && mum != null){
                parent = Pair(dad, mum)
            }
        }
        if(spec.parent == null || parent != null ){
            val person = CultivationHelper.getPersonInfo(spec.name,  getIdentityGender(spec.identity), parent,
                    CultivationSetting.PersonFixedInfoMix(null, null, spec.tianfuWeight, spec.linggenWeight))
            person.specIdentity = spec.identity
            if(spec.profile > 0){
                person.profile = spec.profile
            }
            if(parent != null){
                synchronized(parent.first.children){
                    parent.first.children.add(person.id)
                }
                synchronized(parent.second.children){
                    parent.second.children.add(person.id)
                }
            }
            mPersons[person.id] = person
            if(mClans[person.ancestorId] != null){
                mClans[person.ancestorId]!!.clanPersonList[person.id] = person
            }
            if(alliance != null){
                CultivationHelper.joinFixedAlliance(person, alliance)
            }else{
                CultivationHelper.joinAlliance(person, mAlliance)
                person.singled = true
            }
            addPersonEvent(person,"加入")
            writeHistory("${getPersonBasicString(person)} 加入", person)
            person.equipmentList = CultivationHelper.getSpecPersonEquipment(person)
            CultivationHelper.updatePersonEquipment(person)
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
            mNations.clear()
            mBattleRound = BattleRound()
            mXunDuration = ConcurrentHashMap()
            createAlliance()
            createNation()
            val message = Message.obtain()
            message.what = 4
            mTimeHandler.sendMessage(message)
        }).start()
    }

    private fun battleSingleHandler(block:Boolean = true){
        val minSize = CultivationSetting.BattleSettings.SingleMinSize
        val personsAll = mPersons.filter { CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()
        personsAll.shuffle()
        if(personsAll.size < minSize){
            showToast("persons less than $minSize")
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
            mBattleRound.single++
            writeHistory("第${mBattleRound.single}届  Single Battle Start")
            val restPersons = mutableListOf<Person>()
            var roundNumber = 1
            while (true){
                writeHistory("Single Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = roundSingleHandler(personsAll, restPersons,40)
                if(result)
                    break
            }
            repeat(minSize){ index-> //
                val reverseIndex = minSize - index //32 ~ 1
                val person = restPersons[reverseIndex - 1]
                person.battleRecord[mBattleRound.single] = reverseIndex
                writeHistory("第${mBattleRound.single}届 Single Battle No $reverseIndex : ${person.name}", person)
                if(isTrigger(reverseIndex * 100)) {
                    addSpecialEquipmentEvent(person, "Single")
                }
            }
            CultivationHelper.updateSingleBattleBonus(mPersons)

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
        val minSize = CultivationSetting.BattleSettings.ClanMinSize
        val clans = mClans.filter { it.value.clanPersonList.size > 0 }.map { it.value }.toMutableList()
        if(clans.isEmpty() || clans.size < minSize){
            showToast("Clan less than $minSize")
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
            val restClans = mutableListOf<Clan>()
            var roundNumber = 1
            while (true){
                writeHistory("Clan Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = roundClanHandler(clans, restClans,10, 10)
                if(result)
                    break
            }
            repeat(minSize){ index-> // 0 ~ 3
                val reverseIndex = minSize - index //4 ~ 1
                val clan = restClans[reverseIndex - 1]
                clan.battleRecord[mBattleRound.clan] = reverseIndex
                writeHistory("第${mBattleRound.clan}届 Clan Battle No $reverseIndex : ${clan.name}")
            }
            CultivationHelper.updateClanBattleBonus(mClans)

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
        val minSize =  CultivationSetting.BattleSettings.AllianceMinSize
        val alliances = mAlliance.filter { it.value.personList.isNotEmpty() }.map { it.value }.toMutableList()
        if(alliances.isEmpty() || alliances.size < minSize){
            showToast("Bang less than $minSize")
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
            val restAlliance = mutableListOf<Alliance>()
            var roundNumber = 1
            while (true){
                writeHistory("Bang Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = roundBangHandler(alliances, restAlliance, 20, 20)
                if(result)
                    break
            }
            repeat(minSize){ index-> // 0 ~ 15
                val reverseIndex = minSize - index //16 ~ 1
                val alliance = restAlliance[reverseIndex - 1]
                alliance.battleRecord[mBattleRound.bang] = reverseIndex
                writeHistory("第${mBattleRound.bang}届 Bang Battle No $reverseIndex : ${alliance.name}")
            }
            CultivationHelper.updateAllianceBattleBonus(mAlliance)
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
            val restNation = mutableListOf<Nation>()
            var roundNumber = 1

            while (true){
                writeHistory("Nation Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = roundNationHandler(nations, restNation, 20, 50)
                if(result)
                    break
            }
            val minSize = CultivationSetting.BattleSettings.NationMinSize
            repeat(minSize){ index-> // 0 ~ 3
                val reverseIndex = minSize - index //4 ~ 1
                val nation = mNations[restNation[reverseIndex - 1].id]!!
                nation.battleRecord[mBattleRound.nation] = reverseIndex
                writeHistory("第${mBattleRound.nation}届 Nation Battle No $reverseIndex : ${nation.name}")
            }
            CultivationHelper.updateNationBattleBonus(mNations, mPersons)

            if(!block){
                Thread.sleep(5000)
            }
            val message = Message.obtain()
            message.what = 8
            message.arg1 = if (block) 0 else 1
            mTimeHandler.sendMessage(message)
        }).start()
    }


    private fun roundSingleHandler(persons: MutableList<Person>, restPersons:MutableList<Person>, round:Int):Boolean{
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
                restPersons.add(0,secondPerson)
            }else{
                passIds.add(firstPerson.id)
                restPersons.add(0,firstPerson)
            }
        }
        return if(persons.size == 2){
            persons.removeIf { it.id == passIds[0] }
            restPersons.add(0, persons.first())
            true
        }else{
            persons.removeIf { passIds.contains(it.id) }
            false
        }
    }

    private fun roundNationHandler(nation: MutableList<Nation>, restNation: MutableList<Nation>, round:Int, count:Int):Boolean{
        nation.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until nation.size step 2){
            if( i + 1 >= nation.size){
                break
            }
            val firstNation = nation[i]
            val secondNation = nation[i+1]
            val result = roundMultiBattle(firstNation.nationPersonList, secondNation.nationPersonList, round, count)
            passIds.add(if(result) secondNation.id else firstNation.id)
            restNation.add(0, if(result) secondNation else firstNation)
        }
        return if(nation.size == 2){
            nation.removeIf { it.id == passIds[0] }
            restNation.add(0, nation.first())
            true
        }else{
            nation.removeIf { passIds.contains(it.id) }
            false
        }
    }

    private fun roundBangHandler(alliance: MutableList<Alliance>, restAlliance:MutableList<Alliance>, round:Int, count:Int):Boolean{
        alliance.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until alliance.size step 2){
            if( i + 1 >= alliance.size){
               break
            }
            val firstAlliance = alliance[i]
            val secondAlliance = alliance[i+1]
            val result = roundMultiBattle(firstAlliance.personList, secondAlliance.personList, round, count)
            passIds.add(if(result) secondAlliance.id else firstAlliance.id)
            restAlliance.add(0, if(result) secondAlliance else firstAlliance)
        }
        return if(alliance.size == 2){
            alliance.removeIf { it.id == passIds[0] }
            restAlliance.add(0, alliance.first())
            true
        }else{
            alliance.removeIf { passIds.contains(it.id) }
            false
        }
    }

    private fun roundClanHandler(clan: MutableList<Clan>, restClan: MutableList<Clan>, round:Int, count:Int):Boolean{
        clan.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until clan.size step 2){
            if( i + 1 >= clan.size){
                break
            }
            val firstClan = clan[i]
            val secondClan = clan[i+1]
            val result = roundMultiBattle(firstClan.clanPersonList, secondClan.clanPersonList, round, count)
            passIds.add(if(result) secondClan.id else firstClan.id)
            restClan.add(0, if(result) secondClan else firstClan)
        }
        return if(clan.size == 2){
            clan.removeIf { it.id == passIds[0] }
            restClan.add(0, clan.first())
            true
        }else{
            clan.removeIf { passIds.contains(it.id) }
            false
        }
    }

    //true: fiest win
    private fun roundMultiBattle(firstPersonList:ConcurrentHashMap<String, Person>, secondPersonList:ConcurrentHashMap<String, Person>, round:Int, count:Int):Boolean{
        val firstPersons = firstPersonList.filter { CultivationHelper.getProperty(it.value)[0] > 0 && it.value.type == 0 }.map { it.value }
                .sortedByDescending { it.battleWinner }.take(count).shuffled()
        val secondPersons = secondPersonList.filter { CultivationHelper.getProperty(it.value)[0] > 0 && it.value.type == 0 }.map { it.value }
                .sortedByDescending { it.battleWinner }.take(count).shuffled()

        if(firstPersons.isEmpty()){
            return false
        }else if(secondPersons.isEmpty()){
            return true
        }
        var firstIndex = 0
        var secondIndex = 0
        var firstWin = 0
        var secondWin = 0
        val maxWin = 5
        while (true) {
            if(firstWin >= maxWin){
                firstWin = 0
                firstIndex++
                if (firstIndex == firstPersons.size || firstIndex == count) {
                    return false
                }
            }
            if(secondWin >= maxWin){
                secondWin = 0
                secondIndex++
                if (secondIndex == secondPersons.size || secondIndex == count) {
                    return true
                }
            }

            val result = CultivationBattleHelper.battlePerson(null, firstPersons[firstIndex],
                    secondPersons[secondIndex], round)
            if (result) {
                secondIndex++
                firstWin++
                secondWin = 0
                if (secondIndex == secondPersons.size || secondIndex == count) {
                    return true
                }
            } else {
                firstIndex++
                secondWin++
                firstWin = 0
                if (firstIndex == firstPersons.size || firstIndex == count) {
                   return false
                }
            }
        }
    }

    private fun addBossHandler(){
        val boss = when (Random().nextInt(10)) { // 1 2 3 4 = 10
                in 0 .. 3 -> CultivationEnemyHelper.generateLiYuanBa(mAlliance["6000101"]!!)
                in 4 .. 6 -> CultivationEnemyHelper.generateShadowMao(mAlliance["6000104"]!!)
                in 7 .. 8  -> CultivationEnemyHelper.generateShadowQiu(mAlliance["6000105"]!!)
                else -> CultivationEnemyHelper.generateYaoWang(mAlliance["6000102"]!!)
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

    private fun killPersonShuffle(){
        mPersons.filter { it.value.specIdentity == 0 && isDeadException(it.value) != 1   }
                .map { it.value }.toMutableList()
                .forEach {
            if (isTrigger(10 + CultivationHelper.talentValue(it))){
                deadHandler(it)
            }
        }
    }

    private fun resetCustomBonus(){
        mPersons.filterValues { it.profile < 1000 }.forEach { (_, u) ->
            u.profile = CultivationHelper.getRandomProfile(u.gender)
        }
//        mPersons.forEach { (_: String, u: Person) ->
//            resetCustomBonusSingle(u)
//        }
    }

    private fun resetCustomBonusSingle(person: Person){
        person.followerList.clear()
        person.teji.clear()
        person.careerList = Collections.synchronizedList(mutableListOf())
        person.pointXiuWei = person.maxXiuWei
        person.equipmentList.removeIf {
            it.detail.type <= 3
        }
        person.events.removeIf {
            it.content.contains("\u5236\u9020") || it.content.contains("\u53ec\u5524") ||
                    it.content.contains("\u83B7\u5F97\u7279\u6280")  || it.content.contains("\u83b7\u5f97\u804c\u4e1a")
        }
        CultivationHelper.updatePersonEquipment(person)
    }


    fun showToast(content:String){
        if(isHidden)
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
            R.id.menu_skin->{
                val ft = supportFragmentManager.beginTransaction()
                val newFragment = FragmentDialogSkin.newInstance()
                newFragment.isCancelable = true
                newFragment.show(ft, "dialog_skin")
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
                if(msg.what == 1) {
                    val xun = msg.obj.toString().toLong()
                    val step = msg.arg1
                    activity.xunHandler(xun, step)
                }else if(msg.what == 9){
                    val xun = msg.obj.toString().toLong()
                    activity.xunMicroHandler(xun)
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