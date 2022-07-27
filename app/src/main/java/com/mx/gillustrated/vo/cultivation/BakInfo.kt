package com.mx.gillustrated.vo.cultivation

class BakInfo {
    var xun: Long = 0
    var alliance:Map<String, AllianceConfig> = HashMap()
    var persons:Map<String, Person> = HashMap()
    var clans:Map<String, ClanBak> = HashMap()
    var battleRound:BattleRound? = null
}
