package com.mx.gillustrated.vo.cultivation

import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class ClanBak {
    lateinit var id:String // person.ancestorId
    lateinit var name:String
    var nickName:String = ""
    var createDate:Long = 0//xun
    var persons: List<String> = Collections.synchronizedList(mutableListOf())
    var battleRecord:MutableMap<Int, Int> = mutableMapOf()


    fun toClan(personMap: ConcurrentHashMap<String, Person>):Clan{
        val clan = Clan()
        val zhu = personMap[this.id]
        clan.id = this.id
        clan.name = this.name
        clan.nickName = if(this.nickName == "") this.name.substring(0,1) else this.nickName
        clan.zhu = zhu
        clan.createDate = this.createDate
        clan.clanPersonList.putAll(personMap.filterKeys { this.persons.contains(it) })
        clan.battleRecord = ConcurrentHashMap(this.battleRecord)
        return clan
    }

}

class Clan : ClanBak(){

    var clanPersonList: ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var zhu:Person? = null
    var xiuweiBattle:Int = 0// extra props
    var battleWinner:Int = 0// extra props
    var totalXiuwei:Long = 0// extra props

    fun toClanBak():ClanBak{
        val bak = ClanBak()
        bak.id = super.id
        bak.name = super.name
        bak.nickName = super.nickName
        bak.createDate = super.createDate
        bak.persons = this.clanPersonList.filter { it.value.ancestorId == super.id }.map { it.key }
        bak.battleRecord = super.battleRecord
        return bak
    }
}