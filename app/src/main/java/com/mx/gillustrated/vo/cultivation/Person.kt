package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.util.NameUtil

class Person{
        var id:String = ""
        var name: String = ""
        var gender: NameUtil.Gender = NameUtil.Gender.Default
        lateinit var lingGenType: LingGen
        var lingGenId:String = "" //Tian spec
        var lingGenName:String = ""
        var birthDay:Int = 0
        var lifetime:Int = 100
        var events:MutableList<PersonEvent> = mutableListOf()

        var jingJieId:String = ""
        var jingJieSuccess:Int = 0
        var xiuXei:Int = 0

        //extra props
        var age:Int = 0 // now -  birthDay
        var jinJieName = ""
        var jinJieMax:Int = 0
}