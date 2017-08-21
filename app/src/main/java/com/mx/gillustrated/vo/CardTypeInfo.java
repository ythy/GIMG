package com.mx.gillustrated.vo;

public class CardTypeInfo {
	private String name = ""; 
	private int id = -1;
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
