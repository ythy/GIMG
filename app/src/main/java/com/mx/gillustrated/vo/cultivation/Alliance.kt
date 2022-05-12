package com.mx.gillustrated.vo.cultivation

//zhu 增益 20
class Alliance {
    lateinit var id:String
    lateinit var name:String
    var level:Int = 1// 权重100的约分 默认1
    var lifetime:Int = 0
    var xiuwei:Int = 0
    var lingGen:String? = null
    var zhu:Person? = null
    var hu:MutableList<Person> = mutableListOf()
    var persons:MutableList<Person> = mutableListOf()
    var speedG1:Int = 0
    var speedG1List:MutableList<Person> = mutableListOf()
    var speedG2:Int = 0
    var speedG2List:MutableList<Person> = mutableListOf()

}