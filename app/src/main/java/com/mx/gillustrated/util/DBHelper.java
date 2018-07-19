package com.mx.gillustrated.util;


import java.util.ArrayList;
import java.util.List;

import com.mx.gillustrated.provider.Providerdata;
import com.mx.gillustrated.provider.Providerdata.Card;
import com.mx.gillustrated.provider.Providerdata.CardType;
import com.mx.gillustrated.provider.Providerdata.Game;
import com.mx.gillustrated.provider.Providerdata.Event;
import com.mx.gillustrated.provider.Providerdata.EventChain;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {   
    
    public DBHelper(Context context, String name,    
            CursorFactory factory, int version) {   
        super(context, name, factory, version);
        this.getWritableDatabase(); 
    }
    
    /**
     * should be invoke when you never use DBhelper
     * To release the database and etc.
     */
    public void Close() {
    	this.getWritableDatabase().close();
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {   
        db.execSQL("CREATE TABLE IF NOT EXISTS "    
                + Card.TABLE_NAME + " ("    
                + Card.ID + " INTEGER PRIMARY KEY,"    
                + Card.COLUMN_NID + " INTEGER,"  
                + Card.COLUMN_NAME + " VARCHAR," 
                + Card.COLUMN_FRONT_NAME + " VARCHAR," 
                + Card.COLUMN_LEVEL + " VARCHAR," 
                + Card.COLUMN_ATTR + " VARCHAR,"
                + Card.COLUMN_COST + " INTEGER," 
                + Card.COLUMN_GAMETYPE + " INTEGER,"
                + Card.COLUMN_EVENTTYPE + " INTEGER,"
				+ Card.COLUMN_REMARK + " VARCHAR,"
				+ Card.COLUMN_MAXHP + " INTEGER,"
                + Card.COLUMN_MAXATTACK + " INTEGER," 
                + Card.COLUMN_MAXDEFENSE + " INTEGER," 
                + Card.COLUMN_IMGUPDATE + " INTEGER DEFAULT 0)"); 
        
        db.execSQL("CREATE TABLE IF NOT EXISTS "    
                + Game.TABLE_NAME + " ("    
                + Game.ID + " INTEGER PRIMARY KEY,"   
                + Game.COLUMN_NAME + " VARCHAR )" ); 
        
        db.execSQL("CREATE TABLE IF NOT EXISTS "    
                + CardType.TABLE_NAME + " ("    
                + CardType.ID + " INTEGER PRIMARY KEY,"  
                + CardType.COLUMN_GAMETYPE + " INTEGER," 
                + CardType.COLUMN_NAME + " VARCHAR )" );

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ Event.TABLE_NAME + " ("
				+ Event.ID + " INTEGER PRIMARY KEY,"
				+ Event.COLUMN_GAMEID + " INTEGER, "
				+ Event.COLUMN_CONTENT + " VARCHAR, "
				+ Event.COLUMN_DURATION + " VARCHAR, "
				+ Event.COLUMN_SHOWING + " VARCHAR, "
				+ Event.COLUMN_NAME + " VARCHAR )" );

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + EventChain.TABLE_NAME + " ("
                + EventChain.COLUMN_CARD_NID + " INTEGER ,"
                + EventChain.COLUMN_EVENT_ID + " INTEGER, "
                + "PRIMARY KEY (" + EventChain.COLUMN_CARD_NID + " , "
                + EventChain.COLUMN_EVENT_ID + " )) ");
    }   
    
    @Override
    public void onUpgrade(SQLiteDatabase db,    
            int oldVersion, int newVersion) {   
    	if(newVersion == 2)
        {
			db.execSQL("ALTER TABLE " + Card.TABLE_NAME +
					" ADD " + Card.COLUMN_REMARK + " VARCHAR;");
		}
        else if(newVersion == 3)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + Event.TABLE_NAME + " ("
                    + Event.ID + " INTEGER PRIMARY KEY,"
                    + Event.COLUMN_GAMEID + " INTEGER, "
                    + Event.COLUMN_CONTENT + " VARCHAR, "
                    + Event.COLUMN_DURATION + " VARCHAR, "
                    + Event.COLUMN_NAME + " VARCHAR )" );
        }
        else if(newVersion == 4)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + Event.TABLE_NAME + " ("
                    + Event.ID + " INTEGER PRIMARY KEY,"
                    + Event.COLUMN_GAMEID + " INTEGER, "
                    + Event.COLUMN_CONTENT + " VARCHAR, "
                    + Event.COLUMN_DURATION + " VARCHAR, "
                    + Event.COLUMN_NAME + " VARCHAR )" );

            db.execSQL("ALTER TABLE " + Card.TABLE_NAME +
                    " ADD " + Card.COLUMN_EVENTTYPE + " INTEGER;");

        }
		else if(newVersion == 5)
		{
			db.execSQL("UPDATE " + Card.TABLE_NAME +
					" SET " + Card.COLUMN_FRONT_NAME + " =  '' "
			);
			db.execSQL("ALTER TABLE " + Event.TABLE_NAME +
					" ADD " + Event.COLUMN_SHOWING + " VARCHAR;");
		}
		else if(newVersion == 7)
		{
			db.execSQL("CREATE TABLE IF NOT EXISTS "
					+ EventChain.TABLE_NAME + " ("
					+ EventChain.COLUMN_CARD_NID + " INTEGER ,"
					+ EventChain.COLUMN_EVENT_ID + " INTEGER, "
					+ "PRIMARY KEY (" + EventChain.COLUMN_CARD_NID + " , "
					+ EventChain.COLUMN_EVENT_ID + " )) ");


            db.execSQL(
                    "UPDATE " + Card.TABLE_NAME +
                            " SET " + Card.COLUMN_ATTR + "= ( SELECT " + CardType._ID + " FROM " + CardType.TABLE_NAME +
                            " WHERE " + CardType.COLUMN_NAME + " = " + Card.COLUMN_ATTR + " ) ");
        }
    }
    
    
    public List<CardInfo> queryCards(CardInfo cardinfo, String orderBy, int gametype) {
    	List<CardInfo> infos = new ArrayList<CardInfo>();
    	String[] selectionArg = null;
    	String sql = "SELECT * FROM " + Card.TABLE_NAME;
    	String sqlWhere = "";
    	String selectionValue = "";
    	if(gametype > -1){
    		sqlWhere += Card.COLUMN_GAMETYPE + "=? ";
        	selectionValue += String.valueOf(gametype) + ",";
    	}
    	if(cardinfo != null)
    	{
    		if(cardinfo.getName() != null)
        	{
    			if(!sqlWhere.equals(""))
    				sqlWhere += " and ";
    			sqlWhere += Card.COLUMN_NAME + " like ? ";
        		selectionValue += "%" + cardinfo.getName() + "%,";
        	}
			if(cardinfo.getFrontName() != null)
			{
				if(!sqlWhere.equals(""))
					sqlWhere += " and ";
				sqlWhere += Card.COLUMN_FRONT_NAME + " like ? ";
				selectionValue += "%" + cardinfo.getFrontName() + "%,";
			}
    		if(cardinfo.getId() > -1 )
        	{
        		if(!sqlWhere.equals(""))
    				sqlWhere += " and ";
        		sqlWhere += Card.ID + "=? ";
        		selectionValue += String.valueOf(cardinfo.getId()) + ",";
        	}
        	if(cardinfo.getNid() != 0)
        	{
        		if(!sqlWhere.equals(""))
    				sqlWhere += " and ";
        		sqlWhere += Card.COLUMN_NID + "=? ";
        		selectionValue += String.valueOf(cardinfo.getNid()) + ",";
        	}
        	if(cardinfo.getAttrId() > -1)
        	{
        		if(!sqlWhere.equals(""))
    				sqlWhere += " and ";
        		sqlWhere += Card.COLUMN_ATTR + "=? ";
        		selectionValue += String.valueOf(cardinfo.getAttrId()) + ",";
        	}
        	if(cardinfo.getLevel() != null)
        	{
        		if(!sqlWhere.equals(""))
    				sqlWhere += " and ";
        		sqlWhere += Card.COLUMN_LEVEL + "=? ";
        		selectionValue += String.valueOf(cardinfo.getLevel()) + ",";
        	}
        	if(cardinfo.getCost() != 0)
        	{
        		if(!sqlWhere.equals(""))
    				sqlWhere += " and ";
        		sqlWhere += Card.COLUMN_COST + "=? ";
        		selectionValue += String.valueOf(cardinfo.getCost()) + ",";
        	}if(cardinfo.getEventId() > -1)
			{
				if(!sqlWhere.equals(""))
					sqlWhere += " and ";
				sqlWhere += "EXISTS ( SELECT * FROM  "
						+ EventChain.TABLE_NAME
						+ " WHERE " + Card.TABLE_NAME + "." + Card._ID + " = "
						+ EventChain.TABLE_NAME + "." + EventChain.COLUMN_CARD_NID + " and "
						+ EventChain.TABLE_NAME + "." + EventChain.COLUMN_EVENT_ID + " = ? ) ";

				selectionValue += String.valueOf(cardinfo.getEventId()) + ",";
			}
    	}
    	if(!selectionValue.equals(""))
    		selectionArg = selectionValue.split(",");
    	Cursor cusor = this.getWritableDatabase().rawQuery(sql + (selectionArg == null ? 
    			"" : " WHERE " + sqlWhere) + " order by " + orderBy, selectionArg);
		if (cusor != null) {
			while (cusor.moveToNext()) {
				CardInfo cardInfo = new CardInfo();
				cardInfo.setId(cusor.getInt(cusor.getColumnIndex(Card.ID)));
				cardInfo.setNid(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_NID)));
				cardInfo.setName(cusor.getString(cusor.getColumnIndex(Card.COLUMN_NAME)));
				cardInfo.setFrontName(cusor.getString(cusor.getColumnIndex(Card.COLUMN_FRONT_NAME)));
				cardInfo.setAttr(queryCardTypeName(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_ATTR)),gametype));
                cardInfo.setAttrId(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_ATTR)));
				cardInfo.setLevel(cusor.getString(cusor.getColumnIndex(Card.COLUMN_LEVEL)));
				cardInfo.setCost(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_COST)));
				cardInfo.setGameId(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_GAMETYPE)));
				cardInfo.setMaxHP(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_MAXHP)));
				cardInfo.setMaxAttack(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_MAXATTACK)));
				cardInfo.setMaxDefense(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_MAXDEFENSE)));
                cardInfo.setRemark(cusor.getString(cusor.getColumnIndex(Card.COLUMN_REMARK)));
                cardInfo.setEventId(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_EVENTTYPE)));
				infos.add(cardInfo);
			}
			cusor.close();
		}
		return infos;
    }
    
    public CardInfo queryCardSide( CardInfo searchinfo, int gameid, int position, String order ){
    	CardInfo cardInfo = null;
    	String[] selectionArg = null;
    	String sql = "SELECT * FROM " + Card.TABLE_NAME;
    	String sqlWhere = Card.COLUMN_GAMETYPE + "=? ";
    	String selectionValue = String.valueOf(gameid) + ",";
		
    	if(searchinfo != null)
    	{
    		if(searchinfo.getName() != null)
        	{
    			if(!sqlWhere.equals(""))
    				sqlWhere += " and ";
    			sqlWhere += Card.COLUMN_NAME + " like ? ";
        		selectionValue += "%" + searchinfo.getName() + "%,";
        	}
        	if(searchinfo.getAttrId() > -1)
        	{
        		if(!sqlWhere.equals(""))
    				sqlWhere += " and ";
        		sqlWhere += Card.COLUMN_ATTR + "=? ";
        		selectionValue += String.valueOf(searchinfo.getAttrId()) + ",";
        	}
        	if(searchinfo.getCost() != 0)
        	{
        		if(!sqlWhere.equals(""))
    				sqlWhere += " and ";
        		sqlWhere += Card.COLUMN_COST + "=? ";
        		selectionValue += String.valueOf(searchinfo.getCost()) + ",";
        	}
    	}

    	if(!selectionValue.equals(""))
    		selectionArg = selectionValue.split(",");
    	
    	Cursor cusor = this.getWritableDatabase().rawQuery(sql + (selectionArg == null ? 
    			"" : " WHERE " + sqlWhere) + " order by " + order + " limit " + (position)  + ",1", selectionArg);
    	
    	if (cusor != null) {
			while (cusor.moveToNext()) {
				cardInfo = new CardInfo();
				cardInfo.setId(cusor.getInt(cusor.getColumnIndex(Card.ID)));
				cardInfo.setNid(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_NID)));
				cardInfo.setName(cusor.getString(cusor.getColumnIndex(Card.COLUMN_NAME)));
				cardInfo.setFrontName(cusor.getString(cusor.getColumnIndex(Card.COLUMN_FRONT_NAME)));
                cardInfo.setAttr(queryCardTypeName(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_ATTR)),gameid));
                cardInfo.setAttrId(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_ATTR)));
				cardInfo.setLevel(cusor.getString(cusor.getColumnIndex(Card.COLUMN_LEVEL)));
				cardInfo.setCost(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_COST)));
				cardInfo.setGameId(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_GAMETYPE)));
				cardInfo.setMaxHP(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_MAXHP)));
				cardInfo.setMaxAttack(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_MAXATTACK)));
				cardInfo.setMaxDefense(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_MAXDEFENSE)));
                cardInfo.setRemark(cusor.getString(cusor.getColumnIndex(Card.COLUMN_REMARK)));
                cardInfo.setEventId(cusor.getInt(cusor.getColumnIndex(Card.COLUMN_EVENTTYPE)));
			}
			cusor.close();
		}
    	return cardInfo;
    }
    
    public List<CardInfo> queryCardDropList(String Type, int gametype) {
    	String sql = "SELECT distinct("+ Type + "), count(*) FROM " + Card.TABLE_NAME +
    			" WHERE " + Card.COLUMN_GAMETYPE + "=?"  + " GROUP BY " + Type;
    	Cursor cusor = this.getWritableDatabase().rawQuery(sql, new String[]{ String.valueOf(gametype)} );
        List<CardInfo> infos = new ArrayList();
    	int i = 0;
		if (cusor != null) {
			while (cusor.moveToNext()) {
				CardInfo cardInfo = new CardInfo();
                if(Type == Card.COLUMN_ATTR ) {
                    cardInfo.setName(queryCardTypeName(cusor.getInt(0), gametype));
                    cardInfo.setAttrId(cusor.getInt(0));
                }
                 else
				    cardInfo.setName(cusor.getString(0));
				cardInfo.setNid(cusor.getInt(1));
                infos.add(cardInfo);
			}
			cusor.close();
		}
		return infos;
    }
    

    public void addAllCardInfo(List<CardInfo> list){ 
    	for(int i = 0; i < list.size(); i++)
    	{
    		addCardInfo(list.get(i));  
    	}
    }
    
    public int updateCardInfo(CardInfo cardinfo)
    {
    	String selection = Card.ID + "=?";
    	String[] selectionArg = new String[] {String.valueOf(cardinfo.getId())};
		ContentValues values = new ContentValues(); 
		if(cardinfo.getName() != null)
			values.put(Card.COLUMN_NAME, cardinfo.getName()); 
		if(cardinfo.getFrontName() != null)
			values.put(Card.COLUMN_FRONT_NAME, cardinfo.getFrontName());
        if(cardinfo.getRemark() != null)
            values.put(Card.COLUMN_REMARK, cardinfo.getRemark());
        if(cardinfo.getMaxHP() > 0)
			values.put(Card.COLUMN_MAXHP, cardinfo.getMaxHP());
		if(cardinfo.getMaxAttack() > 0)
			values.put(Card.COLUMN_MAXATTACK, cardinfo.getMaxAttack());
		if(cardinfo.getMaxDefense() > 0)
			values.put(Card.COLUMN_MAXDEFENSE, cardinfo.getMaxDefense());
		if(cardinfo.getAttrId() > -1)
			values.put(Card.COLUMN_ATTR, cardinfo.getAttrId());
		if(cardinfo.getLevel() != null)
			values.put(Card.COLUMN_LEVEL, cardinfo.getLevel());
		if(cardinfo.getCost() > -1)
			values.put(Card.COLUMN_COST, cardinfo.getCost());
		if(cardinfo.getNid() > 0)
			values.put(Card.COLUMN_NID, cardinfo.getNid());
        if(cardinfo.getEventId() > 0)
            values.put(Card.COLUMN_EVENTTYPE, cardinfo.getEventId());

		return this.getWritableDatabase().update(Card.TABLE_NAME, values, selection, selectionArg);
    }
    
    public long addCardInfo(CardInfo cardinfo)
    {
//    	CardInfo card = new CardInfo();
//    	card.setNid(cardinfo.getNid());
//    	List<CardInfo> list = queryCards(card, null);
//    	if(list.size() > 0)
//    		return -1;
	    
    	ContentValues values = new ContentValues();   
    	if(cardinfo.getId() > -1)
    		values.put(Card.ID, cardinfo.getId());   
		values.put(Card.COLUMN_NID, cardinfo.getNid());
        values.put(Card.COLUMN_EVENTTYPE, cardinfo.getEventId());
        values.put(Card.COLUMN_NAME, cardinfo.getName());
		values.put(Card.COLUMN_FRONT_NAME, cardinfo.getFrontName());
        values.put(Card.COLUMN_REMARK, cardinfo.getRemark());
        values.put(Card.COLUMN_GAMETYPE, cardinfo.getGameId());
		values.put(Card.COLUMN_LEVEL, cardinfo.getLevel());   
		values.put(Card.COLUMN_ATTR, cardinfo.getAttrId());
		values.put(Card.COLUMN_COST, cardinfo.getCost());   
		values.put(Card.COLUMN_MAXHP, cardinfo.getMaxHP());   
		values.put(Card.COLUMN_MAXATTACK, cardinfo.getMaxAttack());   
		values.put(Card.COLUMN_MAXDEFENSE, cardinfo.getMaxDefense());  
	    return this.getWritableDatabase().insert(Card.TABLE_NAME, null, values);  
    }
    
    public long delCardInfo(CardInfo cardinfo)
    {
	    String selection = Card.ID + "=?";
    	String[] selectionArg = new String[] {String.valueOf(cardinfo.getId())};
		return this.getWritableDatabase().delete(Card.TABLE_NAME, selection, selectionArg);
    }
    
    public long updateCardName(CardInfo cardinfoNew, CardInfo cardinfoOld)
    {
	    String selection = Card.COLUMN_NAME + "=?";
    	String[] selectionArg = new String[] {String.valueOf(cardinfoOld.getName())};
    	ContentValues values = new ContentValues(); 
		values.put(Card.COLUMN_NAME, cardinfoNew.getName()); 
		return this.getWritableDatabase().update(Card.TABLE_NAME, values, selection, selectionArg);
    }
    
    public long addGameName(GameInfo info)
    {
    	ContentValues values = new ContentValues();  
    	if(info.getId() > -1)
    		values.put(Game.ID, info.getId());   
		values.put(Game.COLUMN_NAME, info.getName()); 
	    return this.getWritableDatabase().insert(Game.TABLE_NAME, null, values);  
    }
    
    public void addAllGameNameInfo(List<GameInfo> list){ 
    	for(int i = 0; i < list.size(); i++)
    	{
    		addGameName(list.get(i)); 
    	}
    }
    
    public long updateGameName(GameInfo despairInfo)
    {
    	int id = despairInfo.getId();
    	if( id == -1 )
    		return addGameName(despairInfo);
	    String selection = Game.ID + "=?";
    	String[] selectionArg = new String[] {String.valueOf(id)};
    	ContentValues values = new ContentValues(); 
		values.put(Game.COLUMN_NAME, despairInfo.getName()); 
		return this.getWritableDatabase().update(Game.TABLE_NAME, values, selection, selectionArg);
    }
    
    public List<GameInfo> queryGameList(GameInfo gameinfo) {
    	List<GameInfo> infos = new ArrayList<GameInfo>();
    	String[] selectionArg = null;
    	String sql = "SELECT * FROM " + Game.TABLE_NAME;
    	if(gameinfo != null ){
    		sql += " WHERE " +  Game.ID + "=? ";
    		selectionArg = new String[]{ String.valueOf(gameinfo.getId()) };
    	}	

    	Cursor cusor = this.getWritableDatabase().rawQuery(sql, selectionArg);
		if (cusor != null) {
			while (cusor.moveToNext()) {
				GameInfo despairInfo = new GameInfo();
				despairInfo.setId(cusor.getInt(cusor.getColumnIndex(Game.ID)));
				despairInfo.setName(cusor.getString(cusor.getColumnIndex(Game.COLUMN_NAME)));
				infos.add(despairInfo);
			}
			cusor.close();
		}
		 
		return infos;
    }
    
    private long addCardType(CardTypeInfo info)
    {
    	ContentValues values = new ContentValues(); 
    	if(info.getId() > -1)
    		values.put(CardType.ID, info.getId());   
		values.put(CardType.COLUMN_NAME, info.getName()); 
		values.put(CardType.COLUMN_GAMETYPE, info.getGameId()); 
	    return this.getWritableDatabase().insert(CardType.TABLE_NAME, null, values);  
    }

    //查询卡片属性ID 对应的名称
    private String queryCardTypeName(int id, int gameId)
    {
        String sql = "SELECT * FROM " + CardType.TABLE_NAME +
         " WHERE " +  CardType.ID + "=? AND " + CardType.COLUMN_GAMETYPE + "=?";
        String[] selectionArg = new String[]{ String.valueOf(id), String.valueOf(gameId) };

        Cursor cusor = this.getWritableDatabase().rawQuery(sql, selectionArg);
        if (cusor != null) {
            while (cusor.moveToNext()) {
                return cusor.getString(cusor.getColumnIndex(CardType.COLUMN_NAME));
            }
            cusor.close();
        }
        return null;
    }

    public void addAlCardTypeInfo(List<CardTypeInfo> list){ 
    	for(int i = 0; i < list.size(); i++)
    	{
    		addCardType(list.get(i));
    	}
    }
    
    public long updateCardType(CardTypeInfo info)
    {
    	int id = info.getId();
    	if( id < 0 )
    		return addCardType(info);
	    String selection = CardType.ID + "=?";
    	String[] selectionArg = new String[] {String.valueOf(id)};
    	ContentValues values = new ContentValues(); 
		values.put(CardType.COLUMN_NAME, info.getName()); 	
		return this.getWritableDatabase().update(CardType.TABLE_NAME, values, selection, selectionArg);
    }

    
    public List<CardTypeInfo> queryCardTypeList( int gametype ) {
    	List<CardTypeInfo> infos = new ArrayList<CardTypeInfo>();
    	String[] selectionArg = null;
    	String sql = "SELECT * FROM " + CardType.TABLE_NAME;
    	if(gametype > -1 ){
    		sql += " WHERE " +  CardType.COLUMN_GAMETYPE + "=? ";
    		selectionArg = new String[]{ String.valueOf(gametype) };
    	}	

    	Cursor cusor = this.getWritableDatabase().rawQuery(sql, selectionArg);
		if (cusor != null) {
			while (cusor.moveToNext()) {
				CardTypeInfo info = new CardTypeInfo();
				info.setId(cusor.getInt(cusor.getColumnIndex(CardType.ID)));
				info.setGameId(cusor.getInt(cusor.getColumnIndex(CardType.COLUMN_GAMETYPE)));
				info.setName(cusor.getString(cusor.getColumnIndex(CardType.COLUMN_NAME)));
				infos.add(info);
			}
			cusor.close();
		}
		return infos;
    }


    public List<EventInfo> queryEventList(EventInfo eventinfo) {
        List<EventInfo> infos = new ArrayList<EventInfo>();
        String[] selectionArg = null;
		String selectionValue = "";
        String sql = "SELECT * FROM " + Event.TABLE_NAME;
        if(eventinfo != null ){
            sql += " WHERE " +  Event.COLUMN_GAMEID + "=? ";
            selectionValue += String.valueOf(eventinfo.getGameId()) + ",";
            if("Y".equals(eventinfo.getShowing())){
                sql += " and " + Event.COLUMN_SHOWING + "=? ";
                selectionValue += String.valueOf(eventinfo.getShowing()) + ",";
            }
        }
		sql +=  " order by " + Event._ID + " " + Event.SORT_DESC ;
		if(!selectionValue.equals(""))
        	selectionArg = selectionValue.split(",");
        Cursor cusor = this.getWritableDatabase().rawQuery(sql, selectionArg);
        if (cusor != null) {
            while (cusor.moveToNext()) {
                EventInfo despairInfo = new EventInfo();
                despairInfo.setId(cusor.getInt(cusor.getColumnIndex(Event.ID)));
                despairInfo.setName(cusor.getString(cusor.getColumnIndex(Event.COLUMN_NAME)));
                despairInfo.setContent(cusor.getString(cusor.getColumnIndex(Event.COLUMN_CONTENT)));
                despairInfo.setDuration(cusor.getString(cusor.getColumnIndex(Event.COLUMN_DURATION)));
                despairInfo.setGameId(cusor.getInt(cusor.getColumnIndex(Event.COLUMN_GAMEID)));
				despairInfo.setShowing(cusor.getString(cusor.getColumnIndex(Event.COLUMN_SHOWING)));
                infos.add(despairInfo);
            }
            cusor.close();
        }

        return infos;
    }

    public long updateEvent(EventInfo despairInfo)
    {
        int id = despairInfo.getId();
        if( id == -1 )
            return addEvent(despairInfo);
        String selection = Event.ID + "=?";
        String[] selectionArg = new String[] {String.valueOf(id)};
        ContentValues values = new ContentValues();
        values.put(Event.COLUMN_NAME, despairInfo.getName());
        values.put(Event.COLUMN_DURATION, despairInfo.getDuration());
        values.put(Event.COLUMN_CONTENT, despairInfo.getContent());
		values.put(Event.COLUMN_SHOWING, despairInfo.getShowing());
        return this.getWritableDatabase().update(Event.TABLE_NAME, values, selection, selectionArg);
    }

    public long addEvent(EventInfo info)
    {
        ContentValues values = new ContentValues();
		if(info.getId() > -1)
			values.put(Event.ID, info.getId());
		values.put(Event.COLUMN_NAME, info.getName());
        values.put(Event.COLUMN_GAMEID, info.getGameId());
		values.put(Event.COLUMN_DURATION, info.getDuration());
		values.put(Event.COLUMN_CONTENT, info.getContent());
		values.put(Event.COLUMN_SHOWING, info.getShowing());
        return this.getWritableDatabase().insert(Event.TABLE_NAME, null, values);
    }

	public void addAllEvents(List<EventInfo> list){
		for(int i = 0; i < list.size(); i++)
		{
			addEvent(list.get(i));
		}
	}

    public EventInfo queryEvent(EventInfo eventinfo) {
        EventInfo result = new EventInfo();
        String[] selectionArg = null;
        String sql = "SELECT * FROM " + Event.TABLE_NAME;
        if(eventinfo != null ){
            sql += " WHERE " +  Event.ID + "=? ";
            selectionArg = new String[]{ String.valueOf(eventinfo.getId()) };
        }
        Cursor cusor = this.getWritableDatabase().rawQuery(sql, selectionArg);
        if (cusor != null) {
            while (cusor.moveToNext()) {
                result.setId(cusor.getInt(cusor.getColumnIndex(Event.ID)));
                result.setName(cusor.getString(cusor.getColumnIndex(Event.COLUMN_NAME)));
                result.setContent(cusor.getString(cusor.getColumnIndex(Event.COLUMN_CONTENT)));
                result.setDuration(cusor.getString(cusor.getColumnIndex(Event.COLUMN_DURATION)));
                result.setGameId(cusor.getInt(cusor.getColumnIndex(Event.COLUMN_GAMEID)));
				result.setShowing(cusor.getString(cusor.getColumnIndex(Event.COLUMN_SHOWING)));
            }
            cusor.close();
        }
        return result;
    }

	public long delEventInfo(EventInfo eventInfo)
	{
		String selection = Event.ID + "=?";
		String[] selectionArg = new String[] {String.valueOf(eventInfo.getId())};
		return this.getWritableDatabase().delete(Event.TABLE_NAME, selection, selectionArg);
	}

	// 卡片和活动关联 开始

	public long setCardEvent( int nid, int[] events ) {
		for( int i = 0; i < events.length; i++ ){
			addCardEvent( nid, events[i] );
		}
		return 0;
	}

	private boolean checkCardDespairIsExisting( int nid, int id   ){
		String selection = EventChain.COLUMN_CARD_NID + "=?  AND " +  EventChain.COLUMN_EVENT_ID + "=? ";
		String[] selectionArg = new String[] {String.valueOf(nid), String.valueOf(id)};
		Cursor cusor = this.getWritableDatabase().query(EventChain.TABLE_NAME, null, selection, selectionArg, null, null, null);
		if(cusor.getCount() > 0)
			return true;
		else
			return false;
	}

	private long addCardEvent( int nid, int id ){
		if(checkCardDespairIsExisting(nid, id) || id == -1)
			return -1;
		ContentValues values = new ContentValues();
		values.put(EventChain.COLUMN_CARD_NID, nid);
		values.put(EventChain.COLUMN_EVENT_ID,id);
		return this.getWritableDatabase().insert(EventChain.TABLE_NAME, null, values);
	}

	public void addAllCardEvent(List<Integer[]> list){
		for(int i = 0; i < list.size(); i++)
		{
			addCardEvent(list.get(i)[0], list.get(i)[1]);
		}
	}

	public long delCardEvent( int nid, int id )
	{
		String selection = EventChain.COLUMN_CARD_NID + "=? and "
				+ EventChain.COLUMN_EVENT_ID + "=?";
		String[] selectionArg = new String[] {String.valueOf(nid), String.valueOf(id)};
		return this.getWritableDatabase().delete(EventChain.TABLE_NAME, selection, selectionArg);
	}

	/**
	 * get card`s event name
	 * @param nid
	 * @return
	 */
	public List<Integer> queryCardEvents( int nid ) {
		String selection = EventChain.COLUMN_CARD_NID + "=?";
		String[] selectionArg = new String[] {String.valueOf(nid)};
		Cursor cusor = this.getWritableDatabase().query(EventChain.TABLE_NAME, null, selection, selectionArg, null, null, null);
		List<Integer> eventlist = new  ArrayList<Integer>();
		if (cusor != null) {
			while (cusor.moveToNext()) {
				eventlist.add(cusor.getInt(cusor.getColumnIndex(EventChain.COLUMN_EVENT_ID)));
			}
			cusor.close();
		}
		return eventlist;
	}

	public List<Integer[]> queryAllCardEvents() {
		Cursor cusor = this.getWritableDatabase().query(EventChain.TABLE_NAME, null, null, null, null, null, null);
		List<Integer[]> eventlist = new  ArrayList<Integer[]>();
		if (cusor != null) {
			while (cusor.moveToNext()) {
				Integer[] array = new Integer[2];
				array[0] = cusor.getInt(cusor.getColumnIndex(EventChain.COLUMN_CARD_NID));
				array[1] = cusor.getInt(cusor.getColumnIndex(EventChain.COLUMN_EVENT_ID));
				eventlist.add(array);
			}
			cusor.close();
		}
		return eventlist;
	}
	// 卡片和活动关联 结束
} 