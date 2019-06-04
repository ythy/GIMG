package com.mx.gillustrated.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.mx.gillustrated.R;
import com.mx.gillustrated.adapter.CardTypeListAdapter;
import com.mx.gillustrated.adapter.CardTypeListAdapter.DespairTouchListener;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.component.ResourceController;
import com.mx.gillustrated.listener.ListenerListViewScrollHandler;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.GameInfo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;

public class GameInfoActivity extends BaseActivity {

    private Button btnAdd;
    private ListView mLvGameMain;
    private RelativeLayout pageVboxLayout;

    private List<CardTypeInfo> mList;
    private int mGameType;
    private CardTypeListAdapter mAdapter;
    private ResourceController mResourceController;

    @BindView(R.id.et_number1)
    EditText mEtNumber1;

    @BindView(R.id.et_number2)
    EditText mEtNumber2;

    @BindView(R.id.et_number3)
    EditText mEtNumber3;


    @BindView(R.id.etGameDetail)
    EditText mEtGameDetail;

    @BindView(R.id.etGameName)
    EditText mEtGameName;

    @BindView(R.id.chkOrientation)
    CheckBox chkOrientation;

    @BindView(R.id.chkOrientationEvent)
    CheckBox chkOrientationE;

    @BindView(R.id.chkEventGap)
    CheckBox chkEventGap;

    @OnTextChanged(R.id.et_number1)
    void onNumber1TextChanged(CharSequence text){
        mResourceController.setNumber1(text.toString());
    }

    @OnTextChanged(R.id.et_number2)
    void onNumber2TextChanged(CharSequence text){
        mResourceController.setNumber2(text.toString());
    }

    @OnTextChanged(R.id.et_number3)
    void onNumber3TextChanged(CharSequence text){
        mResourceController.setNumber3(text.toString());
    }

    @OnCheckedChanged(R.id.chkOrientation)
    void onOrientationCheckedChanged(CheckBox checkBox) {
        mSP.edit().putBoolean(SHARE_IMAGE_ORIENTATION + mGameType, checkBox.isChecked()).commit();
    }

    @OnCheckedChanged(R.id.chkOrientationEvent)
    void onOrientationECheckedChanged(CheckBox checkBox) {
        mSP.edit().putBoolean(SHARE_IMAGE_ORIENTATION_EVENT + mGameType, checkBox.isChecked()).commit();
    }

    @OnCheckedChanged(R.id.chkEventGap)
    void onEventGapCheckedChanged(CheckBox checkBox) {
        mResourceController.setEventImagesGap(checkBox.isChecked());
    }

    @BindView(R.id.chkHeader)
    CheckBox chkHeader;

    @OnCheckedChanged(R.id.chkHeader)
    void onHeaderCheckedChanged(CheckBox checkBox) {
        mSP.edit().putBoolean(SHARE_SHOW_HEADER_IMAGES + mGameType, checkBox.isChecked()).commit();
    }

    @BindView(R.id.spinnerPager)
    Spinner spinnerPager;

    @OnItemSelected(R.id.spinnerPager)
    void onPagerChanged(int position) {
        String[] array = getResources().getStringArray(R.array.pagerArray);
        mSP.edit().putInt(SHARE_PAGE_SIZE + mGameType, Integer.parseInt(array[position])).commit();
    }

