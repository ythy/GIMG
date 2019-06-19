package com.mx.gillustrated.database.imp;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.mx.gillustrated.vo.CardEventInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by maoxin on 2018/7/23.
 */

public class EventInfoDaoImp extends RuntimeExceptionDao<EventInfo, Integer> {

    private static Dao<EventInfo, Integer> mDao = null;

    private static Dao<EventInfo, Integer> getDao(OrmLiteSqliteOpenHelper orm) {
        if (mDao == null){
            try {
                mDao = orm.getDao(EventInfo.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mDao;
    }

    public EventInfoDaoImp(OrmLiteSqliteOpenHelper orm) {
        super(EventInfoDaoImp.getDao(orm));
    }

    public void addEventInfos(List<EventInfo> infos){
        for(EventInfo info: infos){
            this.createIfNotExists(info);
        }
    }

    public List<EventInfo> getListByGameId(int gameId, String showFlag ){
        QueryBuilder<EventInfo, Integer> qb = this.queryBuilder();
        Where<EventInfo, Integer> where = qb.where();
        qb.orderByRaw( EventInfo.COLUMN_INDEX + " desc, " + EventInfo.ID + " asc");
        try {
            where.eq(EventInfo.COLUMN_GAMEID, gameId);
            if(showFlag != null) {
                where.and().eq(EventInfo.COLUMN_SHOWING, showFlag);
            }
            return this.query(qb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int delEventInfoByGameId(int gameId){
        DeleteBuilder<EventInfo, Integer> db = this.deleteBuilder();
        Where<EventInfo, Integer> where = db.where();
        try {
            where.eq(EventInfo.COLUMN_GAMEID, gameId);
            return this.delete(db.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
