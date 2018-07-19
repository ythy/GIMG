package com.mx.gillustrated.util;

import android.os.Environment;
import android.widget.Toast;

import com.mx.gillustrated.activity.MainActivity;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by maoxin on 2018/7/19.
 */

public class DataBakUtil {

    private static final String BakFileName = "cardinfo.json";

    public static void saveDataToFiles(DBHelper mDBHelper){
        try {
            CommonUtil.printFile(generateJsonString(mDBHelper), CommonUtil.generateDataFile(BakFileName));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void getDataFromFiles(DBHelper mDBHelper){
        File fileDir = new File(Environment.getExternalStorageDirectory(),
                MConfig.SD_DATA_PATH);
        File jsonFile = new File(fileDir.getPath(), BakFileName);
        if(jsonFile.exists()){
            String out = JsonFileReader.getJson(jsonFile);
            JSONObject jsonObj;
            try {
                jsonObj = new JSONObject(out);
                mDBHelper.addAllCardInfo(JsonFileReader.setListData(jsonObj.getJSONArray("rows")));
                mDBHelper.addAllGameNameInfo(JsonFileReader.setGameListData(jsonObj.getJSONArray("rowsGame")));
                mDBHelper.addAlCardTypeInfo(JsonFileReader.setCardTypeListData(jsonObj.getJSONArray("rowsCardType")));
                mDBHelper.addAllEvents(JsonFileReader.setEventListData(jsonObj.getJSONArray("rowsEvents")));
                mDBHelper.addAllCardEvent(JsonFileReader.setCardEventListData(jsonObj.getJSONArray("rowsCardEvents")));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static  String generateJsonString(DBHelper mDBHelper) throws JSONException
    {
        List<CardInfo> data = mDBHelper.queryCards(null, null, -1);
        JSONArray rows = new JSONArray();
        for(int i = 0; i < data.size(); i++)
        {
            JSONObject line = new JSONObject();
            line.put("nid", data.get(i).getNid());
            line.put("id", data.get(i).getId());
            line.put("gameid", data.get(i).getGameId());
            line.put("frontname", data.get(i).getFrontName());
            line.put("remark", data.get(i).getRemark() == null ? "" : data.get(i).getRemark());
            line.put("event", data.get(i).getEventId());
            line.put("name", data.get(i).getName());
            line.put("attr", data.get(i).getAttrId());
            line.put("cost", data.get(i).getCost());
            line.put("level", data.get(i).getLevel());
            line.put("maxHP", data.get(i).getMaxHP());
            line.put("maxAttack", data.get(i).getMaxAttack());
            line.put("maxDefense", data.get(i).getMaxDefense());
            rows.put(line);
        }

        List<GameInfo> dataGame = mDBHelper.queryGameList(null);
        JSONArray rowsGame = new JSONArray();
        for(int i = 0; i < dataGame.size(); i++)
        {
            JSONObject line = new JSONObject();
            line.put("id", dataGame.get(i).getId());
            line.put("name", dataGame.get(i).getName());
            rowsGame.put(line);
        }

        List<CardTypeInfo> dataCardType = mDBHelper.queryCardTypeList(-1);
        JSONArray rowsCardType = new JSONArray();
        for(int i = 0; i < dataCardType.size(); i++)
        {
            JSONObject line = new JSONObject();
            line.put("id", dataCardType.get(i).getId());
            line.put("gameid", dataCardType.get(i).getGameId());
            line.put("name", dataCardType.get(i).getName());
            rowsCardType.put(line);
        }

        List<EventInfo> eventlist = mDBHelper.queryEventList(null);
        JSONArray rowsEvents = new JSONArray();
        for(int i = 0; i < eventlist.size(); i++)
        {
            JSONObject line = new JSONObject();
            line.put("id", eventlist.get(i).getId());
            line.put("gameid", eventlist.get(i).getGameId());
            line.put("name", eventlist.get(i).getName());
            line.put("content", eventlist.get(i).getContent());
            line.put("duration", eventlist.get(i).getDuration());
            line.put("showing", eventlist.get(i).getShowing() == null ? "" :  eventlist.get(i).getShowing() );
            rowsEvents.put(line);
        }

        List<Integer[]> cardEvents = mDBHelper.queryAllCardEvents();
        JSONArray rowsCardEvents = new JSONArray();
        for(int i = 0; i < cardEvents.size(); i++)
        {
            JSONObject line = new JSONObject();
            line.put("cardId", cardEvents.get(i)[0]);
            line.put("eventId", cardEvents.get(i)[1]);
            rowsCardEvents.put(line);
        }

        JSONObject result = new JSONObject();
        result.put("rows", rows);
        result.put("rowsGame", rowsGame);
        result.put("rowsCardType", rowsCardType);
        result.put("rowsEvents", rowsEvents);
        result.put("rowsCardEvents", rowsCardEvents);
        result.put("head", "GIMG");
        return result.toString();
    }


}
