package com.mx.gillustrated.vo.cultivation

class Enemy {
    lateinit var id:String
    lateinit var name:String
    var HP:Int = 0
    var maxHP:Int = 0
    var attack:Int = 0
    var defence:Int = 0
    var speed:Int = 0
    var attackFrequency:Int = 12 //xun
    var birthDay:Long = 0 //xun
    var lifetime:Long = 0 //xun
    var isDead:Boolean = false
}