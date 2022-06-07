package com.mx.gillustrated.vo.cultivation

import java.util.*

open class ClanBak {
    lateinit var id:String // person.ancestorId
    lateinit var name:String
    var createDate:Int = 0//xun
    var persons: List<String> = Collections.synchronizedList(mutableListOf())

    fun toClan(list:List<Person>):Clan{
        val clan = Clan()
        clan.id = this.id
        clan.name = this.name
        clan.createDate = this.createDate
        clan.clanPersonList.addAll(this.persons.mapNotNull { list.find { p->!p.isDead && p.id == it} })
        return clan
    }
}

class Clan : ClanBak(){
    var totalXiuwei:Long = 0// extra props
    var clanPersonList: MutableList<Person> = Collections.synchronizedList(mutableListOf())

    fun toClanBak():ClanBak{
        val bak = ClanBak()
        bak.id = super.id
        bak.name = super.name
        bak.createDate = super.createDate
        bak.persons = this.clanPersonList.map { it.id }
        return bak
    }
}