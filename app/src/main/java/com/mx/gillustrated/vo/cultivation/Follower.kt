package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.util.NameUtil
import java.util.*

class Follower() : Parcelable {
    lateinit var id: String
    lateinit var name: String
    var nid:String =  UUID.randomUUID().toString()
    var rarity: Int = 0
    var property =  mutableListOf(0,0,0,0,0,0,0,0)
    var teji = mutableListOf<String>()
    var gender = NameUtil.Gender.Male
    var unique:Boolean = false
    var commission: Int = 0
    var type:Int = 0 // 0 normal, 1 spec can not add manually
    //以下字段不在配置里
    var uniqueName: String = "" //unique为true时为空
    var isDead:Boolean = false

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        nid = parcel.readString()
        rarity = parcel.readInt()
        unique = parcel.readByte() != 0.toByte()
        commission = parcel.readInt()
        type = parcel.readInt()
        uniqueName = parcel.readString()
        isDead = parcel.readByte() != 0.toByte()
    }

    override fun toString(): String {
        return CultivationHelper.showing("$name(${property.take(4).joinToString()})")
    }

    fun copy():Follower{
        val follower = Follower()
        follower.id = this.id
        follower.name = this.name
        follower.nid = this.nid
        follower.rarity = this.rarity
        follower.property = this.property
        follower.teji = this.teji
        follower.gender = this.gender
        follower.unique = this.unique
        follower.commission = this.commission
        follower.type = this.type
        follower.uniqueName = this.uniqueName
        follower.isDead = this.isDead
        return follower
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(nid)
        parcel.writeInt(rarity)
        parcel.writeByte(if (unique) 1 else 0)
        parcel.writeInt(commission)
        parcel.writeInt(type)
        parcel.writeString(uniqueName)
        parcel.writeByte(if (isDead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Follower> {
        override fun createFromParcel(parcel: Parcel): Follower {
            return Follower(parcel)
        }

        override fun newArray(size: Int): Array<Follower?> {
            return arrayOfNulls(size)
        }
    }

}