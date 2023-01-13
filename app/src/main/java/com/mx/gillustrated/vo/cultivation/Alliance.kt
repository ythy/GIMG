package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import java.util.concurrent.ConcurrentHashMap

open class AllianceBak():Parcelable{
    var persons:List<String> = mutableListOf()
    var battleRecord:MutableMap<Int, Int> = mutableMapOf()

    constructor(parcel: Parcel) : this() {
        persons = parcel.createStringArrayList()
        parcel.readMap(battleRecord, Map::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(persons)
        parcel.writeMap(battleRecord)
    }

    fun toAlliance(personMap: ConcurrentHashMap<String, Person>, allianceConfig: AllianceConfig):Alliance{
        val alliance = Alliance()
        alliance.id = allianceConfig.id
        alliance.name = allianceConfig.name
        alliance.type = allianceConfig.type
        alliance.level = allianceConfig.level
        alliance.maxPerson = allianceConfig.maxPerson
        alliance.lifetime = allianceConfig.lifetime
        alliance.xiuwei = allianceConfig.xiuwei
        alliance.xiuweiMulti = allianceConfig.xiuweiMulti
        alliance.lingGen = allianceConfig.lingGen
        alliance.success = allianceConfig.success
        alliance.tianfu = allianceConfig.tianfu
        alliance.property = allianceConfig.property.toMutableList()
        alliance.nation = allianceConfig.nation

        alliance.battleRecord = ConcurrentHashMap(this.battleRecord)
        alliance.personList.putAll(personMap.filterKeys { this.persons.contains(it) })
        return alliance
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AllianceBak> {
        override fun createFromParcel(parcel: Parcel): AllianceBak {
            return AllianceBak(parcel)
        }

        override fun newArray(size: Int): Array<AllianceBak?> {
            return arrayOfNulls(size)
        }
    }
}


open class AllianceConfig() : AllianceBak(), Parcelable {
    lateinit var id:String
    lateinit var name:String
    var type:Int = 0//类型，0 all, 1 spec
    var level:Int = 1// 权重100的约分 默认1
    var maxPerson:Int = 0//最大人数
    var lifetime:Int = 0
    var xiuwei:Int = 0
    var xiuweiMulti:Int = 0
    var lingGen:String? = null
    var tianfu:Int = 0 //要求的tianfu number
    var success:Int = 0//突破率
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
    var nation:String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        type = parcel.readInt()
        level = parcel.readInt()
        maxPerson = parcel.readInt()
        lifetime = parcel.readInt()
        xiuwei = parcel.readInt()
        xiuweiMulti = parcel.readInt()
        lingGen = parcel.readString()
        tianfu = parcel.readInt()
        success = parcel.readInt()
        property = parcel.createIntArray().toMutableList()
        nation = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(type)
        parcel.writeInt(level)
        parcel.writeInt(maxPerson)
        parcel.writeInt(lifetime)
        parcel.writeInt(xiuwei)
        parcel.writeInt(xiuweiMulti)
        parcel.writeString(lingGen)
        parcel.writeInt(tianfu)
        parcel.writeInt(success)
        parcel.writeIntArray(property.toIntArray())
        parcel.writeString(nation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AllianceConfig> {
        override fun createFromParcel(parcel: Parcel): AllianceConfig {
            return AllianceConfig(parcel)
        }

        override fun newArray(size: Int): Array<AllianceConfig?> {
            return arrayOfNulls(size)
        }
    }
}

class Alliance() : AllianceConfig(), Parcelable {

    var zhuPerson:Person? = null
    var personList:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var totalXiuwei:Long = 0 // extra props
    var xiuweiBattle:Int = 0 // extra props
    var battleWinner:Int = 0 // extra props

    constructor(parcel: Parcel) : this() {
        zhuPerson = parcel.readParcelable(Person::class.java.classLoader)
        personList = parcel.readValue(ConcurrentHashMap::class.java.classLoader) as ConcurrentHashMap<String, Person>
        totalXiuwei = parcel.readLong()
        xiuweiBattle = parcel.readInt()
        battleWinner = parcel.readInt()
    }

    fun toBak():AllianceBak{
        val bak = AllianceBak()
        bak.battleRecord = super.battleRecord
        bak.persons = this.personList.filter { it.value.allianceId == super.id && it.value.type == 0}.map { it.key }
        return bak
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeParcelable(zhuPerson, flags)
        parcel.writeValue(personList)
        parcel.writeLong(totalXiuwei)
        parcel.writeInt(battleWinner)
        parcel.writeInt(xiuweiBattle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Alliance> {
        override fun createFromParcel(parcel: Parcel): Alliance {
            return Alliance(parcel)
        }

        override fun newArray(size: Int): Array<Alliance?> {
            return arrayOfNulls(size)
        }
    }


}