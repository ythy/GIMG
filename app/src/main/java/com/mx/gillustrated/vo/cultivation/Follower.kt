package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.util.NameUtil

class Follower {
    lateinit var id: String
    lateinit var name: String
    var rarity: Int = 0
    var property =  mutableListOf(0,0,0,0,0,0,0,0)
    var teji = mutableListOf<String>()
    var gender = NameUtil.Gender.Male
    var unique:Boolean = false
    var commission: Int = 0
    //以下字段不在配置里
    var uniqueName: String = "" //unique为true时为空

    override fun toString(): String {
        return CultivationHelper.showing("$name(${property.take(4).joinToString()})")
    }

    fun copy():Follower{
        val follower = Follower()
        follower.id = this.id
        follower.name = this.name
        follower.rarity = this.rarity
        follower.property = this.property
        follower.teji = this.teji
        follower.gender = this.gender
        follower.unique = this.unique
        follower.commission = this.commission
        follower.uniqueName = this.uniqueName
        return follower
    }

}