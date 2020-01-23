package com.mx.gillustrated.vo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = CharacterInfo.TABLE_NAME)
public class CharacterInfo {

    public static final String TABLE_NAME = "character_Info";
    public static final String ID = "_id";
    public static final String COLUMN_GAMEID = "game_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_NATIONALITY = "nationality";
    public static final String COLUMN_DOMAIN = "domain";
    public static final String COLUMN_AGE= "age";
    public static final String COLUMN_SKILLED= "skilled";


    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName=ID)
    private int id;
    @DatabaseField
    private String name = null;
    @DatabaseField(columnName = COLUMN_GAMEID)
    private int gameId;
    @DatabaseField
    private int nationality;
    @DatabaseField
    private int domain;
    @DatabaseField
    private int age;
    @DatabaseField
    private int skilled;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getNationality() {
        return nationality;
    }

    public void setNationality(int nationality) {
        this.nationality = nationality;
    }

    public int getDomain() {
        return domain;
    }

    public void setDomain(int domain) {
        this.domain = domain;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSkilled() {
        return skilled;
    }

    public void setSkilled(int skilled) {
        this.skilled = skilled;
    }
}
