package com.mx.gillustrated.vo;

import android.os.Parcel;
import android.os.Parcelable;

public class CardInfo extends SpinnerInfo implements Parcelable{
	
	private int id = -1;
	private int nid;
	private int gameId;
	private int eventId = -1;
	private String frontName = null;
	private String name = null;
	private String attr;
	private int attrId = -1;
	private String level;
	private int cost;
	private int maxHP;
	private int maxAttack;
	private int maxDefense;
	private String remark = null; //卡片备注

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getAttrId() {
		return attrId;
	}

	public void setAttrId(int attrId) {
		this.attrId = attrId;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}
	
	public String getFrontName() {
		return frontName;
	}

	public void setFrontName(String frontname) {
		this.frontName = frontname;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getMaxHP() {
		return maxHP;
	}

	public void setMaxHP(int maxHP) {
		this.maxHP = maxHP;
	}

	public int getMaxAttack() {
		return maxAttack;
	}

	public void setMaxAttack(int maxAttack) {
		this.maxAttack = maxAttack;
	}

	public int getMaxDefense() {
		return maxDefense;
	}

	public void setMaxDefense(int maxDefense) {
		this.maxDefense = maxDefense;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(attrId);
		dest.writeInt(gameId);
		dest.writeString(frontName);
		dest.writeString(name);
		dest.writeString(attr);
		dest.writeInt(nid);
		dest.writeString(level);
		dest.writeInt(cost);
		dest.writeInt(maxHP);
		dest.writeInt(maxAttack);
		dest.writeInt(maxDefense);
		dest.writeString(remark);
		dest.writeInt(eventId);
	}
	
	 public static final Parcelable.Creator<CardInfo> CREATOR = new Creator<CardInfo>()
	    {
	        @Override
	        public CardInfo[] newArray(int size)
	        {
	            return new CardInfo[size];
	        }
	        
	        @Override
	        public CardInfo createFromParcel(Parcel in)
	        {
	            return new CardInfo(in);
	        }
	    };
	    
	    public CardInfo(Parcel in)
	    {
	    	id = in.readInt();
	    	attrId = in.readInt();
	    	gameId = in.readInt();
	    	frontName = in.readString();
			name = in.readString();
			attr = in.readString();
			nid = in.readInt();
			level = in.readString();
			cost = in.readInt();
			maxHP = in.readInt();
			maxAttack = in.readInt();
			maxDefense = in.readInt();
			remark = in.readString();
			eventId = in.readInt();
	    }
	    
	    public CardInfo()
	    {
	    	
	    }

		public CardInfo(String name)
		{
			this.name = name;
		}

}
