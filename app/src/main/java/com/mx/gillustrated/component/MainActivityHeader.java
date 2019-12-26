package com.mx.gillustrated.component;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mx.gillustrated.R;
import com.mx.gillustrated.activity.MainActivity;
import com.mx.gillustrated.vo.CardInfo;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by maoxin on 2018/7/31.
 */

public class MainActivityHeader {

    private Map<String, TextView> mTextViewMap;
    private ListHeaderView mListHeaderView;
    private MainActivity mContext;
    private HeaderHandle mHeaderHandle;
    private ResourceController mResourceController;

    public MainActivityHeader(MainActivity context, HeaderHandle headerHandle, int gameId){
        mContext = context;
        mHeaderHandle = headerHandle;
        initialize();
        setResourceController(gameId);
    }

    public void setResourceController(int gameId){
        mResourceController = new ResourceController(mContext, gameId);
        mListHeaderView.tvHP.setText(mResourceController.getNumber1());
        mListHeaderView.tvAttack.setText(mResourceController.getNumber2());
        mListHeaderView.tvDefense.setText(mResourceController.getNumber3());
        mListHeaderView.tvExtra1.setText(mResourceController.getNumber4());
        mListHeaderView.tvExtra2.setText(mResourceController.getNumber5());
        if( "E1".equals(mResourceController.getNumber4())){
            mListHeaderView.ivExtraGap1.setVisibility(View.GONE);
            mListHeaderView.tvExtra1.setVisibility(View.GONE);
        }else{
            mListHeaderView.ivExtraGap1.setVisibility(View.VISIBLE);
            mListHeaderView.tvExtra1.setVisibility(View.VISIBLE);
        }
        if( "E2".equals(mResourceController.getNumber5())){
            mListHeaderView.ivExtraGap2.setVisibility(View.GONE);
            mListHeaderView.tvExtra2.setVisibility(View.GONE);
        }else{
            mListHeaderView.ivExtraGap2.setVisibility(View.VISIBLE);
            mListHeaderView.tvExtra2.setVisibility(View.VISIBLE);
        }
        if(!mContext.mSP.getBoolean(mContext.SHARE_SHOW_COST_COLUMN + gameId, false)){
            mListHeaderView.ivCostGap.setVisibility(View.GONE);
            mListHeaderView.tvCost.setVisibility(View.GONE);
        }else{
            mListHeaderView.ivCostGap.setVisibility(View.VISIBLE);
            mListHeaderView.tvCost.setVisibility(View.VISIBLE);
        }
    }

    private void initialize(){
        mListHeaderView = new ListHeaderView();
        setHeaderClickHandler(mListHeaderView.tvHP, CardInfo.COLUMN_MAXHP);
        setHeaderClickHandler(mListHeaderView.tvAttack, CardInfo.COLUMN_MAXATTACK);
        setHeaderClickHandler(mListHeaderView.tvDefense, CardInfo.COLUMN_MAXDEFENSE);
        setHeaderClickHandler(mListHeaderView.tvExtra1, CardInfo.COLUMN_EXTRA_VALUE1);
        setHeaderClickHandler(mListHeaderView.tvExtra2, CardInfo.COLUMN_EXTRA_VALUE2);
        setHeaderClickHandler(mListHeaderView.tvName, CardInfo.COLUMN_NAME);
        setHeaderClickHandler(mListHeaderView.tvAttr, CardInfo.COLUMN_ATTR);
        setHeaderClickHandler(mListHeaderView.tvCost, CardInfo.COLUMN_COST);
        setHeaderClickHandler(mListHeaderView.tvImg, CardInfo.COLUMN_NID);

        mTextViewMap = new HashMap<String, TextView>(){{
            put(CardInfo.COLUMN_NID, mListHeaderView.tvImg);
            put(CardInfo.COLUMN_MAXHP, mListHeaderView.tvHP);
            put(CardInfo.COLUMN_MAXATTACK, mListHeaderView.tvAttack);
            put(CardInfo.COLUMN_MAXDEFENSE, mListHeaderView.tvDefense);
            put(CardInfo.COLUMN_EXTRA_VALUE1, mListHeaderView.tvExtra1);
            put(CardInfo.COLUMN_EXTRA_VALUE2, mListHeaderView.tvExtra2);
            put(CardInfo.COLUMN_NAME, mListHeaderView.tvName);
            put(CardInfo.COLUMN_ATTR, mListHeaderView.tvAttr);
            put(CardInfo.COLUMN_COST, mListHeaderView.tvCost);
        }};

    }

    private void setHeaderClickHandler(TextView tv, final String column){
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHeaderColor(column);
                mHeaderHandle.onHeaderClick(column);
            }
        });
    }

    public void setHeaderColor(String orderby){
        for(Map.Entry<String, TextView> iterator : mTextViewMap.entrySet()){
            if(iterator.getValue() != null)
                iterator.getValue().setBackgroundColor(mListHeaderView.colorWhite2);
        }
        if(mTextViewMap.get(orderby) != null)
            mTextViewMap.get(orderby).setBackgroundColor(mListHeaderView.colorWhite);
    }

    class ListHeaderView{
        @BindView(R.id.tvHeaderImg)
        TextView tvImg;
        @BindView(R.id.tvHeaderName) TextView tvName;
        @BindView(R.id.tvHeaderAttr) TextView tvAttr;
        @BindView(R.id.tvHeaderCost) TextView tvCost;
        @BindView(R.id.tvHeaderHP) TextView tvHP;
        @BindView(R.id.tvHeaderAttack) TextView tvAttack;
        @BindView(R.id.tvHeaderDefense) TextView tvDefense;
        @BindView(R.id.tvHeaderExtra1) TextView tvExtra1;
        @BindView(R.id.tvHeaderExtra2) TextView tvExtra2;
        @BindView(R.id.ivExtra1Gap) ImageView ivExtraGap1;
        @BindView(R.id.ivExtra2Gap) ImageView ivExtraGap2;
        @BindView(R.id.ivCostGap) ImageView ivCostGap;
        @BindColor(R.color.color_white2) int colorWhite2;
        @BindColor(R.color.color_white) int colorWhite;

        public ListHeaderView()
        {
            View view = mContext.findViewById(R.id.ll_header);
            ButterKnife.bind(this, view);
        }
    }

    public interface HeaderHandle{
        void onHeaderClick(String c);
    }
}
