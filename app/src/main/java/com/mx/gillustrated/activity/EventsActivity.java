package com.mx.gillustrated.activity;

import java.util.ArrayList;
import java.util.List;

import com.mx.gillustrated.R;
import com.mx.gillustrated.adapter.EventsAdapter;
import com.mx.gillustrated.listener.ListenerListViewScrollHandler;
import com.mx.gillustrated.vo.EventInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class EventsActivity extends BaseActivity {

    @OnClick(R.id.btnDespairAdd)
    void onAddBtnClickListerner(){
        Intent intent = new Intent(EventsActivity.this, EventInfoActivity.class);
        intent.putExtra("event", 0);
        intent.putExtra("game", mGameId);
        startActivity(intent);
    }

    @BindView(R.id.lvDespairMain) ListView mLvDespairMain;
    @BindView(R.id.pageVBox) RelativeLayout pageVboxLayout;
    @OnItemClick(R.id.lvDespairMain)
    void listItemClickHandler(int position){
        Intent intent = new Intent(EventsActivity.this, EventInfoActivity.class);
        intent.putExtra("event", mList.get(position).getId());
        intent.putExtra("game", mGameId);
        startActivity(intent);
    }

    private EventsAdapter mAdapter;
    private List<EventInfo> mList;
    private int mGameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_events);
        ButterKnife.bind(this);

        mGameId = getIntent().getIntExtra("game", 0);
        pageVboxLayout.setVisibility(View.GONE);
        mLvDespairMain.setOnScrollListener(new ListenerListViewScrollHandler(mLvDespairMain, pageVboxLayout));
        mList = new ArrayList<EventInfo>();
        mAdapter = new EventsAdapter(this, mList);

        searchMain();

    }
//
//    DespairTouchListener despairTouchListener = new DespairTouchListener(){
//
//        @Override
//        public void onSaveBtnClickListener(EventInfo info) {
//            EventInfo request = new EventInfo();
//            request.setId(info.getId());
//            request.setName(info.getName());
//            request.setGameId(mGameId);
//            long result = mDBHelper.updateEvent(request);
//            if( result > -1) {
//                Toast.makeText(EventsActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
//                searchMain();
//            }
//
//        }
//    };


    Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                mList.clear();
                mList.addAll((List<EventInfo>) msg.obj);
                updateList(true);
            }
        }

    };

    private void searchMain(){
        mainHandler.post( new Runnable() {
            @Override
            public void run() {
                List<EventInfo> list = mOrmHelper.getEventInfoDao().getListByGameId(mGameId, null);
                Message msg = mainHandler.obtainMessage();
                msg.what = 1;
                msg.obj = list;
                mainHandler.sendMessage(msg);
            }
        });
    }

    private void updateList(boolean flag) {
        if (flag)
            mLvDespairMain.setAdapter(mAdapter);
        else
            mAdapter.notifyDataSetChanged();
    }

}

