package com.mx.gillustrated.vo.cultivation
import com.mx.gillustrated.component.CultivationHelper

open class TipsBak {
    lateinit var id: String
    var level: Int = 0

    fun toTips():Tips{
        return Tips(id, level)
    }

}

data class TipsConfig(
        val id:String,
        val name:String,
        val type:Int = 0,// 0: alliance  2: lingGen  3: reward
        val rarity:Int = 0,
        val talent:Int = 0, // min talent
        val difficulty:Int = 0,
        val bonus:MutableList<Int> = mutableListOf(),
        val alliances:MutableList<String> = mutableListOf(),
        val teji:MutableList<String> = mutableListOf(),
        val hp:MutableList<Int> = mutableListOf(),
        val attack:MutableList<Int> = mutableListOf(),
        val defence:MutableList<Int> = mutableListOf(),
        val speed:MutableList<Int> = mutableListOf(),
        val lingGen: MutableList<String> = mutableListOf()
){
    //Gson 序列化使用
    constructor():this(
            "", "", 0,0,0, 0, mutableListOf(),mutableListOf(),mutableListOf()
            ,mutableListOf(),mutableListOf(),mutableListOf(),mutableListOf(),mutableListOf()
    )
}

class Tips(id:String, level:Int = 0):TipsBak() {
    val detail:TipsConfig = CultivationHelper.mConfig.tips.find { it.id == id } ?: TipsConfig("", "")

    init {
        this.id = id
        this.level = level
    }

    fun toBak():TipsBak{
        val bak = TipsBak()
        bak.id = this.id
        bak.level = this.level
        return bak
    }
}