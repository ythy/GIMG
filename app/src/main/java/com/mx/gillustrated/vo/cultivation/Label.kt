package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper
import java.util.*

class Label {

    lateinit var id:String
    lateinit var name:String
    var weight:Int = 0
    var rarity:Int = 0
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
    var teji:MutableList<String> = mutableListOf()
    var follower:MutableList<String> = mutableListOf()

    override fun toString(): String {
        val tejiName = if (teji.isNotEmpty()) CultivationHelper.mConfig.teji.find { it.id == teji[0] }?.name ?: "" else ""
        val followerName = if (follower.isNotEmpty()) CultivationHelper.mConfig.follower.find { it.id == follower[0] }?.name ?: "" else ""
        return CultivationHelper.showing("$name: (${property.take(6).joinToString()}) $tejiName $followerName ")
    }

    fun copy():Label{
        val label = Label()
        label.id = this.id
        label.name = this.name
        label.rarity = this.rarity
        label.weight = this.weight
        label.property = this.property.toMutableList()
        label.teji = Collections.synchronizedList(this.teji.toMutableList())
        label.follower = Collections.synchronizedList(this.follower.toMutableList())
        return label
    }
}