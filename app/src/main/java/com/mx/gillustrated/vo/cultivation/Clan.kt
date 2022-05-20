package com.mx.gillustrated.vo.cultivation

class Clan {
    lateinit var id:String // person.ancestorId
    lateinit var name:String
    var createDate:Int = 0//xun
    var persons = mutableListOf<String>()

    var totalXiuwei:Long = 0// extra props
}