package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper
import java.util.*

open class TeJiConfig {
    lateinit var id:String
    lateinit var name:String
    var description:String = ""
    var rarity:Int = 0
    var weight:Int = 0
    var type:Int = 0
    var chance:Int = 100
    var status:String = ""
    var statusRound:Int = 0 // combining with status
    var power:Int = 0
    var extraPower:MutableList<Int> = mutableListOf(0,0,0,0)
    var spec:MutableList<Int> = mutableListOf()//专属
    var specName:MutableList<String> = mutableListOf()//专属

    fun toTeji():TeJi{
        val teJi = TeJi()
        teJi.id = this.id
        teJi.name = this.name
        teJi.rarity = this.rarity
        teJi.description = this.description
        teJi.weight = this.weight
        teJi.type = this.type
        teJi.chance = this.chance
        teJi.status = this.status
        teJi.statusRound = this.statusRound
        teJi.power = this.power
        teJi.spec =  Collections.synchronizedList(this.spec.toMutableList())
        teJi.specName =  Collections.synchronizedList(this.specName.toMutableList())
        teJi.extraPower =  Collections.synchronizedList(this.extraPower.toMutableList())
        return teJi
    }

    override fun toString(): String {
        return CultivationHelper.showing("$name-$rarity")
    }

}


class TeJi: TeJiConfig() {

    var form:Int = 0 // 0 list 1 ex equipment 2 amulet 3 fixed spec person 4 label

}