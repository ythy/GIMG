package com.mx.gillustrated.vo

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = CardCharacterInfo.TABLE_NAME)
class CardCharacterInfo(@DatabaseField(uniqueCombo = true) var cardNid: Int = 0, @DatabaseField(uniqueCombo = true) var charId: Int = 0) {

    companion object {
        const val TABLE_NAME = "character_chain"
        const val COLUMN_CARD_NID = "cardNid"
        const val COLUMN_CHARACTER_ID = "charId"
    }
}
