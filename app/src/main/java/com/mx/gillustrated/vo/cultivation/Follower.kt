package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.util.NameUtil
import java.util.*

open class FollowerBak() : Parcelable {
    lateinit var id: String
    var uniqueName: String = "" //unique为true时为空

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        uniqueName = parcel.readString()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(uniqueName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FollowerBak> {
        override fun createFromParcel(parcel: Parcel): FollowerBak {
            return FollowerBak(parcel)
        }

        override fun newArray(size: Int): Array<FollowerBak?> {
            return arrayOfNulls(size)
        }
    }
}

open class FollowerConfig() : FollowerBak(), Parcelable {
    lateinit var name: String
    var rarity: Int = 0
    var commission: Int = 0
    var type:Int = 0 // 0 normal, 1 spec can not add manually
    var max:Int = 1//max number can be auto added
    var property =  mutableListOf(0,0,0,0,0,0,0,0)
    var teji = mutableListOf<String>()
    var gender = NameUtil.Gender.Male

    fun toFollower(uniqueNameParam:String = ""):Follower{
        val follower = Follower()
        follower.id = this.id
        follower.name = this.name
        follower.uniqueName = uniqueNameParam
        follower.rarity = this.rarity
        follower.property =  Collections.synchronizedList(this.property.toMutableList())
        follower.teji = Collections.synchronizedList(this.teji.toMutableList())
        follower.gender = this.gender
        follower.commission = this.commission
        follower.type = this.type
        follower.max = this.max
        return follower
    }

    fun copy():FollowerConfig{
        return this.toFollower()
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        rarity = parcel.readInt()
        commission = parcel.readInt()
        type = parcel.readInt()
        max = parcel.readInt()
        gender = NameUtil.Gender.valueOf(parcel.readString()!!)
        property = Collections.synchronizedList(parcel.createIntArray().toMutableList())
        teji = Collections.synchronizedList(parcel.createStringArrayList())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(rarity)
        parcel.writeInt(commission)
        parcel.writeInt(type)
        parcel.writeInt(max)
        parcel.writeIntArray(property.toIntArray())
        parcel.writeStringList(teji)
        parcel.writeString(gender.props)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FollowerConfig> {
        override fun createFromParcel(parcel: Parcel): FollowerConfig {
            return FollowerConfig(parcel)
        }

        override fun newArray(size: Int): Array<FollowerConfig?> {
            return arrayOfNulls(size)
        }
    }


}

class Follower() : FollowerConfig(), Parcelable {
    var isDead:Boolean = false
    var unique:Boolean = false

    constructor(parcel: Parcel) : this() {
        unique = parcel.readByte() != 0.toByte()
        isDead = parcel.readByte() != 0.toByte()
    }

    fun toBak():FollowerBak{
        val bak = FollowerBak()
        bak.id = this.id
        bak.uniqueName = this.uniqueName
        return  bak
    }

    override fun toString(): String {
        return CultivationHelper.showing("$name(${property.take(4).joinToString()})")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (unique) 1 else 0)
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

        fun make(ids: String, uniqueNameParam: String = ""): Follower {
            return CultivationHelper.mConfig.follower.find { it.id == ids }!!.toFollower(uniqueNameParam)
        }
    }

}