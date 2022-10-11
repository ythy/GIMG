package com.mx.gillustrated.component

import android.annotation.SuppressLint
import com.mx.gillustrated.component.CultivationSetting.HistoryInfo
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("SetTextI18n")
object CultivationHelper {

    lateinit var mConfig:Config
    lateinit var mBattleRound:BattleRound
    lateinit var mXunDuration:ConcurrentHashMap<Pair<String, Int>, Long>
    var maxFemaleProfile = 0 // 默认0号 1001开始不随机使用
    var maxMaleProfile = 0 // 默认0号 1001开始不随机使用
    var pinyinMode:Boolean = false //是否pinyin模式
    var mCurrentXun:Long = 0//当前时间
    var mHistoryTempData:MutableList<HistoryInfo> = Collections.synchronizedList(mutableListOf())

    fun writeHistory(content:String){
        mHistoryTempData.add(0, HistoryInfo(0, content, null, null))
    }

    fun writeHistory(content:String, person: Person?){
        mHistoryTempData.add(0, HistoryInfo(1, content, person, null))
    }

    fun writeHistory(content:String, battleId: String?){
        mHistoryTempData.add(0, HistoryInfo(2, content, null, battleId))
    }

    fun joinAlliance(person: Person, allAlliance:ConcurrentHashMap<String, Alliance>){
        val options = if(person.lingGenId == "") {
            allAlliance.filter { it.value.level == 1 && it.value.type == 0 }.filter { person.lingGenName.indexOf(it.value.lingGen!!) >= 0 }.map { it.value }.toMutableList()
        }else{
            allAlliance.filter { it.value.level == 1 && it.value.type == 0 }.map { it.value }.toMutableList()
        }
        options.addAll(allAlliance.filter { it.value.level > 1 && person.tianfus.filter { f->f.rarity >=2 }.size >= it.value.tianfu  && it.value.personList.size < it.value.maxPerson }.map { it.value })
        val random = Random().nextInt(options.map { 100 / it.level }.sum() )
        var count = 0
        for (i in 0 until options.size){
            val alliance = options[i]
            count += 100 / alliance.level
            if(random < count){
                alliance.personList[person.id] = person
                person.allianceId = alliance.id
                person.allianceName = alliance.name
                person.allianceSuccess = alliance.success
                person.allianceProperty = alliance.property
                person.extraXuiweiMulti = getExtraXuiweiMulti(person, alliance)
                person.lifetime = person.age + (person.lifetime - person.age) * ( 100 + alliance.lifetime ) / 100
                person.nationId = alliance.nation
                break
            }
        }
    }

    fun joinFixedAlliance(person: Person, alliance:Alliance, changed:Boolean = false){
        alliance.personList[person.id] = person
        person.allianceId = alliance.id
        person.allianceName = alliance.name
        person.allianceSuccess = alliance.success
        person.allianceProperty = alliance.property
        person.extraXuiweiMulti = getExtraXuiweiMulti(person, alliance)
        person.lifetime = person.age + (person.lifetime - person.age) * ( 100 + alliance.lifetime ) / 100
        if(alliance.type >= 3 && !changed){
            person.singled = true
            person.dink = true
        }
        person.nationId = alliance.nation
    }

    fun changedToFixedAlliance(person: Person, allAlliance:ConcurrentHashMap<String, Alliance>, newAlliance:Alliance){
        val originAlliance = allAlliance[person.allianceId]!!
        originAlliance.personList.remove(person.id)
        if(originAlliance.zhuPerson == person)
            originAlliance.zhuPerson = null
        person.equipmentList.removeIf { it.first == "7006101" || it.first == "7006102" }
        joinFixedAlliance(person, newAlliance, true)
    }

    fun updateAllianceGain(allAlliance:ConcurrentHashMap<String, Alliance>, updated:Boolean = false){
        allAlliance.forEach { data->
            val alliance = data.value
            val alivePersons = alliance.personList
            if(alivePersons.isNotEmpty()){
                if(updated) {
                    //updateHuInAlliance(alliance, alivePersons)
                    updateZhuInAlliance(alliance, alivePersons)
                    updateG1InAlliance(alliance, alivePersons)
                }
            }else{
                alliance.zhuPerson = null
            }
        }
    }

