package com.mx.gillustrated.util;

import java.sql.Array;
import java.util.Iterator;
import java.util.List;

import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.EventInfo;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class UIUtils {
	
	public static ArrayAdapter<String> getAttrSpinnerAdapter( Context context, List<CardTypeInfo> cardTypesList ) {
		String[] spinnerAttrData = new String[cardTypesList.size()];
		for(int i = 0; i < cardTypesList.size(); i++)
			spinnerAttrData[i] = cardTypesList.get(i).getName();
		ArrayAdapter< String> adapter = 
				new ArrayAdapter< String>( context, 
				android.R.layout.simple_gallery_item, spinnerAttrData);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	public static void setSpinnerClick(List<Spinner> spinners){
		for (Iterator<Spinner> iter = spinners.iterator(); iter.hasNext();) {
			final Spinner spinner = (Spinner) iter.next();
			setSpinnerSingleClick(spinner);
		}
	}

	public static void setSpinnerSingleClick(final Spinner spinner){
		LinearLayout llparentCost = (LinearLayout) spinner.getParent();
		llparentCost.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				spinner.performClick();
			}
		});

	}
}
