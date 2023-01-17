package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper

open class CareerBak():Parcelable {
    lateinit var id:String
    var level:Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        level = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(level)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CareerBak> {
        override fun createFromParcel(parcel: Parcel): CareerBak {
            return CareerBak(parcel)
        }

        override fun newArray(size: Int): Array<CareerBak?> {
            return arrayOfNulls(size)
        }
    }
}


open class CareerConfig(): CareerBak(), Parcelable {
    lateinit var name:String
    var rarity:Int = 0
    var weight:Int = 0
    var maxLevel:Int = 100
    var upgradeBasicXiuwei:Long = 0L

    fun toCareer(levelParams:Int = 0):Career{
        val career = Career()
        career.id = this.id
        career.level = levelParams
        career.name = this.name
        career.rarity = this.rarity
        career.weight = this.weight
        career.maxLevel = this.maxLevel
        career.upgradeBasicXiuwei = this.upgradeBasicXiuwei
        return career
    }

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        rarity = parcel.readInt()
        weight = parcel.readInt()
        maxLevel = parcel.readInt()
        upgradeBasicXiuwei = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(rarity)
        parcel.writeInt(weight)
        parcel.writeInt(maxLevel)
        parcel.writeLong(upgradeBasicXiuwei)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CareerConfig> {
        override fun createFromParcel(parcel: Parcel): CareerConfig {
            return CareerConfig(parcel)
        }

        override fun newArray(size: Int): Array<CareerConfig?> {
            return arrayOfNulls(size)
        }
    }

}

class Career: CareerConfig() {

    override fun toString(): String {
        return if(level >= maxLevel)
            CultivationHelper.showing(name)
        else
            "${CultivationHelper.showing(name)}$level"
    }

    fun toBak():CareerBak{
        val bak = CareerBak()
        bak.id = this.id
        bak.level = this.level
        return  bak
    }


    companion object CREATOR{
        fun make(ids: String, level: Int = 0): Career {
            return CultivationHelper.mConfig.career.find { it.id == ids }!!.toCareer(level)
        }
    }
}