    private fun updateZhuInAlliance(alliance: Alliance, persons:ConcurrentHashMap<String, Person>){
        if(alliance.zhuPerson != null){
            return
        }
        val personList = Collections.synchronizedList( persons.map { it.value }.filter { it.type == 0 })
        alliance.zhuPerson = personList.sortedBy { it.lastBirthDay }.first()
    }

    private fun updateG1InAlliance(alliance: Alliance, persons:ConcurrentHashMap<String, Person>){
        val total = Math.min(10, Math.max(1, persons.size / 4))
        val personList =  Collections.synchronizedList( persons.map { it.value }.filter { it.type == 0 })
        alliance.speedG1PersonList.clear()
        personList.sortByDescending { it.extraSpeed }
        for (i in 0 until total){
            alliance.speedG1PersonList[personList[i].id] = personList[i]
        }

    }

    private fun getTianFu(parent: Pair<Person, Person>?, fixedTianfus:MutableList<String>?, tianFuWeight:Int):MutableList<TianFu>{
        if(fixedTianfus != null && fixedTianfus.isNotEmpty()){
            return fixedTianfus.map { getPersonTianfu(it)!! }.toMutableList()
        }
        val tianFus = mutableListOf<TianFu>()
        mConfig.tianFuType.groupBy { it.type }.forEach { (_, l) ->
            var data: TianFu? = null
            val sortU = l.sortedByDescending { it.weight }
            for (i in 0 until sortU.size){
                if(tianFuWeight > 1 && sortU[i].rarity < 3)
                    continue
                if(Random().nextInt( Math.max(1, sortU[i].weight / tianFuWeight )) == 0){
                    data = sortU[i]
                    break
                }
            }
            if(data != null){
                tianFus.add(data)
            }else if(parent != null){
                for (i in 0 until sortU.size){
                    var addonWeight = 1
                    val extraFirst = parent.first.tianfus.find { it.type == sortU[i].type }
                    val extraSecond = parent.second.tianfus.find { it.type == sortU[i].type }
                    if(extraFirst != null && extraFirst.rarity >= 3 )
                        addonWeight += extraFirst.rarity
                    if(extraSecond != null && extraSecond.rarity >= 3)
                        addonWeight += extraSecond.rarity
                    if(Random().nextInt(sortU[i].weight/addonWeight) == 0){
                        data = sortU[i]
                        break
                    }
                }
                if(data != null){
                    tianFus.add(data)
                }
            }
        }
        return tianFus
    }

    private fun getLingGenDetail(id:String):Triple<LingGen, String, String>{
        val lingGen = mConfig.lingGenType.find { it.id == id }!!
        var lingGenName = ""
        var lingGenId = ""
        if (lingGen.type > 0) {
            val arr = mConfig.lingGenTian.filter { it.type == lingGen.type }
            val tianIndex = Random().nextInt(arr.size)
            lingGenId = arr[tianIndex].id
            lingGenName = arr[tianIndex].name
        } else {
            var type = mutableListOf("金", "水", "木", "火", "土")
            var typeNum = 5
            var total = lingGen.id.substring(6).toInt()
            while (total > 0){
                val index = Random().nextInt(typeNum)
                val selectType = type[index]
                lingGenName += selectType
                type = type.filter { it != selectType}.toMutableList()
                total--
                typeNum--
            }
        }
        return Triple(lingGen, lingGenId, lingGenName)
    }

    private fun getParentWeightForLingGen(person: Person?):Int{
        return  when(person?.lingGenType?.type ?: 0) {
            1 -> 5000
            2 -> 500
            3 -> 200
            4 -> 100
            5 -> 1
            else -> 10000
        }
    }

