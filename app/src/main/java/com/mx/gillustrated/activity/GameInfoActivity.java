package com.mx.gillustrated.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.mx.gillustrated.R;
import com.mx.gillustrated.adapter.CardTypeListAdapter;
import com.mx.gillustrated.adapter.CardTypeListAdapter.DespairTouchListener;
import com.mx.gillustrated.listener.ListenerListViewScrollHandler;
import com.mx.gillustrated.provider.Providerdata;
import com.mx.gillustrated.util.DBHelper;
import com.mx.gillustrated.vo.CardTypeInfo;
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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class GameInfoActivity extends BaseActivity {
	
	private Button btnAdd;
	private TextView tvGameName;
	private ListView mLvGameMain;
	private RelativeLayout pageVboxLayout;
	
	private List<CardTypeInfo> mList;
	private int mGameType;
	private CardTypeListAdapter mAdapter;

	@BindView(R.id.chkOrientation)
	CheckBox chkOrientation;

	@OnCheckedChanged(R.id.chkOrientation)
	void onOrientationCheckedChanged(CheckBox checkBox){
		mSP.edit().putBoolean(SHARE_IMAGE_ORIENTATION, checkBox.isChecked()).commit();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_gameinfo);
		ButterKnife.bind(this);

		mGameType = getIntent().getIntExtra("game", -1);
		
		btnAdd = (Button) findViewById(R.id.btnGameAdd);
		btnAdd.setOnClickListener(onAddBtnClickListerner);
		mLvGameMain = (ListView) findViewById(R.id.lvGameInfoMain);
		tvGameName = (TextView) findViewById(R.id.tvGameName);
		GameInfo gameinfoList = mOrmHelper.getGameInfoDao().queryForId(mGameType);
		tvGameName.setText(gameinfoList.getName());
		pageVboxLayout = (RelativeLayout) findViewById(R.id.pageVBox);
		pageVboxLayout.setVisibility(View.GONE);


		chkOrientation.setChecked(mSP.getBoolean(SHARE_IMAGE_ORIENTATION, false));

		mLvGameMain.setOnScrollListener(new ListenerListViewScrollHandler(mLvGameMain, pageVboxLayout));
		mList = new ArrayList<CardTypeInfo>();
		mAdapter = new CardTypeListAdapter(this, mList);
		mAdapter.setDespairTouchListener(despairTouchListener);
		
		searchMain();
		
	}
	
	private void  searchMain(){
		mainHandler.post( new Runnable() {
			@Override
			public void run() {
				List<CardTypeInfo> list = mOrmHelper.getCardTypeInfoDao().queryForEq(Providerdata.CardType.COLUMN_GAMETYPE, mGameType);
				Message msg = mainHandler.obtainMessage();
				msg.what = 1;
				msg.obj = list;
				mainHandler.sendMessage(msg);
			}
		});
	}
	
	Handler mainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 1){
				mList.clear();
				List<CardTypeInfo> result = (List<CardTypeInfo>) msg.obj;
				mList.addAll(result);
				updateList(true);
			}
		}
		
	};
	
	DespairTouchListener despairTouchListener = new DespairTouchListener(){

		@Override
		public void onSaveBtnClickListener(CardTypeInfo info) {
			// TODO Auto-generated method stub
			long result = mOrmHelper.getCardTypeInfoDao().update(info);
			if( result > -1) {
				Toast.makeText(GameInfoActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
				searchMain();
			}
		}
		
	};
	
	View.OnClickListener onAddBtnClickListerner = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {		
			mList.add(0, new CardTypeInfo(-1, mGameType));
			updateList(false);
		}
	};
	
	private void updateList(boolean flag) {
		if (flag)
			mLvGameMain.setAdapter(mAdapter);
		else
			mAdapter.notifyDataSetChanged();
	}
	
}
