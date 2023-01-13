package com.mx.gillustrated.vo.cultivation

class BakInfo {
    var xun: Long = 0
    var alliance:Map<String, AllianceBak> = HashMap()
    var persons:Map<String, Person> = HashMap()
    var clans:Map<String, ClanBak> = HashMap()
    var nation:Map<String, NationBak> = HashMap()
    var battleRound:BattleRound? = null
    var xunDuration:Map<String, Long> = HashMap()
    var bossRecord:MutableList<MutableMap<Int, String>> = mutableListOf()
}
