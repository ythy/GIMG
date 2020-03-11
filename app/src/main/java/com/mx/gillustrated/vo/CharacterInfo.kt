@file:Suppress("unused")

package com.mx.gillustrated.vo

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = CharacterInfo.TABLE_NAME)
class CharacterInfo {

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = ID)
    var id: Int = 0
    @DatabaseField
    var name: String? = null
    @DatabaseField(columnName = COLUMN_GAMEID)
    var gameId: Int = 0
    @DatabaseField
    var nationality: Int = 0
    @DatabaseField
    var domain: Int = 0
    @DatabaseField
    var age: Int = 0
    @DatabaseField
    var skilled: Int = 0
    @DatabaseField
    var character: Int = 0
    @DatabaseField
    var prop: Int = 0
    @DatabaseField
    var association = ""

    companion object {

        const val TABLE_NAME = "character_Info"
        const val ID = "_id"
        const val COLUMN_GAMEID = "game_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CHARACTER = "character"
        const val COLUMN_NATIONALITY = "nationality"
        const val COLUMN_DOMAIN = "domain"
        const val COLUMN_AGE = "age"
        const val COLUMN_SKILLED = "skilled"
        const val COLUMN_PROP = "prop"
        const val COLUMN_ASSOCIATION_NAME = "association"
    }
}
