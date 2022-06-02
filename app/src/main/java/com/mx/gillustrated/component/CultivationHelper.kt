package com.mx.gillustrated.component

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.*
import java.util.*

@SuppressLint("SetTextI18n")
@RequiresApi(Build.VERSION_CODES.N)
object CultivationHelper {

    lateinit var mConfig:Config
    var mCurrentXun:Int = 0//当前时间
    var mHistoryTempData:MutableList<HistoryInfo> = Collections.synchronizedList(mutableListOf())
    fun writeHistory(content:String, person: Person?, type:Int = 1){
        mHistoryTempData.add(0, HistoryInfo(content, person, type))
    }

    fun joinAlliance(person: Person, allAlliance:MutableList<Alliance>){
        val options = if(person.lingGenId == "") {
            allAlliance.filter { it.level == 1 }.filter { person.lingGenName.indexOf(it.lingGen!!) >= 0 }.toMutableList()
        }else{
            allAlliance.filter { it.level == 1 }.toMutableList()
        }
        options.addAll(allAlliance.filter { it.level > 1 && person.tianfus.filter { f->f.rarity >=2 }.size >= it.tianfu  && it.personList.filter { p-> !p.isDead }.size < it.maxPerson })
        val random = Random().nextInt(options.map { 100 / it.level }.sum() )
        var count = 0
        for (i in 0 until options.size){
            val alliance = options[i]
            count += 100 / alliance.level
            if(random < count){
                alliance.personList.add(person)
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

    fun updateAllianceGain(allAlliance: MutableList<Alliance>, updated:Boolean = false){
        allAlliance.forEach { alliance->
            alliance.totalXiuwei = alliance.personList.sumByDouble { it.maxXiuWei.toDouble() }.toLong()
            val alivePersons = alliance.personList.filter { !it.isDead }
            if(alivePersons.isNotEmpty()){
                var zhu = alivePersons.first()
                alivePersons.forEach {
                    if (it.birthDay.last().first < zhu.birthDay.last().first)
                        zhu = it
                }
                alliance.zhuPerson = zhu

                if(updated) {
                    updateHuInAlliance(alliance, alivePersons)
                    updateG1InAlliance(alliance, alivePersons.toMutableList())
                }

                alivePersons.forEach { p->
                    p.allianceXiuwei = alliance.xiuwei
                    if(alliance.speedG1PersonList.find { it.id == p.id} != null){
                        p.allianceXiuwei += alliance.speedG1
                    }
                    if(p.id == alliance.zhuPerson?.id){
                        p.allianceXiuwei += 20
                    }
                    if(alliance.huPersons.find { it.id == p.id } != null){
                        p.allianceXiuwei += 10
                    }
                }
            }else{
                alliance.zhuPerson = null
                alliance.huPersons.clear()
            }
        }
    }

    private fun updateHuInAlliance(alliance: Alliance, persons:List<Person>){
        val huSize =   Math.min(4, Math.max(1, persons.size / 10))
        val hu = mutableListOf<Person>()
        for(i in 0 until huSize){
            val random = Random().nextInt(persons.size)
            if(hu.find { persons[random].id == it.id } == null)
                hu.add(persons[random])
        }
        alliance.huPersons.clear()
        alliance.huPersons.addAll(hu)
    }

    private fun updateG1InAlliance(alliance: Alliance, persons:MutableList<Person>){
        val total = Math.min(10, Math.max(1, persons.size / 4))
        persons.sortByDescending { it.extraSpeed }
        alliance.speedG1PersonList.clear()
        alliance.speedG1PersonList.addAll(persons.filterIndexed { index, _ -> index < total })

    }

    private fun getTianFu(parent: Pair<Person, Person>?):MutableList<TianFu>{
        val tianFus = mutableListOf<TianFu>()
        mConfig.tianFuType.groupBy { it.type }.forEach { (_, u) ->
            var data: TianFu? = null
            for (i in 0 until u.size){
                if(Random().nextInt(u[i].weight) == 0){
                    data = u[i]
                    break
                }
            }
            if(data != null){
                tianFus.add(data)
            }else if(parent != null){
                for (i in 0 until u.size){
                    var weight = u[i].weight
                    val extraFirst = parent.first.tianfus.find { it.type == u[i].type }
                    val extraSecond = parent.second.tianfus.find { it.type == u[i].type }
                    if(extraFirst != null)
                        weight /= 50
                    if(extraSecond != null)
                        weight /= 50
                    weight = Math.max(1, weight)
                    if(Random().nextInt(weight) == 0){
                        data = u[i]
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

    private fun getLingGen(parent: Pair<Person, Person>?):Triple<LingGen, String, String>{
        var firestNumber = 10
        var secondNumber = 10
        if(parent != null){
            if(parent.first.lingGenType.id == "1000006")
                firestNumber = 5
            else if(parent.first.lingGenType.id == "1000007")
                firestNumber = 1
            if(parent.second.lingGenType.id == "1000006")
                secondNumber = 5
            else if(parent.second.lingGenType.id == "1000007")
                secondNumber = 1
        }
        val selectNumber = Random().nextInt(20 + firestNumber + secondNumber)
        var lingGenName = ""
        var lingGenId = ""
        var lingGen: LingGen? = null
        if(parent == null || selectNumber < 20){
            val lingGenList = mConfig.lingGenType
            lingGenList.sortedBy { it.randomBasic }
            val sum = lingGenList.sumBy { it.randomBasic }
            val random = Random().nextInt(sum)
            var current = 0 //当前分布
            for ( i in 0 until lingGenList.size){
                current += lingGenList[i].randomBasic
                if(random < current){
                    lingGen = lingGenList[i]
                    break
                }
            }

            when {
                lingGen!!.id == "1000006" -> {
                    val arr = mConfig.lingGenTian.filter { it.type == 0 }
                    val tianIndex = Random().nextInt(arr.size)
                    lingGenId = arr[tianIndex].id
                    lingGenName = arr[tianIndex].name
                }
                lingGen.id == "1000007" -> {
                    val arr = mConfig.lingGenTian.filter { it.type == 1 }
                    val tianIndex = Random().nextInt(arr.size)
                    lingGenId = arr[tianIndex].id
                    lingGenName = arr[tianIndex].name
                }
                else -> {
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
            }
        }else{
            val maxPerson:Person = if(selectNumber in 20 until 20 + firestNumber){
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

    fun getPersonInfo(name:Pair<String, String?>?, gender: NameUtil.Gender?,
                              lifetime:Int = 100, parent:Pair<Person, Person>? = null): Person {
        val personGender = gender ?: when (Random().nextInt(2)) {
            0 -> NameUtil.Gender.Male
            else -> NameUtil.Gender.Female
        }
        val personName = if(name != null)
            if(name.second != null) name else NameUtil.getChineseName(name.first, personGender)
        else
            NameUtil.getChineseName(null, personGender)

        val lingGen = getLingGen(parent)
        val tianFus = getTianFu(parent)
        val isFav = lifetime == 100000
        val birthDay:Pair<Int, Int> = Pair(mCurrentXun, 0)
        val result = Person()
        result.id =  UUID.randomUUID().toString()
        result.name = personName.first + personName.second
        result.pinyinName = PinyinUtil.convert(result.name)
        result.lastName = personName.first
        result.gender = personGender
        result.lingGenType = lingGen.first
        result.lingGenName = lingGen.third
        result.lingGenId = lingGen.second
        if(lingGen.second != ""){
            result.extraProperty = mConfig.lingGenTian.find { it.id == lingGen.second }?.property!!
        }
        result.birthDay.add(birthDay)
        val initJingJie = mConfig.jingJieType[0]
        result.jingJieId = initJingJie.id
        result.jinJieName = getJinJieName(initJingJie.name)
        result.jinJieColor = initJingJie.color
        result.jingJieSuccess = initJingJie.success
        result.jinJieMax = initJingJie.max
        result.profile = if(isFav) 1 else 0
        result.isFav = isFav
        result.tianfus = tianFus
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

    fun getPersonBasicString(person:Person, detail:Boolean = true):String{
        return if(detail)
            "${if(person.jinJieName.indexOf("-") > -1) person.pinyinName else person.name} (${person.age}/${person.lifetime}:${person.jinJieName}) ${person.lingGenName} "
        else
            ""
    }

    fun battle(person1: Person, person2: Person, round:Int, xiuwei:Int):Boolean{
        val props1 = getProperty(person1)
        val props2 = getProperty(person2)
        var hp1 = props1[0]
        var hp2  = props2[0]
        for (it in 0 until round){
            val first = if(props1[4] == props2[4]) Random().nextInt(2) == 0
                                    else props1[4] > props2[4]
            if(first){
                val hpReduced2 = Math.max(1, props1[2] - props2[3])
                hp2 -= hpReduced2
                if(hp2 > 0){
                    val hpReduced1 = Math.max(1, props2[2] - props1[3])
                    hp1 -= hpReduced1
                    if(hp1 <= 0){
                        break
                    }
                }else{
                    break
                }
            }else{
                val hpReduced1 = Math.max(1, props2[2] - props1[3])
                hp1 -= hpReduced1
                if(hp1 > 0){
                    val hpReduced2 = Math.max(1, props1[2] - props2[3])
                    hp2 -= hpReduced2
                    if(hp2 <= 0){
                        break
                    }
                }else{
                    break
                }
            }
        }
        val firstWin = hp1 >= hp2
        if(firstWin){
            writeHistory("Battle End: ${person1.name}($hp1) 🔪 ${person2.name}($hp2)", person1)
            person1.xiuXei += xiuwei / 4
            person2.xiuXei -= xiuwei
        }else{
            writeHistory("Battle End: ${person2.name}($hp2) 🔪 ${person1.name}($hp1)", person2)
            person2.xiuXei += xiuwei / 4
            person1.xiuXei -= xiuwei
        }
        person1.HP += hp1 - props1[0]
        person2.HP += hp2 - props2[0]
        return firstWin
    }

    fun getProperty(person: Person):MutableList<Int>{
        val property = person.extraProperty.mapIndexed { index, it ->
            it + person.allianceProperty[index]
        }
        val lingGenLevel = person.lingGenType.color // 0 until 6
        val zhuan = person.lifeTurn
        val jingJieLevel = getJingJieLevel(person.jingJieId)

        val extraHP = 100 * zhuan + 5 * lingGenLevel + jingJieLevel.first + 4 * jingJieLevel.second + property[0]
        val attack =  20 * zhuan + 2 * lingGenLevel + 2 * jingJieLevel.second +  property[1]
        val defence =  20 * zhuan + 2 * lingGenLevel + 2 * jingJieLevel.second +  property[2]
        val speed = 10 * zhuan + 2 * lingGenLevel + property[3]

        return mutableListOf(person.HP + extraHP, person.maxHP + extraHP,
                attack, defence, speed)
    }

    fun getXiuweiGrow(person:Person):Int{
        val basic = person.lingGenType.qiBasic + person.extraXiuwei + person.allianceXiuwei
        val multi = (person.extraXuiweiMulti + 100).toDouble() / 100
        return (basic * multi).toInt()
    }

    fun getTotalSuccess(person:Person, jingJieBonus:Int):Int{
        val tianfuSuccess = person.extraTupo
        val allianceSuccess = person.allianceSuccess
        val currentSuccess = person.jingJieSuccess
        var bonus = 0
        if(jingJieBonus > 0 && person.lingGenType.jinBonus.isNotEmpty()){
            bonus = person.lingGenType.jinBonus[jingJieBonus - 1]
        }
        return currentSuccess + tianfuSuccess + allianceSuccess + bonus
    }

    private fun getExtraXuiweiMulti(person:Person, alliance:Alliance? = null):Int{
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

    fun updatePartner(allPerson: MutableList<Person>){
        val males = allPerson.filter { !it.isDead && it.gender == NameUtil.Gender.Male && it.partner == null && (it.lifetime - it.age > 200) }
        val females = allPerson.filter { !it.isDead && it.gender == NameUtil.Gender.Female && it.partner == null && (it.lifetime - it.age > 200) }
        if(males.size > 5 && females.size > 5){
            val man = males[Random().nextInt(males.size)]
            val woman = females.sortedBy { Math.abs(man.age - it.age) }[0]
            if(man.ancestorId != null && woman.ancestorId != null && man.ancestorId == woman.ancestorId)
                return
            createPartner(man, woman)
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

    fun isPinyinMode(person: Person):Boolean{
        return person.jinJieName.indexOf("-") > -1
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

    fun getJinJieName(input:String, pinyinMode:Boolean = false):String{
        if(pinyinMode)
            return input
        val split = input.split("-")
        val prefix = split[0]
        val grade = split[1].toInt()
        return if(prefix == "LianQi"){
            NameMapper[prefix] + (if(grade<10) "${grade}层" else "圆满")
        }else{
            (NameMapper[prefix] ?: prefix) + LevelMapper[grade]
        }
    }

    val CommonColors = arrayOf("#EAEFE8", "#417B29", "#367CC4", "#7435C1", "#D22E59", "#FB23B7", "#CDA812", "#F2E40A", "#04B4BA")
    private val LevelMapper = mapOf(
            1 to "初期", 2 to "中期", 3 to "后期", 4 to "圆满"
    )
    private val NameMapper = mapOf(
            "LianQi" to "炼气", "ZhuJi" to "筑基","JinDan" to "金丹","YuanYing" to "元婴","HuaShen" to "化神","LianXu" to "炼虚","HeTi" to "合体",
            "DaCheng" to "大乘","DiXian" to "地仙","TianXian" to "天仙","JinXian" to "金仙","TaiYi" to "太乙金仙","DaLuo" to "大罗金仙","HunYuan" to "混元金仙",
            "DaDao" to "大道圣人","TianDao" to "天道圣人", "ShenJing" to "神境", "ZhiShang" to "大道至上", "ChuangZao" to "创造道者", "ZhuZai" to "创造主宰"
    )

    // type 1 人物信息
    data class HistoryInfo(var content:String, var person:Person?, var type:Int = 0)
}