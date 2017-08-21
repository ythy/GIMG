package com.mx.gillustrated.activity;

import java.util.ArrayList;
import java.util.List;

import com.mx.gillustrated.R;
import com.mx.gillustrated.adapter.GameListAdapter;
import com.mx.gillustrated.adapter.GameListAdapter.DespairTouchListener;
import com.mx.gillustrated.listener.ListenerListViewScrollHandler;
import com.mx.gillustrated.provider.Providerdata;
import com.mx.gillustrated.util.DBHelper;
import com.mx.gillustrated.vo.GameInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GameListActivity extends BaseActivity {
	
	private Button mBtnAdd;
	private ListView mLvDespairMain;
	private GameListAdapter mAdapter;
	private List<GameInfo> mList;
	private RelativeLayout pageVboxLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);
			
		mBtnAdd = (Button) findViewById(R.id.btnDespairAdd);
		mBtnAdd.setOnClickListener(onAddBtnClickListerner);
		
		mLvDespairMain = (ListView) findViewById(R.id.lvDespairMain);
		pageVboxLayout = (RelativeLayout) findViewById(R.id.pageVBox);
		pageVboxLayout.setVisibility(View.GONE);
		
		mLvDespairMain.setOnScrollListener(new ListenerListViewScrollHandler(mLvDespairMain, pageVboxLayout));
		mList = new ArrayList<GameInfo>();
		mAdapter = new GameListAdapter(this, mList);
		mAdapter.setDespairTouchListener(despairTouchListener);
		
		searchMain();
		
	}
	
	DespairTouchListener despairTouchListener = new DespairTouchListener(){

		@Override
		public void onSaveBtnClickListener(GameInfo info) {
			long result = mDBHelper.updateGameName(info);
			if( result > -1) {
				Toast.makeText(GameListActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
				searchMain();
			}
				
		}

		@Override
		public void onDetailBtnClickListener(GameInfo info) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(GameListActivity.this, GameInfoActivity.class);
			intent.putExtra("game", info.getId());
			startActivity(intent);
		}
		
	};
	
	View.OnClickListener onAddBtnClickListerner = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mList.add(0, new GameInfo());
			updateList(false);
		}
	};
	
	Handler mainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 1){
				mList.clear();
				mList.addAll((List<GameInfo>) msg.obj);
				updateList(true);
			}
		}
		
	};
	
	private void searchMain(){
		mainHandler.post( new Runnable() {
			@Override
			public void run() {
				List<GameInfo> list = mDBHelper.queryGameList(null);
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

