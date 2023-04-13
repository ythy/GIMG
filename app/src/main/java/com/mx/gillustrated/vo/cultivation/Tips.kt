package com.mx.gillustrated.vo.cultivation
import com.mx.gillustrated.component.CultivationHelper

open class TipsBak {
    lateinit var id: String
    var level: Int = 0

    fun toTips():Tips{
        return Tips(id, level)
    }

}
//alliance 1-4 nei; 5,6 gongji; 7;jueji; 8: nei; 9:shen; 10: SSS
data class TipsConfig(
        val id:String,
        val name:String,
        val type:Int = 0,// 0: alliance  2: lingGen  3: reward
        val rarity:Int = 0,
        val talent:Int = 0, // min talent
        val difficulty:Int = 0,
        val bonus:MutableList<Int> = mutableListOf(),
        val teji:MutableList<String> = mutableListOf(),
        val hp:MutableList<Int> = mutableListOf(),
        val attack:MutableList<Int> = mutableListOf(),
        val defence:MutableList<Int> = mutableListOf(),
        val speed:MutableList<Int> = mutableListOf()
){
    //Gson 序列化使用
    constructor():this(
            "", "", 0,0,0, 0, mutableListOf(),mutableListOf()
            ,mutableListOf(),mutableListOf(),mutableListOf(),mutableListOf()
    )
}

class Tips(id:String, level:Int = 0):TipsBak() {

    val detail:TipsConfig = CultivationHelper.mConfig.tips.find { it.id == id } ?: TipsConfig("", "")
    var tipsName = detail.name

    init {
        this.id = id
        this.level = level
    }

    constructor(id:String, level: Int, name:String):this(id, level){
        this.tipsName = name
    }


    fun toBak():TipsBak{
        val bak = TipsBak()
        bak.id = this.id
        bak.level = this.level
        return bak
    }
}