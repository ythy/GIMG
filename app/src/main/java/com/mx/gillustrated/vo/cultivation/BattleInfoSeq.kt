package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationHelper

class BattleInfoSeq constructor( var round:Int, var seq:Int, var content:String){

    override fun toString(): String {
        if(round > 0)
            return CultivationHelper.showing("第${round}轮-$seq: $content")
        else
            return CultivationHelper.showing(content)
    }
}