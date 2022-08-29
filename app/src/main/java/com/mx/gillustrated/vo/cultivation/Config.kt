package com.mx.gillustrated.vo.cultivation

data class Config constructor(val lingGenType: List<LingGen>, val jingJieType:List<JingJie>,
                              val lingGenTian:List<SimpleData>,
                              val tianFuType: List<TianFu>,
                              val danYaoType: List<DanYao>,
                              val alliance: List<AllianceConfig>,
                              val equipment: List<Equipment>,
                              val teji: List<TeJi>,
                              val status: List<Status>,
                              val follower:List<Follower>,
                              val career:List<Career>
)