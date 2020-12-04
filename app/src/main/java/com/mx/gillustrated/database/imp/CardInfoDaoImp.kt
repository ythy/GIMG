package com.mx.gillustrated.database.imp

import android.content.Context
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.RawRowMapper
import com.j256.ormlite.dao.RuntimeExceptionDao
import com.mx.gillustrated.activity.BaseActivity
import com.mx.gillustrated.database.DataBaseHelper
import com.mx.gillustrated.vo.CardEventInfo
import com.mx.gillustrated.vo.CardInfo
import com.mx.gillustrated.vo.CardTypeInfo
import java.sql.SQLException
import java.util.ArrayList

/**
 * Created by maoxin on 2018/7/24.
 */

class CardInfoDaoImp(orm: DataBaseHelper) : RuntimeExceptionDao<CardInfo, Int>(getDao(orm)) {
    private val mOrm: DataBaseHelper = orm
    private val mContext: Context = orm.mContext


    fun addCardInfos(infos: List<CardInfo>) {
        for (info in infos) {
            this.createIfNotExists(info)
        }
    }

    fun delCardsById(list: List<Int>) {
        for (id in list) {
            this.deleteById(id)
        }
    }

    fun delCardInfoByGameId(gameId: Int): Int {
        val db = this.deleteBuilder()
        val where = db.where()
        try {
            where.eq(CardInfo.COLUMN_GAMETYPE, gameId)
            return this.delete(db.prepare())
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return -1
    }

    fun queryCards(cardinfo: CardInfo, orderByParam: String, isAsc: Boolean, position: Long, limit: Long): List<CardInfo>? {
        var orderBy = orderByParam
        var result: List<CardInfo>? = null
        val qb = this.queryBuilder()
        val showHeader = mContext.getSharedPreferences("commonset", Context.MODE_PRIVATE).getBoolean(BaseActivity.SHARE_SHOW_HEADER_IMAGES + cardinfo.gameId, false)
        if (orderBy == CardInfo.COLUMN_NID && !showHeader)
            orderBy = CardInfo.COLUMN_TOTAL
        qb.orderByRaw(orderBy + " " + if (isAsc) "ASC" else "DESC")
        qb.selectRaw("*," + CardInfo.COLUMN_MAXATTACK + "+" + CardInfo.COLUMN_MAXDEFENSE + "+" + CardInfo.COLUMN_MAXHP + "+" + CardInfo.COLUMN_EXTRA_VALUE1
                + "+" + CardInfo.COLUMN_EXTRA_VALUE2 + " " + CardInfo.COLUMN_TOTAL + ","
                + "( SELECT " + CardTypeInfo.COLUMN_NAME  + " FROM "  + CardTypeInfo.TABLE_NAME
                + " WHERE " +  CardTypeInfo.TABLE_NAME + "." + CardTypeInfo.ID + " = " + CardInfo.TABLE_NAME + "." + CardInfo.COLUMN_ATTR
                +  " ) " + CardInfo.COLUMN_ATTR_NAME )
        try {
            qb.limit(limit)
            qb.offset(position)

            val where = qb.where()
            where.eq(CardInfo.COLUMN_GAMETYPE, cardinfo.gameId)
            if (cardinfo.pinyinName != null)
                where.and().like(CardInfo.COLUMN_PINYIN_NAME, cardinfo.pinyinName!! + "%")
            if (cardinfo.name != null)
                where.and().like(CardInfo.COLUMN_NAME, "%" + cardinfo.name + "%")
            if (cardinfo.frontName != null)
                where.and().eq(CardInfo.COLUMN_FRONT_NAME, cardinfo.frontName)
            if (cardinfo.id > 0)
                where.and().eq(CardInfo.ID, cardinfo.id)
            if (cardinfo.nid > 0)
                where.and().eq(CardInfo.COLUMN_NID, cardinfo.nid)
            if (cardinfo.attrId > 0)
                where.and().eq(CardInfo.COLUMN_ATTR, cardinfo.attrId)
            if (cardinfo.level != null)
                where.and().eq(CardInfo.COLUMN_LEVEL, cardinfo.level)
            if (cardinfo.cost > -1)
                where.and().eq(CardInfo.COLUMN_COST, cardinfo.cost)
            if (cardinfo.eventId > 0) {
                where.and().raw("EXISTS ( SELECT * FROM  "
                        + CardEventInfo.TABLE_NAME
                        + " WHERE " + CardInfo.TABLE_NAME + "." + CardInfo.ID + " = "
                        + CardEventInfo.TABLE_NAME + "." + CardEventInfo.COLUMN_CARD_NID + " and "
                        + CardEventInfo.TABLE_NAME + "." + CardEventInfo.COLUMN_EVENT_ID + " = " + cardinfo.eventId + "  ) ")
            }
            result = this.queryRaw(qb.prepareStatementString(), CardInfoRowMapper()).results

            //计算总数
            val qbCount = this.queryBuilder()
            qbCount.setCountOf(true)
            qbCount.selectRaw("*")
            qbCount.setWhere(where)
            val count = this.countOf(qbCount.prepare()).toInt()

            // used as tiers
            if(orderBy == CardInfo.COLUMN_NID && showHeader && !isAsc){
                var index = 1
                result.forEach {
                    it.totalCount = count
                    if(it.nid > 0 )
                        it.nid = index++
                }
            }else{
                result.forEach {
                    it.totalCount = count
                    it.nid = 0
                }
            }


        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return result
    }

    private fun getAttrName(id: Int): String? {
        val result = this.mOrm.cardTypeInfoDao.queryForId(id)
        return if (result == null) "" else result.name
    }

    fun queryCardDropList(type: String, gametype: Int): MutableList<CardInfo> {
        var result: List<Array<String>> = ArrayList()
        val qb = this.queryBuilder()
        qb.selectRaw("DISTINCT ( $type) ")
        qb.selectRaw("COUNT (*)")
        qb.groupBy(type)
        try {
            qb.where().eq(CardInfo.COLUMN_GAMETYPE, gametype)
            result = qb.queryRaw().results
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        val cardInfos = mutableListOf<CardInfo>()
        for (row in result) {
            val cardInfo = CardInfo()
            if (type === CardInfo.COLUMN_ATTR) {
                cardInfo.name = getAttrName(Integer.parseInt(row[0]))
                cardInfo.attrId = Integer.parseInt(row[0])
            } else
                cardInfo.name = row[0]
            cardInfo.nid = Integer.parseInt(row[1])
            cardInfos.add(cardInfo)
        }
        return cardInfos
    }

    fun updateCardName(cardinfoNew: CardInfo, cardinfoOld: CardInfo): Long {
        val ub = this.updateBuilder()
        try {
            ub.updateColumnValue(CardInfo.COLUMN_NAME, cardinfoNew.name)
            ub.updateColumnValue(CardInfo.COLUMN_PINYIN_NAME, cardinfoNew.pinyinName)
            ub.where().eq(CardInfo.COLUMN_NAME, cardinfoOld.name)
            return this.update(ub.prepare()).toLong()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return -1
    }


    private class CardInfoRowMapper : RawRowMapper<CardInfo> {

        @Throws(SQLException::class)
        override fun mapRow(columnNames: Array<String>, resultColumns: Array<String>): CardInfo {
            val info = CardInfo()
            for (i in columnNames.indices) {
                when (columnNames[i]) {
                    CardInfo.ID -> info.id = Integer.parseInt(resultColumns[i])
                    CardInfo.COLUMN_ATTR -> info.attrId = Integer.parseInt(resultColumns[i])
                    CardInfo.COLUMN_COST -> info.cost = Integer.parseInt(resultColumns[i])
                    CardInfo.COLUMN_FRONT_NAME -> info.frontName = resultColumns[i]
                    CardInfo.COLUMN_GAMETYPE -> info.gameId = Integer.parseInt(resultColumns[i])
                    CardInfo.COLUMN_LEVEL -> info.level = resultColumns[i]
                    CardInfo.COLUMN_MAXATTACK -> info.maxAttack = resultColumns[i]
                    CardInfo.COLUMN_MAXDEFENSE -> info.maxDefense = resultColumns[i]
                    CardInfo.COLUMN_MAXHP -> info.maxHP = resultColumns[i]
                    CardInfo.COLUMN_NAME -> info.name = resultColumns[i]
                    CardInfo.COLUMN_NID -> info.nid = Integer.parseInt(resultColumns[i])
                    CardInfo.COLUMN_PINYIN_NAME -> info.pinyinName = resultColumns[i]
                    CardInfo.COLUMN_REMARK -> info.remark = resultColumns[i]
                    CardInfo.COLUMN_SHOW_HEAD -> info.profile = resultColumns[i]
                    CardInfo.COLUMN_EXTRA_VALUE1 -> info.extraValue1 = resultColumns[i]
                    CardInfo.COLUMN_EXTRA_VALUE2 -> info.extraValue2 = resultColumns[i]
                    CardInfo.COLUMN_ATTR_NAME  -> info.attr = resultColumns[i]
                }

            }
            return info
        }
    }

    companion object {

        private var mDao: Dao<CardInfo, Int>? = null

        private fun getDao(orm: DataBaseHelper): Dao<CardInfo, Int>? {
            if (mDao == null) {
                try {
                    mDao = orm.getDao(CardInfo::class.java)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }

            }
            return mDao
        }
    }

}
