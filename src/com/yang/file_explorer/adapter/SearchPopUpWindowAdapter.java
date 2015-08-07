package com.yang.file_explorer.adapter;

import com.yang.file_explorer.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchPopUpWindowAdapter extends BaseAdapter{

	String[] arraryStrings;
	Context  mcontenxt;
	
	public SearchPopUpWindowAdapter(String[] paramArraryOfStrings, Context c) {
		// TODO Auto-generated constructor stub
		arraryStrings = paramArraryOfStrings;
		mcontenxt = c;
		
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return arraryStrings.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = (TextView)View.inflate(mcontenxt, R.layout.dropdown_item, null);
		}
		((TextView)convertView).setText(arraryStrings[position]);
		return convertView;
	}

}
