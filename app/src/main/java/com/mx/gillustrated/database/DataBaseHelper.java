package com.mx.gillustrated.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mx.gillustrated.R;
import com.mx.gillustrated.database.imp.CardEventInfoDaoImp;
import com.mx.gillustrated.database.imp.CardInfoDaoImp;
import com.mx.gillustrated.database.imp.CardTypeInfoDaoImp;
import com.mx.gillustrated.database.imp.EventInfoDaoImp;
import com.mx.gillustrated.database.imp.GameInfoDaoImp;
import com.mx.gillustrated.vo.CardEventInfo;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import java.sql.SQLException;

/**
 * Created by maoxin on 2018/7/20.
 */

public class DataBaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application
    private static final String DATABASE_NAME = "GIMG.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 9;

    private static final Class[] CONFIG_CLASSES = {
            GameInfo.class, CardTypeInfo.class, CardEventInfo.class, EventInfo.class, CardInfo.class,
    };

    public DataBaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION,
                R.raw.ormlite_config);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            for (Class configClass: CONFIG_CLASSES) {
                TableUtils.createTableIfNotExists(connectionSource, configClass);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    public GameInfoDaoImp getGameInfoDao(){
        return new GameInfoDaoImp(this);
    }

    public CardTypeInfoDaoImp getCardTypeInfoDao(){
        return new CardTypeInfoDaoImp(this);
    }

    public CardEventInfoDaoImp getCardEventInfoDao(){
        return new CardEventInfoDaoImp(this);
    }

    public EventInfoDaoImp getEventInfoDao(){
        return new EventInfoDaoImp(this);
    }

    public CardInfoDaoImp getCardInfoDao(){
        return new CardInfoDaoImp(this);
    }
}
