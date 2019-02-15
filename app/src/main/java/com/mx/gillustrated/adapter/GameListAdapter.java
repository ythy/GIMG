package com.mx.gillustrated.adapter;

import java.util.List;

import com.mx.gillustrated.R;
import com.mx.gillustrated.vo.GameInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GameListAdapter extends BaseAdapter {

	private Context mcontext;
	private LayoutInflater layoutInflator;
	private List<GameInfo> list;
	private DespairTouchListener mListener;
	
	public GameListAdapter() {
	}

	public GameListAdapter(Context context, List<GameInfo> items) {
		mcontext = context;
		layoutInflator = LayoutInflater.from(mcontext);
		list = items;
	}
	
	public void setDespairTouchListener( DespairTouchListener listener) {
		mListener = listener;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		Component component = null;
		
		if (convertView == null) {
			convertView = layoutInflator.inflate(
					R.layout.adapter_game, null);
			component= new Component();
			component.tvName = (EditText) convertView
					.findViewById(R.id.etGameName);
			component.btnSave = (Button) convertView
					.findViewById(R.id.btnDespairModify);
			component.btnDetail = (Button) convertView
					.findViewById(R.id.btnGameDetail);
			convertView.setTag(component);
		}
		else
			component = (Component) convertView.getTag();  
		
		final Component currentComponent = component;
		final int position = arg0;
		try {
			
			component.tvName.setText(list.get(arg0).getName());
			component.btnSave.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String name = currentComponent.tvName.getText().toString();
					final int id = list.get(position).getId();
					GameInfo despairInfo = new GameInfo();
					despairInfo.setId(id);
					despairInfo.setName(name);
					despairInfo.setDetail(list.get(position).getDetail());
					mListener.onSaveBtnClickListener(despairInfo);
				}
			});
			component.btnDetail.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String name = currentComponent.tvName.getText().toString();
					final int id = list.get(position).getId();
					GameInfo despairInfo = new GameInfo();
					despairInfo.setId(id);
					despairInfo.setName(name);
					mListener.onDetailBtnClickListener(despairInfo);
				}
			});
			
			component.tvName.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
		                 //   index = position;
	                }
	                return false;
				}
	        });
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return convertView;
	}
	
	public interface DespairTouchListener {
		public void onSaveBtnClickListener( GameInfo info );
		public void onDetailBtnClickListener( GameInfo info );
	}
	
	private static class Component{
		 public EditText tvName;  
		 public Button btnSave;
		 public Button btnDetail;  
	}
}
