package com.mx.gillustrated.database.imp;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.GameInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by maoxin on 2018/7/23.
 */

public class GameInfoDaoImp extends RuntimeExceptionDao<GameInfo, Integer> {

    private static Dao<GameInfo, Integer> mDao = null;

    private static Dao<GameInfo, Integer> getDao(OrmLiteSqliteOpenHelper orm) {
        if (mDao == null){
            try {
                mDao = orm.getDao(GameInfo.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mDao;
    }

    public GameInfoDaoImp(OrmLiteSqliteOpenHelper orm) {
        super(GameInfoDaoImp.getDao(orm));
    }

    public void addGameInfos(List<GameInfo> infos){
        for(GameInfo info: infos){
            this.createIfNotExists(info);
        }
    }




}
