package com.mx.gillustrated.database.imp;

import android.content.Context;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.mx.gillustrated.activity.BaseActivity;
import com.mx.gillustrated.database.DataBaseHelper;
import com.mx.gillustrated.vo.CardEventInfo;
import com.mx.gillustrated.vo.CardInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maoxin on 2018/7/24.
 */

public class CardInfoDaoImp  extends RuntimeExceptionDao<CardInfo, Integer> {

    private static Dao<CardInfo, Integer> mDao = null;
    private DataBaseHelper mOrm = null;
    private Context mContext;

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
        this.mContext = orm.mContext;
    }

    public void addCardInfos(List<CardInfo> infos){
        for(CardInfo info: infos){
            this.createIfNotExists(info);
        }
    }

    public void delCardsById(List<Integer> list){
        for(int id:list){
            this.deleteById(id);
        }
    }

    public int delCardInfoByGameId(int gameId){
        DeleteBuilder<CardInfo, Integer> db = this.deleteBuilder();
        Where<CardInfo, Integer> where = db.where();
        try {
            where.eq(CardInfo.COLUMN_GAMETYPE, gameId);
            return this.delete(db.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<CardInfo> queryCards(CardInfo cardinfo, String orderBy, boolean isAsc, long position, long limit) {
        List<CardInfo> result = null;
        QueryBuilder<CardInfo, Integer> qb = this.queryBuilder();
        boolean showHeader = mContext.getSharedPreferences("commonset", Context.MODE_PRIVATE).getBoolean(BaseActivity.SHARE_SHOW_HEADER_IMAGES, false);
        if(orderBy.equals(CardInfo.COLUMN_NID) && !showHeader)
            orderBy = CardInfo.COLUMN_TOTAL;
        //qb.orderBy(orderBy, isAsc);
        qb.orderByRaw(orderBy + " " + ( isAsc ? "ASC" : "DESC") );
        //qb.selectRaw("*");
        qb.selectRaw("*," + CardInfo.COLUMN_MAXATTACK + "+" +  CardInfo.COLUMN_MAXDEFENSE + "+" + CardInfo.COLUMN_MAXHP + "+" + CardInfo.COLUMN_EXTRA_VALUE1
                + "+" + CardInfo.COLUMN_EXTRA_VALUE2 + " " + CardInfo.COLUMN_TOTAL);
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
                where.and().eq(CardInfo.COLUMN_FRONT_NAME, cardinfo.getFrontName() );
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
            result = this.queryRaw(qb.prepareStatementString(), new CardInfoRowMapper()).getResults();

            //计算总数
            QueryBuilder<CardInfo, Integer> qbCount = this.queryBuilder();
            qbCount.setCountOf(true);
            qbCount.selectRaw("*");
            qbCount.setWhere(where);
            int count = (int) this.countOf(qbCount.prepare());

            for( CardInfo cardInfo : result ){
                cardInfo.setAttr(getAttrName(cardInfo.getAttrId()));
                cardInfo.setTotalCount(count);
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


    private static class CardInfoRowMapper implements RawRowMapper<CardInfo>{

        @Override
        public CardInfo mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
            CardInfo info = new CardInfo();
            for(int i = 0; i < columnNames.length; i++){
                switch (columnNames[i]){
                    case CardInfo.ID:
                        info.setId(Integer.parseInt(resultColumns[i]));
                        break;
                    case CardInfo.COLUMN_ATTR:
                        info.setAttrId(Integer.parseInt(resultColumns[i]));
                        break;
                    case CardInfo.COLUMN_COST:
                        info.setCost(Integer.parseInt(resultColumns[i]));
                        break;
                    case CardInfo.COLUMN_FRONT_NAME:
                        info.setFrontName(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_GAMETYPE:
                        info.setGameId(Integer.parseInt(resultColumns[i]));
                        break;
                    case CardInfo.COLUMN_LEVEL:
                        info.setLevel(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_MAXATTACK:
                        info.setMaxAttack(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_MAXDEFENSE:
                        info.setMaxDefense(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_MAXHP:
                        info.setMaxHP(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_NAME:
                        info.setName(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_NID:
                        info.setNid(Integer.parseInt(resultColumns[i]));
                        break;
                    case CardInfo.COLUMN_PINYIN_NAME:
                        info.setPinyinName(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_REMARK:
                        info.setRemark(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_SHOW_HEAD:
                        info.setProfile(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_EXTRA_VALUE1:
                        info.setExtraValue1(resultColumns[i]);
                        break;
                    case CardInfo.COLUMN_EXTRA_VALUE2:
                        info.setExtraValue2(resultColumns[i]);
                        break;
                }

            }
            return info;
        }
    }

}
