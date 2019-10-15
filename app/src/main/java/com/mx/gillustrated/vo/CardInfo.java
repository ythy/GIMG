package com.mx.gillustrated.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = CardInfo.TABLE_NAME)
public class CardInfo extends SpinnerInfo implements Parcelable{

	public static final String TABLE_NAME = "card_info";
	public static final String SORT_DESC = "DESC ";
	public static final String SORT_ASC = "ASC ";

	public static final String ID = "_id";
	public static final String COLUMN_NID = "nid";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_FRONT_NAME = "front_name";
	public static final String COLUMN_LEVEL = "level";
	public static final String COLUMN_ATTR= "attr";
	public static final String COLUMN_COST = "cost";
	public static final String COLUMN_MAXHP = "max_hp";
	public static final String COLUMN_MAXATTACK = "max_attack";
	public static final String COLUMN_MAXDEFENSE = "max_defense";
	public static final String COLUMN_GAMETYPE = "game_type";
	public static final String COLUMN_IMGUPDATE = "img_update";
	public static final String COLUMN_REMARK = "remark";
	public static final String COLUMN_EVENTTYPE = "event_type";
	public static final String COLUMN_PINYIN_NAME = "pinyin_name";
	public static final String COLUMN_SHOW_HEAD = "has_profile";

	public static final String COLUMN_TOTAL = "total_number";

	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName=ID)
	private int id;
	@DatabaseField
	private int nid;
	@DatabaseField(columnName = COLUMN_GAMETYPE)
	private int gameId;
	@DatabaseField(columnName = COLUMN_EVENTTYPE)
	private int eventId;
	@DatabaseField(columnName = COLUMN_FRONT_NAME)
	private String frontName = null;
	@DatabaseField
	private String name = null;
	private String attr;
	@DatabaseField(columnName = COLUMN_ATTR)
	private int attrId;
	@DatabaseField
	private String level;
	@DatabaseField
	private int cost = -1;
	@DatabaseField(columnName = COLUMN_MAXHP, defaultValue = "")
	private String maxHP;
	@DatabaseField(columnName = COLUMN_MAXATTACK, defaultValue = "")
	private String maxAttack;
	@DatabaseField(columnName = COLUMN_MAXDEFENSE, defaultValue = "")
	private String maxDefense;
	@DatabaseField(columnName = COLUMN_IMGUPDATE, defaultValue = "0")
	private int imageUpdate;
	@DatabaseField
	private String remark = null; //卡片备注
	@DatabaseField(columnName = COLUMN_PINYIN_NAME)
	private String pinyinName;
	@DatabaseField(columnName = COLUMN_SHOW_HEAD)
	private String profile; //是否有头像

	private int totalCount;

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public String getPinyinName() {
		return pinyinName;
	}

	public void setPinyinName(String pinyinName) {
		this.pinyinName = pinyinName;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getRemark() {
		return remark;
	}

	public int getImageUpdate() {
		return imageUpdate;
	}

	public void setImageUpdate(int imageUpdate) {
		this.imageUpdate = imageUpdate;
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

	public String getMaxHP() {
		return maxHP;
	}

	public void setMaxHP(String maxHP) {
		this.maxHP = maxHP;
	}

	public String getMaxAttack() {
		return maxAttack;
	}

	public void setMaxAttack(String maxAttack) {
		this.maxAttack = maxAttack;
	}

	public String getMaxDefense() {
		return maxDefense;
	}

	public void setMaxDefense(String maxDefense) {
		this.maxDefense = maxDefense;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
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
		dest.writeString(maxHP);
		dest.writeString(maxAttack);
		dest.writeString(maxDefense);
		dest.writeString(remark);
		dest.writeInt(eventId);
		dest.writeInt(imageUpdate);
		dest.writeString(profile);
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
			maxHP = in.readString();
			maxAttack = in.readString();
			maxDefense = in.readString();
			remark = in.readString();
			eventId = in.readInt();
			imageUpdate = in.readInt();
			profile = in.readString();
	    }
	    
	    public CardInfo()
	    {
	    	
	    }

		public CardInfo(String name)
		{
			this.name = name;
		}

		public CardInfo(String[] searchParams){
			this.name = searchParams[0];
			this.cost = Integer.parseInt(searchParams[1]);
			this.attrId = Integer.parseInt(searchParams[2]);
			this.eventId = Integer.parseInt(searchParams[3]);
			this.gameId = Integer.parseInt(searchParams[4]);
			this.frontName = searchParams[5];
			this.level = searchParams[6];
		}

		public String[] getCardSearchParam(){
			return new String[]{
				this.name, String.valueOf(this.cost),
					String.valueOf(this.attrId),
					String.valueOf(this.eventId),
					String.valueOf(this.gameId),
					this.frontName,
					this.level,
			};
		}

}
