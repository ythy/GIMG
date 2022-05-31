package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.util.NameUtil

class Person{
        var id:String = ""
        var name: String = ""
        var lastName: String = ""
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
        var profile:Int = 0
        var partner:String? = null //赋值一次
        var partnerName:String? = null //赋值一次
        var parent:Pair<String, String>? = null//唯一
        var parentName:Pair<String, String>? = null//赋值一次，显示用
        var children:MutableList<String> = mutableListOf()
        var lifeTurn:Int = 0//

        var HP:Int = 10
        var maxHP:Int = 10
        var extraProperty:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)//update once

        var jingJieId:String = ""
        var jingJieSuccess:Int = 0
        var xiuXei:Int = 0
        var maxXiuWei:Long = 0
        var allianceId:String = ""
        var allianceXiuwei:Int = 0 //alliance 增益 zhu / speed； 每轮更新
        var allianceSuccess:Int = 0 //alliance 增益 更新一次
        var allianceProperty:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)//alliance

        //extra props
        var ancestorLevel:Int = 0 // 0 初代
        var ancestorId:String? = null// 一次
        lateinit var pinyinName:String// 一次
        var age:Int = 0 // now -  birthDay
        var jinJieName = "" //  updated by xun
        var jinJieColor = 0 //  updated by xun
        var jinJieMax:Int = 0 // updated by xun
        var extraXiuwei:Int = 0 //tianfu
        var extraTupo:Int = 0 //tianfu
        var extraSpeed:Int = 0 //tianfu
        var extraXuiweiMulti:Int = 0 //tianfu + alliance 更新一次
        var allianceName:String = "" // alliance

}