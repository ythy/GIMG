package com.mx.gillustrated.database.imp;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.mx.gillustrated.provider.Providerdata;
import com.mx.gillustrated.vo.CardEventInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by maoxin on 2018/7/23.
 */

public class CardEventInfoDaoImp  extends RuntimeExceptionDao<CardEventInfo, Integer> {

    private static Dao<CardEventInfo, Integer> mDao = null;

    private static Dao<CardEventInfo, Integer> getDao(OrmLiteSqliteOpenHelper orm) {
        if (mDao == null){
            try {
                mDao = orm.getDao(CardEventInfo.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mDao;
    }

    public CardEventInfoDaoImp(OrmLiteSqliteOpenHelper orm) {
        super(CardEventInfoDaoImp.getDao(orm));
    }

    public void addCardEvents(List<CardEventInfo> infos){
        for(CardEventInfo event: infos){
            List<CardEventInfo> matching = this.queryForMatching(event);
            // -1 是空白行
            if(event.getEventId() > -1 && (matching == null || matching.size() == 0))
                this.create(event);
        }
    }

    public int delCardEvents(CardEventInfo info){
        DeleteBuilder<CardEventInfo, Integer> qb  = this.deleteBuilder();
        try {
            qb.where().eq("cardNid", info.getCardNid());
            qb.where().eq("eventId", info.getEventId());
            return this.delete(qb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<CardEventInfo> getListByCardId(int cardId){
        QueryBuilder<CardEventInfo, Integer> qb = this.queryBuilder();
        try {
            qb.where().eq("cardNid", cardId);
            return this.query(qb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
