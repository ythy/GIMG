package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by maoxin on 2017/2/22.
 */

@DatabaseTable(tableName = EventInfo.TABLE_NAME)
public class EventInfo extends SpinnerInfo  {

    public static final String TABLE_NAME = "event_info";
    public static final String ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_GAMEID= "gameid";
    public static final String COLUMN_SHOWING = "showFlag";

    @DatabaseField
    private String name = null; //活动名称
    @DatabaseField(columnName = COLUMN_GAMEID)
    private int gameId;
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName=ID)
    private int id;
    @DatabaseField
    private String duration = null; //活动期间
    @DatabaseField
    private String content = null;  //活动内容
    @DatabaseField(columnName = COLUMN_SHOWING)
    private String showing = null; //是否是卡片可选项

    public EventInfo(){
    }

    public EventInfo(String name){
        this.name = name;
    }

    public String getShowing() {
        return showing;
    }

    public void setShowing(String showing) {
        this.showing = showing;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
