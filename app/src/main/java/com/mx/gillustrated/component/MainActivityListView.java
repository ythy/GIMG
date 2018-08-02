package com.mx.gillustrated.component;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mx.gillustrated.R;
import com.mx.gillustrated.activity.DetailActivity;
import com.mx.gillustrated.activity.MainActivity;
import com.mx.gillustrated.adapter.DataListAdapter;
import com.mx.gillustrated.database.DataBaseHelper;
import com.mx.gillustrated.listener.ListenerListViewScrollHandler;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.vo.CardInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by maoxin on 2018/8/2.
 */

public class MainActivityListView {
    private static String INIT_ORDER_BY = CardInfo.ID;
    private static String INIT_ORDER_TYPE = CardInfo.SORT_DESC;

    private MainActivity mContext;
    private DataBaseHelper mOrmHelper;
    private DataView mDataView;
    private DataViewHandle mDataViewHandle;
    private List<CardInfo> mList;
    private DataListAdapter mAdapter;
    private int mListViewLastPosition; //保存最后一次本页面滚动位置

    private static final int  PAGE_SIZE = 20;
    private int currentPage;
    private int initPage; //初始页数，默认是1  如果从详细页面返回，可能为1+
    private int totalItemCount;

    private CardInfo mSearchCondition;
    private String mOrderBy;
    private String mIsAsc;


    public MainActivityListView(MainActivity context, DataBaseHelper ormHelper, DataViewHandle handle){
        mContext = context;
        mOrmHelper = ormHelper;
        mDataViewHandle = handle;
        mDataView = new DataView();
        initialize();
    }

    private void initialize(){
        mListViewLastPosition = mContext.getIntent().getIntExtra("position", 0);
        currentPage = initPage = mContext.getIntent().getIntExtra("currentPage", 1);
        String order =  mContext.getIntent().getStringExtra("orderBy");
        mOrderBy = order == null ? INIT_ORDER_BY : order.split("\\*")[0];
        mIsAsc =  order == null ? INIT_ORDER_TYPE : order.split("\\*")[1];

        mDataView.pageVboxLayout.setVisibility(android.view.View.GONE);
        mDataView.listViewMain.setOnItemClickListener(itemClickListener);
        mDataView.listViewMain.setOnScrollListener(new ListenerListViewScrollHandler( mDataView.listViewMain,  mDataView.pageVboxLayout, 0,
                new ListenerListViewScrollHandler.ScrollHandle(){
                    @Override
                    public void scrollLastRow(int totalCount) {
                        if( totalCount == totalItemCount  )
                            return;
                        currentPage++;
                        search();
                    }
                }));
        mList = new ArrayList();
        mAdapter = new DataListAdapter(mContext, mList);
    }


    public void search(){
        mDataViewHandle.onSearchStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CardInfo> list =  mOrmHelper.getCardInfoDao().queryCards(mSearchCondition, mOrderBy, mIsAsc == CardInfo.SORT_ASC,
                        initPage == 1 ? (currentPage - 1) * PAGE_SIZE : 0, initPage * PAGE_SIZE );
                Message msg = Message.obtain();
                msg.obj = list;
                handler.sendMessage(msg);
            }
        }).start();
    }

    public void searchData(final CardInfo info){
        mSearchCondition = info;
        currentPage = 1;
        search();
    }

    public void searchData(String pinyin){
        mSearchCondition.setPinyinName(pinyin);
        currentPage = 1;
        search();
    }

    public void searchDataOrderby(String order){
        if(mOrderBy.equals(order))
            mIsAsc = mIsAsc.equals(CardInfo.SORT_ASC) ? CardInfo.SORT_DESC : CardInfo.SORT_ASC;
        else
            mIsAsc = CardInfo.SORT_DESC;
        mOrderBy = order;
        currentPage = 1;
        search();
    }

    public void setOrderBy(String orderBy, String isAsc){
        mOrderBy = orderBy == null ? INIT_ORDER_BY : orderBy;
        mIsAsc = isAsc == null ? INIT_ORDER_TYPE : isAsc;
    }

    public String getOrderBy(){
        return mOrderBy + "*" + mIsAsc;
    }

    public CardInfo getSearchCondition(){
        return mSearchCondition;
    }

    public int[] getIdList(){
        int[] list = new int[mList.size()];
        for(int i = 0; i < mList.size(); i++)
            list[i] = mList.get(i).getId();
        return list;
    }

    public  List<CardInfo> getDataList(){
        return mList;
    }


    private void updateList(boolean flag) {
        if (flag)
            mDataView.listViewMain.setAdapter(mAdapter);
        else
            mAdapter.notifyDataSetChanged();

        if(mListViewLastPosition > 0 ){
            mDataView.listViewMain.setSelection(mListViewLastPosition);
            mListViewLastPosition = 0;
        }

    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                long arg3) {
            if (position >= 0) {
                CardInfo info = (CardInfo) arg0.getItemAtPosition(position);
                mDataViewHandle.onListItemClick(info, position, arg0.getCount(), currentPage);
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            List<CardInfo> list = (List<CardInfo>) msg.obj;
            totalItemCount = list.size() == 0 ? 0 : list.get(0).getTotalCount();

            if(currentPage == 1 || initPage > 1){
                initPage = 1;//恢复 只有初始第一次可能 >1
                mList.clear();
                mList.addAll(list);
                updateList(true);
            }else{
                mList.addAll(list);
                updateList(false);
            }
            mDataViewHandle.onSearchCompleted();
        }
    };

    class DataView{
        @BindView(R.id.lvMain)
        ListView listViewMain;
        @BindView(R.id.pageVBox)
        RelativeLayout pageVboxLayout;

        DataView(){
            ButterKnife.bind(this, mContext);
        }

    }

    public interface DataViewHandle{
        void onListItemClick(CardInfo info, int position, int totalCount, int currentPage);
        void onSearchCompleted();
        void onSearchStart();
    }

}