    private fun getLingGen(parent: Pair<Person, Person>?, lingGenTypeFixed:String? = null, lingGenWeight:Int):Triple<LingGen, String, String>{
        if(lingGenTypeFixed != null){
            return getLingGenDetail(lingGenTypeFixed)
        }
        val lingGenName: String
        val lingGenId: String
        var lingGen: LingGen? = null
        val firstNumber = getParentWeightForLingGen(parent?.first)
        val secondNumber = getParentWeightForLingGen(parent?.second)
        if(parent != null && isTrigger(40000 / (firstNumber + secondNumber))){
            val selectNumber = Random().nextInt(firstNumber + secondNumber)
            val maxPerson:Person = if(selectNumber < firstNumber){
                parent.first
            }else{
                parent.second
            }
            lingGen = maxPerson.lingGenType
            lingGenId = maxPerson.lingGenId
            lingGenName = maxPerson.lingGenName
        }else{
            val lingGenList = mConfig.lingGenType.sortedByDescending { it.randomBasic }
            for (i in 0 until lingGenList.size){
                val weight = Math.max(1, lingGenList[i].randomBasic / lingGenWeight)
                if(Random().nextInt(weight) == 0){
                    lingGen = lingGenList[i]
                    break
                }
            }
            val info = getLingGenDetail(lingGen!!.id)
            lingGenId = info.second
            lingGenName = info.third
        }
        return Triple(lingGen, lingGenId, lingGenName)
    }

     fun getTeji(weight:Int = 1, multi:Boolean = true):MutableList<String>{
         val result = mutableListOf<String>()
         if(!multi){
             val teji = mConfig.teji.filter { it.type != 4 }.shuffled()[0]
             if(isTrigger( teji.weight / Math.max(1, weight))){
                 result.add(teji.id)
             }
         }else{
             mConfig.teji.filter { it.type != 4 }.forEach {
                 if(isTrigger( it.weight / Math.max(1, weight))){
                     result.add(it.id)
                 }
             }
         }
        return result
    }

    fun getCareer():MutableList<String>{
        val result = mutableListOf<String>()
        mConfig.career.sortedByDescending { it.rarity }.forEach {
            if(result.isEmpty() && Random().nextInt( it.weight ) == 0){
                result.add(it.id)
            }
        }
        return result
    }

    fun makeEquipment(type: Int, weight: Int):Equipment?{
        val list = mConfig.equipment.filter { it.type == type && it.rarity * 10 < weight }.map {
            it.copy()
        }.shuffled()
        return if(list.isEmpty())
            null
        else{
            val equipment = list[0]
            val success = Random().nextInt( (Math.pow(equipment.rarity.toDouble(), 5.0) / Math.log(weight.toDouble())).toInt() ) == 0
            if(success)
                equipment
            else
                null
        }
    }

    fun makeFollower(weight: Int):Follower?{
        val list = mConfig.follower.filter { it.rarity * 10 < weight && it.type == 0 }.map {
            it.copy()
        }.shuffled()
        return if(list.isEmpty())
            null
        else{
            val follower = list[0]
            val success = Random().nextInt( (Math.pow(follower.rarity.toDouble(), 5.0) / Math.log(weight.toDouble())).toInt() ) == 0
            if(success)
                follower
            else
                null
        }
    }

    fun getPersonInfo(name:Pair<String, String?>?, gender: NameUtil.Gender?,
                              lifetime:Long = 100, parent:Pair<Person, Person>? = null, fav:Boolean = false, mix: CultivationSetting.PersonFixedInfoMix? = null): Person {
        val personGender = gender ?: when (Random().nextInt(2)) {
            0 -> NameUtil.Gender.Male
            else -> NameUtil.Gender.Female
        }
        val personName = if(name != null)
            if(name.second != null) name else NameUtil.getChineseName(name.first, personGender)
        else
            NameUtil.getChineseName(null, personGender)

        val lingGen = getLingGen(parent, mix?.lingGenId, mix?.lingGenWeight ?: 1)
        val tianFus = getTianFu(parent, mix?.tianFuIds, mix?.tianFuWeight ?: 1)
        val birthDay:Pair<Long, Long> = Pair(mCurrentXun, 0)
        val result = Person()
        result.id =  UUID.randomUUID().toString()
        result.name = personName.first + personName.second
        result.lastName = personName.first
        result.gender = personGender
        result.lingGenType = lingGen.first
        result.lingGenName = lingGen.third
        result.lingGenId = lingGen.second
        result.birthDay.add(birthDay)
        result.lastBirthDay = mCurrentXun
        setPersonJingjie(result)
        result.profile = if(fav) 1001 else getRandomProfile(result.gender)
        result.isFav = fav
        result.tianfus = tianFus
        result.teji = Collections.synchronizedList(getTeji())
        result.careerList = Collections.synchronizedList(getCareer().map { Triple(it, 0, "") })
        result.lifetime = lifetime + (tianFus.find { it.type == 3 }?.bonus ?: 0)
        updatePersonExtraProperty(result)

        if(parent != null){
            result.parent = Pair(parent.first.id, parent.second.id)
            result.parentName = Pair(parent.first.name, parent.second.name)
            result.ancestorLevel = parent.first.ancestorLevel + 1
            result.ancestorId = parent.first.ancestorId ?: parent.first.id
        }else{
            result.ancestorId = result.id
        }

        return result
    }

