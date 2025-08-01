package com.mx.gillustrated.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.text.format.DateFormat
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.BaseInputConnection
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.CultivationHistoryAdapter
import com.mx.gillustrated.component.*
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
import com.mx.gillustrated.component.CultivationSetting.getIdentityGender
import com.mx.gillustrated.component.CultivationSetting.getIdentityIndex
import com.mx.gillustrated.component.CultivationSetting.PresetInfo
import com.mx.gillustrated.component.CultivationSetting.getAllSpecPersons
import com.mx.gillustrated.component.CultivationSetting.getIdentityType
import com.mx.gillustrated.databinding.ActivityCultivationBinding
import com.mx.gillustrated.dialog.*
import com.mx.gillustrated.service.StopService
import com.mx.gillustrated.util.*
import com.mx.gillustrated.vo.cultivation.*
import java.io.IOException
import java.lang.Exception
import java.lang.ref.WeakReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
class CultivationActivity : GameBaseActivity() {

    private var mThreadRunnable = true
    private var mHistoryThreadRunnable = true
    private var isHidden = false// Activity 是否不可见
    private var mHiddenTime = 0L
    private var mHiddenXun = 0L
    private var mLastSaveTime:LocalDateTime? = null
    private var mSpeed = 10L//流失速度
    private val mInitPersonCount = 1000//初始化Person数量
    private var readRecord = true

    private var mBoss:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    private val mTimeHandler:TimeHandler = TimeHandler(this)

    private val mExecutor:ExecutorService = Executors.newFixedThreadPool(20)
    private val mMainExecutor:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor() // 1s 为单位
    private val mHistoryExecutor:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val mMicroMainExecutor:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor() //12xun 为单位

    private lateinit var binding: ActivityCultivationBinding

