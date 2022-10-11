package com.mx.gillustrated.vo.cultivation

class BakInfo {
    var xun: Long = 0
    var alliance:Map<String, AllianceConfig> = HashMap()
    var persons:Map<String, Person> = HashMap()
    var clans:Map<String, ClanBak> = HashMap()
    var nation:Map<String, NationBak> = HashMap()
    var battleRound:BattleRound? = null
    var xunDurationPair:Map<Pair<String, Int>, Long> = HashMap()
}
