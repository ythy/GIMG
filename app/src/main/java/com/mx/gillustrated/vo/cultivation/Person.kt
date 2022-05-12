package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.util.NameUtil

class Person{
        var id:String = ""
        var name: String = ""
        var gender: NameUtil.Gender = NameUtil.Gender.Default
        lateinit var lingGenType: LingGen
        var lingGenId:String = "" //Tian spec
        var lingGenName:String = ""
        var birthDay:MutableList<Pair<Int, Int>> = mutableListOf()
        var lifetime:Int = 100
        var events:MutableList<PersonEvent> = mutableListOf()
        var tianfus:MutableList<TianFu> = mutableListOf()
        var isDead:Boolean = false
        var isFav:Boolean = false

        var jingJieId:String = ""
        var jingJieSuccess:Int = 0
        var xiuXei:Int = 0
        var maxXiuWei:Int = 0
        var allianceId:String = ""
        var allianceXiuwei:Int = 0 //alliance 增益 zhu / speed

        //extra props
        var age:Int = 0 // now -  birthDay
        var jinJieName = "" //  updated by xun
        var jinJieMax:Int = 0 // updated by xun
        var extraXiuwei:Int = 0 //tianfu
        var extraTupo:Int = 0 //tianfu
        var extraSpeed:Int = 0 //tianfu
        var extraXuiweiMulti:Double = 0.0 //tianfu
        var allianceName:String = "" // alliance

}