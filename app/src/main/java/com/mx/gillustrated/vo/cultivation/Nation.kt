package com.mx.gillustrated.vo.cultivation

import java.util.concurrent.ConcurrentHashMap

open class NationBak {
    lateinit var id:String
    lateinit var name:String
    var zhu:String? = null
    var hu:List<String> = listOf()

    fun toNation():Nation{
        val nation = Nation()
        nation.id = this.id
        nation.name = this.name
        return nation
    }
}


class Nation : NationBak(){
    var nationPersonList: ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var totalTurn:Int = 0
    var nationZhu:Person? = null
    var nationHu:ConcurrentHashMap<String, Person> = ConcurrentHashMap()

    fun toNationBak():NationBak{
        val nation = NationBak()
        nation.id = this.id
        nation.name = this.name
        return nation
    }

    fun copy():Nation{
        val nation = Nation()
        nation.id = this.id
        nation.name = this.name
        return nation
    }

}