package com.mx.gillustrated.component;

import android.widget.Spinner;

import com.mx.gillustrated.R;
import com.mx.gillustrated.activity.MainActivity;
import com.mx.gillustrated.adapter.SpinnerCommonAdapter;
import com.mx.gillustrated.database.DataBaseHelper;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.util.UIUtils;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.EventInfo;
import com.mx.gillustrated.vo.GameInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import static com.mx.gillustrated.util.CommonUtil.setSpinnerItemSelectedByValue2;

/**
 * Created by maoxin on 2018/7/31.
 */

public class MainActivityTop {

    private MainActivity mContext;
    private DataBaseHelper mOrmHelper;
    private TopHandle mTopHandle;
    private Top mTop;
    private List<GameInfo> mSpinnerGameData;
    private int mSpinnerChangedCount = 0; //控制 select变更后是否参与检索
    private String mSpinnerLastSelect; //保存最后一次本页面Spinner检索条件

    private static String DEFAULT_NAME = "名称";
    private static String DEFAULT_COST = "コスト";
    private static String DEFAULT_ATTR = "属性";
    private static String DEFAULT_EVENT = "活动";
    private static String DEFAULT_FRONT_NAME = "前缀";
    private static String DEFAULT_LEVEL = "星级";

    public MainActivityTop(MainActivity context, DataBaseHelper ormHelper, TopHandle handle){
        mContext = context;
        mTopHandle = handle;
        mOrmHelper = ormHelper;
        mTop = new Top();
        initialize();
    }

    private void initialize(){
        mSpinnerLastSelect = mContext.getIntent().getStringExtra("spinnerIndexs");
        UIUtils.setSpinnerClick(new ArrayList<Spinner>(){{
            add(mTop.spinnerName);
            add(mTop.spinnerCost);
            add(mTop.spinnerAttr);
            add(mTop.spinnerEvent);
            add(mTop.spinnerFrontName);
            add(mTop.spinnerLevel);
            add(mTop.spinnerGameList);
        }});
    }

    //此方法触发检索
    private void initializeSppinersByGame(int gametype){
        mSpinnerChangedCount = 6;
        setSpinner(mTop.spinnerName, CardInfo.COLUMN_NAME, DEFAULT_NAME, gametype);
        setSpinner(mTop.spinnerCost, CardInfo.COLUMN_COST, DEFAULT_COST, gametype);
        setSpinner(mTop.spinnerAttr, CardInfo.COLUMN_ATTR, DEFAULT_ATTR, gametype);
        setSpinner(mTop.spinnerFrontName, CardInfo.COLUMN_FRONT_NAME, DEFAULT_FRONT_NAME, gametype);
        setSpinner(mTop.spinnerLevel, CardInfo.COLUMN_LEVEL, DEFAULT_LEVEL, gametype);
        //设置活动下拉列表
        List<EventInfo> mEventList = mOrmHelper.getEventInfoDao().getListByGameId(gametype, "Y");
        mEventList.add(0, new EventInfo(DEFAULT_EVENT));
        SpinnerCommonAdapter adapterEvent =
                new SpinnerCommonAdapter( mContext, mEventList);
        mTop.spinnerEvent.setAdapter(adapterEvent);

        if(mSpinnerLastSelect != null){
            String[] temp = mSpinnerLastSelect.split(",");
            mTop.spinnerName.setSelection(Integer.parseInt(temp[0]));
            mTop.spinnerCost.setSelection(Integer.parseInt(temp[1]));
            mTop.spinnerAttr.setSelection(Integer.parseInt(temp[2]));
            mTop.spinnerEvent.setSelection(Integer.parseInt(temp[3]));
            mTop.spinnerFrontName.setSelection(Integer.parseInt(temp[4]));
            mTop.spinnerLevel.setSelection(Integer.parseInt(temp[5]));
            mSpinnerLastSelect = null;
        }
    }

    private void setSpinner(Spinner spinner, String columnType, String defaultStr, int gametype)
    {
        List<CardInfo> cardArray = mOrmHelper.getCardInfoDao().queryCardDropList(columnType, gametype);
        Collections.sort(cardArray, droplistComparator);
        cardArray.add(0, new CardInfo(defaultStr));
        SpinnerCommonAdapter adapterName =
                new SpinnerCommonAdapter( mContext, cardArray);
        spinner.setAdapter(adapterName);
    }

    public String getSpinnerSelectedIndexs(){
        return mTop.spinnerName.getSelectedItemPosition() + "," +
                mTop.spinnerCost.getSelectedItemPosition() + "," +
                mTop.spinnerAttr.getSelectedItemPosition() + "," +
                mTop.spinnerEvent.getSelectedItemPosition() + "," +
                mTop.spinnerFrontName.getSelectedItemPosition() + "," +
                mTop.spinnerLevel.getSelectedItemPosition() + ",";
    }

