package com.mx.gillustrated.vo.cultivation

import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class ClanBak {
    lateinit var id:String // person.ancestorId
    lateinit var name:String
    var createDate:Long = 0//xun
    var persons: List<String> = Collections.synchronizedList(mutableListOf())

    fun toClan(personMap: ConcurrentHashMap<String, Person>):Clan{
        val clan = Clan()
        clan.id = this.id
        clan.name = this.name
        clan.createDate = this.createDate
        clan.clanPersonList.putAll(personMap.filterKeys { this.persons.contains(it) })
        return clan
    }
}

class Clan : ClanBak(){
    var totalXiuwei:Long = 0// extra props
    var clanPersonList: ConcurrentHashMap<String, Person> = ConcurrentHashMap()

    fun toClanBak():ClanBak{
        val bak = ClanBak()
        bak.id = super.id
        bak.name = super.name
        bak.createDate = super.createDate
        bak.persons = this.clanPersonList.filter { it.value.ancestorId == super.id }.map { it.key }
        return bak
    }
}