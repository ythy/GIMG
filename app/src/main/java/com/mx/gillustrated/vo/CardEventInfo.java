package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by maoxin on 2018/7/23.
 */

@DatabaseTable(tableName = CardEventInfo.TABLE_NAME)
public class CardEventInfo {

    public static final String TABLE_NAME = "event_chain";
    public static final String COLUMN_CARD_NID = "cardNid";
    public static final String COLUMN_EVENT_ID = "eventId";

    @DatabaseField(uniqueCombo = true)
    private int cardNid;

    @DatabaseField(uniqueCombo = true)
    private int eventId;

    public CardEventInfo(){
    }

    public CardEventInfo(int cardNid, int eventId){
        this.cardNid = cardNid;
        this.eventId = eventId;
    }

    public int getCardNid() {
        return cardNid;
    }

    public void setCardNid(int cardNid) {
        this.cardNid = cardNid;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}
