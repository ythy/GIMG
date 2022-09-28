package com.mx.gillustrated.vo.cultivation

import java.util.concurrent.ConcurrentHashMap


open class NationBak {
    lateinit var id:String
    lateinit var name:String
    var postTop:Triple<String?, String?, String?>? = null// 1 2 3
    var postCishi:List<String> = listOf()
    var postDuwei:List<String> = listOf()

    fun toNation(personMap: ConcurrentHashMap<String, Person>):Nation{
        val nation = Nation()
        nation.id = this.id
        nation.name = this.name
        if(this.postTop?.first != null)
            nation.emperor = personMap[this.postTop!!.first.toString()]
        if(this.postTop?.second != null)
            nation.taiWei = personMap[this.postTop!!.second.toString()]
        if(this.postTop?.third != null)
            nation.shangShu = personMap[this.postTop!!.third.toString()]
        nation.ciShi = postCishi.mapNotNull { personMap[it] }.toMutableList()
        nation.duWei = postDuwei.mapNotNull { personMap[it] }.toMutableList()
        return nation
    }
}

//nationPost: 0 无，1 di，2 taiwei 3 shangshu 4 cishi 5 douwei
class Nation : NationBak(){
    var nationPersonList: ConcurrentHashMap<String, Person> = ConcurrentHashMap() //此值不保存到全局mNation里
    var totalTurn:Int = 0
    var emperor:Person? = null
    var taiWei:Person? = null
    var shangShu:Person? = null
    var ciShi:MutableList<Person> = mutableListOf()
    var duWei:MutableList<Person> = mutableListOf()

    fun toNationBak():NationBak{
        val nation = NationBak()
        nation.id = this.id
        nation.name = this.name
        nation.postTop = Triple(this.emperor?.id, this.taiWei?.id, this.shangShu?.id)
        nation.postCishi = this.ciShi.map { it.id }
        nation.postDuwei = this.duWei.map { it.id }
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
        return nation
    }

}