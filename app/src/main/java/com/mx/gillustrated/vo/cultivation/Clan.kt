package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class ClanBak() :Parcelable {
    lateinit var id:String // person.ancestorId
    lateinit var name:String
    var createDate:Long = 0//xun
    var persons: List<String> = Collections.synchronizedList(mutableListOf())
    var battleRecord:MutableMap<Int, Int> = mutableMapOf()
    var xiuweiBattle:Int = 0
    var battleWinner:Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        createDate = parcel.readLong()
        persons = parcel.createStringArrayList()
        parcel.readMap(battleRecord, Map::class.java.classLoader)
        xiuweiBattle = parcel.readInt()
        battleWinner = parcel.readInt()
    }

    fun toClan(personMap: ConcurrentHashMap<String, Person>):Clan{
        val clan = Clan()
        clan.id = this.id
        clan.name = this.name
        clan.zhu = personMap[this.id]
        clan.createDate = this.createDate
        clan.clanPersonList.putAll(personMap.filterKeys { this.persons.contains(it) })
        clan.battleRecord = ConcurrentHashMap(this.battleRecord)
        return clan
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeLong(createDate)
        parcel.writeStringList(persons)
        parcel.writeMap(battleRecord)
        parcel.writeInt(battleWinner)
        parcel.writeInt(xiuweiBattle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ClanBak> {
        override fun createFromParcel(parcel: Parcel): ClanBak {
            return ClanBak(parcel)
        }

        override fun newArray(size: Int): Array<ClanBak?> {
            return arrayOfNulls(size)
        }
    }
}

class Clan() : ClanBak(), Parcelable{
    var totalXiuwei:Long = 0// extra props
    var clanPersonList: ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var zhu:Person? = null

    constructor(parcel: Parcel) : this() {
        totalXiuwei = parcel.readLong()
        clanPersonList =  parcel.readValue(ConcurrentHashMap::class.java.classLoader) as ConcurrentHashMap<String, Person>
    }

    fun toClanBak():ClanBak{
        val bak = ClanBak()
        bak.id = super.id
        bak.name = super.name
        bak.createDate = super.createDate
        bak.persons = this.clanPersonList.filter { it.value.ancestorId == super.id }.map { it.key }
        bak.battleRecord = super.battleRecord
        return bak
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeLong(totalXiuwei)
        parcel.writeValue(clanPersonList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Clan> {
        override fun createFromParcel(parcel: Parcel): Clan {
            return Clan(parcel)
        }

        override fun newArray(size: Int): Array<Clan?> {
            return arrayOfNulls(size)
        }
    }
}