package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable

class Enemy() :Parcelable {
    lateinit var id:String
    lateinit var name:String
    var type:Int = 0
    var HP:Int = 0
    var maxHP:Int = 0
    var attack:Int = 0
    var defence:Int = 0
    var speed:Int = 0
    var attackFrequency:Int = 12 //xun
    var birthDay:Long = 0 //xun
    var lifetime:Long = 0 //xun
    var isDead:Boolean = false

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        name = parcel.readString()!!
        type = parcel.readInt()
        HP = parcel.readInt()
        maxHP = parcel.readInt()
        attack = parcel.readInt()
        defence = parcel.readInt()
        speed = parcel.readInt()
        attackFrequency = parcel.readInt()
        birthDay = parcel.readLong()
        lifetime = parcel.readLong()
        isDead = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(type)
        parcel.writeInt(HP)
        parcel.writeInt(maxHP)
        parcel.writeInt(attack)
        parcel.writeInt(defence)
        parcel.writeInt(speed)
        parcel.writeInt(attackFrequency)
        parcel.writeLong(birthDay)
        parcel.writeLong(lifetime)
        parcel.writeByte(if (isDead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Enemy> {
        override fun createFromParcel(parcel: Parcel): Enemy {
            return Enemy(parcel)
        }

        override fun newArray(size: Int): Array<Enemy?> {
            return arrayOfNulls(size)
        }
    }
}