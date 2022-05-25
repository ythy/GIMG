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

    var mHistoryTempData:MutableList<HistoryInfo> = mutableListOf()
    fun writeHistory(content:String, person: Person?, type:Int = 1){
        mHistoryTempData.add(0, HistoryInfo(content, person, type))
    }

    fun joinAlliance(person: Person, allAlliance:MutableList<Alliance>){
        var options = allAlliance.filter { it.level == 1 }.toMutableList()
        if(person.lingGenId == "") {
            options = options.filter { person.lingGenName.indexOf(it.lingGen!!) >= 0 }.toMutableList()
        }
        options.addAll(allAlliance.filter { it.level > 1 && person.tianfus.size >= it.tianfu && it.persons.size < it.maxPerson })
        val random = Random().nextInt(options.map { 100 / it.level }.sum() )
        var count = 0
        for (i in 0 until options.size){
            val alliance = options[i]
            count += 100 / alliance.level
            if(random < count){
                alliance.persons.add(person.id)
                person.allianceId = alliance.id
                person.allianceName = alliance.name
                person.allianceSuccess = alliance.success
                person.extraXuiweiMulti += alliance.xiuweiMulti
                person.lifetime = person.lifetime * ( 100 + alliance.lifetime ) / 100
                break
            }
        }
    }

    fun exitAlliance(person: Person, allAlliance:MutableList<Alliance>){
        val alliance =  allAlliance.find { it.id == person.allianceId }
        if(alliance != null){
            alliance.persons.removeIf { it == person.id }
            alliance.speedG1List.remove(person.id)
        }
        person.allianceId = ""
        person.allianceName = ""
    }

    fun updateAllianceGain(currentXun: Int, allAlliance: MutableList<Alliance>, allPerson: MutableList<Person>){
        allAlliance.forEach { alliance->
            val fixedPersons = alliance.persons.toList()
            val fixedAllPerson = allPerson.toList()
            val persons = fixedPersons.mapNotNull { fixedAllPerson.find { p -> p.id == it } }.toMutableList()
            alliance.totalXiuwei = persons.sumByDouble { it.maxXiuWei.toDouble() }.toLong()
            if(persons.isNotEmpty()){
                persons.sortBy { it.birthDay.last().first }
                val zhu = persons.first()
                alliance.zhu = zhu.id
                //10 nian 一次，Fu 范围内必中
                if(currentXun % 120 == 0) {
                    val base = persons.toMutableList()
                    base.sortByDescending { it.extraSpeed }
                    val result = mutableListOf<String>()
                    var total = Math.min(10, Math.max(1, persons.size / 4))
                    val specBase = base.filter { it.extraSpeed > 0 }.toMutableList()
                    while (total-- > 0) {
                        if(specBase.isNotEmpty()){
                            val totalRandom = specBase.map { it.extraSpeed }.sum()
                            val random = Random().nextInt(totalRandom)
                            var count = 0
                            for (p in 0 until specBase.size ){
                                count += base[p].extraSpeed
                                if(random < count){
                                    val selectPerson = specBase[p]
                                    specBase.remove(selectPerson)
                                    base.remove(selectPerson)
                                    result.add(selectPerson.id)
                                    break
                                }
                            }
                        }else{
                            val random = Random().nextInt(base.size)
                            val selectPerson = base[random]
                            base.remove(selectPerson)
                            result.add(selectPerson.id)
                        }
                    }
                    alliance.speedG1List = result
                }

                persons.forEach { p->
                    p.allianceXiuwei = alliance.xiuwei
                    if(alliance.speedG1List.find { it == p.id} != null){
                        p.allianceXiuwei += alliance.speedG1
                    }
                    if(p.id == alliance.zhu){
                        p.allianceXiuwei += 20
                    }
                }

            }else{
                alliance.zhu = null
            }

        }
    }

    private fun getTianFu(config:Config, parent: Pair<Person, Person>?):MutableList<TianFu>{
        val tianFus = mutableListOf<TianFu>()
        config.tianFuType.groupBy { it.type }.forEach { (_, u) ->
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

    private fun getLingGen(config:Config, parent: Pair<Person, Person>?):Triple<LingGen, String, String>{
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
            val lingGenList = config.lingGenType
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

            if(lingGen!!.id == "1000006"){
                val arr = config.lingGenTian.filter { it.type == 0 }
                val tianIndex = Random().nextInt(arr.size)
                lingGenId = arr[tianIndex].id
                lingGenName = arr[tianIndex].name
            }else if( lingGen.id == "1000007"){
                val arr = config.lingGenTian.filter { it.type == 1 }
                val tianIndex = Random().nextInt(arr.size)
                lingGenId = arr[tianIndex].id
                lingGenName = arr[tianIndex].name
            }else{
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

    fun getPersonInfo(config:Config, currentXun:Int, name:Pair<String, String?>?, gender: NameUtil.Gender?,
                              lifetime:Int = 100, parent:Pair<Person, Person>? = null): Person {
        val personGender = gender ?: when (Random().nextInt(2)) {
            0 -> NameUtil.Gender.Male
            else -> NameUtil.Gender.Female
        }
        val personName = if(name != null)
            if(name.second != null) name else NameUtil.getChineseName(name.first, personGender)
        else
            NameUtil.getChineseName(null, personGender)

        val lingGen = getLingGen(config, parent)
        val tianFus = getTianFu(config, parent)
        val isFav = lifetime == 100000
        val birthDay:Pair<Int, Int> = Pair(currentXun, 0)
        val result = Person()
        result.id =  UUID.randomUUID().toString()
        result.name = personName.first + personName.second
        result.pinyinName = PinyinUtil.convert(result.name)
        result.lastName = personName.first
        result.gender = personGender
        result.lingGenType = lingGen.first
        result.lingGenName = lingGen.third
        result.lingGenId = lingGen.second
        result.birthDay.add(birthDay)
        result.jingJieId = config.jingJieType[0].id
        result.jinJieName = getJinJieName(config.jingJieType[0].name)
        result.jinJieColor = config.jingJieType[0].color
        result.jingJieSuccess = config.jingJieType[0].success
        result.jinJieMax = config.jingJieType[0].max
        result.profile = if(isFav) 1 else 0
        result.isFav = isFav
        result.tianfus = tianFus
        result.lifetime = lifetime + (tianFus.find { it.type == 3 }?.bonus ?: 0)
        result.extraXiuwei = tianFus.find { it.type == 1 }?.bonus ?: 0
        result.extraTupo = tianFus.find { it.type == 4 }?.bonus ?: 0
        result.extraSpeed = tianFus.find { it.type == 5 }?.bonus ?: 0
        result.extraXuiweiMulti =  tianFus.find { it.type == 2 }?.bonus ?: 0

        if(parent != null){
            result.parent = Pair(parent.first.id, parent.second.id)
            result.parentName = Pair(parent.first.name, parent.second.name)
            result.ancestorLevel = parent.first.ancestorLevel + 1
            result.ancestorId = parent.first.ancestorId ?: parent.first.id
        }

        return result
    }

    fun getPersonBasicString(person:Person, detail:Boolean = true):String{
        return if(detail)
            "${if(person.jinJieName.indexOf("-") > -1) person.pinyinName else person.name} (${person.age}/${person.lifetime}:${person.jinJieName}) ${person.lingGenName} "
        else
            ""
    }

    fun addPersonEvent(currentXun: Int, person:Person, content:String, event:Event? = null){
        val personEvent = PersonEvent()
        personEvent.nid = UUID.randomUUID().toString()
        personEvent.happenTime = currentXun
        personEvent.content = content
        personEvent.detail = event
        person.events.add(personEvent)
    }

    //20年选定一次
    fun updatePartner(currentXun: Int, allPerson: MutableList<Person>){
        if(currentXun % 240 == 0) {
            val males = allPerson.filter { it.gender == NameUtil.Gender.Male && it.partner == null && (it.lifetime - it.age > 200) }
            val females = allPerson.filter { it.gender == NameUtil.Gender.Female && it.partner == null && (it.lifetime - it.age > 200) }
            if(males.size > 5 && females.size > 5){
                val man = males[Random().nextInt(males.size)]
                val woman = females.sortedBy { Math.abs(man.age - it.age) }[0]
                if(man.ancestorId != null && woman.ancestorId != null && man.ancestorId == woman.ancestorId)
                    return
                createPartner(currentXun, man, woman)
            }
        }
    }


    fun createPartner(currentXun: Int, man:Person, woman:Person){
        man.partner = woman.id
        man.partnerName = woman.name
        woman.partner = man.id
        woman.partnerName = man.name
        addPersonEvent(currentXun, man,"${currentXun / 12}年 与${woman.name}结伴")
        addPersonEvent(currentXun, woman,"${currentXun / 12}年 与${man.name}结伴")
        writeHistory("${getPersonBasicString(man)} 与 ${getPersonBasicString(woman)} 结伴了", null, 0)
    }

    fun getYearString(currentXun: Int):String{
        return "${currentXun / 12}年"
    }

    fun isPinyinMode(person: Person):Boolean{
        return person.jinJieName.indexOf("-") > -1
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

    val CommonColors = arrayOf("#EAEFE8", "#417B29", "#367CC4", "#7435C1", "#D22E59", "#FB23B7", "#CDA812", "#F2E40A", "#4C0404")
    val LevelMapper = mapOf(
            1 to "初期", 2 to "中期", 3 to "后期", 4 to "圆满"
    )
    val NameMapper = mapOf(
            "LianQi" to "炼气", "ZhuJi" to "筑基","JinDan" to "金丹","YuanYing" to "元婴","HuaShen" to "化神","LianXu" to "炼虚","HeTi" to "合体",
            "DaCheng" to "大乘","DiXian" to "地仙","TianXian" to "天仙","JinXian" to "金仙","TaiYi" to "太乙金仙","DaLuo" to "大罗金仙","HunYuan" to "混元金仙",
            "DaDao" to "大道圣人","TianDao" to "天道圣人", "ShenJing" to "神境", "ZhiShang" to "大道至上", "ChuangZao" to "创造道者", "ZhuZai" to "创造主宰"
    )

    // type 1 人物信息
    data class HistoryInfo(var content:String, var person:Person?, var type:Int = 0)
}