@file:Suppress("unused")

package com.mx.gillustrated.vo

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = CardTypeInfo.TABLE_NAME)
class CardTypeInfo constructor(gid:Int) : SpinnerInfo() {

    @DatabaseField
    override var name: String? = ""

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = ID)
    override var id: Int = 0

    @DatabaseField(columnName = COLUMN_GAMETYPE)
    var gameId: Int = 0

    init {
        this.gameId = gid
    }
    constructor():this(0)

    companion object {

        const val TABLE_NAME = "card_type_info"
        const val ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_GAMETYPE = "game_type"
    }

}
