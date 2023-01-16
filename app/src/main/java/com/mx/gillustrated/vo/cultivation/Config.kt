package com.mx.gillustrated.vo.cultivation

data class Config constructor(val lingGenType: List<LingGen>, val jingJieType:List<JingJie>,
                              val lingGenTian:List<SimpleData>,
                              val tianFuType: List<TianFu>,
                              val alliance: List<AllianceConfig>,
                              val equipment: List<EquipmentConfig>,
                              val teji: List<TeJiConfig>,
                              val status: List<Status>,
                              val follower:List<FollowerConfig>,
                              val career:List<CareerConfig>,
                              val nation:List<NationConfig>,
                              val label:List<Label>
)