package com.mx.gillustrated.vo.cultivation

import java.util.concurrent.ConcurrentHashMap


open class NationBak {
    var battleRecord:MutableMap<Int, Int> = mutableMapOf()

    fun toNation(nationConfig: NationConfig):Nation{
        val nation = Nation()
        nation.id = nationConfig.id
        nation.name = nationConfig.name
        nation.battleRecord = ConcurrentHashMap(this.battleRecord)
        return nation
    }
}

open class NationConfig: NationBak() {
    lateinit var id:String
    lateinit var name:String
}

//nationPost: 0 无，1 di，2 taiwei 3 shangshu 4 cishi 5 douwei
class Nation : NationConfig(){
    var nationPersonList: ConcurrentHashMap<String, Person> = ConcurrentHashMap() //此值不保存到全局mNation里
    var totalTurn:Int = 0
    var emperor:String? = null
    var taiWei:String? = null
    var shangShu:String? = null
    var ciShi:MutableList<String> = mutableListOf()
    var duWei:MutableList<String> = mutableListOf()
    var xiuweiBattle:Int = 0
    var battleWinner:Int = 0

    fun toNationBak():NationBak{
        val nation = NationBak()
        nation.battleRecord = this.battleRecord
        return nation
    }

    fun copy():Nation{
        val nation = Nation()
        nation.id = this.id
        nation.name = this.name
        nation.emperor = this.emperor
        nation.taiWei = this.taiWei
        nation.shangShu = this.shangShu
        nation.ciShi = this.ciShi.toMutableList()
        nation.duWei = this.duWei.toMutableList()
        nation.battleWinner = this.battleWinner
        nation.xiuweiBattle = this.xiuweiBattle
        nation.battleRecord = this.battleRecord
        return nation
    }

}