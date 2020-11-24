package com.mx.gillustrated.database.imp

import android.util.Log
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.RuntimeExceptionDao
import com.mx.gillustrated.vo.CardCharacterInfo
import java.sql.SQLException

class CardCharacterInfoDaoImp(orm: OrmLiteSqliteOpenHelper) : RuntimeExceptionDao<CardCharacterInfo, Int>(getDao(orm)) {

    fun addCardCharacter(info:CardCharacterInfo):Boolean {
        val matching = this.queryForMatching(info)
        // 0 是空白行
        if (info.charId > 0 && (matching == null || matching.size == 0)){
            this.create(info)
            return true
        }
        return false
    }

    fun delCardChar(info: CardCharacterInfo): Int {
        val qb = this.deleteBuilder()
        try {
            qb.where().eq(CardCharacterInfo.COLUMN_CARD_NID, info.cardNid)
                    .and()
                    .eq(CardCharacterInfo.COLUMN_CHARACTER_ID, info.charId)
            return this.delete(qb.prepare())
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return -1
    }

    fun getListByCardId(cardId: Int): List<CardCharacterInfo>? {
        val qb = this.queryBuilder()
        qb.orderByRaw("rowid")
        try {
            qb.where().eq(CardCharacterInfo.COLUMN_CARD_NID, cardId)
            return this.query(qb.prepare())
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {

        private var mDao: Dao<CardCharacterInfo, Int>? = null

        private fun getDao(orm: OrmLiteSqliteOpenHelper): Dao<CardCharacterInfo, Int>? {
            if (mDao == null) {
                try {
                    mDao = orm.getDao(CardCharacterInfo::class.java)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }

            }
            return mDao
        }
    }
}