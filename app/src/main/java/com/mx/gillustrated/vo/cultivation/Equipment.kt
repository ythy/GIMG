package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper
import java.nio.channels.MulticastChannel
import java.util.*

class Equipment() :Parcelable {
    lateinit var id:String
    lateinit var name:String
    var seq:Int = 0
    var type:Int = 0 // 0 Bao; 1 Wu; 2 Jia, >10 huizhang
    var rarity:Int = 0//5 30, 6 40，7 50，8 ~
    var xiuwei:Int = 0
    var success:Int = 0
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
    var maxCount:Int = 1//重复计算属性上限，默认1
    var spec:MutableList<Int> = mutableListOf()//专属
    var teji:MutableList<String> = mutableListOf()
    //以下字段不在配置里
    var uniqueName:String = ""
    var childrenAll:MutableList<Equipment> = mutableListOf() //计算用
    var children:MutableList<Equipment> = mutableListOf() // 显示用

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        name = parcel.readString()!!
        uniqueName = parcel.readString()!!
        seq = parcel.readInt()
        type = parcel.readInt()
        rarity = parcel.readInt()
        xiuwei = parcel.readInt()
        success = parcel.readInt()
        property = parcel.createIntArray().toMutableList()
        maxCount = parcel.readInt()
        children = parcel.createTypedArrayList(Equipment)
        childrenAll = parcel.createTypedArrayList(Equipment)
        teji = Collections.synchronizedList(parcel.createStringArrayList())
        spec = Collections.synchronizedList(parcel.createIntArray().toMutableList())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(uniqueName)
        parcel.writeInt(seq)
        parcel.writeInt(type)
        parcel.writeInt(rarity)
        parcel.writeInt(xiuwei)
        parcel.writeInt(success)
        parcel.writeIntArray(property.toIntArray())
        parcel.writeInt(maxCount)
        parcel.writeTypedList(children)
        parcel.writeTypedList(childrenAll)
        parcel.writeStringList(teji)
        parcel.writeIntArray(spec.toIntArray())
    }

    override fun describeContents(): Int {
        return 0
    }


    override fun toString(): String {
        return CultivationHelper.showing("$name:($xiuwei/$success)(${property.take(4).joinToString()})")
    }

    fun copy():Equipment{
        val equipment = Equipment()
        equipment.id = this.id
        equipment.name = this.name
        equipment.rarity = this.rarity
        equipment.uniqueName = this.uniqueName
        equipment.seq = this.seq
        equipment.type = this.type
        equipment.xiuwei = this.xiuwei
        equipment.success = this.success
        equipment.property = this.property
        equipment.maxCount = this.maxCount
        equipment.children = this.children
        equipment.childrenAll = this.childrenAll
        equipment.teji = this.teji
        equipment.spec = this.spec
        return equipment
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