    fun getRandomProfile(gender: NameUtil.Gender, profile:Int = 0):Int{
        if (gender == NameUtil.Gender.Female && profile == 0 && maxFemaleProfile > 1) {
            return Random().nextInt(maxFemaleProfile - 1) + 1
        }
        else if (gender == NameUtil.Gender.Male && profile == 0 && maxMaleProfile > 1) {
            return Random().nextInt(maxMaleProfile - 1) + 1
        }
        return profile
    }

    fun updatePersonInborn(person: Person, tianFuWeight: Int = 1, lingGenWeight: Int = 1, alliance: Alliance? = null){
        val lingGen = getLingGen(null, null, lingGenWeight)
        val tianFus = getTianFu(null, null, tianFuWeight)
        person.lingGenType = lingGen.first
        person.lingGenName = lingGen.third
        person.lingGenId = lingGen.second
        person.tianfus = tianFus
        updatePersonExtraProperty(person, alliance)
    }

    fun setPersonJingjie(person: Person, level: Int = 0){
        val initJingJie = mConfig.jingJieType[level]
        person.jingJieId = initJingJie.id
        person.jinJieName = getJinJieName(initJingJie.name)
        person.jinJieColor = initJingJie.color
        person.jingJieSuccess = initJingJie.success
        person.jinJieMax = initJingJie.max
    }

    fun getPersonBasicString(person:Person):String{
        return "${person.name} (${person.age}/${person.lifetime}:${person.jinJieName}) ${person.lingGenName} "
    }

    fun updatePersonExtraProperty(person: Person, alliance: Alliance? = null){
        val tianFus = person.tianfus.map { getPersonTianfu(it.id)!! }
        person.tianfus = tianFus.toMutableList()
        person.extraXiuwei = tianFus.find { it.type == 1 }?.bonus ?: 0
        person.extraTupo = tianFus.find { it.type == 4 }?.bonus ?: 0
        person.extraSpeed = tianFus.find { it.type == 5 }?.bonus ?: 0
        person.extraProperty = mConfig.lingGenTian.find { it.id == person.lingGenId }?.property ?: mutableListOf(0,0,0,0,0,0,0,0)
        person.extraXuiweiMulti =  getExtraXuiweiMulti(person, alliance)
    }

    //0 ~ 5
    //20220927 nation add bonus：emperor attack + 100 hp + 200, taiwei hp + 200, shangshu attack + 100, cishi hp + 100, duwei attack + 50
    //
    fun getProperty(person: Person):MutableList<Int>{
        val property = person.extraProperty.mapIndexed { index, it ->
            it + person.allianceProperty[index] + person.equipmentProperty[index]
        }
        val nationAttack = if(person.nationPost == 1) 100 else if(person.nationPost == 3 ) 100 else if(person.nationPost == 5 ) 50 else 0
        val nationHP = if(person.nationPost == 1) 200 else if(person.nationPost == 2 ) 200 else if(person.nationPost == 4 ) 100 else 0

        val jingJieLevel = getJingJieLevel(person.jingJieId)
        val extraHP = jingJieLevel.first + 10 * jingJieLevel.second + property[0] + nationHP// 0 ~ 70 + 80
        val attack =  5 * jingJieLevel.second +  property[1] + nationAttack// yuan 2, hua 4, he 4,di 5, tai 6, zhun 7, di 8 // 0 ~ 40
        val defence = 5 * jingJieLevel.second +  property[2] // 0 ~ 40
        val speed =   5 * jingJieLevel.second + property[3] // 0 ~ 40

        //val multipleSpeed = Math.max((person.HP + extraHP).toFloat()/( person.maxHP + extraHP).toFloat(), 0.1f)
       // val multiplePrimary = Math.max((person.HP + extraHP).toFloat()/( person.maxHP + extraHP).toFloat(), 0.5f)
        return mutableListOf(person.HP + extraHP, person.maxHP + extraHP,
                person.attack + attack,  person.defence + defence, person.speed + speed, extraHP)
    }

