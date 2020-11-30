package com.mx.gillustrated.vo

import android.os.Parcel
import android.os.Parcelable

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = CardInfo.TABLE_NAME)
class CardInfo : SpinnerInfo, Parcelable {

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = ID)
    override var id: Int = 0
    @DatabaseField
    override var nid: Int = 0
    @DatabaseField(columnName = COLUMN_GAMETYPE)
    var gameId: Int = 0
    @DatabaseField(columnName = COLUMN_EVENTTYPE)
    var eventId: Int = 0
    @DatabaseField(columnName = COLUMN_FRONT_NAME)
    var frontName: String? = null
    @DatabaseField
    override var name: String? = null
    var attr: String? = null
    @DatabaseField(columnName = COLUMN_ATTR)
    var attrId: Int = 0
    @DatabaseField
    var level: String? = null
    @DatabaseField
    var cost = -1
    @DatabaseField(columnName = COLUMN_MAXHP, defaultValue = "")
    var maxHP: String? = null
    @DatabaseField(columnName = COLUMN_MAXATTACK, defaultValue = "")
    var maxAttack: String? = null
    @DatabaseField(columnName = COLUMN_MAXDEFENSE, defaultValue = "")
    var maxDefense: String? = null
    @DatabaseField(columnName = COLUMN_EXTRA_VALUE1, defaultValue = "")
    var extraValue1: String? = null
    @DatabaseField(columnName = COLUMN_EXTRA_VALUE2, defaultValue = "")
    var extraValue2: String? = null

    @DatabaseField(columnName = COLUMN_IMGUPDATE, defaultValue = "0")
    var imageUpdate: Int = 0
    @DatabaseField
    var remark: String? = null //卡片备注
    @DatabaseField(columnName = COLUMN_PINYIN_NAME)
    var pinyinName: String? = null
    @DatabaseField(columnName = COLUMN_SHOW_HEAD)
    var profile: String? = null //是否有头像

    var totalCount: Int = 0

    val cardSearchParam: Array<String?>
        get() = arrayOf(this.name, this.cost.toString(), this.attrId.toString(), this.eventId.toString(), this.gameId.toString(), this.frontName, this.level)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(attrId)
        dest.writeInt(gameId)
        dest.writeString(frontName)
        dest.writeString(name)
        dest.writeString(attr)
        dest.writeInt(nid)
        dest.writeString(level)
        dest.writeInt(cost)
        dest.writeString(maxHP)
        dest.writeString(maxAttack)
        dest.writeString(maxDefense)
        dest.writeString(remark)
        dest.writeInt(eventId)
        dest.writeInt(imageUpdate)
        dest.writeString(profile)
        dest.writeString(extraValue1)
        dest.writeString(extraValue2)
    }

    constructor(`in`: Parcel) {
        id = `in`.readInt()
        attrId = `in`.readInt()
        gameId = `in`.readInt()
        frontName = `in`.readString()
        name = `in`.readString()
        attr = `in`.readString()
        nid = `in`.readInt()
        level = `in`.readString()
        cost = `in`.readInt()
        maxHP = `in`.readString()
        maxAttack = `in`.readString()
        maxDefense = `in`.readString()
        remark = `in`.readString()
        eventId = `in`.readInt()
        imageUpdate = `in`.readInt()
        profile = `in`.readString()
        extraValue1 = `in`.readString()
        extraValue2 = `in`.readString()
    }

    constructor()

    constructor(name: String?) {
        this.name = name
    }

    constructor(gameId: Int) {
        this.gameId = gameId
    }

    constructor(id: Int, name: String?) {
        this.id = id
        this.name = name
    }

    constructor(searchParams: Array<String>) {
        this.name = searchParams[0]
        this.cost = Integer.parseInt(searchParams[1])
        this.attrId = Integer.parseInt(searchParams[2])
        this.eventId = Integer.parseInt(searchParams[3])
        this.gameId = Integer.parseInt(searchParams[4])
        this.frontName = searchParams[5]
        this.level = searchParams[6]
    }

    companion object {

        const val TABLE_NAME = "card_info"
        const val SORT_DESC = "DESC "
        const val SORT_ASC = "ASC "

        const val ID = "_id"
        const val COLUMN_NID = "nid"
        const val COLUMN_NAME = "name"
        const val COLUMN_FRONT_NAME = "front_name"
        const val COLUMN_LEVEL = "level"
        const val COLUMN_ATTR = "attr"
        const val COLUMN_COST = "cost"
        const val COLUMN_MAXHP = "max_hp"
        const val COLUMN_MAXATTACK = "max_attack"
        const val COLUMN_MAXDEFENSE = "max_defense"
        const val COLUMN_EXTRA_VALUE1 = "extra_value_1"
        const val COLUMN_EXTRA_VALUE2 = "extra_value_2"
        const val COLUMN_GAMETYPE = "game_type"
        const val COLUMN_IMGUPDATE = "img_update"
        const val COLUMN_REMARK = "remark"
        const val COLUMN_EVENTTYPE = "event_type"
        const val COLUMN_PINYIN_NAME = "pinyin_name"
        const val COLUMN_SHOW_HEAD = "has_profile"

        const val COLUMN_TOTAL = "total_number"
        const val COLUMN_TOTAL_COUNT = "total_count"
        const val COLUMN_ATTR_NAME = "attr_name"

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<CardInfo> = object : Parcelable.Creator<CardInfo> {

            override fun newArray(size: Int): Array<CardInfo?> {
                return arrayOfNulls(size)
            }

            override fun createFromParcel(`in`: Parcel): CardInfo {
                return CardInfo(`in`)
            }
        }
    }

}
