package com.mx.gillustrated.component;

import android.view.View;
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

    public MainActivityHeader(MainActivity context, HeaderHandle headerHandle){
        mContext = context;
        mHeaderHandle = headerHandle;
        initialize();
    }

    private void initialize(){
        mListHeaderView = new ListHeaderView();
        setHeaderClickHandler(mListHeaderView.tvHP, CardInfo.COLUMN_MAXHP);
        setHeaderClickHandler(mListHeaderView.tvAttack, CardInfo.COLUMN_MAXATTACK);
        setHeaderClickHandler(mListHeaderView.tvDefense, CardInfo.COLUMN_MAXDEFENSE);
        setHeaderClickHandler(mListHeaderView.tvName, CardInfo.COLUMN_NAME);
        setHeaderClickHandler(mListHeaderView.tvAttr, CardInfo.COLUMN_ATTR);
        setHeaderClickHandler(mListHeaderView.tvCost, CardInfo.COLUMN_COST);
        setHeaderClickHandler(mListHeaderView.tvImg, CardInfo.COLUMN_NID);

        mTextViewMap = new HashMap<String, TextView>(){{
            put(CardInfo.COLUMN_NID, mListHeaderView.tvImg);
            put(CardInfo.COLUMN_MAXHP, mListHeaderView.tvHP);
            put(CardInfo.COLUMN_MAXATTACK, mListHeaderView.tvAttack);
            put(CardInfo.COLUMN_MAXDEFENSE, mListHeaderView.tvDefense);
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
