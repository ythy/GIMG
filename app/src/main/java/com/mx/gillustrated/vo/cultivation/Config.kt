package com.mx.gillustrated.vo.cultivation

data class Config constructor(val lingGenType: List<LingGen>, val jingJieType:List<JingJie>,
                              val lingGenTian:List<SimpleData>,
                              val tianFuType: List<TianFu>,
                              val alliance: List<AllianceConfig>,
                              val equipment: List<EquipmentConfig>,
                              val teji: List<TeJi>,
                              val status: List<Status>,
                              val follower:List<Follower>,
                              val career:List<Career>,
                              val nation:List<Nation>,
                              val label:List<Label>
)