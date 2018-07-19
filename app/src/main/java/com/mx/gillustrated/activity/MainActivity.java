package com.mx.gillustrated.activity;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.mx.gillustrated.adapter.SpinnerCommonAdapter;
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
import com.mx.gillustrated.util.DataBakUtil;
import com.mx.gillustrated.util.JsonFileReader;
import com.mx.gillustrated.util.ServiceUtils;
import com.mx.gillustrated.util.UIUtils;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindView;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity {

    private static String TAG = "MainActivity";
    private static String INIT_ORDER_BY = Card.ID;
    private static String INIT_ORDER_TYPE = Card.SORT_DESC;
    private static String DEFAULT_NAME = "名称";
    private static String DEFAULT_COST = "コスト";
    private static String DEFAULT_ATTR = "属性";
    private static String DEFAULT_EVENT = "活动";

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
    private String mSpinnerLastSelect; //保存最后一次本页面Spinner检索条件
    private int mListViewLastPosition; //保存最后一次本页面滚动位置

    @BindView(R.id.lvMain) ListView listViewMain;
    @BindView(R.id.spinnerName) Spinner spinnerName;
    @BindView(R.id.spinnerCost) Spinner spinnerCost;
    @BindView(R.id.spinnerAttr) Spinner spinnerAttr;
    @BindView(R.id.spinnerFrontName) Spinner spinnerEvent;
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
        if(spinnerEvent.getSelectedItemPosition() > 0)
            mSpinnerChangedCount++;

        mCurrentOrderBy = INIT_ORDER_BY;
        mCurrentOrderType =  INIT_ORDER_TYPE;

        if(mSpinnerChangedCount == 0){ //没有变化
            searchData(); //默认检索
        }
        spinnerName.setSelection(0);
        spinnerCost.setSelection(0);
        spinnerAttr.setSelection(0);
        spinnerEvent.setSelection(0);
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
        CommonUtil.setGameType(this, mGameType);
        startSearchMainData();
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
        mGameType = getIntent().getIntExtra("game", CommonUtil.getGameType(this));
        ButterKnife.bind(this);

        initViewAndVariable();
        initHeader();
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

    private void initViewAndVariable(){
        UIUtils.setSpinnerClick(new ArrayList<Spinner>(){{
            add(spinnerName);
            add(spinnerCost);
            add(spinnerAttr);
            add(spinnerEvent);
            add(spinnerGameList);
        }});
        pageVboxLayout.setVisibility(View.GONE);
        listViewMain.setOnItemClickListener(itemClickListener);
        listViewMain.setOnScrollListener(new ListenerListViewScrollHandler(listViewMain, pageVboxLayout, 0));
        mList = new ArrayList<CardInfo>();
        mAdapter = new DataListAdapter(this, mList);
        String order =  getIntent().getStringExtra("orderBy");
        mCurrentOrderBy = order == null ? INIT_ORDER_BY : order.split("\\*")[0];
        mCurrentOrderType =  order == null ? INIT_ORDER_TYPE : order.split("\\*")[1];
        mSpinnerLastSelect = getIntent().getStringExtra("spinnerIndexs");
        mListViewLastPosition = getIntent().getIntExtra("position", 0);
    }

    private void initHeader() {
        mListHeaderView = new ListHeaderView(findViewById(R.id.ll_header));
        setHeaderClickHandler(mListHeaderView.tvHP, Card.COLUMN_MAXHP);
        setHeaderClickHandler(mListHeaderView.tvAttack, Card.COLUMN_MAXATTACK);
        setHeaderClickHandler(mListHeaderView.tvDefense, Card.COLUMN_MAXDEFENSE);
        setHeaderClickHandler(mListHeaderView.tvName, Card.COLUMN_NAME);
        setHeaderClickHandler(mListHeaderView.tvAttr, Card.COLUMN_ATTR);
        setHeaderClickHandler(mListHeaderView.tvCost, Card.COLUMN_COST);
        setHeaderClickHandler(mListHeaderView.tvImg, Card.COLUMN_NID);

        mTextViewMap = new HashMap<String, TextView>(){{
            put(Card.COLUMN_NID, mListHeaderView.tvImg);
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
                spinnerAttr.getSelectedItem() == null || spinnerEvent.getSelectedItem() == null)
            return;

		CardInfo card = new CardInfo();
        CardInfo spinnerSelected;

        spinnerSelected = (CardInfo) spinnerName.getSelectedItem();
        if(!spinnerSelected.getName().equals(DEFAULT_NAME))
        {
            card.setName(spinnerSelected.getName());
        }
        EventInfo spinnerSelected2 = (EventInfo) spinnerEvent.getSelectedItem();
        if(!spinnerSelected2.getName().equals(DEFAULT_EVENT))
        {
            card.setEventId(spinnerSelected2.getId());
        }
        spinnerSelected = (CardInfo) spinnerCost.getSelectedItem();
        if(!spinnerSelected.getName().equals(DEFAULT_COST))
        {
            card.setCost(Integer.parseInt(spinnerSelected.getName()));
        }
        spinnerSelected = (CardInfo) spinnerAttr.getSelectedItem();
        if(!spinnerSelected.getName().equals(DEFAULT_ATTR))
        {
            card.setAttrId(spinnerSelected.getAttrId());
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
                    int gameSelected = 0;
                    spinnerGameData = new int[list.size()];
                    spinnerData = new String[list.size()];
                    for(int i = 0; i < list.size(); i++){
                        spinnerData[i] = list.get(i).getName();
                        spinnerGameData[i] = list.get(i).getId();
                        if(spinnerGameData[i] == mGameType)
                            gameSelected = i;
                    }
                    ArrayAdapter< String> adapterName =
                            new ArrayAdapter< String>( getBaseContext(),
                                    android.R.layout.simple_gallery_item, spinnerData);
                    adapterName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerGameList.setAdapter(adapterName);
                    spinnerGameList.setSelection(gameSelected);
                    mGameType = list.get(gameSelected).getId();
                }else{
                    new Thread() {
                        public void run() {
                            DataBakUtil.getDataFromFiles(mDBHelper);
                            mainHandler.sendEmptyMessage(1);
                        }
                    }.start();
                }
            }
        }).subscribe();
	}


	
	private void startSearchMainData(){
		mSpinnerChangedCount = 4;
		setSpinner(spinnerName, Card.COLUMN_NAME, DEFAULT_NAME);
		setSpinner(spinnerCost, Card.COLUMN_COST, DEFAULT_COST);
		setSpinner(spinnerAttr, Card.COLUMN_ATTR, DEFAULT_ATTR);
        //设置活动下拉列表
        EventInfo requst = new EventInfo();
        requst.setGameId(mGameType);
        requst.setShowing("Y");
        List<EventInfo> mEventList = mDBHelper.queryEventList(requst);
        mEventList.add(0, new EventInfo(DEFAULT_EVENT));
        SpinnerCommonAdapter<EventInfo> adapterEvent =
                new SpinnerCommonAdapter( this, mEventList);
        spinnerEvent.setAdapter(adapterEvent);

        if(mSpinnerLastSelect != null){
            String[] temp = mSpinnerLastSelect.split(",");
            spinnerName.setSelection(Integer.parseInt(temp[0]));
            spinnerCost.setSelection(Integer.parseInt(temp[1]));
            spinnerAttr.setSelection(Integer.parseInt(temp[2]));
            spinnerEvent.setSelection(Integer.parseInt(temp[3]));
            mSpinnerLastSelect = null;
        }

	}

    //设置筛选列表
	private void setSpinner(Spinner spinner, String columnType, String defaultStr)
	{
		List<CardInfo> cardArray = mDBHelper.queryCardDropList(columnType, mGameType);
        Collections.sort(cardArray, droplistComparator);
        cardArray.add (0, new CardInfo(defaultStr));

        SpinnerCommonAdapter<CardInfo> adapterName =
				new SpinnerCommonAdapter( this, cardArray);
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
			if (position >= 0) {
				Intent intent = new Intent(MainActivity.this,
						DetailActivity.class);
				CardInfo info = (CardInfo) arg0.getItemAtPosition(position);
				info.setGameId(mGameType);
				intent.putExtra("card", info);
				intent.putExtra("cardSearchCondition", mSearchCondition);
				intent.putExtra("orderBy", mCurrentOrderBy + "*" + mCurrentOrderType);
				intent.putExtra("positon", position);
				intent.putExtra("totalCount", arg0.getCount());
                intent.putExtra("spinnerIndexs", spinnerName.getSelectedItemPosition() + "," +
                        spinnerCost.getSelectedItemPosition() + "," +
                        spinnerAttr.getSelectedItemPosition() + "," +
                        spinnerEvent.getSelectedItemPosition() + "," );
				startActivity(intent);
			}

		}
	};

    private void updateList(boolean flag) {
		if (flag)
			listViewMain.setAdapter(mAdapter);
		else
			mAdapter.notifyDataSetChanged();

        if(mListViewLastPosition > 0 ){
            listViewMain.setSelection(mListViewLastPosition);
            mListViewLastPosition = 0;
        }

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
                DataBakUtil.saveDataToFiles(mDBHelper);
                Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
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
	


    static class ListHeaderView{
        @BindView(R.id.tvHeaderImg) TextView tvImg;
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

