package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable

class Equipment() :Parcelable {
    lateinit var id:String
    lateinit var name:String
    var type:Int = 0 // 0 Bao; 1 Wu; 2 Jia, 10 huizhang
    var rarity:Int = 0//5 30, 6 40，7 50，8 ~
    var xiuwei:Int = 0
    var success:Int = 0
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        name = parcel.readString()!!
        type = parcel.readInt()
        rarity = parcel.readInt()
        xiuwei = parcel.readInt()
        success = parcel.readInt()
        property = parcel.createIntArray().toMutableList()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(type)
        parcel.writeInt(rarity)
        parcel.writeInt(xiuwei)
        parcel.writeInt(success)
        parcel.writeIntArray(property.toIntArray())
    }

    override fun describeContents(): Int {
        return 0
    }


    override fun toString(): String {
        return "$rarity$name:($xiuwei/$success)(${property.take(4).joinToString()})"
    }

    companion object CREATOR : Parcelable.Creator<Equipment> {
        override fun createFromParcel(parcel: Parcel): Equipment {
            return Equipment(parcel)
        }

        override fun newArray(size: Int): Array<Equipment?> {
            return arrayOfNulls(size)
        }
    }

}