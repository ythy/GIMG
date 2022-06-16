package com.mx.gillustrated.vo.cultivation

import java.util.concurrent.ConcurrentHashMap

//zhu 增益 20
open class AllianceConfig {
    lateinit var id:String
    lateinit var name:String
    var level:Int = 1// 权重100的约分 默认1
    var maxPerson:Int = 0//最大人数
    var lifetime:Int = 0
    var xiuwei:Int = 0
    var xiuweiMulti:Int = 0
    var lingGen:String? = null
    var tianfu:Int = 0 //要求的tianfu number
    var success:Int = 0//突破率
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
    var speedG1:Int = 0
    var speedG2:Int = 0
    var persons:List<String> = mutableListOf()

    fun toAlliance(personMap: ConcurrentHashMap<String, Person>):Alliance{
        val alliance = Alliance()
        alliance.id = this.id
        alliance.name = this.name
        alliance.lingGen = this.lingGen
        alliance.personList.putAll(personMap.filterKeys { this.persons.contains(it) })
        return alliance
    }
}

class Alliance: AllianceConfig() {

    var zhuPerson:Person? = null
    var huPersons:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var personList:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var speedG1PersonList:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var speedG2PersonList:ConcurrentHashMap<String, Person> =ConcurrentHashMap()

    var totalXiuwei:Long = 0// extra props
    var isPinyinMode:Boolean = false// extra props

    fun toConfig():AllianceConfig{
        val config = AllianceConfig()
        config.id = super.id
        config.name = super.name
        config.lingGen = super.lingGen
        config.persons = this.personList.filter { it.value.allianceId == super.id }.map { it.key }
        return config
    }






}