    public CardInfo getSpinnerInfo(){
        if(mTop.spinnerName.getSelectedItem() == null || mTop.spinnerCost.getSelectedItem() == null ||
                mTop.spinnerAttr.getSelectedItem() == null || mTop.spinnerEvent.getSelectedItem() == null ||
                mTop.spinnerFrontName.getSelectedItem() == null || mTop.spinnerLevel.getSelectedItem() == null)
            return null;

        CardInfo card = new CardInfo();
        CardInfo spinnerSelected;

        spinnerSelected = (CardInfo) mTop.spinnerName.getSelectedItem();
        if(!spinnerSelected.getName().equals(DEFAULT_NAME))
        {
            card.setName(spinnerSelected.getName());
        }
        EventInfo spinnerSelected2 = (EventInfo) mTop.spinnerEvent.getSelectedItem();
        if(!spinnerSelected2.getName().equals(DEFAULT_EVENT))
        {
            card.setEventId(spinnerSelected2.getId());
        }
        spinnerSelected = (CardInfo) mTop.spinnerCost.getSelectedItem();
        if(!spinnerSelected.getName().equals(DEFAULT_COST))
        {
            card.setCost(Integer.parseInt(spinnerSelected.getName()));
        }
        spinnerSelected = (CardInfo) mTop.spinnerAttr.getSelectedItem();
        if(!spinnerSelected.getName().equals(DEFAULT_ATTR))
        {
            card.setAttrId(spinnerSelected.getAttrId());
        }
        spinnerSelected = (CardInfo) mTop.spinnerFrontName.getSelectedItem();
        if(!spinnerSelected.getName().equals(DEFAULT_FRONT_NAME))
        {
            card.setFrontName(spinnerSelected.getName());
        }
        spinnerSelected = (CardInfo) mTop.spinnerLevel.getSelectedItem();
        if(!spinnerSelected.getName().equals(DEFAULT_LEVEL))
        {
            card.setLevel(spinnerSelected.getName());
        }
        return card;
    }

    public void setGameList(int gameType, List<GameInfo> list){
        mSpinnerGameData = list;
        SpinnerCommonAdapter adapter;
        adapter = new SpinnerCommonAdapter( mContext, list);
        mTop.spinnerGameList.setAdapter(adapter);
        setSpinnerItemSelectedByValue2( mTop.spinnerGameList, String.valueOf(gameType));
    }

    class Top{
        @BindView(R.id.spinnerName) Spinner spinnerName;
        @BindView(R.id.spinnerCost) Spinner spinnerCost;
        @BindView(R.id.spinnerAttr) Spinner spinnerAttr;
        @BindView(R.id.spinnerEvent) Spinner spinnerEvent;
        @BindView(R.id.spinnerGame) Spinner spinnerGameList;
        @BindView(R.id.spinnerFrontName) Spinner spinnerFrontName;
        @BindView(R.id.spinnerLevel) Spinner spinnerLevel;

        @OnClick(R.id.btnRefresh)
        void btnRefreshClickListener(){
            mTopHandle.onRefresh();

            mSpinnerChangedCount = 0;
            if(spinnerName.getSelectedItemPosition() > 0)
                mSpinnerChangedCount++;
            if(spinnerCost.getSelectedItemPosition() > 0)
                mSpinnerChangedCount++;
            if(spinnerAttr.getSelectedItemPosition() > 0)
                mSpinnerChangedCount++;
            if(spinnerEvent.getSelectedItemPosition() > 0)
                mSpinnerChangedCount++;
            if(spinnerFrontName.getSelectedItemPosition() > 0)
                mSpinnerChangedCount++;
            if(spinnerLevel.getSelectedItemPosition() > 0)
                mSpinnerChangedCount++;

            if(mSpinnerChangedCount == 0){ //没有变化
                mTopHandle.onSearchData(); //默认检索
            }else{
                spinnerName.setSelection(0);
                spinnerCost.setSelection(0);
                spinnerAttr.setSelection(0);
                spinnerEvent.setSelection(0);
                spinnerFrontName.setSelection(0);
                spinnerLevel.setSelection(0);
            }

        }

        @OnItemSelected({R.id.spinnerName, R.id.spinnerCost, R.id.spinnerAttr, R.id.spinnerEvent, R.id.spinnerFrontName, R.id.spinnerLevel})
        void onSelectlistener(){
            if(mSpinnerChangedCount > 0)
                mSpinnerChangedCount--;
            if(mSpinnerChangedCount == 0)
                mTopHandle.onSearchData();
        }

        @OnItemSelected(R.id.spinnerGame)
        void onGameTypeSelectlistener(int position){
            int gameType = mSpinnerGameData.get(position).getId();
            CommonUtil.setGameType(mContext, gameType);
            mTopHandle.onGameTypeChanged(gameType);
            initializeSppinersByGame(gameType); // 检索入口 由setSelection触发
        }


        Top(){
            ButterKnife.bind(this, mContext);
        }

    }

    public interface TopHandle{
        void onSearchData();
        void onRefresh();
        void onGameTypeChanged(int type);
    }

    private static Comparator<CardInfo> droplistComparator = new Comparator<CardInfo>() {

        @Override
        public int compare(CardInfo o1, CardInfo o2) {
            return o2.getNid() - o1.getNid();
        }
    };
}
