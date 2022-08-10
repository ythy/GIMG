package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper

class BattleInfoSeq constructor( var round:Int, var seq:Int, var content:String){

    var teji:String? = null
    var winner:String = ""
    var looser:String = ""

    constructor(round:Int, seq:Int, content:String, t:String):this(round, seq, content){
        teji = t
    }

    override fun toString(): String {
        if(round > 0)
            return CultivationHelper.showing("第${round}轮-$seq: $content")
        else
            return CultivationHelper.showing(content)
    }
}