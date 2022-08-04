package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper

class TeJi() : Parcelable {
    lateinit var id:String
    lateinit var name:String
    var description:String = ""
    var rarity:Int = 0
    var weight:Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        rarity = parcel.readInt()
        weight = parcel.readInt()
    }

    override fun toString(): String {
        return CultivationHelper.showing("$name-$rarity")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(rarity)
        parcel.writeInt(weight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TeJi> {
        override fun createFromParcel(parcel: Parcel): TeJi {
            return TeJi(parcel)
        }

        override fun newArray(size: Int): Array<TeJi?> {
            return arrayOfNulls(size)
        }
    }
}