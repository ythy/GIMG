package com.mx.gillustrated.vo.cultivation

import java.util.concurrent.ConcurrentHashMap

open class AllianceBak{
    var persons:List<String> = mutableListOf()
    var battleRecord:MutableMap<Int, Int> = mutableMapOf()


    fun toAlliance(personMap: ConcurrentHashMap<String, Person>, allianceConfig: AllianceConfig):Alliance{
        val alliance = Alliance()
        alliance.id = allianceConfig.id
        alliance.name = allianceConfig.name
        alliance.abridgeName = allianceConfig.abridgeName
        alliance.type = allianceConfig.type
        alliance.level = allianceConfig.level
        alliance.maxPerson = allianceConfig.maxPerson
        alliance.lifetime = allianceConfig.lifetime
        alliance.xiuwei = allianceConfig.xiuwei
        alliance.xiuweiMulti = allianceConfig.xiuweiMulti
        alliance.lingGen = allianceConfig.lingGen
        alliance.success = allianceConfig.success
        alliance.tianfu = allianceConfig.tianfu
        alliance.property = allianceConfig.property.toMutableList()
        alliance.nation = allianceConfig.nation
        alliance.tips = allianceConfig.tips.toMutableList()

        alliance.battleRecord = ConcurrentHashMap(this.battleRecord)
        alliance.personList.putAll(personMap.filterKeys { this.persons.contains(it) })
        return alliance
    }

}


open class AllianceConfig : AllianceBak() {
    lateinit var id:String
    lateinit var name:String
    var abridgeName:String = ""
    var type:Int = 0//类型，0 all, 1 spec
    var level:Int = 1// 权重100的约分 默认1
    var maxPerson:Int = 0//最大人数
    var lifetime:Int = 0
    var xiuwei:Int = 0
    var xiuweiMulti:Int = 0
    var lingGen:String? = null
    var tianfu:Int = 0 //要求的tianfu number
    var success:Int = 0//突破率
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
    var nation:String = ""
    var tips:MutableList<String> = mutableListOf()//max  level 9

}

class Alliance : AllianceConfig() {

    var zhuPerson:Person? = null
    var personList:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var totalXiuwei:Long = 0 // extra props
    var xiuweiBattle:Int = 0 // extra props
    var battleWinner:Int = 0 // extra props


    fun toBak():AllianceBak{
        val bak = AllianceBak()
        bak.battleRecord = super.battleRecord
        bak.persons = this.personList.filter { it.value.allianceId == super.id && it.value.type == 0}.map { it.key }
        return bak
    }

}