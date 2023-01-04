package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper
import java.util.*

class Label() : Parcelable {

    lateinit var id:String
    lateinit var name:String
    var weight:Int = 0
    var rarity:Int = 0
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
    var teji:MutableList<String> = mutableListOf()
    var follower:MutableList<String> = mutableListOf()

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        weight = parcel.readInt()
        rarity = parcel.readInt()
        property = parcel.createIntArray().toMutableList()
        teji = Collections.synchronizedList(parcel.createStringArrayList())
        follower = Collections.synchronizedList(parcel.createStringArrayList())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(weight)
        parcel.writeInt(rarity)
        parcel.writeIntArray(property.toIntArray())
        parcel.writeStringList(teji)
        parcel.writeStringList(follower)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Label> {
        override fun createFromParcel(parcel: Parcel): Label {
            return Label(parcel)
        }

        override fun newArray(size: Int): Array<Label?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        val tejiName = if (teji.isNotEmpty()) CultivationHelper.mConfig.teji.find { it.id == teji[0] }?.name ?: "" else ""
        val followerName = if (follower.isNotEmpty()) CultivationHelper.mConfig.follower.find { it.id == follower[0] }?.name ?: "" else ""
        return CultivationHelper.showing("$name: (${property.take(6).joinToString()}) $tejiName $followerName ")
    }

    fun copy():Label{
        val label = Label()
        label.id = this.id
        label.name = this.name
        label.rarity = this.rarity
        label.weight = this.weight
        label.property = this.property.toMutableList()
        label.teji = Collections.synchronizedList(this.teji.toMutableList())
        label.follower = Collections.synchronizedList(this.follower.toMutableList())
        return label
    }
}