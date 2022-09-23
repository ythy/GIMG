package com.mx.gillustrated.vo.cultivation

import java.util.concurrent.ConcurrentHashMap

class Nation{
    lateinit var id:String
    lateinit var name:String

    var nationPersonList: ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var totalTurn:Int = 0

    fun copy():Nation{
        val nation = Nation()
        nation.id = this.id
        nation.name = this.name
        return nation
    }
}