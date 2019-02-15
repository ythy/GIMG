package com.mx.gillustrated.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mx.gillustrated.vo.CardEventInfo;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import android.content.Context;
import android.content.res.AssetManager;

public class JsonFileReader {

	public static String getJson(File fileName) {

		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String line;
			while ((line = bf.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}
	
	public static List<CardInfo> setListData(JSONArray array) {
		List<CardInfo> result = new ArrayList<CardInfo>();
		try {
			int len = array.length();
			for (int i = 0; i < len; i++) {
				JSONObject object = array.getJSONObject(i);
				CardInfo cardinfo = new CardInfo();
				cardinfo.setNid(object.getInt("nid"));
				cardinfo.setId(object.getInt("id"));
				cardinfo.setGameId(object.getInt("gameid"));
				cardinfo.setFrontName(object.getString("frontname"));
				cardinfo.setRemark(object.getString("remark"));
				cardinfo.setEventId(object.getInt("event"));
				cardinfo.setName(object.getString("name"));
				cardinfo.setPinyinName(object.getString("pinyinName"));
				cardinfo.setAttrId(object.getInt("attr"));
				cardinfo.setLevel(object.getString("level"));
				cardinfo.setCost(object.getInt("cost"));
				cardinfo.setMaxHP(object.getInt("maxHP"));
				cardinfo.setMaxAttack(object.getInt("maxAttack"));
				cardinfo.setMaxDefense(object.getInt("maxDefense"));
				result.add(cardinfo);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static List<GameInfo> setGameListData(JSONArray array) {
		List<GameInfo> result = new ArrayList<GameInfo>();
		try {
			int len = array.length();
			for (int i = 0; i < len; i++) {
				JSONObject object = array.getJSONObject(i);
				GameInfo gameinfo = new GameInfo();
				gameinfo.setId(object.getInt("id")); 
				gameinfo.setName(object.getString("name"));
				gameinfo.setDetail(object.getString("detail"));
				result.add(gameinfo);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static List<CardTypeInfo> setCardTypeListData(JSONArray array) {
		List<CardTypeInfo> result = new ArrayList<CardTypeInfo>();
		try {
			int len = array.length();
			for (int i = 0; i < len; i++) {
				JSONObject object = array.getJSONObject(i);
				CardTypeInfo cardinfo = new CardTypeInfo();
				cardinfo.setId(object.getInt("id"));
				cardinfo.setGameId(object.getInt("gameid"));
				cardinfo.setName(object.getString("name"));
				result.add(cardinfo);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<EventInfo> setEventListData(JSONArray array) {
		List<EventInfo> result = new ArrayList<EventInfo>();
		try {
			int len = array.length();
			for (int i = 0; i < len; i++) {
				JSONObject object = array.getJSONObject(i);
				EventInfo info = new EventInfo();
				info.setId(object.getInt("id"));
				info.setGameId(object.getInt("gameid"));
				info.setName(object.getString("name"));
				info.setContent(object.getString("content"));
				info.setDuration(object.getString("duration"));
				info.setShowing(object.getString("showing"));
				result.add(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<CardEventInfo> setCardEventListData(JSONArray array) {
		List<CardEventInfo> result = new ArrayList<CardEventInfo>();
		try {
			int len = array.length();
			for (int i = 0; i < len; i++) {
				JSONObject object = array.getJSONObject(i);
				CardEventInfo info = new CardEventInfo( object.getInt("cardId"), object.getInt("eventId"));
				result.add(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}


}
