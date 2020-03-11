package com.mx.gillustrated.vo

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by maoxin on 2018/7/23.
 */

@DatabaseTable(tableName = CardEventInfo.TABLE_NAME)
class CardEventInfo {

    @DatabaseField(uniqueCombo = true)
    var cardNid: Int = 0

    @DatabaseField(uniqueCombo = true)
    var eventId: Int = 0

    constructor()

    constructor(cardNid: Int, eventId: Int) {
        this.cardNid = cardNid
        this.eventId = eventId
    }

    companion object {
        const val TABLE_NAME = "event_chain"
        const val COLUMN_CARD_NID = "cardNid"
        const val COLUMN_EVENT_ID = "eventId"
    }
}
