package com.mx.gillustrated.database.imp

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.RuntimeExceptionDao
import com.j256.ormlite.stmt.DeleteBuilder
import com.j256.ormlite.stmt.QueryBuilder
import com.mx.gillustrated.vo.CardEventInfo

import java.sql.SQLException

/**
 * Created by maoxin on 2018/7/23.
 */

class CardEventInfoDaoImp(orm: OrmLiteSqliteOpenHelper) : RuntimeExceptionDao<CardEventInfo, Int>(getDao(orm)) {

    fun addCardEvents(infos: List<CardEventInfo>) {
        for (event in infos) {
            val matching = this.queryForMatching(event)
            // 0 是空白行
            if (event.eventId > 0 && (matching == null || matching.size == 0))
                this.create(event)
        }

        for (event in getListByCardId(infos[0].cardNid)!!) {
            var deleted = true
            infos.forEach {
                 if(it.eventId == event.eventId)
                    deleted = false
            }
            if (deleted)
                delCardEvents(event)
        }
    }

    fun delCardEvents(info: CardEventInfo): Int {
        val qb = this.deleteBuilder()
        try {
            qb.where().eq(CardEventInfo.COLUMN_CARD_NID, info.cardNid)
                    .and()
                    .eq(CardEventInfo.COLUMN_EVENT_ID, info.eventId)
            return this.delete(qb.prepare())
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return -1
    }

    fun getListByCardId(cardId: Int): List<CardEventInfo>? {
        val qb = this.queryBuilder()
        try {
            qb.where().eq(CardEventInfo.COLUMN_CARD_NID, cardId)
            return this.query(qb.prepare())
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {

        private var mDao: Dao<CardEventInfo, Int>? = null

        private fun getDao(orm: OrmLiteSqliteOpenHelper): Dao<CardEventInfo, Int>? {
            if (mDao == null) {
                try {
                    mDao = orm.getDao(CardEventInfo::class.java)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }

            }
            return mDao
        }
    }
}
