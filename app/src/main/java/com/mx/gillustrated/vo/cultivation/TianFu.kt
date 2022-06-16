package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable

/*
    type 1: Base Xiuwei
    type 2: Multi Xiuwei
    type 3: life
    type 4: Tupo
 */
class TianFu() :Parcelable {
    lateinit var id:String
    lateinit var name:String
    var type:Int = 0
    var bonus:Int = 0
    var weight:Int = 0
    var rarity:Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        name = parcel.readString()!!
        type = parcel.readInt()
        bonus = parcel.readInt()
        weight = parcel.readInt()
        rarity = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(type)
        parcel.writeInt(bonus)
        parcel.writeInt(weight)
        parcel.writeInt(rarity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TianFu> {
        override fun createFromParcel(parcel: Parcel): TianFu {
            return TianFu(parcel)
        }

        override fun newArray(size: Int): Array<TianFu?> {
            return arrayOfNulls(size)
        }
    }
}