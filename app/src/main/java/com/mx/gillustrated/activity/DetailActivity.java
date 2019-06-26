package com.mx.gillustrated.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.mx.gillustrated.R;
import com.mx.gillustrated.adapter.SpinnerCommonAdapter;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.component.ResourceController;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.util.PinyinUtil;
import com.mx.gillustrated.util.UIUtils;
import com.mx.gillustrated.vo.CardEventInfo;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailActivity extends BaseActivity {

	private EditText etHP;
	private EditText etAttack;
	private EditText etDefense;
	private EditText etName;
	private EditText etFrontName;
	private EditText etDetail;
	private EditText etNid;
	private CardInfo mCardInfo;
	private Spinner spinnerAttr;
	private Spinner spinnerLevel;
	private EditText etCost;
	private CheckBox chkModify;
	private TextView tvId;
	private int mId;

	private String[] mMainSearchInfo;
	private String mMainSearchOrderBy;
	private int mCurrentPosition;
	private int mMainTotalCount;

	private SparseArray<File> mImagesFiles;
	private SparseArray<View> mImagesView;
	private LinearLayout mLLImages;
	private List<EventInfo> mEventList;
	private SparseArray<View> mEventView = new SparseArray<View>();
	private ResourceController mResourceController;

	@BindView(R.id.tv_header_hp)
	TextView tvHeaderNumber1;

	@BindView(R.id.tv_header_A)
	TextView tvHeaderNumber2;

	@BindView(R.id.tv_header_D)
	TextView tvHeaderNumber3;


	@BindView(R.id.btnSaveEvent)
	Button btnSaveEvent;

	@BindView(R.id.btnAddEvent)
	Button btnAddEvent;

	@BindView(R.id.llShowEvent)
	LinearLayout llShowEvent;

	@BindView(R.id.scrollView)
	ScrollView mScrollView;

	@OnClick(R.id.btnAddEvent)
	void onAddEventClickHandler(){
		addEvent();
		mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.smoothScrollTo(0, 5000);
			}
		});
	}

	@OnClick(R.id.btnSaveEvent)
	void onSaveEvnetClickHandler(){
		List<CardEventInfo> events = new ArrayList<>();
		for( int i = 0; i < mEventView.size(); i++ )
		{
			Spinner spinner = (Spinner) mEventView.get(mEventView.keyAt(i)).findViewById(R.id.spinnerEvent);
			EventInfo info = (EventInfo) spinner.getSelectedItem();
			events.add(new CardEventInfo(mCardInfo.getId(), info.getId()));
		}
		mOrmHelper.getCardEventInfoDao().addCardEvents(events);
		Toast.makeText(DetailActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		ButterKnife.bind(this);

		Intent intent = getIntent();
		mId = intent.getIntExtra("card", 0);
		mMainSearchInfo = intent.getStringArrayExtra("cardSearchCondition");
		mMainSearchOrderBy = intent.getStringExtra("orderBy");
		mCurrentPosition = intent.getIntExtra("positon", -1);
		mMainTotalCount = intent.getIntExtra("totalCount", 0);
		mCardInfo = mOrmHelper.getCardInfoDao().queryForId(mId);
		mResourceController = new ResourceController(this, mCardInfo.getGameId() );

		chkModify = (CheckBox) findViewById(R.id.chkModify);
		etHP = (EditText) findViewById(R.id.etDetailHP);
		etAttack = (EditText) findViewById(R.id.etDetailAttack);
		etDefense = (EditText) findViewById(R.id.etDetailDefense);
		etName = (EditText) findViewById(R.id.etDetailName);
		etFrontName = (EditText) findViewById(R.id.etDetailFrontName);
		etNid = (EditText) findViewById(R.id.etDetailNid);
		etDetail = (EditText) findViewById(R.id.etDetail);

		Button btnSave = (Button) findViewById(R.id.btnSave);
		Button btnSave2 = (Button) findViewById(R.id.btnSave2);
		btnSave.setOnClickListener(btnSaveClickListener);
		btnSave2.setOnClickListener(btnSaveClickListener);
		Button btnDel = (Button) findViewById(R.id.btnDel);
		btnDel.setOnClickListener(btnDelClickListener);
		Button btnDel2 = (Button) findViewById(R.id.btnDel2);
		btnDel2.setOnClickListener(btnDel2ClickListener);

		tvHeaderNumber1.setText(mResourceController.getNumber1());
		tvHeaderNumber2.setText(mResourceController.getNumber2());
		tvHeaderNumber3.setText(mResourceController.getNumber3());

		tvId = (TextView) findViewById(R.id.tvId);
		mLLImages = (LinearLayout) findViewById(R.id.llImages);
		
		spinnerAttr = (Spinner) findViewById(R.id.spinnerAttr);
		List<CardTypeInfo> cardTypes = mOrmHelper.getCardTypeInfoDao().queryForEq(CardInfo.COLUMN_GAMETYPE, mCardInfo.getGameId());
		SpinnerCommonAdapter<CardTypeInfo> adapterName =
				new SpinnerCommonAdapter<CardTypeInfo>( this, cardTypes);
		spinnerAttr.setAdapter(adapterName);

		spinnerLevel = (Spinner) findViewById(R.id.spinnerLevel);
		etCost = (EditText) findViewById(R.id.etDetailCost);

		Button mBtnNext = (Button) findViewById(R.id.btnNext);
		mBtnNext.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				searchCardSide(1);
			}
		});
		Button mBtnLast = (Button) findViewById(R.id.btnLast);
		mBtnLast.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchCardSide(-1);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		showEvents();
		showCardInfo();
	}

	private void showEvents(){
		llShowEvent.removeAllViews();
		mEventList = mOrmHelper.getEventInfoDao().getListByGameId(mCardInfo.getGameId(), "Y");
		mEventList.add(0, new EventInfo(""));

		List<CardEventInfo> events = mOrmHelper.getCardEventInfoDao().getListByCardId(mCardInfo.getId());
		for( int i = 0; i < events.size(); i++ )
		{
			if(isExistInEvent(events.get(i).getEventId())){
				Spinner spinner = addEvent();
				CommonUtil.setSpinnerItemSelectedByValue2(spinner, String.valueOf(events.get(i).getEventId()));
			}
		}
	}

	private boolean isExistInEvent(int id){
		if(mEventList == null)
			return false;
		for (int i = 0; i < mEventList.size(); i++){
			if(mEventList.get(i).getId() == id)
				return true;
		}
		return false;
	}

	private void searchCardSide(int type){
		int newPositon = mCurrentPosition;
		newPositon += type;
		if(newPositon < 0 || newPositon >= mMainTotalCount){
			Toast.makeText(getBaseContext(), "顶端/底端", Toast.LENGTH_SHORT).show();
			return;
		}
		String order = mMainSearchOrderBy.split("\\*")[0];
		boolean isDesc = mMainSearchOrderBy.split("\\*")[1].equals( CardInfo.SORT_ASC) ? true : false;
		CardInfo result = mOrmHelper.getCardInfoDao().queryCards(new CardInfo(mMainSearchInfo) , order, isDesc, newPositon, 1L ).get(0);

		if(result != null){
			mCurrentPosition = newPositon;
			mCardInfo = result;
			mId = mCardInfo.getId();
			showEvents();
			showCardInfo();
		}
	}
	
	private void showCardInfo(){
		CardInfo info = mCardInfo;
		etHP.setText(info.getMaxHP() == 0 ? ""
				: String.valueOf(info.getMaxHP()));
		etAttack.setText(info.getMaxAttack() == 0 ? "" : String.valueOf(info
				.getMaxAttack()));
		etDefense.setText(info.getMaxDefense() == 0 ? "" : String.valueOf(info
				.getMaxDefense()));
		etName.setText(info.getName());
		etFrontName.setText(info.getFrontName());
		etDetail.setText(info.getRemark());
		etNid.setText(String.valueOf(info.getNid()));
		String attr = String.valueOf(info.getAttrId());
		CommonUtil.setSpinnerItemSelectedByValue2(spinnerAttr, attr);
			
		CommonUtil.setSpinnerItemSelectedByValue(spinnerLevel,
				String.valueOf(info.getLevel()));

		etCost.setText(String.valueOf(info.getCost()));
		tvId.setText(String.valueOf(info.getId()));
		
		mImagesFiles = new SparseArray<File>();
		mImagesView = new SparseArray<View>();
		mLLImages.removeAllViews();
		showImages();
	}
	
	View.OnClickListener btnSaveClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			CardInfo card;
			long result = 0;
			// 名称优先批量更新
			if (!chkModify.isChecked()
					&& !mCardInfo.getName().equals("")
					&& !mCardInfo.getName().equals(
							etName.getText().toString().trim())) {
				CardInfo cardOld = mCardInfo;
				card = new CardInfo();
				card.setName(etName.getText().toString().trim());
				card.setPinyinName(PinyinUtil.convert(card.getName()));
				result = mOrmHelper.getCardInfoDao().updateCardName(card, cardOld);
			}

			card = new CardInfo();
			card.setId(mId);
			card.setNid(Integer.parseInt(etNid.getText().toString()));
			card.setGameId(mCardInfo.getGameId());
			CardTypeInfo cardTypeInfo = (CardTypeInfo) spinnerAttr.getSelectedItem();
			card.setAttrId(cardTypeInfo.getId());
			card.setLevel(spinnerLevel.getSelectedItem().toString());

			card.setCost(etCost.getText().toString().trim().equals("") ? 0
					: Integer.parseInt(etCost.getText().toString()));
			card.setName(etName.getText().toString().trim());
			card.setPinyinName(PinyinUtil.convert(card.getName()));
			card.setFrontName(etFrontName.getText().toString().trim());
			card.setRemark(etDetail.getText().toString().trim());
			card.setMaxHP(etHP.getText().toString().trim().equals("") ? 0
					: Integer.parseInt(etHP.getText().toString()));
			card.setMaxAttack(etAttack.getText().toString().trim().equals("") ? 0
					: Integer.parseInt(etAttack.getText().toString()));
			card.setMaxDefense(etDefense.getText().toString().trim().equals("") ? 0
					: Integer.parseInt(etDefense.getText().toString()));
			result += mOrmHelper.getCardInfoDao().update(card);

			if (result > 0) {
				Intent intent = new Intent(DetailActivity.this,
						MainActivity.class);
				intent.putExtra("game", mCardInfo.getGameId());
				intent.putExtra("orderBy", mMainSearchOrderBy);
				intent.putExtra("spinnerIndexs", getIntent().getStringExtra("spinnerIndexs"));
				intent.putExtra("position", mCurrentPosition);
				intent.putExtra("currentPage", getIntent().getIntExtra("currentPage", 1));
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				DetailActivity.this.finish();
			} else
				Toast.makeText(DetailActivity.this, "保存失败", Toast.LENGTH_SHORT)
						.show();

		}
	};

	View.OnClickListener btnDelClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			new AlertDialog.Builder(DetailActivity.this)
					.setMessage("确定要删除吗")
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									long result = mOrmHelper.getCardInfoDao().deleteById(mCardInfo.getId());
									if (result != -1) {
										
										for(int i = 0; i < mImagesFiles.size(); i++)
										{
											CommonUtil.deleteImage(DetailActivity.this,
													mImagesFiles.get(mImagesFiles.keyAt(i)));
										}

										Intent intent = new Intent(
												DetailActivity.this,
												MainActivity.class);
										intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(intent);
										DetailActivity.this.finish();
									} else
										Toast.makeText(DetailActivity.this,
												"删除失败", Toast.LENGTH_SHORT)
												.show();
								}
							}).setNegativeButton("Cancel", null).show();
		}
	};
	
	View.OnClickListener btnDel2ClickListener = new View.OnClickListener() {

		@Override 
		public void onClick(View v) {
			for(int i = 0; i < mImagesView.size(); i++){
				Button btnDel = (Button) mImagesView.valueAt(i).findViewById(R.id.btnDel);
				btnDel.setVisibility(View.VISIBLE);
				Button btnAjust = (Button) mImagesView.valueAt(i).findViewById(R.id.btnAdjust);
				btnAjust.setVisibility(View.VISIBLE);
			}			
		}
		
		
	};
	
	private void showImages()
	{
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			File fileDir = new File(Environment.getExternalStorageDirectory(),
					MConfig.SD_PATH + "/" + mCardInfo.getGameId());
			int index = 0;
			while(++index < 20){
				File imageFile = new File(fileDir.getPath(), CommonUtil.getImageFrontName(mId, index));
				Bitmap bitmap = null;
				if (imageFile.exists())
				{
					mImagesFiles.append(index, imageFile);
					try {
						bitmap = MediaStore.Images.Media.getBitmap(
								this.getContentResolver(), Uri.fromFile(imageFile));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					View child = LayoutInflater.from(DetailActivity.this).inflate(
							R.layout.child_images_gap, null);
					mLLImages.addView(child);
					mImagesView.append(index, child);

					boolean isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mCardInfo.getGameId(), false);
					TextView tvDate = (TextView) child.findViewById(R.id.tvDate);
					tvDate.setText(CommonUtil.getFileLastModified(imageFile));
					ImageView image = (ImageView) child.findViewById(R.id.imgDetails);
					image.setImageBitmap(isOrientation ? CommonUtil.rotatePic(bitmap, 90) : bitmap );
					final int oldIndex = index;
					Button btnAdjust = (Button) child.findViewById(R.id.btnAdjust);
					btnAdjust.setOnClickListener(new View.OnClickListener() {
													 @Override
													 public void onClick(View v) {
														 Intent intent = new Intent(DetailActivity.this, ImageAdjustActivity.class);
														 intent.putExtra("source", mImagesFiles.get(oldIndex).getAbsolutePath());
														 startActivity(intent);
													 }
												 });
					Button btnDel = (Button) child.findViewById(R.id.btnDel);
					btnDel.setTag(index + "*" + 0); 
					btnDel.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String[] tag = v.getTag().toString().split("\\*");
							int key = Integer.parseInt(tag[0]);
							View line = mImagesView.get(key);
							long timenow = Calendar.getInstance().getTime().getTime();
							if(Math.abs(timenow - Long.valueOf(tag[1])) > 5000)
							{
								Toast.makeText(DetailActivity.this, "请再次点击删除", Toast.LENGTH_SHORT).show();
								v.setTag(key + "*" + timenow);
							}else{
								v.setTag(key + "*" + 0);
								CommonUtil.deleteImage(DetailActivity.this, mImagesFiles.get(key)); 
								mLLImages.removeView(line);
								mImagesView.remove(key);
								mImagesFiles.remove(key);
								Toast.makeText(DetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
							}
								
						}
					});				
				}
			}
		}
	}
	
	Handler detailHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 100) {
				showImages();
			}
		}

	};

	private Spinner addEvent(){
		final View child = LayoutInflater.from(DetailActivity.this).inflate(
				R.layout.child_event, null);
		llShowEvent.addView(child);

		final InlineEvent event = new InlineEvent(child);
		UIUtils.setSpinnerSingleClick(event.spinner);
		SpinnerCommonAdapter<EventInfo> adapter =
				new SpinnerCommonAdapter( DetailActivity.this, mEventList);
		event.spinner.setAdapter(adapter);

		event.btnDetail.setOnClickListener(new Button.OnClickListener(
		) {
			@Override
			public void onClick(View v) {
				EventInfo info = (EventInfo) event.spinner.getSelectedItem();
				Intent intent = new Intent(DetailActivity.this, EventInfoActivity.class);
				intent.putExtra("event", info.getId());
				intent.putExtra("game", mCardInfo.getGameId());
				startActivity(intent);
			}
		});

		int roundtag = (int) Math.round(Math.random() * 100000000);
		event.btnDel.setTag( roundtag + "*" + 0);

		event.btnDel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] tag = v.getTag().toString().split("\\*");
				EventInfo info = (EventInfo) event.spinner.getSelectedItem();
				int id = info.getId();
				if( id == 0 ) {
					llShowEvent.removeView(child);
					mEventView.remove(Integer.parseInt(tag[0]));
				}else{
					long timenow = Calendar.getInstance().getTime().getTime();
					if(Math.abs(timenow - Long.valueOf(tag[1])) > 5000)
					{
						Toast.makeText(DetailActivity.this, "请再次点击删除", Toast.LENGTH_SHORT).show();
						v.setTag(tag[0] + "*" + timenow);
					}else{
						v.setTag(tag[0] + "*" + 0);
						long r = mOrmHelper.getCardEventInfoDao().delCardEvents(new CardEventInfo(mCardInfo.getId(), id));
						{
							llShowEvent.removeView(child);
							mEventView.remove(Integer.parseInt(tag[0]));
							Toast.makeText(DetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		});

		mEventView.append(roundtag, child);
		return event.spinner;
	}

	static class InlineEvent{

		@BindView(R.id.spinnerEvent)
		Spinner spinner;

		@BindView(R.id.btnDetail)
		Button btnDetail;

		@BindView(R.id.btnDel)
		Button btnDel;


		public InlineEvent(View view){
			ButterKnife.bind(this, view);
		}

	}


}
