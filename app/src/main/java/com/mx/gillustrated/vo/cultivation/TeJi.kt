package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable
import com.mx.gillustrated.component.CultivationHelper
import java.util.*

open class TeJiConfig() : Parcelable {
    lateinit var id:String
    lateinit var name:String
    var description:String = ""
    var rarity:Int = 0
    var weight:Int = 0
    var type:Int = 0
    var chance:Int = 100
    var status:String = ""
    var statusRound:Int = 0 // combining with status
    var power:Int = 0
    var extraPower:MutableList<Int> = mutableListOf(0,0,0,0)
    var spec:MutableList<Int> = mutableListOf()//专属
    var specName:MutableList<String> = mutableListOf()//专属

    fun toTeji():TeJi{
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
        teJi.power = this.power
        teJi.spec =  Collections.synchronizedList(this.spec.toMutableList())
        teJi.specName =  Collections.synchronizedList(this.specName.toMutableList())
        teJi.extraPower =  Collections.synchronizedList(this.extraPower.toMutableList())
        return teJi
    }

    override fun toString(): String {
        return CultivationHelper.showing("$name-$rarity")
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        rarity = parcel.readInt()
        weight = parcel.readInt()
        type = parcel.readInt()
        statusRound = parcel.readInt()
        status = parcel.readString()
        chance = parcel.readInt()
        power = parcel.readInt()
        spec = Collections.synchronizedList(parcel.createIntArray().toMutableList())
        specName = Collections.synchronizedList(parcel.createStringArrayList())
        extraPower = Collections.synchronizedList(parcel.createIntArray().toMutableList())
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
        parcel.writeInt(power)
        parcel.writeIntArray(spec.toIntArray())
        parcel.writeStringList(specName)
        parcel.writeIntArray(extraPower.toIntArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TeJiConfig> {
        override fun createFromParcel(parcel: Parcel): TeJiConfig {
            return TeJiConfig(parcel)
        }

        override fun newArray(size: Int): Array<TeJiConfig?> {
            return arrayOfNulls(size)
        }
    }

}


class TeJi() : TeJiConfig(), Parcelable {

    var form:Int = 0 // 0 list 1 ex equipment 2 amulet 3 fixed spec person 4 label

    constructor(parcel: Parcel) : this() {
        form = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(form)
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