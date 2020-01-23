package com.mx.gillustrated.database.imp

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.RuntimeExceptionDao
import com.mx.gillustrated.vo.CharacterInfo
import java.sql.SQLException

class CharacterInfoDaoImp(orm: OrmLiteSqliteOpenHelper) : RuntimeExceptionDao<CharacterInfo, Int>(CharacterInfoDaoImp.getDao(orm)) {

    companion object {

        private var mDao: Dao<CharacterInfo, Int>? = null

        private fun getDao(orm: OrmLiteSqliteOpenHelper): Dao<CharacterInfo, Int>? {
            if (mDao == null) {
                try {
                    mDao = orm.getDao(CharacterInfo::class.java)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }

            }
            return mDao
        }
    }

}