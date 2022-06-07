package com.mx.gillustrated.vo.cultivation

import java.util.*

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

    fun toAlliance(persons:List<Person>):Alliance{
        val alliance = Alliance()
        alliance.id = this.id
        alliance.name = this.name
        alliance.lingGen = this.lingGen
        alliance.personList.addAll(this.persons.mapNotNull { persons.find { p->!p.isDead && p.id == it} })
        return alliance
    }
}

class Alliance: AllianceConfig() {

    var zhuPerson:Person? = null
    var huPersons:MutableList<Person> = Collections.synchronizedList(mutableListOf())
    var personList:MutableList<Person> = Collections.synchronizedList(mutableListOf())
    var speedG1PersonList:MutableList<Person> = Collections.synchronizedList(mutableListOf())
    var speedG2PersonList:MutableList<Person> = Collections.synchronizedList(mutableListOf())

    var totalXiuwei:Long = 0// extra props
    var isPinyinMode:Boolean = false// extra props

    fun toConfig():AllianceConfig{
        val config = AllianceConfig()
        config.id = super.id
        config.name = super.name
        config.lingGen = super.lingGen
        config.persons = this.personList.map { it.id }
        return config
    }






}