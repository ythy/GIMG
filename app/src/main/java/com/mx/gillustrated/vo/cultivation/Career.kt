package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper

class Career {
    lateinit var id:String
    lateinit var name:String
    var rarity:Int = 0
    var weight:Int = 0
    var maxLevel:Int = 100
    var upgradeBasicXiuwei:Long = 0L
    // 以下字段不在配置里
    var level:Int = 0



    fun copy():Career{
        val career = Career()
        career.id = this.id
        career.name = this.name
        career.rarity = this.rarity
        career.weight = this.weight
        career.maxLevel = this.maxLevel
        career.upgradeBasicXiuwei = this.upgradeBasicXiuwei
        career.level = this.level
        return career
    }

    override fun toString(): String {
        return "${CultivationHelper.showing(name)}$level"
    }
}