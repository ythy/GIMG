package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.util.NameUtil
import java.util.*

class Person() :Parcelable {
        var id:String = ""
        var name: String = ""
        var lastName: String = ""
        var fullName: String = ""
        var gender: NameUtil.Gender = NameUtil.Gender.Default
        lateinit var lingGenType: LingGen
        var lingGenId:String = "" //Tian spec
        var lingGenName:String = ""
        var birthtime:Long = 0
        var lifetime:Long = 0 // if  lifetime < currentXun  , go dead
        var events:MutableList<PersonEvent> = Collections.synchronizedList(mutableListOf())
        var tianfus:MutableList<TianFu> = mutableListOf()
        var isFav:Boolean = false
        var profile:Int = 0
        var partner:String? = null
        var partnerName:String? = null
        var parent:Pair<String, String>? = null//唯一
        var parentName:Pair<String, String>? = null//赋值一次，显示用
        var children:MutableList<String> =  Collections.synchronizedList(mutableListOf())
        var lifeTurn:Int = 0//
        var singled:Boolean = false
        var dink:Boolean = false
        var gold:Long = 0L

        var HP:Int = 100
        var maxHP:Int = 100
        var attack:Int = 20
        var defence:Int = 20
        var speed:Int = 20
        var extraProperty:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)//update once deps tian linggen

        var equipmentListPair:MutableList<Pair<String, Int>> =  Collections.synchronizedList(mutableListOf())
        var equipmentXiuwei:Int = 0 //
        var equipmentSuccess:Int = 0 //
        var equipmentProperty:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)//
        var teji:MutableList<String> =  Collections.synchronizedList(mutableListOf())
        var followerList:MutableList<Triple<String, String, String>> =  Collections.synchronizedList(mutableListOf())

        var jingJieId:String = ""
        var jingJieSuccess:Int = 0
        var xiuXei:Int = 0
        var maxXiuWei:Long = 0
        var pointXiuWei:Long = 0
        var allianceId:String = ""
        var allianceXiuwei:Int = 0 //alliance 增益 zhu / speed； 每轮更新
        var allianceSuccess:Int = 0 //alliance 增益 初始和读取更新
        var allianceProperty:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)//alliance 初始和读取更新
        var allianceName:String = "" // alliance 初始和读取更新
        var specIdentity:Int = 0 //spec person nid
        var specIdentityTurn:Int = 0 //spec person turn added while dead
        var nationPost:Int = 0
        var neverDead:Boolean = false
        var battleRecord:MutableMap<Int, Int> = mutableMapOf()

        //extra props
        var ancestorLevel:Int = 0 // 0 初代
        var ancestorId:String? = null// 一次
        var jinJieName = "" //  updated by xun
        var jinJieColor = 0 //  updated by xun
        var jinJieMax:Int = 0 // updated by xun
        var extraXiuwei:Int = 0 //tianfu 初始和读取更新
        var extraTupo:Int = 0 //tianfu 初始和读取更新
        var extraSpeed:Int = 0 //tianfu 初始和读取更新
        var extraXuiweiMulti:Int = 0 //tianfu + alliance  初始和读取更新



        //不需要保存
        var type = 0// 标注boss用 boss > 0
        var remainHit = 0// 标注boss attack round
        var nationId = "" //每次读取时赋值
        var clanXiuwei:Int = 0// 每次读取 && update depend clan battle
        var nationXiuwei:Int = 0//每次读取 &&  update depend nation battle
        var battlexiuwei:Int = 0 //每次读取和single battle后更新
        var battleWinner:Int = 0 //每次读取和single battle后更新
        var careerDetailList:MutableList<Career> =  Collections.synchronizedList(mutableListOf())

        constructor(parcel: Parcel) : this() {
                id = parcel.readString()!!
                name = parcel.readString()!!
                lastName = parcel.readString()!!
                fullName = parcel.readString()!!
                gender = NameUtil.Gender.valueOf(parcel.readString()!!)
                lingGenType = parcel.readParcelable(LingGen::class.java.classLoader)!!
                lingGenId = parcel.readString()!!
                lingGenName = parcel.readString()!!
                birthtime = parcel.readLong()
                lifetime = parcel.readLong()
                events =  Collections.synchronizedList(parcel.createTypedArrayList(PersonEvent))
                tianfus = Collections.synchronizedList(parcel.createTypedArrayList(TianFu))
                isFav = parcel.readByte() != 0.toByte()
                profile = parcel.readInt()
                partner = parcel.readString()
                partnerName = parcel.readString()
                parent = parcel.readValue(Pair::class.java.classLoader) as Pair<String, String>?
                parentName = parcel.readValue(Pair::class.java.classLoader) as Pair<String, String>?
                children = Collections.synchronizedList(parcel.createStringArrayList())
                lifeTurn = parcel.readInt()
                singled = parcel.readByte() != 0.toByte()
                dink = parcel.readByte() != 0.toByte()
                gold = parcel.readLong()

                HP = parcel.readInt()
                maxHP = parcel.readInt()
                extraProperty = parcel.createIntArray().toMutableList()

                equipmentListPair = mutableListOf<Pair<String, Int>>().apply {
                        parcel.readList(this, Pair::class.java.classLoader)
                }
                equipmentXiuwei = parcel.readInt()
                equipmentSuccess = parcel.readInt()
                equipmentProperty = parcel.createIntArray().toMutableList()
                teji = Collections.synchronizedList(parcel.createStringArrayList())
                followerList = mutableListOf<Triple<String, String, String>>().apply {
                        parcel.readList(this, Triple::class.java.classLoader)
                }

                jingJieId = parcel.readString()
                jingJieSuccess = parcel.readInt()
                xiuXei = parcel.readInt()
                maxXiuWei = parcel.readLong()
                pointXiuWei = parcel.readLong()
                allianceId = parcel.readString()
                allianceXiuwei = parcel.readInt()
                allianceSuccess = parcel.readInt()
                allianceProperty= parcel.createIntArray().toMutableList()
                parcel.readMap(battleRecord, Map::class.java.classLoader)
                battlexiuwei = parcel.readInt()
                battleWinner = parcel.readInt()

                ancestorLevel = parcel.readInt()
                ancestorId = parcel.readString()
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
                specIdentity = parcel.readInt()
                specIdentityTurn = parcel.readInt()
                nationPost = parcel.readInt()
                neverDead = parcel.readByte() != 0.toByte()
                clanXiuwei = parcel.readInt()
                nationXiuwei = parcel.readInt()

                careerDetailList =  Collections.synchronizedList(parcel.createTypedArrayList(Career))
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(id)
                parcel.writeString(name)
                parcel.writeString(lastName)
                parcel.writeString(fullName)
                parcel.writeString(gender.props)
                parcel.writeParcelable(lingGenType, flags)
                parcel.writeString(lingGenId)
                parcel.writeString(lingGenName)
                parcel.writeLong(lifetime)
                parcel.writeLong(birthtime)
                parcel.writeTypedList(events)
                parcel.writeTypedList(tianfus)
                parcel.writeByte(if (isFav) 1 else 0)
                parcel.writeInt(profile)
                parcel.writeString(partner)
                parcel.writeString(partnerName)

                parcel.writeValue(parent)
                parcel.writeValue(parentName)
                parcel.writeStringList(children)
                parcel.writeInt(lifeTurn)
                parcel.writeByte(if (singled) 1 else 0)
                parcel.writeByte(if (dink) 1 else 0)
                parcel.writeLong(gold)

                parcel.writeInt(HP)
                parcel.writeInt(maxHP)
                parcel.writeIntArray(extraProperty.toIntArray())

                parcel.writeList(equipmentListPair)
                parcel.writeInt(equipmentXiuwei)
                parcel.writeInt(equipmentSuccess)
                parcel.writeIntArray(equipmentProperty.toIntArray())
                parcel.writeStringList(teji)
                parcel.writeList(followerList)

                parcel.writeString(jingJieId)
                parcel.writeInt(jingJieSuccess)
                parcel.writeInt(xiuXei)
                parcel.writeLong(maxXiuWei)
                parcel.writeLong(pointXiuWei)
                parcel.writeString(allianceId)
                parcel.writeInt(allianceXiuwei)
                parcel.writeInt(allianceSuccess)
                parcel.writeIntArray(allianceProperty.toIntArray())
                parcel.writeMap(battleRecord)
                parcel.writeInt(battleWinner)
                parcel.writeInt(battlexiuwei)

                parcel.writeInt(ancestorLevel)
                parcel.writeString(ancestorId)
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
                parcel.writeInt(specIdentity)
                parcel.writeInt(specIdentityTurn)
                parcel.writeInt(nationPost)
                parcel.writeByte(if (neverDead) 1 else 0)
                parcel.writeInt(clanXiuwei)
                parcel.writeInt(nationXiuwei)

                parcel.writeTypedList(careerDetailList)

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