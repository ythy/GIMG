package com.mx.gillustrated.component

import com.mx.gillustrated.vo.cultivation.EquipmentConfig

object CultivationAmuletHelper {
    // weight: max 10000
    data class AmuletType(val id:Int, val name:String, val weight:Int, val rarityBonus:Int, val addProperty:MutableList<Boolean>,
                          val addXiuwei:Boolean, val props:MutableList<AmuletProps>, val config:MutableList<AmuletConfig>)
    data class AmuletProps(val id:Int, val weight:Int, val rarityBase:Int, val bonus:Int, val xiuwei:Int, val prefix:String, val teji:MutableList<String> = mutableListOf())
    data class AmuletConfig(val equipmentId:String, val weight:Int, val rarityBonus:Int, val propsMulti:Int)

    private object Amulet {
        val configSmall = mutableListOf(AmuletConfig("7005101",1, 0, 1))
        val configLarge = mutableListOf(AmuletConfig("7005102",50, 1, 2))
        val configGrand = mutableListOf(AmuletConfig("7005103",500, 2, 4))
        val configNecklace = mutableListOf(AmuletConfig("7005104",1, 0, 1))
        val configRing = mutableListOf(AmuletConfig("7005105",1, 0, 1))
        val configNormal = mutableListOf(configSmall[0], configLarge[0], configGrand[0])

        val propsNormal = mutableListOf(
                AmuletProps(0,1, 1,5, 10, "\u51f9\u51f8\u4e4b"),
                AmuletProps(1,10, 2, 10, 20, "\u7cbe\u826f\u4e4b"),
                AmuletProps(2,50, 3, 15, 30, "\u5de5\u5320\u4e4b"),
                AmuletProps(3,200, 4, 20, 40, "\u73e0\u5b9d\u5320\u4e4b"),
                AmuletProps(4,1000,5, 30, 50, "\u5927\u5e08\u4e4b"),
                AmuletProps(5,5000, 6, 40, 80, "\u5b97\u5e08\u4e4b"),
                AmuletProps(6,20000, 7,50, 100, "\u795e\u5320\u4e4b")
        )

        val propsTal = mutableListOf(AmuletProps(0,1,9,50, 100, "", mutableListOf("8001005")))
        val propsTorch = mutableListOf(AmuletProps(0,1,9,100, 100, ""))
        val propsGheed = mutableListOf(AmuletProps(0,1,6,40, 100, ""))
        val propsJordan = mutableListOf(AmuletProps(0,1,9,100, 200, ""))
        val propsBul = mutableListOf(AmuletProps(0,1,10,100, 200, "", mutableListOf("8003006")))
        val propsNagel = mutableListOf(AmuletProps(0,1,4,0, 50, ""))
        val propsRaven = mutableListOf(AmuletProps(0,1,5,50, 0, ""))
        val propsMara = mutableListOf(AmuletProps(0,1,6,50, 100, ""))

        val types = mutableListOf(
                AmuletType(111,  "\u602a\u5f02", 50, 1, mutableListOf(false,true,true,false), false, propsNormal, configNormal),
                AmuletType(112,  "\u6B8B\u66B4", 50, 1, mutableListOf(false,true,false,true), false, propsNormal, configNormal),
                AmuletType(113,  "\u6bc1\u706d", 100,2, mutableListOf(true,true,true,true), false, propsNormal, configNormal),
                AmuletType(114,  "\u4e0d\u673d", 100,2, mutableListOf(true,true,false,false), true, propsNormal, configNormal),

                AmuletType(201,  "\u5854-\u62c9\u590f\u7684\u5224\u51b3", 5000,0, mutableListOf(false,true,false,true), true, propsTal, configNecklace),
                AmuletType(202,  "\u5730\u72f1\u706b\u70ac", 5000,0, mutableListOf(true,false,true,false), true, propsTorch, configLarge),
                AmuletType(203,  "\u57fa\u5fb7\u7684\u8fd0\u6c14", 2000,0, mutableListOf(false,false,false,true), true, propsGheed, configGrand),
                AmuletType(204,  "\u4e54\u4e39\u4e4b\u77f3", 5000,0, mutableListOf(true,false,false,false), true, propsJordan, configRing),
                AmuletType(205,  "\u5e03\u5c14\u51ef\u7d22\u4e4b\u6212", 5000,0, mutableListOf(false,true,false,false), false, propsBul, configRing),
                AmuletType(206,  "\u62ff\u5404\u7684\u6212\u6307", 200,0, mutableListOf(false,false,false,false), true, propsNagel, configRing),
                AmuletType(207,  "\u4e4c\u9e26\u4e4b\u971c", 200,0, mutableListOf(true,false,false,true), false, propsRaven, configRing),
                AmuletType(208,  "\u739B\u62C9\u7684\u4E07\u82B1\u7B52", 1000,0, mutableListOf(false,false,true,false), true, propsMara, configNecklace)
        )

    }

