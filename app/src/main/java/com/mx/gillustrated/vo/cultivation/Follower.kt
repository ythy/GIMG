package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper

class Follower {
    lateinit var id: String
    lateinit var name: String
    var rarity: Int = 0
    var property =  mutableListOf(0,0,0,0,0,0,0,0)
    var teji = mutableListOf<String>()
    var unique:Boolean = false
    var commission: Int = 0
    var uniqueName: String = "" //unique为true时为空

    override fun toString(): String {
        return CultivationHelper.showing("$name(${property.take(4).joinToString()})")
    }
}