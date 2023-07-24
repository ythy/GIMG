package com.mx.gillustrated.component

import com.mx.gillustrated.vo.cultivation.EquipmentConfig

object CultivationAmuletHelper {
    // weight: max 10000
    data class AmuletType(val id:Int, val name:String, val equipmentId:String, val weightBase:Int, val rarityBase:Int, val property:MutableList<Int>,
                          val xiuwei:Int, val teji:MutableList<String> = mutableListOf())
    data class AmuletNormal(val id:Int, val weight:Int, val rarityAddon:Int, val bonusMultiple:Int, val xiuweiAddon:Int, val prefix:String)

    object Amulet {
        val NormalSizeWeight = mutableListOf(1,50,500)// used in creation
        val NormalSizeMultiple = mutableListOf(1,1,2,4)
        val NormalSizeRarityAddon = mutableListOf(0,0,1,2)

        val NormalSettings = mutableListOf(
                AmuletNormal(1,1, 1,5, 10, "\u51f9\u51f8\u4e4b"),
                AmuletNormal(2,10, 2, 10, 20, "\u7cbe\u826f\u4e4b"),
                AmuletNormal(3,50, 3, 15, 30, "\u5de5\u5320\u4e4b"),
                AmuletNormal(4,200, 4, 20, 40, "\u73e0\u5b9d\u5320\u4e4b"),
                AmuletNormal(5,1000,5, 30, 50, "\u5927\u5e08\u4e4b"),
                AmuletNormal(6,5000, 6, 40, 80, "\u5b97\u5e08\u4e4b"),
                AmuletNormal(7,20000, 7,50, 100, "\u795e\u5320\u4e4b")
        )

        val types = mutableListOf(
            AmuletType(111,  "\u602a\u5f02", "",50, 1, mutableListOf(0,1,1,0), 0),
            AmuletType(112,  "\u6B8B\u66B4", "",50, 1, mutableListOf(0,1,0,1), 0),
            AmuletType(113,  "\u6bc1\u706d", "",100,2, mutableListOf(1,1,1,1), 0),
            AmuletType(114,  "\u4e0d\u673d", "",100,2, mutableListOf(1,1,0,0), 0),

            AmuletType(201,  "\u5854-\u62c9\u590f\u7684\u5224\u51b3", "7005104",5000,9, mutableListOf(0,50,0,50), 100, mutableListOf("8001005")),
            AmuletType(202,  "\u5730\u72f1\u706b\u70ac", "7005102",5000,9, mutableListOf(200,0,100,0), 100),
            AmuletType(203,  "\u57fa\u5fb7\u7684\u8fd0\u6c14", "7005103",2000,6, mutableListOf(0,0,0,50), 100),
            AmuletType(204,  "\u4e54\u4e39\u4e4b\u77f3", "7005105",5000,9, mutableListOf(200,0,0,0), 200),
            AmuletType(205,  "\u5e03\u5c14\u51ef\u7d22\u4e4b\u6212","7005105", 5000,10, mutableListOf(0,100,0,0), 200, mutableListOf("8003006")),
            AmuletType(208,  "\u739B\u62C9\u7684\u4E07\u82B1\u7B52", "7005104",2000,6, mutableListOf(0,0,50,0), 100)
        )

    }

    // SEQ 5位 like 201   {0,1,2,3} >0 Normal   {0,1,2,3,4,5,6,7} >0 Normal id
    fun createEquipmentCustom(typeId:Int = 0, weightAddon:Int = 1):Pair<String, Int>?{
        //↓ 选取type
        var amuletType:AmuletType? = null
        if(typeId == 0){
            Amulet.types.toMutableList().sortedByDescending { it.weightBase }.forEach {
                if(amuletType == null && CultivationHelper.isTrigger(it.weightBase / weightAddon) ){
                    amuletType = it
                }
            }
        }else{
            amuletType = Amulet.types.find { it.id == typeId}
        }
        if (amuletType == null)
            return null

        val type = amuletType!!
        if (type.equipmentId != ""){
            return Pair(type.equipmentId, "${type.id}00".toInt())
        }
        //↓Normal
        val size = Amulet.NormalSizeWeight.toMutableList().sortedBy { it }.reduceIndexed { index, acc, i ->
            if (CultivationHelper.isTrigger(i)){
                index + 1
            }else
                acc
        }
        val normal = Amulet.NormalSettings.toMutableList().sortedBy { it.weight }.reduce { acc, amuletNormal ->
            if (CultivationHelper.isTrigger(amuletNormal.weight)){
                amuletNormal.copy()
            }else
                acc
        }
        return Pair(when(size){1->"7005101" 2-> "7005102" else-> "7005103"}, "${type.id}$size${normal.id}".toInt())
    }

    fun getEquipmentCustom(equipmentId:String, seq:Int):Pair<EquipmentConfig, String>{
        val amuletType = Amulet.types.find { it.id == seq / 100 }!!.copy()
        val size = ( seq / 10 ) % 10
        val normal = Amulet.NormalSettings.find { it.id == seq % 10 }

        val equipmentConfig = CultivationHelper.mConfig.equipment.find { it.id == equipmentId}!!


        val property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
        property.take(4).forEachIndexed { index, _ ->
            property[index] =  amuletType.property[index] * (normal?.bonusMultiple ?: 1) * Amulet.NormalSizeMultiple[size]
        }
        return Pair(EquipmentConfig(
                equipmentConfig.id,
                equipmentConfig.name,
                equipmentConfig.type,
                amuletType.rarityBase + (normal?.rarityAddon ?: 0) + Amulet.NormalSizeRarityAddon[size],
                amuletType.xiuwei + (normal?.xiuweiAddon ?: 0) * Amulet.NormalSizeMultiple[size],
                0,
                property,
                mutableListOf(),
                mutableListOf(),
                amuletType.teji,
                mutableListOf()
        ),  if (size == 0) amuletType.name else "${normal!!.prefix}${amuletType.name}${equipmentConfig.name}")
    }
}