package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationAmuletHelper
import com.mx.gillustrated.component.CultivationEnemyHelper
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting

open class EquipmentBak(val id: String, val amuletSerialNo:Int = 0) {

    fun toEquipment():Equipment{
        return Equipment(id, amuletSerialNo)
    }

}

//8 100 //9 150 //10 200 //11 250 //12 300 //13 400 //14 500 //15 600
//300hp: 150 //200hp: 100 //100hp: 50
data class EquipmentConfig(
        val id:String,
        val name:String,
        val type: Int, // 0 Bao; 1 Wu; 2 Jia, 3 Yao; 5 Amulet; 6 boss?; 7 tips; 8 exclusive?; 9 spec; ?: 不添加至equipment list
        val rarity:Int = 0,//5 30, 6 40，7 50，8 ~
        val xiuwei:Int = 0,
        val success:Int = 0,
        val property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0),
        val spec:MutableList<Int> = mutableListOf(),//专属
        val specName:MutableList<String> = mutableListOf(),//专属
        val teji:MutableList<String> = mutableListOf(),
        val follower:MutableList<String> = mutableListOf(),
        val specTeji:MutableList<String> = mutableListOf(),
        val specTejiName:MutableList<String> = mutableListOf()
){
    //Gson 序列化使用
    constructor():this(
            "", "", 0, 0, 0, 0,  mutableListOf(0,0,0,0,0,0,0,0), mutableListOf(),
            mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
    )

    override fun toString(): String {
        return CultivationHelper.showing("$name:($xiuwei/$success)(${property.take(4).joinToString()})")
    }
}

class Equipment(pId: String, pAmuletSerialNo: Int = 0, pDetails:EquipmentConfig? = null) : EquipmentBak(pId, pAmuletSerialNo) {
    var detail:EquipmentConfig
    var uniqueName:String = ""
    var sortedWeight:Int = 0
    var childrenAll:MutableList<Equipment> = mutableListOf() //计算用
    var children:MutableList<Equipment> = mutableListOf() // 显示用

    init {
        if(amuletSerialNo > 0){ // equipment type == 5
            val setting = CultivationAmuletHelper.getEquipmentCustom(pId, amuletSerialNo)
            this.detail = setting.first
            this.uniqueName = setting.second
            this.sortedWeight = 10 + setting.first.rarity
        }else{
            this.detail = pDetails ?: CultivationHelper.mConfig.equipment.find { it.id == this.id }!!
            this.uniqueName = this.detail.name
            this.sortedWeight = when(this.detail.type){
                in 0..3 -> 100 + this.detail.rarity // career
                8 -> 10000 +  this.detail.rarity // exclude
                9 -> 1000 +  this.detail.rarity // custom
                else -> this.detail.rarity
            }
        }
    }

    override fun toString(): String {
        return CultivationHelper.showing("${detail.name}:(${detail.xiuwei}/${detail.success})(${detail.property.take(4).joinToString()})")
    }

    fun toBak():EquipmentBak{
        return  EquipmentBak(this.id, this.amuletSerialNo)
    }

}