    private var badgeInit = 0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCultivationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        try {
            if(Build.getSerial().contains("EMULATOR")){ //判断是否是模拟器
                pinyinMode = true
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

        initLayout()
        initListener()
        loadConfig()
        mProgressDialog.show()
        Thread{
            Thread.sleep(100)
            val backup = CultivationBakUtil.getDataFromFiles(this)
            val message = Message.obtain()
            message.what = 3
            message.obj = backup
            message.arg1 = CultivationBakUtil.findFemaleHeaderSize()
            message.arg2 = CultivationBakUtil.findMaleHeaderSize()
            mTimeHandler.sendMessage(message)
        }.start()
    }

    private fun initListener(){

        binding.btnSave.setOnClickListener {
            saveAllData()
        }
        binding.btnNation.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogNationList.newInstance()
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_nation_list")
        }
        binding.btnTime.setOnClickListener {
            if(binding.btnTime.tag == "OFF")
                setTimeLooper(true)
            else
                setTimeLooper(false)
        }
        binding.btnMenu.setOnClickListener {
            val mInputConnection = BaseInputConnection(binding.btnMenu, true)
            val down = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU)
            mInputConnection.sendKeyEvent(down)
            val up = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU)
            mInputConnection.sendKeyEvent(up)
        }
        binding.btnAlliance.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogAllianceList.newInstance()
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_alliance_list")
        }
        binding.btnList.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPersonList.newInstance()
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_person_list")
        }
        binding.btnClan.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogClanList.newInstance()
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_clan_list")
        }
    }


    override fun onPause() {
        super.onPause()
        if(!isFinishing){
            isHidden = true
            mHiddenTime = Date().time
            mHiddenXun = mCurrentXun
            val serviceIntent = Intent(this, StopService::class.java)
            serviceIntent.putExtra("badgeNumber", getBadgeNumber())
            this.startForegroundService(serviceIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        isHidden = false
        val serviceIntent = Intent(this, StopService::class.java)
        stopService(serviceIntent)
        setTextOnTop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mThreadRunnable = false
        mHistoryThreadRunnable = false
        isHidden = false
        val serviceIntent = Intent(this, StopService::class.java)
        stopService(serviceIntent)
    }

    private fun setTextOnTop(saveAction:Boolean = false){
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val saveDate = if (mLastSaveTime == null) "" else formatter.format(mLastSaveTime)
        if (!saveAction){
            if(mHiddenTime > 0){
                val duration = (Date().time - mHiddenTime) / 1000 / 60  //分钟
                binding.tvSince.text = "离开${duration}分，经过${(mCurrentXun - mHiddenXun) / 12}年, $saveDate"
            }
        }else{
            binding.tvSince.text = "上一次保存： $saveDate"
        }

    }


    private fun saveAllData(setBusyProgression:Boolean = true){
        if(setBusyProgression){
            mProgressDialog.show()
        }
        setTimeLooper(false)
        Thread{
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
                it.HP = it.HP.coerceAtMost(it.maxHP)
                it.children = it.children.filterNot { f-> getOnlinePersonDetail(f) == null }.toMutableList()
            }
            genreShuffled()
            backupInfo.persons = mPersons.mapValues { it.value.toPersonBak() }
            backupInfo.clans = mClans.mapValues { it.value.toClanBak() }
            backupInfo.nation = mNations.mapValues {
                it.value.toNationBak()
            }
            CultivationBakUtil.saveDataToFiles(this, Gson().toJson(backupInfo))
            val message = Message.obtain()
            message.what = 2
            message.arg1 = if(setBusyProgression) 0 else 1
            mTimeHandler.sendMessage(message)
            mLastSaveTime = LocalDateTime.now()
        }.start()
    }

    private fun genreShuffled(){
        mPersons.forEach { (_: String, u: Person) ->
            genreShuffledSingle(u)
        }
    }

    private fun genreShuffledSingle(u: Person){
        if (u.battleRecord.count { it.value == 1 } > 999 && !u.genres.contains("7300002")){
            u.genres.add("7300002")
            CultivationHelper.generateTips(u, mAlliance[u.allianceId]!!)
        }
        if (u.feiziFavor > 0 && !u.genres.contains("7300003")){
            u.genres.add("7300003")
            CultivationHelper.generateTips(u, mAlliance[u.allianceId]!!)
        }
        if ((mClans[u.clanId]?.crest ?: -1) == 2  && !u.genres.contains("7310001")){
            u.genres.add("7310001")
            CultivationHelper.generateTips(u, mAlliance[u.allianceId]!!)
        }
    }

    private fun temp(){
        val specConfig = getAllSpecPersons()
        mPersons.filterValues { it.specIdentity > 0 }.forEach { (_, u) ->
            val config = specConfig.find { c-> c.identity == u.specIdentity }
            if (config != null){
                if (config.profile > 0)
                    u.profile = config.profile
                else if(config.profile == 0 && u.profile > 0)
                    u.profile = 0
                u.name = config.name.first + config.name.second + CultivationSetting.createLifeTurnName(u.specIdentityTurn)
                u.fullName = config.name.first + config.name.second
                u.lastName = config.name.first

                if(config.partner == 0){
                    if( u.partner != null && getOnlinePersonDetail(u.partner)?.partner == u.id){
                        getOnlinePersonDetail(u.partner)?.partner = null
                    }
                    u.partner = null
                    u.partnerName = null
                }
                if(config.parent == null){
                    if(u.parent != null){
                        getOnlinePersonDetail(u.parent?.first)?.children?.removeIf { it == u.id }
                        getOnlinePersonDetail(u.parent?.second)?.children?.removeIf { it == u.id }
                    }
                    u.parent = null
                    u.parentName = null
                }

                if (u.partner != null && mPersons[u.partner ?: ""]?.partner == u.id){
                    mPersons[u.partner ?: ""]?.partnerName = u.name
                }
            }else if(getIdentityType(u.specIdentity) != 1) {
                deadHandler(u, true)
            }
        }


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



    }

    private fun init(json:String?){
        CultivationSetting.TEMP_SP_JIE_TURN = mSP.getInt("cultivation_jie", CultivationSetting.SP_JIE_TURN)
        CultivationSetting.TEMP_TALENT_PROTECT = mSP.getInt("cultivation_talent_protect", CultivationSetting.SP_TALENT_PROTECT)
        CultivationSetting.TEMP_DEAD_SYMBOL = mSP.getString("cultivation_dead_symbol", CultivationSetting.SP_DEAD_SYMBOL)!!
        CultivationSetting.TEMP_SKIN_BATTLE_MIN = mSP.getInt("cultivation_skin_battle_min", CultivationSetting.SP_SKIN_BATTLE_MIN)

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

            mConfig.nation.forEach { nationConfig ->
                val nationBak = backup.nation[nationConfig.id]
                if(nationBak == null){
                    mNations[nationConfig.id] = NationBak().toNation(nationConfig)
                }else{
                    mNations[nationConfig.id] = nationBak.toNation(nationConfig)
                }
            }
            mConfig.alliance.forEach { allianceConfig ->
                val allianceBak = backup.alliance[allianceConfig.id]
                if(allianceBak == null){
                    mAlliance[allianceConfig.id] = AllianceBak().toAlliance(allianceConfig)
                }else{
                    mAlliance[allianceConfig.id] = allianceBak.toAlliance(allianceConfig)
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
            if(it.value.label.contains("4100302")){
                CultivationHelper.resumeLife(it.value, mAlliance)
            }
            CultivationHelper.updatePersonEquipment(it.value)
        }
        CultivationHelper.updateAllianceBattleBonus(mAlliance)
        CultivationHelper.updateClanBattleBonus(mClans)
        CultivationHelper.updateSingleBattleBonus(mPersons)
        //CultivationHelper.updateBossBattleBonus(mPersons)
        mPersons.forEach {
           it.value.skin = CultivationHelper.generateSkinValue(it.value)
           val alliance =  mAlliance[it.value.allianceId]!!
           CultivationHelper.generateTips(it.value, alliance)
            it.value.tipsList.removeIf { tips->
                !CultivationHelper.tipsJudgment(it.value, tips.detail, alliance)
            }
           CultivationHelper.updatePersonExtraProperty(it.value, alliance)
        }
        if(out == null || out.trim() == ""){
            startWorld()
        }else{
            writeHistory("返回世界...")
        }
        badgeInit = mPersons.map { it.value }.find { it.specIdentity == 12000041 }?.lifeTurn ?: 0
        registerTimeLooper()
        registerHistoryTimeLooper()
    }


    private fun startWorld(){
        writeHistory("进入世界...")//
        addSpecPerson()
        mInitPersonCount.addMultiPerson()
    }

    private fun initLayout(){
        if(pinyinMode)
            binding.navigationCultivation.inflateMenu(R.menu.menu_nav_left_cultivation)
        else
            binding.navigationCultivation.inflateMenu(R.menu.menu_nav_left_cultivation_cn)

        binding.navigationCultivationEnd.setNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_reset -> {
                    resetHandler()
                }
                R.id.menu_reset_wtf->{
                }
                R.id.menu_shuffle->{
                }
                R.id.menu_temp->{
                    temp()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            false
        }

        binding.navigationCultivation.setNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_grace->{
                    mPersons.forEach { (_: String, u: Person) ->
                        u.xiuXei = u.xiuXei.coerceAtLeast(0)
                    }
                    showToast("不用谢")
                }
                R.id.menu_lucky ->{
                    addAmuletEquipmentEvent(null, "Lucky", 1)
                    addTipsEquipmentEvent(null, "Lucky", 1)
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
                R.id.menu_enemy_list->{
                    addBossHandler()
                }
                R.id.menu_multi->{
                    mInitPersonCount.addMultiPerson()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            false
        }
        binding.lvHistory.layoutManager = LinearLayoutManager(this)
        binding.lvHistory.itemAnimator = null
        binding.lvHistory.adapter = CultivationHistoryAdapter(
                object : CultivationHistoryAdapter.Callback{
                    override fun onItemClick(item: CultivationSetting.HistoryInfo) {
                        val ft = supportFragmentManager.beginTransaction()
                        if(item.type == 1){
                            val person = item.person!!
                            val newFragment = FragmentDialogPerson.newInstance()
                            newFragment.isCancelable = false
                            val bundle = Bundle()
                            bundle.putString("id", person.id)
                            newFragment.arguments = bundle
                            newFragment.show(ft, "dialog_person_info")
                        }else if(item.type == 2 && CultivationBattleHelper.mBattles[item.battleId] != null){
                            val newFragment = FragmentDialogBattleInfo.newInstance()
                            newFragment.isCancelable = false
                            val bundle = Bundle()
                            bundle.putString("id", item.battleId)
                            newFragment.arguments = bundle
                            newFragment.show(ft, "dialog_battle_info")
                        }
                    }
                })

        loadSkin()
    }

    override fun loadSkin(){
        val skin =  mSP.getString("cultivation_skin", "spring")
        binding.llSkin.background = when(skin){
            "spring" -> AppCompatResources.getDrawable(this,R.drawable.skin_theme_spring)
            "rain" -> AppCompatResources.getDrawable(this,R.drawable.skin_theme_rain)
            "equinox" -> AppCompatResources.getDrawable(this,R.drawable.skin_theme_spring_equinox)
            "grain_rain" -> AppCompatResources.getDrawable(this,R.drawable.skin_theme_grain_rain)
            "grain_rain2" -> AppCompatResources.getDrawable(this,R.drawable.skin_theme_grain_rain2)
            "grain_rain3" -> AppCompatResources.getDrawable(this,R.drawable.skin_theme_grain_rain3)
            "summer_begin" -> AppCompatResources.getDrawable(this,R.drawable.skin_theme_summer_begin)
            else -> AppCompatResources.getDrawable(this,R.drawable.skin_theme_spring)
        }
    }

    private fun loadConfig(){
        mConfig = Gson().fromJson(JsonFileReader.getJsonFromAssets(this,"definition.json"), Config::class.java)
    }

    private fun createAlliance() {
        mConfig.alliance.forEach { allianceConfig ->
            mAlliance[allianceConfig.id] = AllianceBak().toAlliance(allianceConfig)
        }
    }

    private fun createNation(){
        mConfig.nation.forEach { nationConfig ->
            mNations[nationConfig.id] = NationBak().toNation(nationConfig)
        }
    }



    private fun getSpecOnlinePerson(id:Int?):Person?{
        return  mPersons.map { it.value }.find { it.specIdentity == id}
    }

    fun setTimeLooper(flag:Boolean){
        if(flag){
            if(!isHidden){
                binding.btnTime.tag = "ON"
                binding.btnTime.text = resources.getString(R.string.cultivation_stop)
            }
            mThreadRunnable = true
        }else{
            if(!isHidden) {
                binding.btnTime.tag = "OFF"
                binding.btnTime.text = resources.getString(R.string.cultivation_start)
            }
            mThreadRunnable = false
        }
    }

    private fun deadHandler(it:Person, force:Boolean = false){
        mPersons.remove(it.id)
        val partner = getOnlinePersonDetail(it.partner)
        if( partner != null ){
            partner.partnerName = null
            partner.partner = null
        }
        val alliance = mAlliance[it.allianceId]

        if(it.specIdentity > 0 && alliance != null && !force){ //特殊处理SpecName
            val config = getAllSpecPersons().find { p-> p.identity == it.specIdentity } //maybe null
            if(config != null || alliance.type == 1){
                val person = when (alliance.type) {
                    0 -> //Name2
                        addSingleSpecPerson(config!!)
                    else -> addSingleSpecPerson(config!!, alliance)
                }

                if(person != null){
                    person.specIdentityTurn = it.specIdentityTurn + 1
                    person.name = person.name + CultivationSetting.createLifeTurnName(person.specIdentityTurn)
                }
            }
        }else{
            writeHistory("${getPersonBasicString(it)} 卒")
        }
    }

    // 0 dead; 1 cost 1; 2 cost reduce turn; 3 chong jing jie
    private fun isDeadException(person:Person):Int{
        if(CultivationHelper.isNeverDead(person)){
            return 1
        }else if(person.specIdentity == 0 && isTalent(person)){
            return 1
        }else if(CultivationHelper.getJingJie(person.jingJieId).color == 13  ){
            return 3
        }else if(person.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN){
            return 2
        }
        return 0
    }


    private fun xunMicroHandler(currentXun:Long){
        mExecutor.execute {
            updateInfoByXun(currentXun)
        }
    }

    private fun xunHandler(currentXun:Long, step:Int) {
        val year = "${currentXun / 12}${CultivationHelper.showing("年")}"
        if(!isHidden) {
            binding.tvDate.text = year
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
                    it.lifetime += 5000L * 12
                }
                2 -> {
                    it.lifetime += 10000L * 12
                    it.lifeTurn -= CultivationSetting.TEMP_SP_JIE_TURN
                    addPersonEvent(it,"修行失败")
                }
                3 -> {
                    it.lifetime += 20000L * 12
                }
            }
        }

        var remainingStep = step
        loop@ while (remainingStep-- > 0){
            val currentJinJie = CultivationHelper.getJingJie(it.jingJieId)
            val xiuweiGrow = getXiuweiGrow(it)
            it.maxXiuWei += xiuweiGrow
            it.pointXiuWei += xiuweiGrow
            it.xiuXei += xiuweiGrow
            if (it.xiuXei < currentJinJie.max) {
                continue@loop
            }
            val next = CultivationHelper.getNextJingJie(it.jingJieId)
            it.xiuXei = 0
            val totalSuccess = CultivationHelper.getTotalSuccess(it)
            val random = Random().nextInt(100)
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
                    if(it.jinJieColor > 4){
                        CultivationHelper.handleTipsLevel(it)
                    }
                } else {
                    val commonText = "转转成功，${personDataString[2]} $random/$totalSuccess"
                    if(it.isFav){
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
                if(it.jinJieColor > 9){
                    CultivationHelper.handleTipsLevel(it)
                }
            }
        }
    }


    private fun updateInfoByXun(currentXun:Long){
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
                addSpecPerson()
                saveAllData(false)
            }
            inDurationByXun("Xun12000", 12000) -> {
                updateBadge()
            }
            else -> randomBattle(currentXun)
        }
        updateCareerEffect(currentXun)
        updateClans(currentXun)
        updateBoss(currentXun)
    }

    private fun updateBadge(){
        if(isHidden){
            val count = getBadgeNumber()
            if (count > 0){
                BadgeUtils.setBadgeNumber(this,count)
            }
        }
    }

    private fun getBadgeNumber():Int{
        val currentNum = mPersons.map { it.value }.find { it.specIdentity == 12000041 }?.lifeTurn ?: 0
        return currentNum - badgeInit
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
        if(isHidden){
            if(CultivationHelper.mHistoryTempData.size > 100){
                CultivationHelper.mHistoryTempData.clear()
            }
            return
        }
        val list = (binding.lvHistory.adapter as CultivationHistoryAdapter).getData().toMutableList()
        if(list.size > 400 && mThreadRunnable){
            list.clear()
            CultivationBattleHelper.mBattles.clear()
        }
        val tempList = CultivationHelper.mHistoryTempData.toList()
        CultivationHelper.mHistoryTempData.clear()

        if(pinyinMode){
            tempList.forEach {
                it.content = PinyinUtil.convert(it.content)
            }
        }

        list.addAll(0, if(tempList.size > 200) tempList.subList(0, 200) else tempList)
        (binding.lvHistory.adapter as CultivationHistoryAdapter).submitList(list)
        binding.lvHistory.smoothScrollToPosition(0)
    }

    private fun Int.addMultiPerson() {
        setTimeLooper(false)
        mProgressDialog.show()
        Thread{
            var temp = this
            Thread.sleep(500)
            try {
                while(temp-- > 0){
                    mExecutor.execute(AddPersonRunnable(temp, mTimeHandler))
                }
            }catch (e:IOException) {
                mExecutor.shutdown()
            }
        }.start()
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

   override fun combinedPersonRelationship(person: Person, log:Boolean){
        CultivationHelper.joinAlliance(person, mAlliance, mPersons)
        mPersons[person.id] = person
        genreShuffledSingle(person)
        if(log){
            val extra = person.parentName
            addPersonEvent(person,"加入")
            writeHistory("${getPersonBasicString(person)} 加入${ if (extra == null) "" else " ←$extra"}", person)
        }

    }


    override fun killPerson(id:String){
        setTimeLooper(false)
        Thread{
            Thread.sleep(500)
            val person = mPersons[id]
            if(person != null){
                person.lifetime = mCurrentXun
                deadHandler(person)
            }
            val message = Message.obtain()
            message.what = 6
            mTimeHandler.sendMessage(message)
        }.start()
    }

    override fun addPersonLifetime(id:String):Boolean{
        val person = mPersons[id]
        if(person != null){
            person.lifetime += 5000L
            val commonText = "天机，寿命增加5000Xun"
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
        if(inDurationByXun("BattleBoss", weight[0].first * 12 , xun) && isTrigger(weight[0].second)) {
            addBossHandler()
        }
        if(inDurationByXun("BattleSingle", weight[1].first * 12, xun) && isTrigger(weight[1].second)) {
            battleSingleHandler(false)
        }
        if(inDurationByXun("BattleBang", weight[2].first * 12, xun) && isTrigger(weight[2].second)) {
            battleBangHandler(false)
        }
        if(inDurationByXun("BattleClan", weight[3].first * 12, xun) && isTrigger(weight[3].second)) {
            battleClanHandler(false)
        }
    }

    private fun addAmuletEquipmentEvent(person:Person? = null, tag:String = "", weight:Int = 100){
        if(isTrigger(weight)){
            val lucky = person ?: mPersons.map { it.value }.shuffled().first()
            val spec = CultivationAmuletHelper.createEquipmentCustom(0, 50)
            if(spec == null || lucky.equipmentList.find { it.id == spec.first && it.amuletSerialNo == spec.second } != null){
                return
            }
            lucky.equipmentList.add(Equipment(spec.first, spec.second))
            CultivationHelper.updatePersonEquipment(lucky)
            val equipment = CultivationAmuletHelper.getEquipmentCustom(spec.first, spec.second)
            val commonText = "$tag \u5929\u5b98\u8d50\u798f \u83b7\u5f97${equipment.second}"
            addPersonEvent(lucky, commonText)
            writeHistory("${getPersonBasicString(lucky)} $commonText", lucky)
        }
    }

    private fun addTipsEquipmentEvent(person:Person? = null, tag:String = "", weight:Int = 100){
        if(isTrigger(weight)){
            val lucky = person ?: mPersons.map { it.value }.shuffled().first()
            val tips = mConfig.tips.filter { it.type == 3 }.shuffled()[0]
            if(lucky.tipsList.find { it.id == tips.id } == null){
                lucky.tipsList.add(Tips(tips.id, 0))
                val commonText = "$tag \u83B7\u5F97\u79D8\u7C4D : ${tips.name}"
                addPersonEvent(lucky, commonText)
                writeHistory("${getPersonBasicString(lucky)} $commonText", lucky)
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
                val baseNumber = if(children.isEmpty()) 10L else (children.size * 2).toDouble().pow(5.0).toLong()
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
                        writeHistory("${u.name} 倒", u)
                        addAmuletEquipmentEvent(person, "Boss", (100f / u.type).roundToInt())
                        addTipsEquipmentEvent(person, "Boss", (1000f / u.type).roundToInt())
                        mBossRecord[u.type - 1][mBattleRound.boss[u.type - 1]] = person.id
                        //CultivationHelper.updateBossBattleBonus(mPersons)
                    }else{
                        u.remainHit --
                        val punishWeight = mSP.getInt("cultivation_punish_boss_million", CultivationSetting.SP_PUNISH_BOSS_MILLION)
                        person.xiuXei -= u.type * punishWeight * 10000
                        if(u.remainHit <= 0){
                            writeHistory("${u.name} 消失", u)
                        }
                    }
                }
            }
        }
    }

    private fun updateClans(xun:Long){
        if(inDurationByXun("ClanCreated", 480, xun)) {
            mPersons.filter { it.value.ancestorOrignId != null && it.value.clanId == "" }
                .map { it.value }.toMutableList()
                .groupBy { it.ancestorOrignId }.forEach { (originId, list) ->
                    if (list.size >= 5 && mPersons[originId] != null ) {
                        val newClan = CultivationHelper.createClan(mPersons[originId]!!, mClans)
                        for ((_, u) in mPersons.filterValues { it.ancestorOrignId == originId }) {
                            u.clanId = newClan.id
                            u.clanHierarchy = u.ancestorOrignLevel
                        }
                    }
                }
        }
        if(inDurationByXun("ClanUpdated", 4800, xun)) {
            mClans.forEach {
                val clanPersons = mPersons.filterValues { p -> p.clanId == it.key }
                if(clanPersons.isEmpty()){
                    mClans.remove(it.key)
                }else{
                    if(getOnlinePersonDetail(it.value.elder) == null){
                        val elder = clanPersons.map { p -> p.value }.minBy { p-> p.clanHierarchy }
                        it.value.elder = elder.id
                        it.value.zhu = elder
                    }
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
                val supplement = abs(currentHP)
                if(it.lifetime - mCurrentXun > supplement * 12 + 2000){
                    it.lifetime -= supplement * 12
                    it.HP += supplement
                }
            }
            if(it.HP > it.maxHP )
                it.HP = it.maxHP
        }
    }

    private fun fillingHP(){
        for ((_: String, it: Person) in mPersons) {
            val currentHP = CultivationHelper.getProperty(it)[0]
            if(currentHP < 0){
                val supplement = abs(currentHP - 10)
                it.HP += supplement
            }
        }
    }


    private fun updateCareer(){
        for ((_: String, person: Person) in mPersons) {
            val list = person.careerList
            var addonCareer:CareerConfig? = null
            if(list.size == 0 || list.all { it.level >= it.detail.maxLevel }){
                if(person.pointXiuWei > 50000000) {
                    person.pointXiuWei -= 50000000
                    if(isTrigger(5.0.pow(list.size.toDouble()).toInt())){
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
                        if(equipment != null && equipment.detail.rarity > (person.equipmentList.filter { it.detail.type == equipmentType }.maxByOrNull { it.detail.rarity }?.detail?.rarity ?: 0)){
                            person.equipmentList.removeIf { it.detail.type == equipmentType }
                            person.equipmentList.add(equipment)
                            val commonText = "\u5236\u9020 : ${equipment.detail.name}"
                            CultivationHelper.updatePersonEquipment(person)
                            if(equipment.detail.rarity >= 8)
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
                        if(person.teji.mapNotNull { m-> mConfig.teji.find { it.id == m } }.filter { it.type < 4 }.size >= max(3, career.level / 20)  )
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
                if((currentPartner?.specIdentity
                                ?: 0) > 0 && currentPartner?.specIdentity != p.partner){
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

            if(alliance != null){
                CultivationHelper.joinFixedAlliance(person, alliance)
            }else{
                CultivationHelper.joinAlliance(person, mAlliance, mPersons)
                person.singled = true
            }
            addPersonEvent(person,"加入")
            writeHistory("${getPersonBasicString(person)} 加入", person)
            person.equipmentList = CultivationHelper.getSpecPersonEquipment(person)
            CultivationHelper.updatePersonEquipment(person)
            genreShuffledSingle(person)
            return person
        }
        return null
    }

    private fun resetHandler(){
        setTimeLooper(false)
        mProgressDialog.show()
        CultivationHelper.mHistoryTempData.clear()
        (binding.lvHistory.adapter as CultivationHistoryAdapter).submitList(mutableListOf())
        Thread{
            Thread.sleep(1000)
            mCurrentXun = 0
            mClans.clear()
            mPersons.clear()
            mAlliance.clear()
            mNations.clear()
            mBattleRound = BattleRound()
            mXunDuration = ConcurrentHashMap()
            createAlliance()
            createNation()
            val message = Message.obtain()
            message.what = 4
            mTimeHandler.sendMessage(message)
        }.start()
    }



    private fun battleSingleHandler(block:Boolean = true){
        val minSize = CultivationSetting.BattleSettings.SingleMinSize
        if(mPersons.size < minSize){
            showToast("persons less than $minSize")
            return
        }
        setTimeLooper(false)
        if(block){
            CultivationHelper.mHistoryTempData.clear()
            (binding.lvHistory.adapter as CultivationHistoryAdapter).submitList(mutableListOf())
        }
        Thread{
            Thread.sleep(500)
            fillingHP()
            val personsAll = mPersons.filter { CultivationHelper.getProperty(it.value)[0] > 0 }.map { it.value }.toMutableList()
            personsAll.shuffle()
            mBattleRound.single++
            writeHistory("第${mBattleRound.single}届  Single Battle Start")
            val restPersons = mutableListOf<Person>()
            var roundNumber = 1
            while (true){
                writeHistory("Single Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = 40.roundSingleHandler(personsAll, restPersons)
                if(result)
                    break
            }
            repeat(minSize){ index-> //
                val reverseIndex = minSize - index //32 ~ 1
                val person = restPersons[reverseIndex - 1]
                person.battleRecord[mBattleRound.single] = reverseIndex
                writeHistory("第${mBattleRound.single}届 Single Battle No $reverseIndex : ${person.name}", person)
                addAmuletEquipmentEvent(person, "Single", reverseIndex * 100)
            }
            CultivationHelper.updateSingleBattleBonus(mPersons)

            if(!block){
                Thread.sleep(5000)
            }
            val message = Message.obtain()
            message.what = 8
            message.arg1 = if (block) 0 else 1
            mTimeHandler.sendMessage(message)
        }.start()
    }

    private fun battleClanHandler(block: Boolean = true){
        val minSize = CultivationSetting.BattleSettings.ClanMinSize
        val clans = mClans.filter { mPersons.filterValues { p-> p.clanId == it.value.id }.isNotEmpty() }
            .map { it.value }.toMutableList()
        if(clans.isEmpty() || clans.size < minSize){
            showToast("Clan less than $minSize")
            return
        }
        setTimeLooper(false)
        if(block){
            CultivationHelper.mHistoryTempData.clear()
            (binding.lvHistory.adapter as CultivationHistoryAdapter).submitList(mutableListOf())
        }
        Thread{
            Thread.sleep(500)
            fillingHP()
            mBattleRound.clan++
            writeHistory("第${mBattleRound.clan}届 Clan Battle Start")
            val restClans = mutableListOf<Clan>()
            var roundNumber = 1
            while (true){
                writeHistory("Clan Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = 10.roundClanHandler(clans, restClans, 10)
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
        }.start()
    }

    private fun battleBangHandler(block: Boolean = true){
        val minSize =  CultivationSetting.BattleSettings.AllianceMinSize
        val alliances = mAlliance.filter { mPersons.filterValues { p-> p.allianceId == it.value.id }.isNotEmpty()  }
            .map { it.value }.toMutableList()
        if(alliances.isEmpty() || alliances.size < minSize){
            showToast("Bang less than $minSize")
            return
        }
        setTimeLooper(false)
        if(block){
            CultivationHelper.mHistoryTempData.clear()
            (binding.lvHistory.adapter as CultivationHistoryAdapter).submitList(mutableListOf())
        }
        Thread{
            Thread.sleep(500)
            mBattleRound.bang++
            writeHistory("第${mBattleRound.bang}届 Bang Battle Start")
            val restAlliance = mutableListOf<Alliance>()
            var roundNumber = 1
            while (true){
                writeHistory("Bang Battle ${roundNumber}轮 Start")
                roundNumber++
                val result = 20.roundBangHandler(alliances, restAlliance, 20)
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
        }.start()
    }

    private fun Int.roundSingleHandler(persons: MutableList<Person>, restPersons: MutableList<Person>):Boolean{
        persons.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until persons.size step 2) {
            if (i + 1 >= persons.size) {
                break
            }
            val firstPerson = persons[i]
            val secondPerson = persons[i + 1]
            val result = CultivationBattleHelper.battlePerson(null, firstPerson, secondPerson, this)
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

    private fun Int.roundBangHandler(alliance: MutableList<Alliance>, restAlliance: MutableList<Alliance>, count: Int):Boolean{
        alliance.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until alliance.size step 2){
            if( i + 1 >= alliance.size){
               break
            }
            val firstAlliance = alliance[i]
            val secondAlliance = alliance[i+1]
            val result = roundMultiBattle(getAlliancePersonList(firstAlliance.id), getAlliancePersonList(secondAlliance.id), this, count)
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

    private fun Int.roundClanHandler(clan: MutableList<Clan>, restClan: MutableList<Clan>, count: Int):Boolean{
        clan.shuffle()
        val passIds = mutableListOf<String>()
        for (i in 0 until clan.size step 2){
            if( i + 1 >= clan.size){
                break
            }
            val firstClan = clan[i]
            val secondClan = clan[i+1]
            val result = roundMultiBattle(getClanPersonList(firstClan.id), getClanPersonList(secondClan.id), this, count)
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
        val firstPersonsBase = firstPersonList.filter { CultivationHelper.getProperty(it.value)[0] > 0 && it.value.type == 0 }.map { it.value }
                .sortedByDescending { it.battleWinner }.take(count)
        val secondPersonsBase = secondPersonList.filter { CultivationHelper.getProperty(it.value)[0] > 0 && it.value.type == 0 }.map { it.value }
                .sortedByDescending { it.battleWinner }.take(count)
        firstPersonsBase.forEachIndexed { index, person ->
            person.battleMaxWin = if (index < 2 ) 10 else 5
        }
        secondPersonsBase.forEachIndexed { index, person ->
            person.battleMaxWin = if (index < 2 ) 10 else 5
        }
        val firstPersons = firstPersonsBase.shuffled()
        val secondPersons = secondPersonsBase.shuffled()

        if(firstPersons.isEmpty()){
            return false
        }else if(secondPersons.isEmpty()){
            return true
        }
        var firstIndex = 0
        var secondIndex = 0
        var firstWin = 0
        var secondWin = 0
        var firstMaxWin = firstPersons[firstIndex].battleMaxWin
        var secondMaxWin = secondPersons[secondIndex].battleMaxWin
        while (true) {
            if(firstWin >= firstMaxWin){
                firstWin = 0
                firstIndex++
                if (firstIndex == firstPersons.size || firstIndex == count) {
                    return false
                }
                firstMaxWin = firstPersons[firstIndex].battleMaxWin
            }
            if(secondWin >= secondMaxWin){
                secondWin = 0
                secondIndex++
                if (secondIndex == secondPersons.size || secondIndex == count) {
                    return true
                }
                secondMaxWin = secondPersons[secondIndex].battleMaxWin
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
                secondMaxWin = secondPersons[secondIndex].battleMaxWin
            } else {
                firstIndex++
                secondWin++
                firstWin = 0
                if (firstIndex == firstPersons.size || firstIndex == count) {
                   return false
                }
                firstMaxWin = firstPersons[firstIndex].battleMaxWin
            }
        }
    }

    private fun addBossHandler(){
        val boss = CultivationEnemyHelper.generateBoss(mAlliance)
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


    override fun showToast(content:String){
        if(isHidden)
            return
        try {
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            Log.e("CultivationActivity", e.message ?: "")
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

        class TimeHandler constructor(val context: CultivationActivity):Handler(Looper.getMainLooper()){

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
                    activity.setTextOnTop(true)
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