package com.mx.gillustrated.adapter;

import java.io.File;
import java.util.List;

import com.mx.gillustrated.R;
import com.mx.gillustrated.activity.BaseActivity;
import com.mx.gillustrated.activity.MainActivity;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.util.CommonUtil;
import com.mx.gillustrated.vo.CardInfo;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DataListAdapter extends BaseAdapter {

	private MainActivity mcontext;
	private LayoutInflater layoutInflator;
	private List<CardInfo> list;

	public DataListAdapter() {
	}

	public DataListAdapter(MainActivity context, List<CardInfo> items) {
		mcontext = context;
		layoutInflator = LayoutInflater.from(mcontext);
		list = items;
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
					R.layout.adapter_mainlist, null);
			component= new Component();
			component.ivHeader = (ImageView) convertView
					.findViewById(R.id.ivHeader);
			component.tvName = (TextView) convertView
					.findViewById(R.id.tvName);
			component.tvFrontName = (TextView) convertView
					.findViewById(R.id.tvFrontName);
			component.tvAttr = (TextView) convertView
					.findViewById(R.id.tvAttr);
			component.tvCost = (TextView) convertView
					.findViewById(R.id.tvCost);
			component.tvHP = (TextView) convertView
					.findViewById(R.id.tvHP);
			component.tvAttack = (TextView) convertView
					.findViewById(R.id.tvAttack);
			component.tvDefense = (TextView) convertView
					.findViewById(R.id.tvDefense);
			component.tvNid = (TextView) convertView
					.findViewById(R.id.tvNid);
			component.tvTotal = (TextView) convertView
					.findViewById(R.id.tvTotal);
			convertView.setTag(component);

			if( list.get(arg0).getNid() > 0)
				component.tvNid.setVisibility(View.VISIBLE);
			else
				component.tvNid.setVisibility(View.GONE);
		}
		else
		{
			component = (Component) convertView.getTag();
		}

		boolean showHeader = mcontext.mSP.getBoolean(BaseActivity.SHARE_SHOW_HEADER_IMAGES + list.get(arg0).getGameId(), false);
		if(showHeader){
			component.tvTotal.setVisibility(View.GONE);
			try {
				File imageDir = new File(Environment.getExternalStorageDirectory(), MConfig.SD_HEADER_PATH + "/" + list.get(arg0).getGameId());
				File file = new File(imageDir.getPath(), list.get(arg0).getId() + "_h.png");
				if(file.exists())
				{
					Bitmap bmp = MediaStore.Images.Media.getBitmap(mcontext.getContentResolver(), Uri.fromFile(file));
					component.ivHeader.setImageBitmap(bmp);
				}
				else
					component.ivHeader.setImageBitmap(null);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			component.tvTotal.setVisibility(View.VISIBLE);
			int total = list.get(arg0).getMaxHP() + list.get(arg0).getMaxAttack() + list.get(arg0).getMaxDefense();
			component.tvTotal.setText( String.valueOf(total));
		}

		component.tvName.setText(list.get(arg0).getName());
		component.tvFrontName.setText(list.get(arg0).getFrontName());
		String attr = list.get(arg0).getAttr();
		component.tvAttr.setText(attr);
		component.tvCost.setText(String.valueOf(list.get(arg0).getCost()));
		component.tvHP.setText(String.valueOf(list.get(arg0).getMaxHP()));
		component.tvAttack.setText(String.valueOf(list.get(arg0).getMaxAttack()));
		component.tvDefense.setText(String.valueOf(list.get(arg0).getMaxDefense()));
		component.tvNid.setText(String.valueOf(list.get(arg0).getNid()));

		return convertView;
	}

	private static class Component{
		 public ImageView ivHeader;
		 public TextView tvFrontName;
		 public TextView tvName;
		 public TextView tvAttr;
		 public TextView tvCost;
		 public TextView tvHP;
		 public TextView tvAttack;
		 public TextView tvDefense;
		 public TextView tvNid;
		 public TextView tvTotal;

	}
}
