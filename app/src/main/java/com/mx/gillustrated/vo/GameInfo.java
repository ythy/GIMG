package com.mx.gillustrated.vo;

public class GameInfo {
	
	private String name = null; //游戏名称
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
