package com.mx.gillustrated.vo.cultivation

class Career {
    lateinit var id:String
    lateinit var name:String
    var rarity:Int = 0
    var weight:Int = 0
    var upgradeBasicXiuwei:Long = 0L
    var upgradeBasicSuccess:Int = 0
    // 以下字段不在配置里
    var level:Int = 0



    fun copy():Career{
        val career = Career()
        career.id = this.id
        career.name = this.name
        career.rarity = this.rarity
        career.weight = this.weight
        career.upgradeBasicXiuwei = this.upgradeBasicXiuwei
        career.upgradeBasicSuccess = this.upgradeBasicSuccess
        return career
    }
}