package com.mx.gillustrated.activity;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.mx.gillustrated.MyApplication;
import com.mx.gillustrated.common.DBCall;
import com.mx.gillustrated.dialog.DialogExportImg;
import com.mx.gillustrated.R;
import com.mx.gillustrated.adapter.DataListAdapter;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.listener.ListenerListViewScrollHandler;
import com.mx.gillustrated.provider.Providerdata;
import com.mx.gillustrated.provider.Providerdata.Card;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.util.DBHelper;
import com.mx.gillustrated.util.JsonFileReader;
import com.mx.gillustrated.util.ServiceUtils;
import com.mx.gillustrated.util.UIUtils;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MainActivity extends BaseActivity {

    private static String TAG = "MainActivity";

    @BindView(R.id.lvMain) ListView listViewMain;
    @BindView(R.id.spinnerName) Spinner spinnerName;
    @BindView(R.id.spinnerCost) Spinner spinnerCost;
    @BindView(R.id.spinnerAttr) Spinner spinnerAttr;
    @BindView(R.id.spinnerFrontName) Spinner spinnerFrontName;
    @BindView(R.id.spinnerGame) Spinner spinnerGameList;
    @BindView(R.id.pageVBox) RelativeLayout pageVboxLayout;

    @BindColor(R.color.color_white2) int mColorWhite2;
    @BindColor(R.color.color_white) int mColorWhite;


    @OnClick(R.id.btnAdd)
    void btnAddClickListener(){
        Intent intent = new Intent(MainActivity.this, AddCardActivity.class);
        intent.putExtra("game", mGameType);
        startActivity(intent);
    }

    @OnClick(R.id.btnRefresh)
    void btnRefreshClickListener(){
        mSpinnerChangedCount = 0;
        if(spinnerName.getSelectedItemPosition() > 0)
            mSpinnerChangedCount++;
        if(spinnerCost.getSelectedItemPosition() > 0)
            mSpinnerChangedCount++;
        if(spinnerAttr.getSelectedItemPosition() > 0)
            mSpinnerChangedCount++;
        if(spinnerFrontName.getSelectedItemPosition() > 0)
            mSpinnerChangedCount++;

        initParms();
        if(mSpinnerChangedCount == 0){
            searchData(); //默认检索
        }

        spinnerName.setSelection(0);
        spinnerCost.setSelection(0);
        spinnerAttr.setSelection(0);
        spinnerFrontName.setSelection(0);
    }

    @OnItemSelected({R.id.spinnerName, R.id.spinnerCost, R.id.spinnerAttr, R.id.spinnerFrontName})
    void onSelectlistener(){
        if(mSpinnerChangedCount > 0)
            mSpinnerChangedCount--;
        if(mSpinnerChangedCount == 0)
            searchData();
    }

    @OnItemSelected(R.id.spinnerGame)
    void onSelectlistener(int position){
        mGameType = spinnerGameData[position];
        refreshMainData();
    }

    private static String INIT_ORDER_BY = Card.ID;
    private static String INIT_ORDER_TYPE = Card.SORT_DESC;
	private static String DEFAULT_NAME = "名称";
	private static String DEFAULT_COST = "コスト";
	private static String DEFAULT_ATTR = "属性";
	private static String DEFAULT_FRONTNAME = "活动";

    private List<CardInfo> mList;
    private DataListAdapter mAdapter;
	private int mGameType = -1; //游戏类别
	private int[] spinnerGameData;
	private CardInfo mSearchCondition = null;
	private int mSpinnerChangedCount = 0; //控制 select变更后是否参与检索
    private ListHeaderView mListHeaderView;
    private Map<String, TextView> mTextViewMap;
    private String mCurrentOrderBy;
    private String mCurrentOrderType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        UIUtils.setSpinnerClick(new ArrayList<Spinner>(){{
            add(spinnerName);
            add(spinnerCost);
            add(spinnerAttr);
            add(spinnerFrontName);
            add(spinnerGameList);
        }});
        initParms();
        initHeader();

		pageVboxLayout.setVisibility(View.GONE);
		listViewMain.setOnItemClickListener(itemClickListener);
		listViewMain.setOnScrollListener(new ListenerListViewScrollHandler(listViewMain, pageVboxLayout, 1));
        mList = new ArrayList<CardInfo>();
		mAdapter = new DataListAdapter(this, mList);
		setGameList();
		
		//temp
//		File fileDirTemp = new File(Environment.getExternalStorageDirectory(),
//				"backup");
//		File fileTemp = new File(fileDirTemp.getPath(), "ss.text");
//		 try {
//			CommonUtil.copyBigDataToSD(this, "ss.text", fileTemp.getPath());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		 
	}

    private void initParms(){
        mCurrentOrderBy = INIT_ORDER_BY;
        mCurrentOrderType =  INIT_ORDER_TYPE;
    }

    private void initHeader() {
        View headerView = LayoutInflater.from(getBaseContext()).inflate(
                R.layout.adapter_mainlist_header, null);

        mListHeaderView = new ListHeaderView(headerView);
        setHeaderClickHandler(mListHeaderView.tvHP, Card.COLUMN_MAXHP);
        setHeaderClickHandler(mListHeaderView.tvAttack, Card.COLUMN_MAXATTACK);
        setHeaderClickHandler(mListHeaderView.tvDefense, Card.COLUMN_MAXDEFENSE);
        setHeaderClickHandler(mListHeaderView.tvName, Card.COLUMN_NAME);
        setHeaderClickHandler(mListHeaderView.tvAttr, Card.COLUMN_ATTR);
        setHeaderClickHandler(mListHeaderView.tvCost, Card.COLUMN_COST);
        listViewMain.addHeaderView(headerView);

        mTextViewMap = new HashMap<String, TextView>(){{
            put(Card.COLUMN_MAXHP, mListHeaderView.tvHP);
            put(Card.COLUMN_MAXATTACK, mListHeaderView.tvAttack);
            put(Card.COLUMN_MAXDEFENSE, mListHeaderView.tvDefense);
            put(Card.COLUMN_NAME, mListHeaderView.tvName);
            put(Card.COLUMN_ATTR, mListHeaderView.tvAttr);
            put(Card.COLUMN_COST, mListHeaderView.tvCost);
        }};
    }

    void setHeaderClickHandler(TextView tv, final String column){
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentOrderBy == column)
                    mCurrentOrderType = mCurrentOrderType.equals(Card.SORT_ASC) ? Card.SORT_DESC : Card.SORT_ASC;
                else
                    mCurrentOrderType = Card.SORT_DESC;
                mCurrentOrderBy = column;
                searchData();
            }
        });
    }

    private void searchData() {
        setHeaderColor();
        if(spinnerName.getSelectedItem() == null || spinnerCost.getSelectedItem() == null ||
                spinnerAttr.getSelectedItem() == null || spinnerFrontName.getSelectedItem() == null)
            return;

		CardInfo card = new CardInfo();
        if(!spinnerName.getSelectedItem().toString().equals(DEFAULT_NAME))
        {
            card.setName(spinnerName.getSelectedItem().toString());
        }
        if(!spinnerFrontName.getSelectedItem().toString().equals(DEFAULT_FRONTNAME))
        {
            card.setFrontName(spinnerFrontName.getSelectedItem().toString());
        }
        if(!spinnerCost.getSelectedItem().toString().equals(DEFAULT_COST))
        {
            card.setCost(Integer.parseInt(spinnerCost.getSelectedItem().toString()));
        }
        if(!spinnerAttr.getSelectedItem().toString().equals(DEFAULT_ATTR))
        {
            String attr = spinnerAttr.getSelectedItem().toString();
            card.setAttr(attr);
        }
        this.searchCards(card);
    }

    private void setHeaderColor() {
        for(Map.Entry<String, TextView> iterator : mTextViewMap.entrySet()){
            if(iterator.getValue() != null)
                iterator.getValue().setBackgroundColor(mColorWhite2);
        }
        if(mTextViewMap.get(mCurrentOrderBy) != null)
            mTextViewMap.get(mCurrentOrderBy).setBackgroundColor(mColorWhite);
    }

    private void setGameList()
	{
        //ServiceUtils.createMultVoidConnect();
        ServiceUtils.createConnect(new DBCall<List<GameInfo>>() {
            @Override
            public List<GameInfo> enqueue() {
                return mDBHelper.queryGameList(null);
            }
        }).doOnNext(new Consumer<List<GameInfo>>() {
            @Override
            public void accept(List<GameInfo> list) throws Exception {
                String[] spinnerData = null;
                if(list.size() > 0){
                    spinnerGameData = new int[list.size()];
                    spinnerData = new String[list.size()];
                    for(int i = 0; i < list.size(); i++){
                        spinnerData[i] = list.get(i).getName();
                        spinnerGameData[i] = list.get(i).getId();
                    }
                    ArrayAdapter< String> adapterName =
                            new ArrayAdapter< String>( getBaseContext(),
                                    android.R.layout.simple_gallery_item, spinnerData);
                    adapterName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerGameList.setAdapter(adapterName);
                    mGameType = list.get(0).getId();
                }else{
                    new Thread() {
                        public void run() {
                            File fileDir = new File(Environment.getExternalStorageDirectory(),
                                    MConfig.SD_DATA_PATH);
                            File jsonFile = new File(fileDir.getPath(), "cardinfo.json");
                            if(jsonFile.exists()){
                                String out = JsonFileReader.getJson(MainActivity.this, jsonFile);
                                JSONObject jsonObj;
                                try {
                                    jsonObj = new JSONObject(out);
                                    mDBHelper.addAllCardInfo(JsonFileReader.setListData(jsonObj.getJSONArray("rows")));
                                    mDBHelper.addAllGameNameInfo(JsonFileReader.setGameListData(jsonObj.getJSONArray("rowsGame")));
                                    mDBHelper.addAlCardTypeInfo(JsonFileReader.setCardTypeListData(jsonObj.getJSONArray("rowsCardType")));
                                    mDBHelper.addAllEvents(JsonFileReader.setEventListData(jsonObj.getJSONArray("rowsEvents")));
                                    mainHandler.sendEmptyMessage(1);
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                }
            }
        }).subscribe();
	}


	
	private void refreshMainData(){
		mSpinnerChangedCount = 4;
		setSpinner(spinnerName, Card.COLUMN_NAME, DEFAULT_NAME);
        setSpinner(spinnerFrontName, Card.COLUMN_FRONT_NAME, DEFAULT_FRONTNAME);
		setSpinner(spinnerCost, Card.COLUMN_COST, DEFAULT_COST);
		setSpinner(spinnerAttr, Card.COLUMN_ATTR, DEFAULT_ATTR);
	}
	
	private void setSpinner(Spinner spinner, String columnType, String defaultStr)
	{
		String[] spinnerData = null;
		CardInfo[] cardArray = mDBHelper.queryCardDropList(columnType, mGameType);
		Arrays.sort(cardArray, droplistComparator);
		spinnerData = new String[cardArray.length + 1];
		spinnerData[0] = defaultStr;
		for(int i = 1; i <= cardArray.length; i++)
			spinnerData[i] = cardArray[i - 1].getName();
		
		ArrayAdapter< String> adapterName = 
				new ArrayAdapter< String>( this, 
				android.R.layout.simple_gallery_item, spinnerData);
		adapterName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapterName);
	}
	
	static Comparator<CardInfo> droplistComparator = new Comparator<CardInfo>() {
		
		@Override
		public int compare(CardInfo o1, CardInfo o2) {
			return o2.getNid() - o1.getNid();
		}
	};
	
	private void searchCards(CardInfo info){
		mList.clear();
		mSearchCondition = info;
		mList.addAll(mDBHelper.queryCards(info, mCurrentOrderBy + mCurrentOrderType, mGameType));
		updateList(true);
	}
	
	Handler mainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				setGameList();
			}
			else if(msg.what == 2 || msg.what == 4)
			{
				final int index = msg.what;
				new Thread() {
					public void run() {
						int[] list = new int[mList.size()];
						for(int i = 0; i < mList.size(); i++)
							list[i] = mList.get(i).getId();
						CommonUtil.generateHeaderImg(MainActivity.this, list, mGameType, index == 2 ? false : true);
						mainHandler.sendEmptyMessage(3);
					}
				}.start();
			}
			else if (msg.what == 3) {
				Toast.makeText(MainActivity.this, "生成头像完成", Toast.LENGTH_SHORT).show();
			}
		}
		

	};

	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			if (position != 0) {
				Intent intent = new Intent(MainActivity.this,
						DetailActivity.class);
				CardInfo info = (CardInfo) arg0.getItemAtPosition(position);
				info.setGameId(mGameType);
				intent.putExtra("card", info);
				intent.putExtra("cardSearchCondition", mSearchCondition);
				intent.putExtra("orderBy", mCurrentOrderBy + mCurrentOrderType);
				intent.putExtra("positon", position);
				intent.putExtra("totalCount", arg0.getCount());
				startActivity(intent);
			}

		}
	};

	private void updateList(boolean flag) {
		if (flag)
			listViewMain.setAdapter(mAdapter);
		else
			mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override 
    public boolean onOptionsItemSelected(MenuItem item) { 
        super.onOptionsItemSelected(item); 
        switch(item.getItemId())  
        { 
        	case  R.id.menu_out :
	        	try {
	        		CommonUtil.printFile(generateJsonString(), CommonUtil.generateDataFile("cardinfo.json"));
	        		Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
	            break; 
        	case  R.id.menu_gamelist :
        		Intent intent = new Intent(MainActivity.this, GameListActivity.class);
    			startActivity(intent);
		        break;
        	case  R.id.menu_header :
        		DialogExportImg.show(this, mList.get(0).getId(), mGameType, mainHandler);
		        break;
            case  R.id.menu_eventlist :
                Intent intentEvent = new Intent(MainActivity.this, EventsActivity.class);
                intentEvent.putExtra("game", mGameType);
                startActivity(intentEvent);
                break;
        }
        return true; 
    } 
	
	
	private String generateJsonString() throws JSONException
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
			line.put("attr", data.get(i).getAttr());
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

		JSONObject result = new JSONObject();
		result.put("rows", rows);
		result.put("rowsGame", rowsGame);
		result.put("rowsCardType", rowsCardType);
        result.put("rowsEvents", rowsEvents);
		result.put("head", "GIMG");
		return result.toString();
	}


    static class ListHeaderView{
        @BindView(R.id.tvHeaderName) TextView tvName;
        @BindView(R.id.tvHeaderAttr) TextView tvAttr;
        @BindView(R.id.tvHeaderCost) TextView tvCost;
        @BindView(R.id.tvHeaderHP) TextView tvHP;
        @BindView(R.id.tvHeaderAttack) TextView tvAttack;
        @BindView(R.id.tvHeaderDefense) TextView tvDefense;


        public ListHeaderView(View view)
        {
            ButterKnife.bind(this, view);
        }
    }

}