    //type 11,12,13,14 -> B,C,S,E
    fun gainJiEquipment(person:Person, type:Int, level:Int = 0, round:Int = 0){
        val equipment = mConfig.equipment.filter{ it.type == type}.sortedBy { it.id }[level]
        person.equipmentList.add(Triple(equipment.id, round, ""))
        updatePersonEquipment(person)
    }

    fun updatePersonEquipment(person:Person){
        val equipments = person.equipmentList.mapNotNull { mConfig.equipment.find { e-> e.id == it.first } }
        person.equipmentXiuwei = 0
        person.equipmentSuccess = 0
        person.equipmentProperty =  mutableListOf(0,0,0,0,0,0,0,0)
        if(equipments.isNotEmpty()){
            equipments.filter { it.type > 3 }.groupBy { it.id }.forEach { (_, u) ->
                for (index in 0 until u.size){
                    val effectEquipment = u[index]
                    if( index > getEquipmentsMaxCount(effectEquipment, u.size)){
                        break
                    }
                    summationEquipmentValues(person, effectEquipment)
                }
            }
            equipments.filter { it.type <= 3 }.groupBy { it.type }.forEach { (_, u) ->
                val effectEquipment = u.maxBy { it.rarity }!!
                summationEquipmentValues(person, effectEquipment)
            }
        }
    }

    fun getEquipmentsMaxCount(equipment: Equipment, size:Int):Int{
        val maxCount = if(equipment.type == 11 || equipment.type == 12 || equipment.type == 13) equipment.maxCount else 1
        return Math.max(maxCount, Math.round( Math.log(size.toDouble())).toInt())
    }

    private fun summationEquipmentValues(person: Person, effectEquipment: Equipment){
        person.equipmentXiuwei += effectEquipment.xiuwei
        person.equipmentSuccess += effectEquipment.success
        effectEquipment.property.forEachIndexed { pi, pp ->
            person.equipmentProperty[pi] += pp
        }
    }

    //20220927 added nation bonus: cishi + 100, duwei + 50
    fun getXiuweiGrow(person:Person, allAllianceMap:ConcurrentHashMap<String, Alliance>):Int{
        val alliance = allAllianceMap[person.allianceId] ?: return 0
        return getXiuweiGrow(person, alliance)
    }

    fun getXiuweiGrow(person:Person, alliance: Alliance):Int{
        synchronized(person){
            person.allianceXiuwei = alliance.xiuwei
            if(alliance.speedG1PersonList.contains(person.id)){
                person.allianceXiuwei += alliance.speedG1
            }
            if(person.id == alliance.zhuPerson?.id){
                person.allianceXiuwei += 50
            }
        }
        val postXiuwei = getNationXiuwei(person)
        val basic = person.lingGenType.qiBasic + person.extraXiuwei + person.allianceXiuwei + person.equipmentXiuwei + postXiuwei
        val multi = (person.extraXuiweiMulti + 100).toDouble() / 100
        return (basic * multi).toInt()
    }

    fun getNationXiuwei(person: Person):Int{
        return when {
            person.nationPost == 4 -> 100
            person.nationPost == 5 -> 50
            else -> 0
        }
    }

    fun getTotalSuccess(person:Person):Int{
        val tianfuSuccess = person.extraTupo
        val allianceSuccess = person.allianceSuccess
        val currentSuccess = person.jingJieSuccess
        val equipmentSuccess = person.equipmentSuccess
        return currentSuccess + tianfuSuccess + allianceSuccess + equipmentSuccess
    }

    fun getExtraXuiweiMulti(person:Person, alliance:Alliance? = null):Int{
        val tianfu = person.tianfus.find { it.type == 2 }
        val tianValue = getPersonTianfu(tianfu?.id)?.bonus ?: 0
        val allianceValue = alliance?.xiuweiMulti ?: 0
        return tianValue + allianceValue
    }

    fun getPersonTianfu(id:String?):TianFu?{
        if(id == null)
            return null
        return mConfig.tianFuType.find { it.id == id }
    }