    // SEQ like 20101  5位
    fun createEquipmentCustom(fixType:Int = 0):Pair<String, Int>?{
        //↓ 选取type
        var amuletType:AmuletType? = null
        if(fixType == 0){
            Amulet.types.toMutableList().sortedByDescending { it.weight }.forEach {
                if(amuletType == null && CultivationHelper.isTrigger(it.weight) ){
                    amuletType = it
                }
            }
        }else{
            amuletType = Amulet.types.find { it.id == fixType}
        }
        if (amuletType == null)
            return null
        //↓ 选取props
        var props:AmuletProps? = null
        if(amuletType!!.props.size == 1){
            props = amuletType!!.props[0]
        }else{
            amuletType!!.props.sortedByDescending { it.weight }.forEach {
                if(props == null && CultivationHelper.isTrigger(it.weight) ){
                    props = it
                }
            }
        }
        //↓ 选取equipment
        var config:AmuletConfig? = null
        if(amuletType!!.config.size == 1){
            config = amuletType!!.config[0]
        }else {
            amuletType!!.config.sortedByDescending { it.weight }.forEach {
                if (config == null && CultivationHelper.isTrigger(it.weight)) {
                    config = it
                }
            }
        }
        return Pair(config!!.equipmentId, "${amuletType!!.id}${createDecadeSeq(props!!.id)}".toInt())
    }

    fun getEquipmentCustom(id:String, seq:Int):Pair<EquipmentConfig, String>{
        val amuletType = Amulet.types.find { it.id == seq / 100 }!!
        val props = amuletType.props.find { it.id == seq % 100 } ?: amuletType.props[0]
        val config = amuletType.config.find { it.equipmentId == id } ?: amuletType.config[0]
        val equipmentConfig = CultivationHelper.mConfig.equipment.find { it.id == config.equipmentId}!!
        val configPropsMulti = if(amuletType.config.size == 1) 1 else config.propsMulti
        val configRarityBonus = if(amuletType.config.size == 1) 0 else config.rarityBonus

        val property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
        property.take(4).forEachIndexed { index, _ ->
            if (amuletType.addProperty[index]){
                property[index] =  props.bonus * configPropsMulti * (if (index == 0) 5 else 1)
            }else{
                property[index] = 0
            }
        }
        return Pair(EquipmentConfig(
                equipmentConfig.id,
                equipmentConfig.name,
                equipmentConfig.type,
                props.rarityBase + amuletType.rarityBonus + configRarityBonus,
                if(amuletType.addXiuwei) props.xiuwei * configPropsMulti else 0,
                0,
                property,
                mutableListOf(),
                mutableListOf(),
                props.teji,
                mutableListOf()
        ),  if (amuletType.props.size == 1) amuletType.name else "${props.prefix}${amuletType.name}${equipmentConfig.name}")
    }

    fun createDecadeSeq(index:Int):String{
        return  when(index){
            in 0..9 -> "0$index"
            else -> "${Math.min(99, index)}"
        }
    }

}