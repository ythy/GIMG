package com.mx.gillustrated.vo.cultivation


import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.util.NameUtil
import java.util.*

open class PersonBak {
    var id: String = ""
    var name: String = ""
    var lastName: String = ""
    var fullName: String = ""
    var gender: NameUtil.Gender = NameUtil.Gender.Default
    var lingGenTypeId: String = ""
    var lingGenSpecId: String = "" //Tian spec
    var lingGenName: String = ""
    var birthtime: Long = 0
    var lifetime: Long = 0 // if  lifetime < currentXun  , go dead
    var profile: Int = 0
    var partner: String? = null
    var partnerName: String? = null
    var parent: Pair<String, String>? = null//唯一
    var parentName: Pair<String, String>? = null//赋值一次，显示用
    var children: MutableList<String> = Collections.synchronizedList(mutableListOf())
    var lifeTurn: Int = 0
    var deadExceptTimes: Int = 0
    var isFav: Boolean = false
    var singled: Boolean = false
    var dink: Boolean = false
    var neverDead: Boolean = false

    var events: MutableList<PersonEvent> = Collections.synchronizedList(mutableListOf())
    var tianfu: MutableList<String> = mutableListOf()
    var label: MutableList<String> = Collections.synchronizedList(mutableListOf())
    var skin:String = ""
    var ancestorLevel: Int = 0//加入其他clan 会变更
    var ancestorId: String? = null// 可能多次，独立后变更为ID
    var ancestorOrignId: String? = ancestorId// 一次
    var ancestorOrignLevel: Int = ancestorLevel//一直累加

    var teji: MutableList<String> = Collections.synchronizedList(mutableListOf())
    var career: MutableList<CareerBak> = Collections.synchronizedList(mutableListOf())
    var follower: MutableList<FollowerBak> = Collections.synchronizedList(mutableListOf())
    var equipment: MutableList<EquipmentBak> = Collections.synchronizedList(mutableListOf())

    var battleRecord: MutableMap<Int, Int> = mutableMapOf()
    var jingJieId: String = ""
    var jingJieSuccess: Int = 0
    var xiuXei: Int = 0
    var maxXiuWei: Long = 0
    var pointXiuWei: Long = 0
    var allianceId: String = ""
    var specIdentity: Int = 0 //spec person nid
    var specIdentityTurn: Int = 0 //spec person turn added while dead

    fun toPerson(): Person {
        val person = Person()
        person.id = this.id
        person.name = this.name
        person.lastName = this.lastName
        person.fullName = this.fullName
        person.gender = this.gender
        person.lingGenTypeId = this.lingGenTypeId
        person.lingGenSpecId = this.lingGenSpecId
        person.lingGenName = this.lingGenName
        person.birthtime = this.birthtime
        person.lifetime = this.lifetime
        person.events = this.events
        person.label = this.label
        person.skin = this.skin
        person.ancestorLevel = this.ancestorLevel
        person.ancestorId = this.ancestorId
        person.ancestorOrignId = this.ancestorOrignId
        person.ancestorOrignLevel = this.ancestorOrignLevel

        person.isFav = this.isFav
        person.profile = this.profile
        person.partner = this.partner
        person.partnerName = this.partnerName
        person.parent = this.parent
        person.parentName = this.parentName
        person.children = this.children
        person.lifeTurn = this.lifeTurn
        person.deadExceptTimes = this.deadExceptTimes
        person.singled = this.singled
        person.dink = this.dink
        person.neverDead = this.neverDead
        person.teji = this.teji

        person.battleRecord = this.battleRecord
        person.jingJieId = this.jingJieId
        person.jinJieName = CultivationHelper.getJinJieName(CultivationHelper.mConfig.jingJieType.find { it.id == this.jingJieId }!!.name)
        person.jingJieSuccess = this.jingJieSuccess
        person.xiuXei = this.xiuXei
        person.maxXiuWei = this.maxXiuWei
        person.pointXiuWei = this.pointXiuWei
        person.allianceId = this.allianceId
        person.specIdentity = this.specIdentity
        person.specIdentityTurn = this.specIdentityTurn

        person.lingGenDetail = CultivationHelper.mConfig.lingGenType.find { it.id == person.lingGenTypeId }!!
        person.tianfuList = this.tianfu.mapNotNull { CultivationHelper.mConfig.tianFuType.find { t -> t.id == it } }.toMutableList()
        person.equipmentList = this.equipment.map {
           it.toEquipment()
        }.toMutableList()
        if (person.specIdentity > 0){
            person.equipmentList.addAll(CultivationHelper.getSpecPersonEquipment(person))
        }
        person.followerList = this.follower.map {
           it.toFollower()
        }.toMutableList()
        person.careerList = this.career.map {
           it.toCareer()
        }.toMutableList()
        return person
    }

}


class Person: PersonBak() {
    lateinit var lingGenDetail: LingGen
    var tianfuList: MutableList<TianFu> = mutableListOf()
    var equipmentList: MutableList<Equipment> = Collections.synchronizedList(mutableListOf())
    var followerList: MutableList<Follower> = Collections.synchronizedList(mutableListOf())
    var careerList: MutableList<Career> = Collections.synchronizedList(mutableListOf())

    var HP: Int = 100
    var maxHP: Int = 100
    var attack: Int = 20
    var defence: Int = 20
    var speed: Int = 20
    var nationPost: Int = 0
    var nationId = ""

    var extraProperty: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0)//update once deps tian linggen
    var extraXiuwei: Int = 0 //tianfu 初始和读取更新
    var extraTupo: Int = 0 //tianfu 初始和读取更新
    var extraSpeed: Int = 0 //tianfu 初始和读取更新
    var extraXuiweiMulti: Int = 0 //tianfu + alliance  初始和读取更新

    var equipmentXiuwei: Int = 0 //
    var equipmentSuccess: Int = 0 //
    var equipmentProperty: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0)//

    var allianceXiuwei: Int = 0 //alliance 增益 zhu / speed； 每轮更新
    var allianceSuccess: Int = 0 //alliance 增益 初始和读取更新
    var allianceProperty: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0)//alliance 初始和读取更新
    var allianceName: String = "" // alliance 初始和读取更新

    var jinJieName = "" //  updated by xun
    var jinJieColor = 0 //  updated by xun
    var jinJieMax: Int = 0 // updated by xun

    //不需要保存
    var type = 0// 标注boss用 boss > 0
    var remainHit = 0// 标注boss attack round
    var bossXiuwei: Int = 0// 每次读取 && update depend boss battle
    var bossRound: MutableList<Int> = mutableListOf()
    var clanXiuwei: Int = 0// 每次读取 && update depend clan battle
    var nationXiuwei: Int = 0//每次读取 &&  update depend nation battle
    var battlexiuwei: Int = 0 //每次读取和single battle后更新
    var battleWinner: Int = 0 //每次读取和single battle后更新


    fun toPersonBak(): PersonBak {
        val bak = this as PersonBak
        bak.tianfu = this.tianfuList.map { it.id }.toMutableList()
        bak.equipment = this.equipmentList.filter { it.detail.type != 8 }.map { it.toBak() }.toMutableList()
        bak.follower = this.followerList.map { it.toBak() }.toMutableList()
        bak.career = this.careerList.map { it.toBak() }.toMutableList()
        return bak
    }

}