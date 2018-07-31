package com.mx.gillustrated.database.imp;

import android.database.Cursor;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.RawRowObjectMapper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.mx.gillustrated.database.DataBaseHelper;
import com.mx.gillustrated.vo.CardEventInfo;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maoxin on 2018/7/24.
 */

public class CardInfoDaoImp  extends RuntimeExceptionDao<CardInfo, Integer> {

    private static Dao<CardInfo, Integer> mDao = null;
    private DataBaseHelper mOrm = null;

    private static Dao<CardInfo, Integer> getDao(DataBaseHelper orm) {
        if (mDao == null){
            try {
                mDao = orm.getDao(CardInfo.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mDao;
    }

    public CardInfoDaoImp(DataBaseHelper orm) {
        super(CardInfoDaoImp.getDao(orm));
        this.mOrm = orm;
    }

    public void addCardInfos(List<CardInfo> infos){
        for(CardInfo info: infos){
            this.createIfNotExists(info);
        }
    }


    public List<CardInfo> queryCards(CardInfo cardinfo, String orderBy, boolean isAsc, long position, long limit) {
        List<CardInfo> result = null;
        QueryBuilder<CardInfo, Integer> qb = this.queryBuilder();
        qb.orderBy(orderBy, isAsc);
        qb.selectRaw("*");
//        qb.selectRaw("(SELECT " + CardTypeInfo.COLUMN_NAME + " FROM " + CardTypeInfo.TABLE_NAME + " where " +
//                CardTypeInfo.TABLE_NAME +  "." + CardTypeInfo.ID + " = " + CardInfo.TABLE_NAME + "." + CardInfo.COLUMN_ATTR + ") ATTRNAME" );
        try {

            qb.limit(limit);
            qb.offset(position);

            Where<CardInfo, Integer> where =  qb.where();
            where.eq(CardInfo.COLUMN_GAMETYPE, cardinfo.getGameId());
            if(cardinfo.getPinyinName() != null)
                where.and().like(CardInfo.COLUMN_PINYIN_NAME, cardinfo.getPinyinName() + "%");
            if(cardinfo.getName() != null)
                where.and().like(CardInfo.COLUMN_NAME, "%" + cardinfo.getName() + "%");
            if(cardinfo.getFrontName() != null)
                where.and().like(CardInfo.COLUMN_FRONT_NAME, "%" + cardinfo.getFrontName() + "%");
            if(cardinfo.getId() > 0)
                where.and().eq(CardInfo.ID, cardinfo.getId());
            if(cardinfo.getNid() > 0)
                where.and().eq(CardInfo.COLUMN_NID, cardinfo.getNid());
            if(cardinfo.getAttrId() > 0)
                where.and().eq(CardInfo.COLUMN_ATTR, cardinfo.getAttrId());
            if(cardinfo.getLevel() != null)
                where.and().eq(CardInfo.COLUMN_LEVEL, cardinfo.getLevel());
            if(cardinfo.getCost() > -1)
                where.and().eq(CardInfo.COLUMN_COST, cardinfo.getCost());
            if(cardinfo.getEventId() > 0){
                where.and().raw("EXISTS ( SELECT * FROM  "
                        + CardEventInfo.TABLE_NAME
                        + " WHERE " + CardInfo.TABLE_NAME + "." + CardInfo.ID + " = "
                        + CardEventInfo.TABLE_NAME + "." + CardEventInfo.COLUMN_CARD_NID + " and "
                        + CardEventInfo.TABLE_NAME + "." + CardEventInfo.COLUMN_EVENT_ID + " = " + cardinfo.getEventId() + "  ) ");
            }
            result = this.queryRaw(qb.prepareStatementString(), this.getRawRowMapper()).getResults();
            for( CardInfo cardInfo : result ){
                cardInfo.setAttr(getAttrName(cardInfo.getAttrId()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getAttrName(int id){
        return this.mOrm.getCardTypeInfoDao().queryForId(id).getName();
    }

    public List<CardInfo> queryCardDropList(String type, int gametype) {
        List<String[]> result = new ArrayList<>();
        QueryBuilder<CardInfo, Integer> qb = this.queryBuilder();
        qb.selectRaw("DISTINCT ( " + type + ") ");
        qb.selectRaw("COUNT (*)");
        qb.groupBy(type);
        try {
            qb.where().eq(CardInfo.COLUMN_GAMETYPE, gametype);
            result = qb.queryRaw().getResults();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<CardInfo> cardInfos = new ArrayList();
        for( String[] row : result ){
            CardInfo cardInfo = new CardInfo();
            if(type == CardInfo.COLUMN_ATTR ) {
                cardInfo.setName(getAttrName(Integer.parseInt(row[0])));
                cardInfo.setAttrId(Integer.parseInt(row[0]));
            }
            else
                cardInfo.setName(row[0]);
            cardInfo.setNid(Integer.parseInt(row[1]));
            cardInfos.add(cardInfo);
        }
        return cardInfos;
    }

    public long updateCardName(CardInfo cardinfoNew, CardInfo cardinfoOld)
    {
        UpdateBuilder<CardInfo, Integer> ub = this.updateBuilder();
        try {
            ub.updateColumnValue(CardInfo.COLUMN_NAME, cardinfoNew.getName());
            ub.updateColumnValue(CardInfo.COLUMN_PINYIN_NAME, cardinfoNew.getPinyinName());
            ub.where().eq(CardInfo.COLUMN_NAME, cardinfoOld.getName());
            return this.update(ub.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }




}