    fun addPersonEvent(person:Person, content:String){
        val personEvent = PersonEvent()
        personEvent.nid = UUID.randomUUID().toString()
        personEvent.happenTime = mCurrentXun
        personEvent.content = content
        person.events.add(personEvent)
    }

    fun updatePartner(allPerson:ConcurrentHashMap<String, Person>){
        val males = allPerson.filter { !it.value.singled && it.value.gender == NameUtil.Gender.Male && it.value.partner == null && (it.value.lifetime - it.value.age > 200) }.map { it.value }.toMutableList()
        val females =  Collections.synchronizedList(allPerson.filter { !it.value.singled && it.value.gender == NameUtil.Gender.Female && it.value.partner == null && (it.value.lifetime - it.value.age > 200) }.map { it.value })
        if(males.size > 5 && females.size > 5){
            synchronized(females){
                val man = males[Random().nextInt(males.size)]
                val manAge = man.age
                val womanPair = females.map { Pair(it.id, it.age) }.sortedBy { Math.abs(manAge - it.second) }[0]
                val woman = allPerson[womanPair.first]
                if(woman == null || ( man.ancestorId != null && woman.ancestorId != null && man.ancestorId == woman.ancestorId))
                    return
                createPartner(man, woman)
            }
        }
    }


    fun createPartner(man:Person, woman:Person){
        man.partner = woman.id
        man.partnerName = woman.name
        woman.partner = man.id
        woman.partnerName = man.name
        addPersonEvent(man,"与${woman.name}结伴")
        addPersonEvent(woman,"与${man.name}结伴")
        writeHistory("${getPersonBasicString(man)} 与 ${getPersonBasicString(woman)} 结伴了")
    }


    fun getJingJieLevel(id:String):Triple<Int, Int, Int>{
        val list = mConfig.jingJieType.filter { it.color > 0 }
        val current = list.find { it.id == id }
        return if(current != null){
            Triple(list.indexOf(current), current.color, id.toInt() % 10)
        }else{
            Triple(-1, 0, id.substring(1).toInt())
        }
    }

    fun getYearString(xun:Long = mCurrentXun):String{
        val wan = Math.max(1, xun / 12 / 10000)
        return "$wan${showing("万年")}"
    }

    fun showing(input:String):String{
        return if (pinyinMode) PinyinUtil.convert(input.trim()) else input.trim()
    }

    fun isTrigger(weight:Int = 2):Boolean{
        return Random().nextInt(Math.max(1, weight)) == 0
    }

    // symbol 需要唯一
    fun inDurationByXun(symbols:String, duration:Int, currentXun:Long = mCurrentXun):Boolean{
        val lastXun = mXunDuration[Pair(symbols, duration)]
        if(lastXun == null){
            mXunDuration[Pair(symbols, duration)] = currentXun
            return false
        }else if (currentXun - lastXun >= duration){
            val newLastXun = lastXun +  ((currentXun - lastXun) / duration).toInt() * duration
            mXunDuration[Pair(symbols, duration)] = newLastXun
            return true
        }
        return false
    }

    fun getJinJieName(input:String):String{
        if(pinyinMode)
            return input
        val split = input.split("-")
        val prefix = split[0]
        val grade = split[1].toInt()
        return if(prefix == "LianQi"){
            NameMapper[prefix] + (if(grade<10) "$grade\u5c42" else "\u5706\u6ee1")
        }else{
            (NameMapper[prefix] ?: prefix) + LevelMapper[grade]
        }
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

    private val LevelMapper = mapOf(
            1 to "初期", 2 to "中期", 3 to "后期", 4 to "\u5706\u6ee1"
    )
    private val NameMapper = mapOf(
            "LianQi" to "炼气", "ZhuJi" to "筑基","JinDan" to "金丹","YuanYing" to "元婴","HuaShen" to "化神","LianXu" to "炼虚","HeTi" to "合体",
            "DaCheng" to "大乘","DiXian" to "地仙","TianXian" to "天仙","JinXian" to "金仙","TaiYi" to "太乙金仙","DaLuo" to "大罗金仙","HunYuan" to "准圣",
            "DiJing" to "帝境", "ShengJing" to "圣境"
    )



}