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
    var type:Int = 0
    var chance:Int = 100
    var status:String = ""
    var statusRound:Int = 0 // combining with status

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        rarity = parcel.readInt()
        weight = parcel.readInt()
        type = parcel.readInt()
        statusRound = parcel.readInt()
        status = parcel.readString()
        chance = parcel.readInt()
    }

    override fun toString(): String {
        return CultivationHelper.showing("$name-$rarity")
    }

    fun copy():TeJi{
        val teJi = TeJi()
        teJi.id = this.id
        teJi.name = this.name
        teJi.rarity = this.rarity
        teJi.description = this.description
        teJi.weight = this.weight
        teJi.type = this.type
        teJi.chance = this.chance
        teJi.status = this.status
        teJi.statusRound = this.statusRound
        return teJi
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(rarity)
        parcel.writeInt(weight)
        parcel.writeInt(type)
        parcel.writeInt(statusRound)
        parcel.writeInt(chance)
        parcel.writeString(status)
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