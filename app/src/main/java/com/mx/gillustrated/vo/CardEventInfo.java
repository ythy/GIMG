package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by maoxin on 2018/7/23.
 */

@DatabaseTable(tableName = "event_chain")
public class CardEventInfo {

    @DatabaseField(uniqueCombo = true)
    private int cardNid = -1;

    @DatabaseField(uniqueCombo = true)
    private int eventId = -1;

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
