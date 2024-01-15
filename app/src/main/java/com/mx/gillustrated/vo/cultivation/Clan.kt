package com.mx.gillustrated.vo.cultivation

import java.util.concurrent.ConcurrentHashMap

open class ClanBak {
    lateinit var id:String // UUID
    lateinit var name:String
    var elder:String? = null //现任族长
    var nickName:String = ""
    var createDate:Long = 0//xun
    var battleRecord:MutableMap<Int, Int> = mutableMapOf()
    var crest:Int = 0

    fun toClan(personMap: ConcurrentHashMap<String, Person>):Clan{
        val clan = Clan()
        val zhu = personMap[this.elder ?: ""]
        clan.id = this.id
        clan.name = this.name
        clan.elder = this.elder
        clan.crest = this.crest
        clan.nickName = this.nickName
        clan.zhu = zhu
        clan.createDate = this.createDate
        clan.battleRecord = ConcurrentHashMap(this.battleRecord)
        return clan
    }

}

class Clan : ClanBak(){

    var zhu:Person? = null
    var xiuweiBattle:Int = 0// extra props
    var battleWinner:Int = 0// extra props
    var totalXiuwei:Long = 0// extra props
    var totalPerson:String = ""// extra props

    fun toClanBak():ClanBak{
        val bak = ClanBak()
        bak.id = super.id
        bak.name = super.name
        bak.elder = super.elder
        bak.crest = super.crest
        bak.nickName = super.nickName
        bak.createDate = super.createDate
        bak.battleRecord = super.battleRecord
        return bak
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Clan
        if (id != other.id) return false
        return true
    }
}