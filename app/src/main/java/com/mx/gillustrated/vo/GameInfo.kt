package com.mx.gillustrated.vo

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = GameInfo.TABLE_NAME)
class GameInfo constructor(id: Int, name: String?) : SpinnerInfo() {


    constructor(id: Int): this(id, null)
    constructor(): this(0)

    @DatabaseField
    override var name: String? = null //游戏名称

    @DatabaseField
    var detail: String? = null //游戏说明

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = ID)
    override var id: Int = 0

    init {
        this.id = id
        this.name = name
    }

    companion object {
        const val TABLE_NAME = "game_info"
        const val ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DETAIL = "detail"
    }


}
