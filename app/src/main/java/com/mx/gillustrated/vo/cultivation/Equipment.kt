package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper
import java.util.*

open class EquipmentBak(): Parcelable{
    lateinit var id:String
    var seq:Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        seq = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(seq)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EquipmentBak> {
        override fun createFromParcel(parcel: Parcel): EquipmentBak {
            return EquipmentBak(parcel)
        }

        override fun newArray(size: Int): Array<EquipmentBak?> {
            return arrayOfNulls(size)
        }
    }
}

open class EquipmentConfig(): EquipmentBak(), Parcelable{
    lateinit var name:String
    var type:Int = 0 // 0 Bao; 1 Wu; 2 Jia, 3 Yao; 5 Amulet; 6 boss?; 8 exclusive?; 9 spec; ?: 不添加至equipment list
    var rarity:Int = 0//5 30, 6 40，7 50，8 ~
    var xiuwei:Int = 0
    var success:Int = 0
    var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
    var maxCount:Int = 1//重复计算属性上限，默认1
    var spec:MutableList<Int> = mutableListOf()//专属
    var specName:MutableList<String> = mutableListOf()//专属
    var teji:MutableList<String> = mutableListOf()
    var follower:MutableList<String> = mutableListOf()

    fun toEquipment(seq:Int = 0):Equipment{
        val equipment = Equipment()
        equipment.id = this.id
        equipment.name = this.name
        equipment.rarity = this.rarity
        equipment.type = this.type
        equipment.xiuwei = this.xiuwei
        equipment.success = this.success
        equipment.property = this.property
        equipment.maxCount = this.maxCount
        equipment.spec =  Collections.synchronizedList(this.spec.toMutableList())
        equipment.teji = Collections.synchronizedList(this.teji.toMutableList())
        equipment.follower = Collections.synchronizedList(this.follower.toMutableList())
        equipment.specName = Collections.synchronizedList(this.specName.toMutableList())
        equipment.seq = seq
        return equipment
    }

    fun copy():EquipmentConfig{
        return this.toEquipment()
    }

    override fun toString(): String {
        return CultivationHelper.showing("$name:($xiuwei/$success)(${property.take(4).joinToString()})")
    }

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()!!
        type = parcel.readInt()
        rarity = parcel.readInt()
        xiuwei = parcel.readInt()
        success = parcel.readInt()
        property = parcel.createIntArray().toMutableList()
        maxCount = parcel.readInt()
        teji = Collections.synchronizedList(parcel.createStringArrayList())
        follower = Collections.synchronizedList(parcel.createStringArrayList())
        spec = Collections.synchronizedList(parcel.createIntArray().toMutableList())
        specName = Collections.synchronizedList(parcel.createStringArrayList())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(type)
        parcel.writeInt(rarity)
        parcel.writeInt(xiuwei)
        parcel.writeInt(success)
        parcel.writeIntArray(property.toIntArray())
        parcel.writeInt(maxCount)
        parcel.writeStringList(teji)
        parcel.writeStringList(follower)
        parcel.writeIntArray(spec.toIntArray())
        parcel.writeStringList(specName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EquipmentConfig> {
        override fun createFromParcel(parcel: Parcel): EquipmentConfig {
            return EquipmentConfig(parcel)
        }

        override fun newArray(size: Int): Array<EquipmentConfig?> {
            return arrayOfNulls(size)
        }
    }

}


class Equipment() : EquipmentConfig(), Parcelable {
    var uniqueName:String = ""
    var childrenAll:MutableList<Equipment> = mutableListOf() //计算用
    var children:MutableList<Equipment> = mutableListOf() // 显示用

    fun toBak():EquipmentBak{
        val bak = EquipmentBak()
        bak.id = this.id
        bak.seq = this.seq
        return  bak
    }

    constructor(parcel: Parcel) : this() {
        uniqueName = parcel.readString()!!
        seq = parcel.readInt()
        children = parcel.createTypedArrayList(Equipment)
        childrenAll = parcel.createTypedArrayList(Equipment)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uniqueName)
        parcel.writeInt(seq)
        parcel.writeTypedList(children)
        parcel.writeTypedList(childrenAll)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<Equipment> {
        override fun createFromParcel(parcel: Parcel): Equipment {
            return Equipment(parcel)
        }

        override fun newArray(size: Int): Array<Equipment?> {
            return arrayOfNulls(size)
        }

        fun make(ids: String, seqs:Int = 0): Equipment {
           return CultivationHelper.mConfig.equipment.find { it.id == ids }!!.toEquipment(seqs)
        }
    }

}