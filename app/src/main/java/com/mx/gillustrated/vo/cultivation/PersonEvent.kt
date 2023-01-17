package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable

class PersonEvent() :Parcelable {
    var happenTime:Long = 0
    lateinit var content:String

    constructor(parcel: Parcel) : this() {
        happenTime = parcel.readLong()
        content = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(happenTime)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PersonEvent> {
        override fun createFromParcel(parcel: Parcel): PersonEvent {
            return PersonEvent(parcel)
        }

        override fun newArray(size: Int): Array<PersonEvent?> {
            return arrayOfNulls(size)
        }
    }
}