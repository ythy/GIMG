package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper
import java.util.*

class Skin() {

    lateinit var id:String
    lateinit var name:String
    lateinit var resource:String
    var animated:Boolean = false
    var xiuwei:Int = 0
    var spec:MutableList<Int> = mutableListOf()//专属
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
    var teji:MutableList<String> = mutableListOf()
    var follower:MutableList<String> = mutableListOf()

    constructor(ids:String, names:String):this(){
        this.id = ids
        this.name = names
        this.resource = ""
    }

    override fun toString(): String {
        return CultivationHelper.showing("$name($xiuwei)")
    }

    fun copy():Skin{
        val skin = Skin()
        skin.id = this.id
        skin.name = this.name
        skin.resource = this.resource
        skin.animated = this.animated
        skin.xiuwei = this.xiuwei
        skin.property = this.property.toMutableList()
        skin.spec = Collections.synchronizedList(this.spec.toMutableList())
        skin.teji = Collections.synchronizedList(this.teji.toMutableList())
        skin.follower = Collections.synchronizedList(this.follower.toMutableList())
        return skin
    }

}