package com.mx.gillustrated.component

import android.annotation.SuppressLint
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("SetTextI18n")
object CultivationHelper {

    lateinit var mConfig:Config
    lateinit var mBattleRound:BattleRound
    var maxFemaleProfile = 0 // 默认0号 1001开始不随机使用
    var maxMaleProfile = 0 // 默认0号 1001开始不随机使用
    var pinyinMode:Boolean = false //是否pinyin模式
    var mCurrentXun:Long = 0//当前时间
    var mHistoryTempData:MutableList<HistoryInfo> = Collections.synchronizedList(mutableListOf())
    fun writeHistory(content:String, person: Person?, type:Int = 1, battleId: String = ""){
        mHistoryTempData.add(0, HistoryInfo(content, person, type, battleId))
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
                break
            }
        }
    }

    fun joinFixedAlliance(person: Person, alliance:Alliance){
        alliance.personList[person.id] = person
        person.allianceId = alliance.id
        person.allianceName = alliance.name
        person.allianceSuccess = alliance.success
        person.allianceProperty = alliance.property
        person.extraXuiweiMulti = getExtraXuiweiMulti(person, alliance)
        person.lifetime = person.age + (person.lifetime - person.age) * ( 100 + alliance.lifetime ) / 100
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
        val personList = Collections.synchronizedList( persons.map { it.value })
        alliance.zhuPerson = personList.sortedBy { it.lastBirthDay }.first()
    }

    private fun updateG1InAlliance(alliance: Alliance, persons:ConcurrentHashMap<String, Person>){
        val total = Math.min(10, Math.max(1, persons.size / 4))
        val personList =  Collections.synchronizedList( persons.map { it.value })
        alliance.speedG1PersonList.clear()
        personList.sortByDescending { it.extraSpeed }
        for (i in 0 until total){
            alliance.speedG1PersonList[personList[i].id] = personList[i]
        }

    }

    private fun getTianFu(parent: Pair<Person, Person>?, fixedTianfus:MutableList<String>?, tianFuWeight:Int):MutableList<TianFu>{
        if(fixedTianfus != null && fixedTianfus.isNotEmpty()){
            return fixedTianfus.map { mConfig.tianFuType.find { f-> f.id == it }!! }.toMutableList()
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


    private fun getLingGen(parent: Pair<Person, Person>?, lingGenTypeFixed:String? = null, lingGenWeight:Int):Triple<LingGen, String, String>{
        if(lingGenTypeFixed != null){
            return getLingGenDetail(lingGenTypeFixed)
        }
        var firestNumber = 100
        var secondNumber = 100
        if(parent != null){
            when {
                parent.first.lingGenType.type == 1 -> firestNumber = 50
                parent.first.lingGenType.type == 2 -> firestNumber = 5
                parent.first.lingGenType.type == 3 -> firestNumber = 2
                parent.first.lingGenType.type == 4 -> firestNumber = 1
            }
            when {
                parent.second.lingGenType.type == 1 -> secondNumber = 50
                parent.second.lingGenType.type == 2 -> secondNumber = 5
                parent.second.lingGenType.type == 3 -> secondNumber = 2
                parent.second.lingGenType.type == 4 -> secondNumber = 1
            }
        }
        val selectNumber = Random().nextInt(200 + firestNumber + secondNumber)
        val lingGenName: String
        val lingGenId: String
        var lingGen: LingGen? = null
        if(parent == null || lingGenWeight > 1 || selectNumber < 200){
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
        }else{
            val maxPerson:Person = if(selectNumber in 200 until 200 + firestNumber){
                parent.first
            }else{
                parent.second
            }
            lingGen = maxPerson.lingGenType
            lingGenId = maxPerson.lingGenId
            lingGenName = maxPerson.lingGenName
        }
        return Triple(lingGen, lingGenId, lingGenName)
    }

     fun getTeji(weight:Int = 1, multi:Boolean = true):MutableList<String>{
         val result = mutableListOf<String>()
         if(!multi){
             val teji = mConfig.teji.shuffled()[0]
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
                              lifetime:Long = 100, parent:Pair<Person, Person>? = null, fav:Boolean = false, mix:PersonFixedInfoMix? = null): Person {
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
        if(lingGen.second != ""){
            result.extraProperty = mConfig.lingGenTian.find { it.id == lingGen.second }?.property!!
        }
        result.birthDay.add(birthDay)
        result.lastBirthDay = mCurrentXun
        setPersonJingjie(result)
        result.profile = if(fav) 1001 else getRandomProfile(result.gender)
        result.isFav = fav
        result.tianfus = tianFus
        result.teji = Collections.synchronizedList(getTeji())
        result.careerList = Collections.synchronizedList(getCareer().map { Triple(it, 0, "") })
        result.lifetime = lifetime + (tianFus.find { it.type == 3 }?.bonus ?: 0)
        result.extraXiuwei = tianFus.find { it.type == 1 }?.bonus ?: 0
        result.extraTupo = tianFus.find { it.type == 4 }?.bonus ?: 0
        result.extraSpeed = tianFus.find { it.type == 5 }?.bonus ?: 0
        result.extraXuiweiMulti =  getExtraXuiweiMulti(result)

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

    fun updatePersonInborn(person: Person, lingGenWeight: Int = 1, tianFuWeight: Int = 1){
        val lingGen = getLingGen(null, null, lingGenWeight)
        val tianFus = getTianFu(null, null, tianFuWeight)
        person.lingGenType = lingGen.first
        person.lingGenName = lingGen.third
        person.lingGenId = lingGen.second
        if(lingGen.second != ""){
            person.extraProperty = mConfig.lingGenTian.find { it.id == lingGen.second }?.property!!
        }
        person.tianfus = tianFus
        person.extraXiuwei = tianFus.find { it.type == 1 }?.bonus ?: 0
        person.extraTupo = tianFus.find { it.type == 4 }?.bonus ?: 0
        person.extraSpeed = tianFus.find { it.type == 5 }?.bonus ?: 0
        person.extraXuiweiMulti =  getExtraXuiweiMulti(person)
    }

    fun setPersonJingjie(person: Person, level: Int = 0){
        val initJingJie = mConfig.jingJieType[level]
        person.jingJieId = initJingJie.id
        person.jinJieName = getJinJieName(initJingJie.name)
        person.jinJieColor = initJingJie.color
        person.jingJieSuccess = initJingJie.success
        person.jinJieMax = initJingJie.max
    }

    fun getPersonBasicString(person:Person, detail:Boolean = true):String{
        return if(detail)
            "${person.name} (${person.age}/${person.lifetime}:${person.jinJieName}) ${person.lingGenName} "
        else
            ""
    }


    //0 ~ 5
    fun getProperty(person: Person):MutableList<Int>{
        val property = person.extraProperty.mapIndexed { index, it ->
            it + person.allianceProperty[index] + person.equipmentProperty[index]
        }
        val jingJieLevel = getJingJieLevel(person.jingJieId)
        val extraHP = jingJieLevel.first + 10 * jingJieLevel.second + property[0] // 0 ~ 70 + 80
        val attack =  5 * jingJieLevel.second +  property[1] // yuan 2, hua 4, he 4,di 5, tai 6, zhun 7, di 8 // 0 ~ 40
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
            equipments.filter { it.type != 1 && it.type != 2 && it.type != 3 }.groupBy { it.id }.forEach { (_, u) ->
                for (index in 0 until u.size){
                    val effectEquipment = u[index]
                    if( index > effectEquipment.maxCount ){
                        break
                    }
                    summationEquipmentValues(person, effectEquipment)
                }
            }
            equipments.filter { it.type == 1 || it.type == 2 || it.type == 3 }.groupBy { it.type }.forEach { (_, u) ->
                val effectEquipment = u.maxBy { it.rarity }!!
                summationEquipmentValues(person, effectEquipment)
            }
        }
    }

    private fun summationEquipmentValues(person: Person, effectEquipment: Equipment){
        person.equipmentXiuwei += effectEquipment.xiuwei
        person.equipmentSuccess += effectEquipment.success
        effectEquipment.property.forEachIndexed { pi, pp ->
            person.equipmentProperty[pi] += pp
        }
    }

    fun getXiuweiGrow(person:Person, allAllianceMap:ConcurrentHashMap<String, Alliance>):Int{
        val alliance = allAllianceMap[person.allianceId] ?: return 0
        synchronized(person){
            person.allianceXiuwei = alliance.xiuwei
            if(alliance.speedG1PersonList.contains(person.id)){
                person.allianceXiuwei += alliance.speedG1
            }
            if(person.id == alliance.zhuPerson?.id){
                person.allianceXiuwei += 20
            }
        }
        val basic = person.lingGenType.qiBasic + person.extraXiuwei + person.allianceXiuwei + person.equipmentXiuwei
        val multi = (person.extraXuiweiMulti + 100).toDouble() / 100
        return (basic * multi).toInt()
    }

    fun getTotalSuccess(person:Person):Int{
        val tianfuSuccess = person.extraTupo
        val allianceSuccess = person.allianceSuccess
        val currentSuccess = person.jingJieSuccess
        val equipmentSuccess = person.equipmentSuccess
        return currentSuccess + tianfuSuccess + allianceSuccess + equipmentSuccess
    }

    fun getExtraXuiweiMulti(person:Person, alliance:Alliance? = null):Int{
        val tianValue =  person.tianfus.find { it.type == 2 }?.bonus ?: 0
        val allianceValue = alliance?.xiuweiMulti ?: 0
        return tianValue + allianceValue
    }

    fun addPersonEvent(person:Person, content:String, event:Event? = null){
        val personEvent = PersonEvent()
        personEvent.nid = UUID.randomUUID().toString()
        personEvent.happenTime = mCurrentXun
        personEvent.content = content
        personEvent.detail = event
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
        addPersonEvent(man,"${mCurrentXun / 12}年 与${woman.name}结伴")
        addPersonEvent(woman,"${mCurrentXun / 12}年 与${man.name}结伴")
        writeHistory("${getPersonBasicString(man)} 与 ${getPersonBasicString(woman)} 结伴了", null, 0)
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

    fun showing(input:String):String{
        return if (pinyinMode) PinyinUtil.convert(input.trim()) else input.trim()
    }

    fun isTrigger(weight:Int = 2):Boolean{
        return Random().nextInt(Math.max(1, weight)) == 0
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

    data class PersonFixedInfoMix(var lingGenId:String?, var tianFuIds:MutableList<String>?, var tianFuWeight: Int = 1, var lingGenWeight:Int = 1)

    val SpecPersonFirstName3:MutableList<Triple<Pair<String, String>, NameUtil.Gender, Int>> = mutableListOf(Triple(Pair("\u7389", "\u5e1d"), NameUtil.Gender.Male, 0), Triple(Pair("\u83e9","\u63d0"), NameUtil.Gender.Male, 0), Triple(Pair("\u6768","\u622c"), NameUtil.Gender.Male, 0),
            Triple(Pair("\u54ea","\u5412"), NameUtil.Gender.Male, 0), Triple(Pair("\u592a\u4e0a","\u8001\u541b"), NameUtil.Gender.Male, 0),
            Triple(Pair("\u9080","\u6708"), NameUtil.Gender.Female, 1),Triple(Pair("\u601c","\u661f"), NameUtil.Gender.Female, 1),Triple(Pair("\u82cf","\u6a31"), NameUtil.Gender.Female, 1),Triple(Pair("\u674e","\u7ea2\u8896"), NameUtil.Gender.Female, 1),
            Triple(Pair("\u9ec4","\u84c9"), NameUtil.Gender.Female, 2),Triple(Pair("\u8d75","\u654f"), NameUtil.Gender.Female, 2),Triple(Pair("\u5468","\u82b7\u82e5"), NameUtil.Gender.Female, 2), Triple(Pair("\u6bb5","\u8a89"), NameUtil.Gender.Male, 2))


    val SpecPersonFixedName:MutableList<Triple<Pair<String, String>, NameUtil.Gender, PersonFixedInfoMix>> = mutableListOf(
            Triple(Pair("\u9ec4", "\u5e1d"), NameUtil.Gender.Male, PersonFixedInfoMix("1000007", mutableListOf("4000106", "4000206", "4000305", "4000404", "4000506")))
            ,Triple(Pair("\u7384", "\u5973"), NameUtil.Gender.Female, PersonFixedInfoMix("1000007", mutableListOf("4000106", "4000206", "4000304", "4000404", "4000504")))
            ,Triple(Pair("\u5b5f", "\u5a46"), NameUtil.Gender.Female, PersonFixedInfoMix("1000006", mutableListOf("4000104", "4000204", "4000305", "4000402", "4000506")))
            ,Triple(Pair("\u7532", "\u6590\u59ec"), NameUtil.Gender.Female, PersonFixedInfoMix("1000001", mutableListOf("4000103", "4000204", "4000305", "4000503")))
            ,Triple(Pair("\u5c0f", "\u677e\u59ec"), NameUtil.Gender.Female, PersonFixedInfoMix("1000001", mutableListOf("4000104", "4000203", "4000305", "4000503")))
            ,Triple(Pair("\u6bdb", "\u6b23"), NameUtil.Gender.Male, PersonFixedInfoMix("1000008", mutableListOf("4000109", "4000209", "4000305", "4000407", "4000506")))
    )

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

    const val SP_JIE_TURN = 81
    val SP_EVENT_WEIGHT = listOf("1000-10","1200-40","7200-40","8400-40","9600-80")

    // type 1 人物信息, 2 流程信息
    data class HistoryInfo(var content:String, var person:Person?, var type:Int = 0, var battleId:String = "")

    val SpecPersonFirstName2:MutableList<String> = mutableListOf("主", "廿一", "廿三")
    val SpecPersonFirstName:MutableList<String> = mutableListOf("主", "侍", "儿")
    data class SpecPersonInfo(var name:Pair<String, String?>, var gender: NameUtil.Gender?, var allianceIndex: Int, var TianFuWeight:Int, var LingGenWeight:Int)

    val EnemyNames = arrayOf("\u83dc\u83dc", "\u8fdc\u53e4", "\u68ee\u7f57", "\u4e07\u8c61", "\u9b51\u9b45", "\u9b4d\u9b49")
    val CommonColors = arrayOf("#EAEFE8", "#417B29", "#367CC4", "#7435C1", "#D22E59", "#FB23B7", "#CDA812", "#F2E40A", "#04B4BA", "#C18135", "#A5529E")
    private val LevelMapper = mapOf(
            1 to "初期", 2 to "中期", 3 to "后期", 4 to "\u5706\u6ee1"
    )
    private val NameMapper = mapOf(
            "LianQi" to "炼气", "ZhuJi" to "筑基","JinDan" to "金丹","YuanYing" to "元婴","HuaShen" to "化神","LianXu" to "炼虚","HeTi" to "合体",
            "DaCheng" to "大乘","DiXian" to "地仙","TianXian" to "天仙","JinXian" to "金仙","TaiYi" to "太乙金仙","DaLuo" to "大罗金仙","HunYuan" to "准圣",
            "DiJing" to "帝境", "ShengJing" to "圣境"
    )



}