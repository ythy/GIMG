package com.mx.gillustrated.listener;

import com.mx.gillustrated.R;

import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ListenerListViewScrollHandler implements OnScrollListener {
	
	private boolean isLastRow = false;
    private int lastCount = 0; //避免反复触发last事件
	private ListView deliverylist;
	private RelativeLayout pageVboxLayout; //计数用   例： 1/4
	private TextView pageText;
	private int mAddRowNum = 0;//增加了几个额外行 刷新行或者表头行都计入数量
	private ScrollHandle mScrollHandle;

	public ListenerListViewScrollHandler(ListView lv, RelativeLayout rl, int addRowNum, ScrollHandle scrollHandle)
	{
		mAddRowNum = addRowNum;
		mScrollHandle = scrollHandle;
		init(lv, rl);
	}

	public ListenerListViewScrollHandler(ListView lv, RelativeLayout rl)
	{
		init(lv, rl);
	}

	private void init(ListView lv, RelativeLayout rl){
		deliverylist = lv;
		pageVboxLayout = rl;
		pageText = (TextView) pageVboxLayout.findViewById(R.id.pageText);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
        if(firstVisibleItem == 0)
            lastCount = 0;

		if (pageVboxLayout.getVisibility() == View.VISIBLE) {
			setPageBox(true);
		}
		if (firstVisibleItem + visibleItemCount == totalItemCount
				&& totalItemCount > 0 && lastCount < totalItemCount) {
            isLastRow = true;
            lastCount = totalItemCount;
		}
	}

	private void setPageBox(boolean isVisible){
		int totalCount = deliverylist.getCount() - mAddRowNum;
		int visibleCount = deliverylist.getLastVisiblePosition() + 1 - mAddRowNum;
		pageText.setText( isVisible ? visibleCount + "/" + totalCount : "");
		pageVboxLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
			setPageBox(true);
		}
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
			setPageBox(true);
		}
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			setPageBox(false);
			if(isLastRow && mScrollHandle != null) {
				mScrollHandle.scrollLastRow(deliverylist.getCount() - mAddRowNum);
				isLastRow = false;
			}
		}
	}

	public interface ScrollHandle{
		void scrollLastRow(int totalItemCount);
	}
	 

}
