package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import java.util.concurrent.ConcurrentHashMap

//zhu 增益 20
open class AllianceConfig() :Parcelable {
    lateinit var id:String
    lateinit var name:String
    var level:Int = 1// 权重100的约分 默认1
    var maxPerson:Int = 0//最大人数
    var lifetime:Int = 0
    var xiuwei:Int = 0
    var xiuweiMulti:Int = 0
    var lingGen:String? = null
    var tianfu:Int = 0 //要求的tianfu number
    var success:Int = 0//突破率
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
    var speedG1:Int = 0
    var speedG2:Int = 0
    var persons:List<String> = mutableListOf()

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        level = parcel.readInt()
        maxPerson = parcel.readInt()
        lifetime = parcel.readInt()
        xiuwei = parcel.readInt()
        xiuweiMulti = parcel.readInt()
        lingGen = parcel.readString()
        tianfu = parcel.readInt()
        success = parcel.readInt()
        property = parcel.createIntArray().toMutableList()
        speedG1 = parcel.readInt()
        speedG2 = parcel.readInt()
        persons = parcel.createStringArrayList()
    }

    fun toAlliance(personMap: ConcurrentHashMap<String, Person>):Alliance{
        val alliance = Alliance()
        alliance.id = this.id
        alliance.name = this.name
        alliance.lingGen = this.lingGen
        alliance.personList.putAll(personMap.filterKeys { this.persons.contains(it) })
        return alliance
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(level)
        parcel.writeInt(maxPerson)
        parcel.writeInt(lifetime)
        parcel.writeInt(xiuwei)
        parcel.writeInt(xiuweiMulti)
        parcel.writeString(lingGen)
        parcel.writeInt(tianfu)
        parcel.writeInt(success)
        parcel.writeIntArray(property.toIntArray())
        parcel.writeInt(speedG1)
        parcel.writeInt(speedG2)
        parcel.writeStringList(persons)
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
    var huPersons:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var personList:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var speedG1PersonList:ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var speedG2PersonList:ConcurrentHashMap<String, Person> =ConcurrentHashMap()

    var totalXiuwei:Long = 0// extra props
    var isPinyinMode:Boolean = false// extra props

    constructor(parcel: Parcel) : this() {
        zhuPerson = parcel.readParcelable(Person::class.java.classLoader)
        huPersons = parcel.readValue(ConcurrentHashMap::class.java.classLoader) as ConcurrentHashMap<String, Person>
        personList = parcel.readValue(ConcurrentHashMap::class.java.classLoader) as ConcurrentHashMap<String, Person>
        speedG1PersonList = parcel.readValue(ConcurrentHashMap::class.java.classLoader) as ConcurrentHashMap<String, Person>
        speedG2PersonList = parcel.readValue(ConcurrentHashMap::class.java.classLoader) as ConcurrentHashMap<String, Person>
        totalXiuwei = parcel.readLong()
        isPinyinMode = parcel.readByte() != 0.toByte()
    }

    fun toConfig():AllianceConfig{
        val config = AllianceConfig()
        config.id = super.id
        config.name = super.name
        config.lingGen = super.lingGen
        config.persons = this.personList.filter { it.value.allianceId == super.id }.map { it.key }
        return config
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeParcelable(zhuPerson, flags)
        parcel.writeValue(huPersons)
        parcel.writeValue(personList)
        parcel.writeValue(speedG1PersonList)
        parcel.writeValue(speedG2PersonList)
        parcel.writeLong(totalXiuwei)
        parcel.writeByte(if (isPinyinMode) 1 else 0)
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