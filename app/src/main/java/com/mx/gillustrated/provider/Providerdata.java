package com.mx.gillustrated.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Providerdata {

    public static final String AUTHORITY="com.mx.gillustrated.provider.csprovider"; 

    //数据库名称 
    public static final String DATABASE_NAME = "GIMG.db"; 
    
    /**
     * 数据库的版本
     * 版本2 增加Remark
     */
    public static final int DATABASE_VERSION = 7;
    
    
    public static final class Card implements BaseColumns{

       //表名
       public static final String TABLE_NAME = "card_info";

       //访问该ContentProvider的URI
       public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/" + TABLE_NAME);
       
       //新增mimeType  vnd.android.cursor.dir/开头返回多条数据    vnd.android.cursor.item/开头返回单条数据
       public static final String CONTENT_TYPE="vnd.android.cursor.dir/vnd.mx.Card"; 
       public static final String CONTENT_TYPE_ITEM="vnd.android.cursor.item/vnd.mx.Card";

       //列名
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
       public static final String SORT_DESC = " DESC";
       public static final String SORT_ASC = " ASC";
    }
    
    public static final class Game implements BaseColumns{

        //表名
        public static final String TABLE_NAME = "game_info";

        //访问该ContentProvider的URI
        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/" + TABLE_NAME);
        
        //新增mimeType  vnd.android.cursor.dir/开头返回多条数据    vnd.android.cursor.item/开头返回单条数据
        public static final String CONTENT_TYPE="vnd.android.cursor.dir/vnd.mx.Game"; 
        public static final String CONTENT_TYPE_ITEM="vnd.android.cursor.item/vnd.mx.Game";

        //列名
        public static final String ID = "_id"; 
        public static final String COLUMN_NAME = "name";
        
        public static final String SORT_DESC = " DESC";
        public static final String SORT_ASC = " ASC";
     }
    
    public static final class CardType implements BaseColumns{

        //表名
        public static final String TABLE_NAME = "card_type_info";

        //访问该ContentProvider的URI
        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/" + TABLE_NAME);
        
        //新增mimeType  vnd.android.cursor.dir/开头返回多条数据    vnd.android.cursor.item/开头返回单条数据
        public static final String CONTENT_TYPE="vnd.android.cursor.dir/vnd.mx.CardType"; 
        public static final String CONTENT_TYPE_ITEM="vnd.android.cursor.item/vnd.mx.CardType";

        //列名
        public static final String ID = "_id"; 
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_GAMETYPE = "game_type";
        
        public static final String SORT_DESC = " DESC";
        public static final String SORT_ASC = " ASC";
     }

    public static final class Event implements BaseColumns{

        //表名
        public static final String TABLE_NAME = "event_info";

        //访问该ContentProvider的URI
        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/" + TABLE_NAME);

        //新增mimeType  vnd.android.cursor.dir/开头返回多条数据    vnd.android.cursor.item/开头返回单条数据
        public static final String CONTENT_TYPE="vnd.android.cursor.dir/vnd.mx.Event";
        public static final String CONTENT_TYPE_ITEM="vnd.android.cursor.item/vnd.mx.Event";

        //列名
        public static final String ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_GAMEID= "gameid";
        public static final String COLUMN_SHOWING = "showFlag";

        public static final String SORT_DESC = " DESC";
        public static final String SORT_ASC = " ASC";
    }

    public static final class EventChain implements BaseColumns{

        //表名
        public static final String TABLE_NAME = "event_chain";

        //访问该ContentProvider的URI
        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/" + TABLE_NAME);

        //新增mimeType  vnd.android.cursor.dir/开头返回多条数据    vnd.android.cursor.item/开头返回单条数据
        public static final String CONTENT_TYPE="vnd.android.cursor.dir/vnd.mx.EventChain";
        public static final String CONTENT_TYPE_ITEM="vnd.android.cursor.item/vnd.mx.EventChain";

        //列名
        public static final String COLUMN_CARD_NID = "cardNid";
        public static final String COLUMN_EVENT_ID = "eventId";

        public static final String SORT_DESC = " DESC";
        public static final String SORT_ASC = " ASC";
    }
}
