package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = CardTypeInfo.TABLE_NAME)
public class CardTypeInfo extends SpinnerInfo  {

	public static final String TABLE_NAME = "card_type_info";
	public static final String ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_GAMETYPE = "game_type";

	@DatabaseField
	private String name = "";
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName=ID)
	private int id;
	@DatabaseField(columnName=COLUMN_GAMETYPE)
	private int gameId;
	
	public CardTypeInfo(int gid){
		this.gameId = gid;
	}
	
	public CardTypeInfo(){
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getGameId() {
		return gameId;
	}
	public void setGameId(int gameId) {
		this.gameId = gameId;
	}
	
}
