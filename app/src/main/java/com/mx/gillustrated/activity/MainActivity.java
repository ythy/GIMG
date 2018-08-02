package com.mx.gillustrated.activity;

import java.util.ArrayList;
import java.util.List;
import com.mx.gillustrated.common.DBCall;
import com.mx.gillustrated.component.MainActivityHeader;
import com.mx.gillustrated.component.MainActivityListView;
import com.mx.gillustrated.component.MainActivityTop;
import com.mx.gillustrated.dialog.DialogExportImg;
import com.mx.gillustrated.R;
import com.mx.gillustrated.adapter.DataListAdapter;
import com.mx.gillustrated.listener.ListenerListViewScrollHandler;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.util.DataBakUtil;
import com.mx.gillustrated.util.ServiceUtils;
import com.mx.gillustrated.vo.CardInfo;
import com.mx.gillustrated.vo.GameInfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import butterknife.BindView;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity {

    private int mGameType = 0; //游戏类别
    private MainActivityHeader mMainActivityHeader;
    private MainActivityTop mMainActivityTop;
    private MainActivityListView mMainActivityListView;

    @BindView(R.id.etPinyin) EditText etPinyin;
    @BindView(R.id.btnShowEvents) Button btnEvents;

    @OnClick(R.id.btnShowEvents)
    void onBtnShowEventsClick(){
        Intent intentEvent = new Intent(MainActivity.this, EventsActivity.class);
        intentEvent.putExtra("game", mGameType);
        startActivity(intentEvent);
    }

    @OnTextChanged(R.id.etPinyin)
    void onEtPinyinChanged(){
        boolean breaks = etPinyin.getTag() != null && (boolean) etPinyin.getTag();//防止change事件二次检索
        if(breaks){
            etPinyin.setTag(false);
            return;
        }
        String input = etPinyin.getText().toString();
        mMainActivityListView.searchData(input);
    }

    @OnClick(R.id.btnAdd)
    void btnAddClickListener(){
        Intent intent = new Intent(MainActivity.this, AddCardActivity.class);
        intent.putExtra("game", mGameType);
        startActivity(intent);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
        mGameType = getIntent().getIntExtra("game", CommonUtil.getGameType(this));
        ButterKnife.bind(this);

        mMainActivityHeader = new MainActivityHeader(this, new MainActivityHeader.HeaderHandle(){
            @Override
            public void onHeaderClick(String column) {
                mMainActivityListView.searchDataOrderby(column);
            }
        });

        mMainActivityTop = new MainActivityTop(this, mOrmHelper, new MainActivityTop.TopHandle(){
            @Override
            public void onSearchData() {
                mMainActivityHeader.setHeaderColor(mMainActivityListView.getOrderBy().split("\\*")[0]);
                CardInfo card = mMainActivityTop.getSpinnerInfo();
                if(card != null) {
                    card.setGameId(mGameType);
                    mMainActivityListView.searchData(card);
                }
            }
            @Override
            public void onRefresh() {
                mMainActivityListView.setOrderBy(null, null);
                etPinyin.setTag(true);
                etPinyin.setText("");
            }
        });

        mMainActivityListView = new MainActivityListView(this, mOrmHelper, new MainActivityListView.DataViewHandle(){
            @Override
            public void onListItemClick(CardInfo info, int position, int totalCount, int currentPage) {
                Intent intent = new Intent(MainActivity.this,
                        DetailActivity.class);
                intent.putExtra("card", info.getId());
                intent.putExtra("cardSearchCondition", mMainActivityListView.getSearchCondition().getCardSearchParam());
                intent.putExtra("orderBy", mMainActivityListView.getOrderBy());
                intent.putExtra("positon", position);
                intent.putExtra("totalCount", totalCount);
                intent.putExtra("currentPage", currentPage);
                intent.putExtra("spinnerIndexs", mMainActivityTop.getSpinnerSelectedIndexs() );
                startActivity(intent);
            }

            @Override
            public void onSearchCompleted() {
                mProgressDialog.dismiss();
            }

            @Override
            public void onSearchStart() {
                mProgressDialog.show();
            }
        });


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



    private void setGameList()
	{
        ServiceUtils.createConnect(new DBCall<List<GameInfo>>() {
            @Override
            public List<GameInfo> enqueue() {
                return mOrmHelper.getGameInfoDao().queryForAll();
            }
        }).doOnNext(new Consumer<List<GameInfo>>() {
            @Override
            public void accept(List<GameInfo> list) throws Exception {
                if(list.size() > 0){
                    mMainActivityTop.setGameList(mGameType, list);
                }else{
                    new Thread() {
                        public void run() {
                            DataBakUtil.getDataFromFiles(mOrmHelper);
                            mainHandler.sendEmptyMessage(1);
                        }
                    }.start();
                }
            }
        }).subscribe();
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
                    CommonUtil.generateHeaderImg(MainActivity.this, mMainActivityListView.getIdList() , mGameType, index == 2 ? false : true);
                    mainHandler.sendEmptyMessage(3);
					}
				}.start();
			}
			else if (msg.what == 3) {
				Toast.makeText(MainActivity.this, "生成头像完成", Toast.LENGTH_SHORT).show();
			}
		}
	};



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId())
        {
        	case  R.id.menu_out :
                DataBakUtil.saveDataToFiles(mOrmHelper);
                Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
	            break;
        	case  R.id.menu_gamelist :
        		Intent intent = new Intent(MainActivity.this, GameListActivity.class);
    			startActivity(intent);
		        break;
        	case  R.id.menu_header :
        		DialogExportImg.show(this, mMainActivityListView.getDataList().get(0).getId(), mGameType, mainHandler);
		        break;
            case  R.id.menu_eventlist :
                onBtnShowEventsClick();
                break;
        }
        return true;
    }

}

