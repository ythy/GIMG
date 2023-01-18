package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.util.NameUtil

open class FollowerBak {
    lateinit var id: String
    var uniqueName: String = ""

    fun toFollower():Follower{
        return Follower(id, uniqueName)
    }

}

open class FollowerConfig{
    lateinit var id: String
    lateinit var name: String
    var rarity: Int = 0
    var type:Int = 0 // 0 normal, 1 spec can not add manually
    var max:Int = 1//max number can be auto added
    var property =  mutableListOf(0,0,0,0,0,0,0,0)
    var teji = mutableListOf<String>()
    var gender = NameUtil.Gender.Male

    override fun toString(): String {
        return CultivationHelper.showing("$name(${property.take(4).joinToString()})")
    }
}

class Follower(id:String, uniqueName:String = "") : FollowerBak() {
    val detail:FollowerConfig = CultivationHelper.mConfig.follower.find { it.id == id }!!
    var isDead:Boolean = false

    init {
        this.id = id
        this.uniqueName = uniqueName
    }

    fun toBak():FollowerBak{
        val bak = FollowerBak()
        bak.id = this.id
        bak.uniqueName = this.uniqueName
        return  bak
    }

    override fun toString(): String {
        return CultivationHelper.showing("${detail.name}(${detail.property.take(4).joinToString()})")
    }

}