package com.mx.gillustrated.vo.cultivation

class Status {
    lateinit var id: String
    lateinit var name: String
    var description: String = ""
    var rarity: Int = 0
    var chance:Int = 100
    var target = 0 // 0 mine; 1 opponent
    var power:Int = 0

}