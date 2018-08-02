package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = GameInfo.TABLE_NAME)
public class GameInfo extends SpinnerInfo {

	public static final String TABLE_NAME = "game_info";
	public static final String ID = "_id";
	public static final String COLUMN_NAME = "name";

	@DatabaseField
	private String name = null; //游戏名称

	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName=ID)
	private int id;
	
	public GameInfo(){
		
	}
	
	public GameInfo( int id){
		this.id = id;
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
	
	
	
}
