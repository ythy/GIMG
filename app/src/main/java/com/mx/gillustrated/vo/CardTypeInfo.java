package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "card_type_info")
public class CardTypeInfo extends SpinnerInfo  {

	@DatabaseField
	private String name = "";
	@DatabaseField(id = true, columnName="_id")
	private int id = -1;
	@DatabaseField(columnName="game_type")
	private int gameId = -1;
	
	public CardTypeInfo(int id, int gid){
		this.id = id;
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
