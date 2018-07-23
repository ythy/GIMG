package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "game_info")
public class GameInfo {

	@DatabaseField
	private String name = null; //游戏名称

	@DatabaseField(id = true, columnName="_id")
	private int id = -1;
	
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
