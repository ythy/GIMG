package com.mx.gillustrated.database.imp;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;
import com.mx.gillustrated.vo.CardTypeInfo;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by maoxin on 2018/7/23.
 */

public class CardTypeInfoDaoImp  extends RuntimeExceptionDao<CardTypeInfo, Integer> {

    private static Dao<CardTypeInfo, Integer> mDao = null;

    private static Dao<CardTypeInfo, Integer> getDao(OrmLiteSqliteOpenHelper orm) {
        if (mDao == null){
            try {
                mDao = orm.getDao(CardTypeInfo.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mDao;
    }

    public CardTypeInfoDaoImp(OrmLiteSqliteOpenHelper orm) {
        super(CardTypeInfoDaoImp.getDao(orm));
    }

    public void addCardTypes(List<CardTypeInfo> infos){
        for(CardTypeInfo info: infos){
            this.createIfNotExists(info);
        }
    }

    public int delCardTypeInfoByGameId(int gameId){
        DeleteBuilder<CardTypeInfo, Integer> db = this.deleteBuilder();
        Where<CardTypeInfo, Integer> where = db.where();
        try {
            where.eq(CardTypeInfo.COLUMN_GAMETYPE, gameId);
            return this.delete(db.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
