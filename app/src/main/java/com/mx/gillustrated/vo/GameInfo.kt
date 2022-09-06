package com.mx.gillustrated.vo


import android.os.Parcel
import android.os.Parcelable
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = GameInfo.TABLE_NAME)
class GameInfo constructor(id: Int, name: String?) : SpinnerInfo(), Parcelable {


    constructor(id: Int): this(id, null)
    constructor(): this(0)

    @DatabaseField
    override var name: String? = null //游戏名称

    @DatabaseField
    var detail: String? = null //游戏说明

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = ID)
    override var id: Int = 0

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        detail = parcel.readString()
        id = parcel.readInt()
    }

    init {
        this.id = id
        this.name = name
    }

    companion object {
        const val TABLE_NAME = "game_info"
        const val ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DETAIL = "detail"

        @JvmField
        val CREATOR = object : Parcelable.Creator<GameInfo> {
            override fun createFromParcel(parcel: Parcel): GameInfo {
                return GameInfo(parcel)
            }

            override fun newArray(size: Int): Array<GameInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(detail)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }



}
