package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper

open class CareerConfig():Parcelable {
    lateinit var id:String
    lateinit var name:String
    var rarity:Int = 0
    var weight:Int = 0
    var maxLevel:Int = 100
    var upgradeBasicXiuwei:Long = 0L

    fun toCareer():Career{
        val career = Career()
        career.id = this.id
        career.name = this.name
        career.rarity = this.rarity
        career.weight = this.weight
        career.maxLevel = this.maxLevel
        career.upgradeBasicXiuwei = this.upgradeBasicXiuwei
        return career
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        rarity = parcel.readInt()
        weight = parcel.readInt()
        maxLevel = parcel.readInt()
        upgradeBasicXiuwei = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
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

class Career() : CareerConfig(), Parcelable {

    var level:Int = 0

    constructor(parcel: Parcel) : this() {
        level = parcel.readInt()
    }

    override fun toString(): String {
        return if(level >= maxLevel)
            CultivationHelper.showing(name)
        else
            "${CultivationHelper.showing(name)}$level"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(level)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Career> {
        override fun createFromParcel(parcel: Parcel): Career {
            return Career(parcel)
        }

        override fun newArray(size: Int): Array<Career?> {
            return arrayOfNulls(size)
        }
    }
}