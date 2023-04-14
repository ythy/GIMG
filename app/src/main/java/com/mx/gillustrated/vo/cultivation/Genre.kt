package com.mx.gillustrated.vo.cultivation

import java.util.*

class Genre {

    lateinit var id:String
    lateinit var name:String
    var type:Int = 0
    var tips:MutableList<String> = mutableListOf()



    fun copy():Genre{
        val label = Genre()
        label.id = this.id
        label.name = this.name
        label.type = this.type
        label.tips = Collections.synchronizedList(this.tips.toMutableList())
        return label
    }
}