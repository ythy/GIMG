package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper

open class CareerBak{
    lateinit var id:String
    var level:Int = 0

    fun toCareer():Career{
        return Career(id, level)
    }
}

class CareerConfig {
    lateinit var id:String
    lateinit var name:String
    var rarity:Int = 0
    var weight:Int = 0
    var maxLevel:Int = 100
    var upgradeBasicXiuwei:Long = 0L
}

class Career(id:String, level: Int): CareerBak() {
    val detail = CultivationHelper.mConfig.career.find { it.id == id }!!

    init {
        this.id = id
        this.level = level
    }

    override fun toString(): String {
        return if(level >= detail.maxLevel)
            CultivationHelper.showing(detail.name)
        else
            "${CultivationHelper.showing(detail.name)}$level"
    }

    fun toBak():CareerBak{
        val bak = CareerBak()
        bak.id = this.id
        bak.level = this.level
        return  bak
    }

}