package com.mx.gillustrated.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.mx.gillustrated.R;
import com.mx.gillustrated.adapter.SpinnerCommonAdapter;
import com.mx.gillustrated.adapter.SpinnerSimpleAdapter;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.provider.Providerdata;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.util.DBHelper;
import com.mx.gillustrated.util.UIUtils;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;

import android.app.Activity;
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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends BaseActivity {

	private Button btnSave;
	private Button btnSave2;
	private Button btnDel;
	private Button btnDel2;
	private Button mBtnNext;
	private Button mBtnLast;
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
    private Spinner spinnerEvents;
	private EditText etCost;
	private CheckBox chkModify;
	private TextView tvId;
	private int mId;

	private CardInfo mMainSearchInfo;
	private String mMainSearchOrderBy;
	private int mCurrentPosition;
	private int mMainTotalCount;
	
	private SparseArray<File> mImagesFiles;
	private SparseArray<View> mImagesView;
	private LinearLayout mLLImages;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		Intent intent = getIntent();
		CardInfo info = intent.getParcelableExtra("card");
		mMainSearchInfo = intent.getParcelableExtra("cardSearchCondition");
		mMainSearchOrderBy = intent.getStringExtra("orderBy");
		mCurrentPosition = intent.getIntExtra("positon", -1);
		mMainTotalCount = intent.getIntExtra("totalCount", 0);
		
		mCardInfo = info;
		mId = info.getId();

		chkModify = (CheckBox) findViewById(R.id.chkModify);
		etHP = (EditText) findViewById(R.id.etDetailHP);
		etAttack = (EditText) findViewById(R.id.etDetailAttack);
		etDefense = (EditText) findViewById(R.id.etDetailDefense);
		etName = (EditText) findViewById(R.id.etDetailName);
		etFrontName = (EditText) findViewById(R.id.etDetailFrontName);
		etNid = (EditText) findViewById(R.id.etDetailNid);
		etDetail = (EditText) findViewById(R.id.etDetail);

		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave2 = (Button) findViewById(R.id.btnSave2);
		btnSave.setOnClickListener(btnSaveClickListener);
		btnSave2.setOnClickListener(btnSaveClickListener);
		btnDel = (Button) findViewById(R.id.btnDel);
		btnDel.setOnClickListener(btnDelClickListener);
		btnDel2 = (Button) findViewById(R.id.btnDel2);
		btnDel2.setOnClickListener(btnDel2ClickListener);
		
		tvId = (TextView) findViewById(R.id.tvId);
		mLLImages = (LinearLayout) findViewById(R.id.llImages);
		
		spinnerAttr = (Spinner) findViewById(R.id.spinnerAttr);
		List<CardTypeInfo> cardTypes = mDBHelper.queryCardTypeList(mCardInfo.getGameId());
		SpinnerCommonAdapter<CardTypeInfo> adapterName =
				new SpinnerCommonAdapter( this, cardTypes);
		spinnerAttr.setAdapter(adapterName);

        spinnerEvents = (Spinner) findViewById(R.id.spinnerEvents);
        EventInfo requst = new EventInfo();
        requst.setGameId(info.getGameId());
		requst.setShowing("Y");
        List<EventInfo> eventList = mDBHelper.queryEventList(requst);
        eventList.add(0, new EventInfo());
        spinnerEvents.setAdapter(new SpinnerSimpleAdapter(this, eventList));

		spinnerLevel = (Spinner) findViewById(R.id.spinnerLevel);
		etCost = (EditText) findViewById(R.id.etDetailCost);
		
		mBtnNext = (Button) findViewById(R.id.btnNext);
		mBtnNext.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				searchCardSide(1);
			}
		});
		mBtnLast = (Button) findViewById(R.id.btnLast);
		mBtnLast.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchCardSide(-1);
			}
		});
		
		showCardInfo();
		
	}
	
	private void searchCardSide(int type){
		int newPositon = mCurrentPosition;
		newPositon += type;
		if(newPositon < 1 || newPositon >= mMainTotalCount){
			Toast.makeText(getBaseContext(), "顶端/底端", Toast.LENGTH_SHORT).show();
			return;
		}	
		CardInfo result = mDBHelper.queryCardSide(mMainSearchInfo, mCardInfo.getGameId(), newPositon, mMainSearchOrderBy);
		if(result != null){
			mCurrentPosition = newPositon;
			mCardInfo = result;
			mId = mCardInfo.getId();
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

		SpinnerSimpleAdapter eventAdapter = (SpinnerSimpleAdapter)spinnerEvents.getAdapter();
		spinnerEvents.setSelection(eventAdapter.getPosition(info.getEventId()));

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
				result = mDBHelper.updateCardName(card, cardOld);
			}

			card = new CardInfo();
			card.setId(mId);
			card.setNid(Integer.parseInt(etNid.getText().toString()));

			CardTypeInfo cardTypeInfo = (CardTypeInfo) spinnerAttr.getSelectedItem();
			card.setAttrId(cardTypeInfo.getId());
			card.setLevel(spinnerLevel.getSelectedItem().toString());

			EventInfo event = (EventInfo) spinnerEvents.getSelectedItem();
            card.setEventId(event.getId());

			card.setCost(Integer.parseInt(etCost.getText().toString()));
			card.setName(etName.getText().toString().trim());
			card.setFrontName(etFrontName.getText().toString().trim());
			card.setRemark(etDetail.getText().toString().trim());
			card.setMaxHP(etHP.getText().toString().trim().equals("") ? 0
					: Integer.parseInt(etHP.getText().toString()));
			card.setMaxAttack(etAttack.getText().toString().trim().equals("") ? 0
					: Integer.parseInt(etAttack.getText().toString()));
			card.setMaxDefense(etDefense.getText().toString().trim().equals("") ? 0
					: Integer.parseInt(etDefense.getText().toString()));
			result += mDBHelper.updateCardInfo(card);

			if (result > 0) {
				Intent intent = new Intent(DetailActivity.this,
						MainActivity.class);
				intent.putExtra("game", mCardInfo.getGameId());
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
									CardInfo card = new CardInfo();
									card.setId(mCardInfo.getId());
									long result = mDBHelper.delCardInfo(card);
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
							R.layout.child_images, null);
					mLLImages.addView(child);
					mImagesView.append(index, child);

					boolean isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION, false);
					ImageView image = (ImageView) child.findViewById(R.id.imgDetails);
					image.setImageBitmap(isOrientation ? CommonUtil.rotatePic(bitmap, 90) : bitmap );
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

}
