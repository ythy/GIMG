package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationEnemyHelper
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting

open class EquipmentBak(val id: String, val seq:Int = 0) {

    fun toEquipment():Equipment{
        return Equipment(id, seq)
    }

}

data class EquipmentConfig(
        val id:String,
        val name:String,
        val type: Int, // 0 Bao; 1 Wu; 2 Jia, 3 Yao; 5 Amulet; 6 boss?; 8 exclusive?; 9 spec; ?: 不添加至equipment list
        val rarity:Int = 0,//5 30, 6 40，7 50，8 ~
        val xiuwei:Int = 0,
        val success:Int = 0,
        val property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0),
        val spec:MutableList<Int> = mutableListOf(),//专属
        val specName:MutableList<String> = mutableListOf(),//专属
        val teji:MutableList<String> = mutableListOf(),
        val follower:MutableList<String> = mutableListOf()
){
    //Gson 序列化使用
    constructor():this(
            "", "", 0, 0, 0, 0,  mutableListOf(0,0,0,0,0,0,0,0), mutableListOf(),
            mutableListOf(), mutableListOf(), mutableListOf()
    )

    override fun toString(): String {
        return CultivationHelper.showing("$name:($xiuwei/$success)(${property.take(4).joinToString()})")
    }
}

class Equipment(pId: String, pSeq: Int = 0, option:Triple<Int, Int, String>? = null) : EquipmentBak(pId, pSeq) {
    val detail:EquipmentConfig
    var uniqueName:String = ""
    var childrenAll:MutableList<Equipment> = mutableListOf() //计算用
    var children:MutableList<Equipment> = mutableListOf() // 显示用

    init {
        if (option != null){ //equipment type == 6
            val setting = CultivationEnemyHelper.getEquipmentOfBoss(option.first, option.second)
            this.detail = setting.first
            this.uniqueName = setting.second
        }else{
            if(pSeq > 0){ // equipment type == 5
                val setting = CultivationSetting.getEquipmentCustom(pId, pSeq)
                this.detail = setting.first
                this.uniqueName = setting.second
            }else {
                this.detail = CultivationHelper.mConfig.equipment.find { it.id == this.id }!!
                this.uniqueName = this.detail.name
            }
        }
    }



    override fun toString(): String {
        return CultivationHelper.showing("${detail.name}:(${detail.xiuwei}/${detail.success})(${detail.property.take(4).joinToString()})")
    }

    fun toBak():EquipmentBak{
        return  EquipmentBak(this.id, this.seq)
    }

}