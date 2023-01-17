package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.util.NameUtil
import java.util.*

open class PersonBak() : Parcelable {
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
            CultivationHelper.mConfig.equipment.find { e -> e.id == it.id }!!.toEquipment(it.seq)
        }.toMutableList()
        person.followerList = this.follower.map {
            CultivationHelper.mConfig.follower.find { e -> e.id == it.id }!!.toFollower(it.uniqueName)
        }.toMutableList()
        person.careerList = this.career.map {
            CultivationHelper.mConfig.career.find { e -> e.id == it.id }!!.toCareer(it.level)
        }.toMutableList()
        return person
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        lingGenTypeId = parcel.readString()!!
        lingGenSpecId = parcel.readString()!!
        lingGenName = parcel.readString()!!
        name = parcel.readString()!!
        lastName = parcel.readString()!!
        fullName = parcel.readString()!!
        gender = NameUtil.Gender.valueOf(parcel.readString()!!)
        birthtime = parcel.readLong()
        lifetime = parcel.readLong()
        events = Collections.synchronizedList(parcel.createTypedArrayList(PersonEvent))
        tianfu = Collections.synchronizedList(parcel.createStringArrayList())

        isFav = parcel.readByte() != 0.toByte()
        profile = parcel.readInt()
        partner = parcel.readString()
        partnerName = parcel.readString()
        parent = parcel.readValue(Pair::class.java.classLoader) as Pair<String, String>?
        parentName = parcel.readValue(Pair::class.java.classLoader) as Pair<String, String>?
        children = Collections.synchronizedList(parcel.createStringArrayList())
        lifeTurn = parcel.readInt()
        deadExceptTimes = parcel.readInt()
        singled = parcel.readByte() != 0.toByte()
        dink = parcel.readByte() != 0.toByte()
        teji = Collections.synchronizedList(parcel.createStringArrayList())
        jingJieId = parcel.readString()
        jingJieSuccess = parcel.readInt()
        xiuXei = parcel.readInt()
        maxXiuWei = parcel.readLong()
        pointXiuWei = parcel.readLong()
        allianceId = parcel.readString()
        parcel.readMap(battleRecord, Map::class.java.classLoader)
        ancestorLevel = parcel.readInt()
        ancestorId = parcel.readString()
        ancestorOrignId = parcel.readString()
        specIdentity = parcel.readInt()
        specIdentityTurn = parcel.readInt()
        neverDead = parcel.readByte() != 0.toByte()
        label = Collections.synchronizedList(parcel.createStringArrayList())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(lastName)
        parcel.writeString(fullName)
        parcel.writeString(lingGenTypeId)
        parcel.writeString(lingGenSpecId)
        parcel.writeString(lingGenName)
        parcel.writeString(gender.props)

        parcel.writeLong(lifetime)
        parcel.writeLong(birthtime)
        parcel.writeTypedList(events)
        parcel.writeStringList(tianfu)
        parcel.writeByte(if (isFav) 1 else 0)
        parcel.writeInt(profile)
        parcel.writeString(partner)
        parcel.writeString(partnerName)

        parcel.writeValue(parent)
        parcel.writeValue(parentName)
        parcel.writeStringList(children)
        parcel.writeInt(lifeTurn)
        parcel.writeInt(deadExceptTimes)
        parcel.writeByte(if (singled) 1 else 0)
        parcel.writeByte(if (dink) 1 else 0)
        parcel.writeStringList(teji)
        parcel.writeString(jingJieId)
        parcel.writeInt(jingJieSuccess)
        parcel.writeInt(xiuXei)
        parcel.writeLong(maxXiuWei)
        parcel.writeLong(pointXiuWei)
        parcel.writeString(allianceId)
        parcel.writeMap(battleRecord)
        parcel.writeInt(ancestorLevel)
        parcel.writeString(ancestorId)
        parcel.writeString(ancestorOrignId)
        parcel.writeInt(specIdentity)
        parcel.writeInt(specIdentityTurn)
        parcel.writeByte(if (neverDead) 1 else 0)
        parcel.writeStringList(label)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PersonBak> {
        override fun createFromParcel(parcel: Parcel): PersonBak {
            return PersonBak(parcel)
        }

        override fun newArray(size: Int): Array<PersonBak?> {
            return arrayOfNulls(size)
        }
    }

}


class Person() : PersonBak(), Parcelable {
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
        bak.equipment = this.equipmentList.map { it.toBak() }.toMutableList()
        bak.follower = this.followerList.map { it.toBak() }.toMutableList()
        bak.career = this.careerList.map { it.toBak() }.toMutableList()
        return bak
    }

    constructor(parcel: Parcel) : this() {
        lingGenDetail = parcel.readParcelable(LingGen::class.java.classLoader)!!
        tianfuList = Collections.synchronizedList(parcel.createTypedArrayList(TianFu))

        HP = parcel.readInt()
        maxHP = parcel.readInt()

        extraProperty = parcel.createIntArray().toMutableList()
        equipmentXiuwei = parcel.readInt()
        equipmentSuccess = parcel.readInt()
        equipmentProperty = parcel.createIntArray().toMutableList()

        allianceXiuwei = parcel.readInt()
        allianceSuccess = parcel.readInt()
        allianceProperty = parcel.createIntArray().toMutableList()
        battlexiuwei = parcel.readInt()
        battleWinner = parcel.readInt()

        jinJieName = parcel.readString()
        jinJieColor = parcel.readInt()
        jinJieMax = parcel.readInt()
        extraXiuwei = parcel.readInt()
        extraTupo = parcel.readInt()
        extraSpeed = parcel.readInt()
        extraXuiweiMulti = parcel.readInt()
        allianceName = parcel.readString()

        type = parcel.readInt()
        remainHit = parcel.readInt()
        nationId = parcel.readString()
        nationPost = parcel.readInt()
        clanXiuwei = parcel.readInt()
        nationXiuwei = parcel.readInt()

        bossXiuwei = parcel.readInt()
        bossRound = parcel.createIntArray().toMutableList()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(lingGenDetail, flags)
        parcel.writeTypedList(tianfuList)
        parcel.writeInt(HP)
        parcel.writeInt(maxHP)
        parcel.writeIntArray(extraProperty.toIntArray())
        parcel.writeInt(equipmentXiuwei)
        parcel.writeInt(equipmentSuccess)
        parcel.writeIntArray(equipmentProperty.toIntArray())

        parcel.writeInt(allianceXiuwei)
        parcel.writeInt(allianceSuccess)
        parcel.writeIntArray(allianceProperty.toIntArray())
        parcel.writeInt(battleWinner)
        parcel.writeInt(battlexiuwei)

        parcel.writeString(jinJieName)
        parcel.writeInt(jinJieColor)
        parcel.writeInt(jinJieMax)
        parcel.writeInt(extraXiuwei)
        parcel.writeInt(extraTupo)
        parcel.writeInt(extraSpeed)
        parcel.writeInt(extraXuiweiMulti)
        parcel.writeString(allianceName)
        parcel.writeInt(type)
        parcel.writeInt(remainHit)
        parcel.writeString(nationId)
        parcel.writeInt(nationPost)
        parcel.writeInt(clanXiuwei)
        parcel.writeInt(nationXiuwei)

        parcel.writeTypedList(careerList)
        parcel.writeInt(bossXiuwei)
        parcel.writeIntArray(bossRound.toIntArray())

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Person> {
        override fun createFromParcel(parcel: Parcel): Person {
            return Person(parcel)
        }

        override fun newArray(size: Int): Array<Person?> {
            return arrayOfNulls(size)
        }
    }

}