    @OnClick(R.id.btnSaveAll)
    void onSaveClickHandler() {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setId(this.mGameType);
        gameInfo.setDetail(mEtGameDetail.getText().toString());
        gameInfo.setName(mEtGameName.getText().toString());
        int result = mOrmHelper.getGameInfoDao().update(gameInfo);
        if (result == 1)
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btnDelAll)
    void onDeleteAllDataHandler() {
        new AlertDialog.Builder(GameInfoActivity.this)
                .setMessage("确定要删除吗")
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mOrmHelper.getCardInfoDao().delCardInfoByGameId(mGameType);
                                mOrmHelper.getEventInfoDao().delEventInfoByGameId(mGameType);
                                mOrmHelper.getCardTypeInfoDao().delCardTypeInfoByGameId(mGameType);
                                mOrmHelper.getGameInfoDao().deleteById(mGameType);

                                File imagesFileDir = new File(
                                        Environment.getExternalStorageDirectory(),
                                        MConfig.SD_PATH + "/" + mGameType);
                                if (imagesFileDir.exists()) {
                                    File[] child = imagesFileDir.listFiles();
                                    for (int i = 0; i < child.length; i++) {
                                        CommonUtil.deleteImage(GameInfoActivity.this, child[i]);
                                    }
                                    imagesFileDir.delete();
                                }
                                File eventFileDir = new File(
                                        Environment.getExternalStorageDirectory(),
                                        MConfig.SD_EVENT_PATH + "/" + mGameType);
                                if (eventFileDir.exists()) {
                                    File[] child = eventFileDir.listFiles();
                                    for (int i = 0; i < child.length; i++) {
                                        CommonUtil.deleteImage(GameInfoActivity.this, child[i]);
                                    }
                                    eventFileDir.delete();
                                }

                                Intent intent = new Intent(
                                        GameInfoActivity.this,
                                        GameListActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                GameInfoActivity.this.finish();
                            }
                        }).setNegativeButton("Cancel", null).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gameinfo);
        ButterKnife.bind(this);

        mGameType = getIntent().getIntExtra("game", 0);
        mResourceController = new ResourceController(this, mGameType);
        btnAdd = (Button) findViewById(R.id.btnGameAdd);
        btnAdd.setOnClickListener(onAddBtnClickListerner);
        mLvGameMain = (ListView) findViewById(R.id.lvGameInfoMain);
        GameInfo gameinfoList = mOrmHelper.getGameInfoDao().queryForId(mGameType);
        mEtGameName.setText(gameinfoList.getName());
        mEtGameDetail.setText(gameinfoList.getDetail());
        pageVboxLayout = (RelativeLayout) findViewById(R.id.pageVBox);
        pageVboxLayout.setVisibility(View.GONE);

        mEtNumber1.setText(mResourceController.getNumber1());
        mEtNumber2.setText(mResourceController.getNumber2());
        mEtNumber3.setText(mResourceController.getNumber3());

        chkOrientation.setChecked(mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mGameType, false));
        chkOrientationE.setChecked(mSP.getBoolean(SHARE_IMAGE_ORIENTATION_EVENT + mGameType, false));
        chkEventGap.setChecked(mResourceController.getEventImagesGap());
        chkHeader.setChecked(mSP.getBoolean(SHARE_SHOW_HEADER_IMAGES + mGameType, false));
        int pagerSize = mSP.getInt(SHARE_PAGE_SIZE + mGameType, 50);
        String[] pagerArray = getResources().getStringArray(R.array.pagerArray);
        int position = 1;
        for (int i = 0; i < pagerArray.length; i++)
            if (Integer.parseInt(pagerArray[i]) == pagerSize)
                position = i;
        spinnerPager.setSelection(position);

        mLvGameMain.setOnScrollListener(new ListenerListViewScrollHandler(mLvGameMain, pageVboxLayout));
        mList = new ArrayList<CardTypeInfo>();
        mAdapter = new CardTypeListAdapter(this, mList);
        mAdapter.setDespairTouchListener(despairTouchListener);

        searchMain();

    }

    private void searchMain() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                List<CardTypeInfo> list = mOrmHelper.getCardTypeInfoDao().queryForEq(CardTypeInfo.COLUMN_GAMETYPE, mGameType);
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
            if (msg.what == 1) {
                mList.clear();
                List<CardTypeInfo> result = (List<CardTypeInfo>) msg.obj;
                mList.addAll(result);
                updateList(true);
            }
        }

    };

    DespairTouchListener despairTouchListener = new DespairTouchListener() {

        @Override
        public void onSaveBtnClickListener(CardTypeInfo info) {
            // TODO Auto-generated method stub
            Dao.CreateOrUpdateStatus result = mOrmHelper.getCardTypeInfoDao().createOrUpdate(info);
            if (result.isCreated() || result.isUpdated()) {
                Toast.makeText(GameInfoActivity.this, result.isCreated() ? "新增成功" : "更新成功", Toast.LENGTH_SHORT).show();
                searchMain();
            }
        }

        @Override
        public void onDelBtnClickListener(final CardTypeInfo info) {
            new AlertDialog.Builder(GameInfoActivity.this)
                    .setMessage("确定要删除吗")
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    int result = mOrmHelper.getCardTypeInfoDao().delCardTypeInfoById(info.getId(), info.getGameId());
                                    if (result > -1) {
                                        Toast.makeText(GameInfoActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                        searchMain();
                                    }
                                }
                            })
                    .setNegativeButton("Cancel", null).show();
        }


    };

    View.OnClickListener onAddBtnClickListerner = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mList.add(0, new CardTypeInfo(mGameType));
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
