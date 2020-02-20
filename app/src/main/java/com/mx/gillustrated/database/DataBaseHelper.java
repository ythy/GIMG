package com.mx.gillustrated.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mx.gillustrated.R;
import com.mx.gillustrated.database.imp.CardCharacterInfoDaoImp;
import com.mx.gillustrated.database.imp.CardEventInfoDaoImp;
import com.mx.gillustrated.database.imp.CardInfoDaoImp;
import com.mx.gillustrated.database.imp.CardTypeInfoDaoImp;
import com.mx.gillustrated.database.imp.CharacterInfoDaoImp;
import com.mx.gillustrated.database.imp.EventInfoDaoImp;
import com.mx.gillustrated.database.imp.GameInfoDaoImp;
import com.mx.gillustrated.util.PinyinUtil;
import com.mx.gillustrated.vo.CardCharacterInfo;
import com.mx.gillustrated.vo.CardEventInfo;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.CharacterInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by maoxin on 2018/7/20.
 */

public class DataBaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application
    private static final String DATABASE_NAME = "GIMG.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 22;

    private static final Class[] CONFIG_CLASSES = {
            GameInfo.class, CardTypeInfo.class, CardEventInfo.class, EventInfo.class, CardInfo.class,CharacterInfo.class, CardCharacterInfo.class
    };

    public Context mContext;

    public DataBaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION,
                R.raw.ormlite_config);
        mContext = context;
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
        if(oldVersion < 10){
            getCardInfoDao().executeRaw("ALTER TABLE " + CardInfo.TABLE_NAME + " ADD COLUMN  " + CardInfo.COLUMN_PINYIN_NAME + " VARCHAR; ");
        }if(oldVersion < 11){
            List<CardInfo> list = getCardInfoDao().queryForAll();
            for( CardInfo card : list ){
                card.setPinyinName(PinyinUtil.convert(card.getName()));
                getCardInfoDao().update(card);
            }
        }if(oldVersion < 12){
            getGameInfoDao().executeRaw("ALTER TABLE " + GameInfo.TABLE_NAME + " ADD COLUMN  " + GameInfo.COLUMN_DETAIL + " VARCHAR; ");
        }if(oldVersion < 13){
            getGameInfoDao().executeRaw("ALTER TABLE " + EventInfo.TABLE_NAME + " ADD COLUMN  " + EventInfo.COLUMN_INDEX + " INTEGER DEFAULT 0 ; ");
        }if(oldVersion < 14){
            getCardInfoDao().executeRaw("ALTER TABLE " + CardInfo.TABLE_NAME + " ADD COLUMN  " + CardInfo.COLUMN_SHOW_HEAD + " VARCHAR DEFAULT Y ; ");
        }if(oldVersion < 15){
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_COST + " = ? WHERE "
                    + CardInfo.COLUMN_COST + " =? AND " + CardInfo.COLUMN_GAMETYPE + " =? ", "2", "1", "1");
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_COST + " = ? WHERE "
                    + CardInfo.COLUMN_COST + " =? AND " + CardInfo.COLUMN_GAMETYPE + " =? ", "1", "0", "1");
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_COST + " = ? WHERE "
                    + CardInfo.COLUMN_COST + " =? AND " + CardInfo.COLUMN_GAMETYPE + " =? ", "0", "4", "1");
        }if(oldVersion < 16){
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_MAXHP + " = ? WHERE "
                    + CardInfo.COLUMN_MAXHP + " =? ", "", "0");
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_MAXATTACK + " = ? WHERE "
                    + CardInfo.COLUMN_MAXATTACK + " =? ", "", "0");
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_MAXDEFENSE + " = ? WHERE "
                    + CardInfo.COLUMN_MAXDEFENSE + " =? ", "", "0");
        }if(oldVersion < 17){
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_GAMETYPE + " = ? WHERE "
                    + CardInfo.COLUMN_GAMETYPE + " =? AND " + CardInfo.COLUMN_COST + " in( ?, ? ) ", "4", "1", "1", "2");

            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_ATTR + " = ? WHERE "
                    + CardInfo.COLUMN_GAMETYPE + " =? AND " + CardInfo.COLUMN_ATTR + " =? ", "16", "4", "1");
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_ATTR + " = ? WHERE "
                    + CardInfo.COLUMN_GAMETYPE + " =? AND " + CardInfo.COLUMN_ATTR + " =? ", "17", "4", "11");
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_ATTR + " = ? WHERE "
                    + CardInfo.COLUMN_GAMETYPE + " =? AND " + CardInfo.COLUMN_ATTR + " =? ", "18", "4", "12");
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_ATTR + " = ? WHERE "
                    + CardInfo.COLUMN_GAMETYPE + " =? AND " + CardInfo.COLUMN_ATTR + " =? ", "19", "4", "13");
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_ATTR + " = ? WHERE "
                    + CardInfo.COLUMN_GAMETYPE + " =? AND " + CardInfo.COLUMN_ATTR + " =? ", "20", "4", "14");
        }else if(oldVersion < 18){
            getCardInfoDao().executeRaw("ALTER TABLE " + CardInfo.TABLE_NAME + " ADD COLUMN  " + CardInfo.COLUMN_EXTRA_VALUE1 + " VARCHAR ; ");
            getCardInfoDao().executeRaw("ALTER TABLE " + CardInfo.TABLE_NAME + " ADD COLUMN  " + CardInfo.COLUMN_EXTRA_VALUE2 + " VARCHAR ; ");
        }else if(oldVersion < 19){
            getCardInfoDao().executeRaw("UPDATE " + CardInfo.TABLE_NAME + " SET " + CardInfo.COLUMN_ATTR + " = ? WHERE "
                    + CardInfo.COLUMN_GAMETYPE + " =? AND " + CardInfo.COLUMN_ATTR + " > ? ", "22", "5", "24");
        }else if(oldVersion < 20){
            getCharacterInfoDao().executeRaw(" CREATE TABLE " + CharacterInfo.TABLE_NAME + " ( "
                    + CharacterInfo.ID  +  " INTEGER PRIMARY KEY , "
                    + CharacterInfo.COLUMN_NAME  +  " VARCHAR , "
                    + CharacterInfo.COLUMN_GAMEID  +  " INTEGER , "
                    + CharacterInfo.COLUMN_NATIONALITY  +  " INTEGER , "
                    + CharacterInfo.COLUMN_DOMAIN  +  " INTEGER , "
                    + CharacterInfo.COLUMN_SKILLED  +  " INTEGER , "
                    + CharacterInfo.COLUMN_AGE  +  " INTEGER )");

            getCardCharacterInfoDao().executeRaw(" CREATE TABLE " + CardCharacterInfo.TABLE_NAME + " ( "
                    + CardCharacterInfo.COLUMN_CARD_NID  +  " INTEGER , "
                    + CardCharacterInfo.COLUMN_CHARACTER_ID  +  " INTEGER,"
                    + "PRIMARY KEY ( " +  CardCharacterInfo.COLUMN_CARD_NID + " , "
                    + CardCharacterInfo.COLUMN_CHARACTER_ID + " ) " +  " )" );
        }else if(oldVersion < 21){
            getCharacterInfoDao().executeRaw("ALTER TABLE " + CharacterInfo.TABLE_NAME + " ADD COLUMN  " + CharacterInfo.COLUMN_CHARACTER + " INTEGER ; ");
        }else if(oldVersion < 22){
            getCharacterInfoDao().executeRaw("ALTER TABLE " + CharacterInfo.TABLE_NAME + " ADD COLUMN  " + CharacterInfo.COLUMN_PROP + " INTEGER ; ");
        }

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

    public CharacterInfoDaoImp getCharacterInfoDao(){
        return new CharacterInfoDaoImp(this);
    }

    public CardCharacterInfoDaoImp getCardCharacterInfoDao(){
        return new CardCharacterInfoDaoImp(this);
    }
}
