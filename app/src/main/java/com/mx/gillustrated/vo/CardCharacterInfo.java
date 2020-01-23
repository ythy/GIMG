package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = CardCharacterInfo.TABLE_NAME)
public class CardCharacterInfo {

    public static final String TABLE_NAME = "character_chain";
    public static final String COLUMN_CARD_NID = "cardNid";
    public static final String COLUMN_CHARACTER_ID = "charId";

    @DatabaseField(uniqueCombo = true)
    private int cardNid;

    @DatabaseField(uniqueCombo = true)
    private int charId;

    public CardCharacterInfo(){
    }

    public CardCharacterInfo(int cardNid, int charId){
        this.cardNid = cardNid;
        this.charId = charId;
    }

    public int getCardNid() {
        return cardNid;
    }

    public void setCardNid(int cardNid) {
        this.cardNid = cardNid;
    }

    public int getCharId() {
        return charId;
    }

    public void setCharId(int charId) {
        this.charId = charId;
    }
}
