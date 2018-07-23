package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by maoxin on 2017/2/22.
 */

@DatabaseTable(tableName = "event_info")
public class EventInfo extends SpinnerInfo  {
    @DatabaseField
    private String name = null; //活动名称
    @DatabaseField(columnName = "gameid")
    private int gameId = -1;
    @DatabaseField(id = true, columnName="_id")
    private int id = -1;
    @DatabaseField
    private String duration = null; //活动期间
    @DatabaseField
    private String content = null;  //活动内容
    @DatabaseField(columnName = "showFlag")
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
