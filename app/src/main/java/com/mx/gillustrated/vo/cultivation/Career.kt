package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper

class Career() :Parcelable {
    lateinit var id:String
    lateinit var name:String
    var rarity:Int = 0
    var weight:Int = 0
    var maxLevel:Int = 100
    var upgradeBasicXiuwei:Long = 0L
    // 以下字段不在配置里
    var level:Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        rarity = parcel.readInt()
        weight = parcel.readInt()
        maxLevel = parcel.readInt()
        upgradeBasicXiuwei = parcel.readLong()
        level = parcel.readInt()
    }


    fun copy():Career{
        val career = Career()
        career.id = this.id
        career.name = this.name
        career.rarity = this.rarity
        career.weight = this.weight
        career.maxLevel = this.maxLevel
        career.upgradeBasicXiuwei = this.upgradeBasicXiuwei
        career.level = this.level
        return career
    }

    override fun toString(): String {
        return if(level >= maxLevel)
            CultivationHelper.showing(name)
        else
            "${CultivationHelper.showing(name)}$level"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(rarity)
        parcel.writeInt(weight)
        parcel.writeInt(maxLevel)
        parcel.writeLong(upgradeBasicXiuwei)
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