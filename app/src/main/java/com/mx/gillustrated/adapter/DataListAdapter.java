package com.mx.gillustrated.adapter;

import java.io.File;
import java.util.List;

import com.mx.gillustrated.R;
import com.mx.gillustrated.activity.BaseActivity;
import com.mx.gillustrated.activity.MainActivity;
import com.mx.gillustrated.common.MConfig;
import com.mx.gillustrated.component.ResourceController;
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
			component.tvExtra1 = (TextView) convertView
					.findViewById(R.id.tvExtra1);
			component.tvExtra2 = (TextView) convertView
					.findViewById(R.id.tvExtra2);
			component.tvNid = (TextView) convertView
					.findViewById(R.id.tvNid);
			component.tvTotal = (TextView) convertView
					.findViewById(R.id.tvTotal);
			component.ivExtraGap1 = (ImageView) convertView
					.findViewById(R.id.ivExtra1Gap);
			component.ivExtraGap2 = (ImageView) convertView
					.findViewById(R.id.ivExtra2Gap);
			component.ivCostGap = (ImageView) convertView
					.findViewById(R.id.ivCostGap);
			convertView.setTag(component);

			if( list.get(arg0).getNid() > 0)
				component.tvNid.setVisibility(View.VISIBLE);
			else
				component.tvNid.setVisibility(View.GONE);

			ResourceController resourceController = new ResourceController(mcontext, list.get(arg0).getGameId());
			if( "E1".equals(resourceController.getNumber4())){
				component.ivExtraGap1.setVisibility(View.GONE);
				component.tvExtra1.setVisibility(View.GONE);
			}
			if( "E2".equals(resourceController.getNumber5())){
				component.ivExtraGap2.setVisibility(View.GONE);
				component.tvExtra2.setVisibility(View.GONE);
			}
			if(!mcontext.mSP.getBoolean(mcontext.SHARE_SHOW_COST_COLUMN + list.get(arg0).getGameId(), false)){
				component.ivCostGap.setVisibility(View.GONE);
				component.tvCost.setVisibility(View.GONE);
			}

		}
		else
		{
			component = (Component) convertView.getTag();
		}

		boolean hasHeader = "Y".equals(list.get(arg0).getProfile());
		boolean showHeader = mcontext.mSP.getBoolean(BaseActivity.SHARE_SHOW_HEADER_IMAGES + list.get(arg0).getGameId(), false);
		if(showHeader && hasHeader){
			component.tvTotal.setVisibility(View.GONE);
			component.ivHeader.setVisibility(View.VISIBLE);
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
		else if(showHeader) {
			component.tvTotal.setVisibility(View.GONE);
			component.ivHeader.setVisibility(View.GONE);

		}else {
			component.tvTotal.setVisibility(View.VISIBLE);
			component.ivHeader.setVisibility(View.GONE);
			if( !Double.isNaN(Double.valueOf(list.get(arg0).getMaxHP())) &&
					!Double.isNaN(Double.valueOf(list.get(arg0).getMaxAttack())) &&
					!Double.isNaN(Double.valueOf(list.get(arg0).getMaxDefense())) &&
					!Double.isNaN(Double.valueOf(list.get(arg0).getExtraValue1())) &&
					!Double.isNaN(Double.valueOf(list.get(arg0).getExtraValue2()))){
				int total = Integer.parseInt(list.get(arg0).getMaxHP()) +
						Integer.parseInt(list.get(arg0).getMaxAttack()) +
						Integer.parseInt(list.get(arg0).getMaxDefense()) +
						Integer.parseInt(list.get(arg0).getExtraValue1()) +
						Integer.parseInt(list.get(arg0).getExtraValue2());
				component.tvTotal.setText( String.valueOf(total));
			}


		}

		component.tvName.setText(list.get(arg0).getName());
		component.tvFrontName.setText(list.get(arg0).getFrontName());
		String attr = list.get(arg0).getAttr();
		component.tvAttr.setText(attr);
		component.tvCost.setText(String.valueOf(list.get(arg0).getCost()));
		component.tvHP.setText(String.valueOf(list.get(arg0).getMaxHP()));
		component.tvAttack.setText(String.valueOf(list.get(arg0).getMaxAttack()));
		component.tvDefense.setText(String.valueOf(list.get(arg0).getMaxDefense()));
		if(list.get(arg0).getExtraValue1() != null)
			component.tvExtra1.setText(String.valueOf(list.get(arg0).getExtraValue1()));
		else
			component.tvExtra1.setText("");
		if(list.get(arg0).getExtraValue2() != null)
			component.tvExtra2.setText(String.valueOf(list.get(arg0).getExtraValue2()));
		else
			component.tvExtra2.setText("");
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
		 public TextView tvExtra1;
		 public TextView tvExtra2;
		 public TextView tvNid;
		 public TextView tvTotal;
		 public ImageView ivExtraGap1;
		 public ImageView ivExtraGap2;
		 public ImageView ivCostGap;
	}
}
