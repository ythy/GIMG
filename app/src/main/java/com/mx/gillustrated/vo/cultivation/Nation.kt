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


class Nation : NationConfig(){
    var nationPersonList: ConcurrentHashMap<String, Person> = ConcurrentHashMap() //此值不保存到全局mNation里
    var totalTurn:Int = 0
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
        nation.battleWinner = this.battleWinner
        nation.xiuweiBattle = this.xiuweiBattle
        nation.battleRecord = this.battleRecord
        return nation
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Nation
        if (id != other.id) return false
        return true
    }
}