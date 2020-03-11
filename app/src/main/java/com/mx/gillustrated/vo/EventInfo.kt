@file:Suppress("unused")

package com.mx.gillustrated.vo

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by maoxin on 2017/2/22.
 */

@DatabaseTable(tableName = EventInfo.TABLE_NAME)
class EventInfo constructor(name: String?) : SpinnerInfo() {

    @DatabaseField
    override var name: String? = null //活动名称

    @DatabaseField(columnName = COLUMN_GAMEID)
    var gameId: Int = 0

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = ID)
    override var id: Int = 0

    @DatabaseField
    var duration: String? = null //活动期间

    @DatabaseField
    var content: String? = null  //活动内容

    @DatabaseField(columnName = COLUMN_SHOWING)
    var showing: String? = null //是否是卡片可选项

    @DatabaseField(columnName = COLUMN_INDEX)
    var index: Int = 0  //排序用ID

    constructor():this(null)

    init {
        this.name = name
    }

    companion object {
        const val TABLE_NAME = "event_info"
        const val ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_GAMEID = "gameid"
        const val COLUMN_SHOWING = "showFlag"
        const val COLUMN_INDEX = "nid"
    }
}
