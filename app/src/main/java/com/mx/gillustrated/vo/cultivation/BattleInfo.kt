package com.mx.gillustrated.vo.cultivation

import com.mx.gillustrated.component.CultivationBattleHelper.BattleObject

class BattleInfo constructor(var id:String, var attacker:Person?, var defender: Person?, var attackerValue: BattleObject, var defenderValue: BattleObject ) {

    var details = mutableListOf<BattleInfoSeq>()
    var seq:Int = 0 // 临时
    var round:Int = 0 // 临时
}