package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable

data class LingGen constructor(var id:String, var name:String, var randomBasic:Int, var qiBasic:Int, var color:Int, var jinBonus:List<Int>):Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        listOf<Int>().apply {
            parcel.readList(this, Int::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(randomBasic)
        parcel.writeInt(qiBasic)
        parcel.writeInt(color)
        parcel.writeList(jinBonus)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LingGen> {
        override fun createFromParcel(parcel: Parcel): LingGen {
            return LingGen(parcel)
        }

        override fun newArray(size: Int): Array<LingGen?> {
            return arrayOfNulls(size)
        }
    }
}