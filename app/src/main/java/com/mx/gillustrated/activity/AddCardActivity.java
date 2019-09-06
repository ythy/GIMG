package com.mx.gillustrated.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.mx.gillustrated.R;
import com.mx.gillustrated.adapter.SpinnerCommonAdapter;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.util.PinyinUtil;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.MatrixInfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class AddCardActivity extends BaseActivity {

	private Button btnSave;
	private Spinner spinnerAttr;
	private Spinner spinnerLevel;
	private Spinner spinnerType;
	private EditText etId;
	private EditText etNid;
	private EditText etName;
	private EditText etFrontName;
	private EditText etCost;
	private EditText etHP;
	private EditText etAttack;
	private EditText etDefense;
	private ImageView ivNumber;
	private ImageView ivAll;
	private File m_fileNumber;
	private File m_fileAll;
	private Bitmap m_BitMapNumber;
	private Bitmap m_BitMapAll;
	private int mGameType;
	private File mImagesFileDir;
	private Button btnDelNumber;
	private Button btnDelAll;

	@BindView(R.id.chkAdjustImg)
	CheckBox chkAdjustImg;

	@BindView(R.id.tvAdjustImgTop)
	EditText tvAdjustImgTop;

	@BindView(R.id.tvAdjustImgBottom)
	EditText tvAdjustImgBottom;

	@BindView(R.id.tvAdjustImgLeft)
	EditText tvAdjustImgLeft;

	@BindView(R.id.tvAdjustImgRight)
	EditText tvAdjustImgRight;

	@OnClick(R.id.btnSaveMatrix)
	void onSaveMatrixBtnClick(){
		int top = Integer.parseInt(this.tvAdjustImgTop.getText().toString());
		int bottom = Integer.parseInt(this.tvAdjustImgBottom.getText().toString());
		int left = Integer.parseInt(this.tvAdjustImgLeft.getText().toString());
		int right = Integer.parseInt(this.tvAdjustImgRight.getText().toString());
		mSP.edit().putString(SHARE_IMAGES_MATRIX_NUMBER + mGameType, top + "," + bottom + "," + left + "," + right).commit();
		if(chkAdjustImg.isChecked()){
			try {
				this.showPicture();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}

	@OnCheckedChanged(R.id.chkAdjustImg)
	void onCheckAdjustImgTopChanged(boolean checked){
		mSP.edit().putBoolean(SHARE_IMAGES_MATRIX + mGameType, checked).commit();
		try {
			this.showPicture();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		ButterKnife.bind(this);

		mGameType = getIntent().getIntExtra("game", 0);
				
		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(btnSaveClickListener);

		btnDelNumber = (Button) findViewById(R.id.btnDelNumber);
		btnDelNumber.setOnClickListener(btnDelNumberClickListener);
		btnDelAll = (Button) findViewById(R.id.btnDelAll);
		btnDelAll.setOnClickListener(btnDelAllClickListener);

		spinnerAttr = (Spinner) findViewById(R.id.spinnerAttr);
		spinnerLevel = (Spinner) findViewById(R.id.spinnerLevel);
		CommonUtil.setSpinnerItemSelectedByValue(spinnerLevel, "5");
		etNid = (EditText) findViewById(R.id.etDetailNid);
		etName = (EditText) findViewById(R.id.etDetailName);
		etFrontName = (EditText) findViewById(R.id.etDetailFrontName);
		etCost = (EditText) findViewById(R.id.etDetailCost);
		etId = (EditText) findViewById(R.id.etDetailId);
		etHP = (EditText) findViewById(R.id.etDetailHP);
		etAttack = (EditText) findViewById(R.id.etDetailAttack);
		etDefense = (EditText) findViewById(R.id.etDetailDefense);
		spinnerType  = (Spinner) findViewById(R.id.spinnerType);
		spinnerType.setSelection(0);
		spinnerType.setOnItemSelectedListener(onTypeSelectlistener);
		
		ivNumber = (ImageView) findViewById(R.id.imgWithNumber);
		ivAll = (ImageView) findViewById(R.id.imgAll);
		
		List<CardTypeInfo> cardTypes = mOrmHelper.getCardTypeInfoDao().queryForEq("game_type", mGameType);
		SpinnerCommonAdapter<CardTypeInfo> adapterName =
				new SpinnerCommonAdapter( this, cardTypes);
		spinnerAttr.setAdapter(adapterName);

		setImagesMatrixConfig();

		try {
			showPicture();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void setImagesMatrixConfig(){
		String[] numbers = mSP.getString(SHARE_IMAGES_MATRIX_NUMBER + mGameType, "0,0,0,0").split(",");
		tvAdjustImgTop.setText(numbers[0]);
		tvAdjustImgBottom.setText(numbers[1]);
		tvAdjustImgLeft.setText(numbers[2]);
		tvAdjustImgRight.setText(numbers[3]);
		chkAdjustImg.setChecked(mSP.getBoolean(SHARE_IMAGES_MATRIX + mGameType, false));
	}

	private Bitmap getMatrixBitmap(Bitmap input){
		int top = Integer.parseInt(this.tvAdjustImgTop.getText().toString());
		int bottom = Integer.parseInt(this.tvAdjustImgBottom.getText().toString());
		int left = Integer.parseInt(this.tvAdjustImgLeft.getText().toString());
		int right = Integer.parseInt(this.tvAdjustImgRight.getText().toString());

		MatrixInfo matrixinfo = new MatrixInfo();
		matrixinfo.setY( top );
		matrixinfo.setX( left );
		matrixinfo.setHeight( top + bottom );
		matrixinfo.setWidth( left + right );

		return CommonUtil.cutBitmap(input, matrixinfo, true);
	}
	
	
	private void showPicture() throws FileNotFoundException, IOException {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			File fileDir = new File(Environment.getExternalStorageDirectory(),
					MConfig.SRC_PATH);
			if (!fileDir.exists())
				fileDir = new File(Environment.getExternalStorageDirectory(),
						MConfig.SRC_PATH_SAMSUNG);
			if (!fileDir.exists())
				return;

			File file = new File(fileDir.getPath());
			File[] fs = file.listFiles();
			if(fs == null)
				return;
			Arrays.sort(fs, new Comparator<File>() {
				public int compare(File f1, File f2) {
					long diff = f1.lastModified() - f2.lastModified();
					if (diff > 0)
						return -1;
					else if (diff == 0)
						return 0;
					else
						return 1;
				}

				public boolean equals(Object obj) {
					return true;
				}

			});

			btnDelAll.setVisibility(View.GONE);
			btnDelNumber.setVisibility(View.GONE);
			ivAll.setImageBitmap(null);
			ivNumber.setImageBitmap(null);
			boolean isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mGameType, false);
			for (int i = 0; i < fs.length; i++) {
				if (i == 2)
					break;

				Bitmap bmp = MediaStore.Images.Media.getBitmap(
						this.getContentResolver(), Uri.fromFile(fs[i]));
				if(this.chkAdjustImg.isChecked())
					bmp = getMatrixBitmap(bmp);

				if (i == 0) {
					m_fileAll = fs[i];
					m_BitMapAll = bmp;
					ivAll.setImageBitmap(isOrientation ? CommonUtil.rotatePic(bmp, 90) : bmp );
					btnDelAll.setVisibility(View.VISIBLE);
				} else {
					m_fileNumber = fs[i];
					m_BitMapNumber = bmp;
					ivNumber.setImageBitmap(isOrientation ? CommonUtil.rotatePic(bmp, 90) : bmp );
					btnDelNumber.setVisibility(View.VISIBLE);
				}
			}

		}

	}
	
	AdapterView.OnItemSelectedListener onTypeSelectlistener = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int index,
				long arg3) {

			String[] array = getResources().getStringArray(R.array.addType);
			if( array[index].equals("更新附加图") || array[index].equals("更新数值图") ||  array[index].equals("新增单张图"))
			{
				ivNumber.setImageDrawable(null);
				btnDelNumber.setVisibility(View.GONE);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}

	};


	View.OnClickListener btnDelNumberClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			CommonUtil.deleteImages(getBaseContext(), m_fileNumber);
			try {
				showPicture();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	View.OnClickListener btnDelAllClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			CommonUtil.deleteImages(getBaseContext(), m_fileAll);
			try {
				showPicture();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	View.OnClickListener btnSaveClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			mProgressDialog.show();
			new Thread() {
				public void run() {
					
					CardInfo card = new CardInfo();
					if(!"".equals(etNid.getText().toString().trim()))
						card.setNid(Integer.parseInt(etNid.getText().toString()));

					CardTypeInfo cardTypeInfo = (CardTypeInfo) spinnerAttr.getSelectedItem();
					card.setAttrId(cardTypeInfo.getId());
					card.setGameId(mGameType);
					card.setLevel(spinnerLevel.getSelectedItem().toString());
					card.setName(etName.getText().toString().trim());
					card.setPinyinName(PinyinUtil.convert(card.getName()));
					card.setFrontName(etFrontName.getText().toString().trim());
					card.setProfile("Y");
					if (!etCost.getText().toString().trim().equals(""))
						card.setCost(Integer.parseInt(etCost.getText()
								.toString()));
					else
						card.setCost(0);
					if (!etHP.getText().toString().trim().equals(""))
						card.setMaxHP(Integer.parseInt(etHP.getText()
								.toString()));
					if (!etAttack.getText().toString().trim().equals(""))
						card.setMaxAttack(Integer.parseInt(etAttack.getText()
								.toString()));
					if (!etDefense.getText().toString().trim().equals(""))
						card.setMaxDefense(Integer.parseInt(etDefense.getText()
								.toString()));
					int type = spinnerType.getSelectedItemPosition();
					
					mImagesFileDir = new File(
							Environment.getExternalStorageDirectory(),
							MConfig.SD_PATH + "/" + mGameType);
					if(!mImagesFileDir.exists()){
						mImagesFileDir.mkdirs();
			        }
					
					if(type == 0 || type == 1){
						mOrmHelper.getCardInfoDao().create(card);
						long newId = card.getId();
						if (newId != 0) {
							if (m_BitMapAll != null) {
								if(type == 0 && m_BitMapNumber != null )
								{
									createImages((int)newId, m_BitMapNumber, 1);
								}				
								if(type == 0 || type == 1)
								{
									if(m_BitMapNumber != null)
										createImages((int)newId, m_BitMapAll, type == 0 ? 2 : 1);
									else
										createImages((int)newId, m_BitMapAll, 1);
								}
								
								CommonUtil.deleteImages(AddCardActivity.this,
										m_fileAll);
								if(type == 0)
									CommonUtil.deleteImages(AddCardActivity.this,
											m_fileNumber);
							}
							Message msg = Message.obtain();
							msg.what = 1;
							msg.arg1 = (int) newId;
							addHandler.sendMessage(msg);
						}
					}else{
						// 更新数值图  更新附加图
						int id = Integer.parseInt(etId.getText().toString().trim());					
						int nextnum = getNextImagesIndex(id);
						File imageFile = new File(mImagesFileDir.getPath(),
								CommonUtil.getImageFrontName( id, 1));
						File nextFile = new File(mImagesFileDir.getPath(),
								CommonUtil.getImageFrontName( id, nextnum));
						if(type == 2){
							imageFile.renameTo(nextFile);
							createImages(id, m_BitMapAll, 1);
							card.setId(id);
							card.setLevel(null);
							card.setFrontName(null);
							card.setName(null);
							if(card.getCost() > 0 || card.getMaxHP() > 0 || card.getMaxAttack() > 0 || card.getMaxDefense() > 0)
								mOrmHelper.getCardInfoDao().update(card);
						}
						else if(type == 3)
							try {
								CommonUtil.exportImgFromBitmap(m_BitMapAll, nextFile);
							} catch (IOException e) {
								e.printStackTrace();
							} 
						
						CommonUtil.deleteImages(AddCardActivity.this,
								m_fileAll);
						addHandler.sendEmptyMessage(2);
					}
				}
			}.start();
		}
	};
	
	private void createImages(int id, Bitmap bitmap, int num){
		File imageFile;
		FileOutputStream bos;
		imageFile = new File(mImagesFileDir.getPath(),
				CommonUtil.getImageFrontName(id, num));
		try {
			bos = new FileOutputStream(imageFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG,
					30, bos);
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private int getNextImagesIndex(int id){
		int checknum = 3;
		while(true){
			File check = new File(mImagesFileDir.getPath(),
					CommonUtil.getImageFrontName( id, checknum));
			if(!check.exists()) 
				break;
			else
				checknum++;
		}
		return checknum;
	}
	

	private static class AddHandler extends Handler{

		private final WeakReference<AddCardActivity> mActivity;

		AddHandler( AddCardActivity activity){
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				Toast.makeText(mActivity.get(), "id: " + msg.arg1, Toast.LENGTH_SHORT).show();
				mActivity.get().forwardBack();
			} else if (msg.what == 2) {
				mActivity.get().forwardBack();
			}
		}
	}

	Handler addHandler = new AddHandler(this);

	
	private void forwardBack()
	{
		Intent intent = new Intent(AddCardActivity.this,
				MainActivity.class);
		intent.putExtra("game", mGameType);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		AddCardActivity.this.finish();
		mProgressDialog.dismiss();
	}
	
}
