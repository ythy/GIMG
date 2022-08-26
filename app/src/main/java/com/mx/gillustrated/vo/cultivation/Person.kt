package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.util.NameUtil
import java.util.*

class Person() :Parcelable {
        var id:String = ""
        var name: String = ""
        var lastName: String = ""
        var gender: NameUtil.Gender = NameUtil.Gender.Default
        lateinit var lingGenType: LingGen
        var lingGenId:String = "" //Tian spec
        var lingGenName:String = ""
        var birthDay:MutableList<Pair<Long, Long>> =  Collections.synchronizedList(mutableListOf())
        var lifetime:Long = 100
        var events:MutableList<PersonEvent> = Collections.synchronizedList(mutableListOf())
        var tianfus:MutableList<TianFu> = mutableListOf()
        var isFav:Boolean = false
        var profile:Int = 0
        var partner:String? = null //赋值一次
        var partnerName:String? = null //赋值一次
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

        var equipmentList:MutableList<Triple<String, Int, String>> =  Collections.synchronizedList(mutableListOf())
        var equipmentXiuwei:Int = 0 //
        var equipmentSuccess:Int = 0 //
        var equipmentProperty:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)//
        var teji:MutableList<String> =  Collections.synchronizedList(mutableListOf())
        var followerList:MutableList<Triple<String, String, String>> =  Collections.synchronizedList(mutableListOf())

        var jingJieId:String = ""
        var jingJieSuccess:Int = 0
        var xiuXei:Int = 0
        var maxXiuWei:Long = 0
        var allianceId:String = ""
        var allianceXiuwei:Int = 0 //alliance 增益 zhu / speed； 每轮更新
        var allianceSuccess:Int = 0 //alliance 增益 初始和读取更新
        var allianceProperty:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)//alliance 初始和读取更新


        //extra props
        var lastBirthDay:Long = 0
        var lastTotalXun:Long = 0
        var ancestorLevel:Int = 0 // 0 初代
        var ancestorId:String? = null// 一次
        var age:Long = 0 // now -  birthDay
        var jinJieName = "" //  updated by xun
        var jinJieColor = 0 //  updated by xun
        var jinJieMax:Int = 0 // updated by xun
        var extraXiuwei:Int = 0 //tianfu
        var extraTupo:Int = 0 //tianfu
        var extraSpeed:Int = 0 //tianfu
        var extraXuiweiMulti:Int = 0 //tianfu + alliance  初始和读取更新
        var allianceName:String = "" // alliance
        var type = 0// 标注boss用 boss > 1

        constructor(parcel: Parcel) : this() {
                id = parcel.readString()!!
                name = parcel.readString()!!
                lastName = parcel.readString()!!
                gender = NameUtil.Gender.valueOf(parcel.readString()!!)
                lingGenType = parcel.readParcelable(LingGen::class.java.classLoader)!!
                lingGenId = parcel.readString()!!
                lingGenName = parcel.readString()!!
                birthDay = mutableListOf<Pair<Long, Long>>().apply {
                    parcel.readList(this, Pair::class.java.classLoader)
                }
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

                equipmentList = mutableListOf<Triple<String, Int, String>>().apply {
                        parcel.readList(this, Triple::class.java.classLoader)
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
                allianceId = parcel.readString()
                allianceXiuwei = parcel.readInt()
                allianceSuccess = parcel.readInt()
                allianceProperty= parcel.createIntArray().toMutableList()

                lastBirthDay = parcel.readLong()
                ancestorLevel = parcel.readInt()
                ancestorId = parcel.readString()
                age = parcel.readLong()
                jinJieName = parcel.readString()
                jinJieColor = parcel.readInt()
                jinJieMax = parcel.readInt()
                extraXiuwei = parcel.readInt()
                extraTupo = parcel.readInt()
                extraSpeed = parcel.readInt()
                extraXuiweiMulti = parcel.readInt()
                allianceName = parcel.readString()
                type = parcel.readInt()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(id)
                parcel.writeString(name)
                parcel.writeString(lastName)
                parcel.writeString(gender.props)
                parcel.writeParcelable(lingGenType, flags)
                parcel.writeString(lingGenId)
                parcel.writeString(lingGenName)
                parcel.writeList(birthDay)
                parcel.writeLong(lifetime)
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

                parcel.writeList(equipmentList)
                parcel.writeInt(equipmentXiuwei)
                parcel.writeInt(equipmentSuccess)
                parcel.writeIntArray(equipmentProperty.toIntArray())
                parcel.writeStringList(teji)
                parcel.writeList(followerList)

                parcel.writeString(jingJieId)
                parcel.writeInt(jingJieSuccess)
                parcel.writeInt(xiuXei)
                parcel.writeLong(maxXiuWei)
                parcel.writeString(allianceId)
                parcel.writeInt(allianceXiuwei)
                parcel.writeInt(allianceSuccess)
                parcel.writeIntArray(allianceProperty.toIntArray())

                parcel.writeLong(lastBirthDay)
                parcel.writeInt(ancestorLevel)
                parcel.writeString(ancestorId)
                parcel.writeLong(age)
                parcel.writeString(jinJieName)
                parcel.writeInt(jinJieColor)
                parcel.writeInt(jinJieMax)
                parcel.writeInt(extraXiuwei)
                parcel.writeInt(extraTupo)
                parcel.writeInt(extraSpeed)
                parcel.writeInt(extraXuiweiMulti)
                parcel.writeString(allianceName)
                parcel.writeInt(type)
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