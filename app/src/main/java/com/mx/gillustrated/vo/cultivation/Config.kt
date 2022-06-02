package com.mx.gillustrated.vo.cultivation

data class Config constructor(val lingGenType: List<LingGen>, val jingJieType:List<JingJie>,
                              val lingGenTian:List<SimpleData>,
                              val events: List<Event>,
                              val tianFuType: List<TianFu>,
                              val danYaoType: List<DanYao>,
                              val alliance: List<AllianceConfig>)