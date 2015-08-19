package com.yang.file_explorer.adapter;

import java.util.List;

import com.yang.file_explorer.R;
import com.yang.file_explorer.entity.FileIcon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CreateFileListAdater extends ArrayAdapter<FileIcon>{

	private LayoutInflater mLayoutInflater;
	
	private Context mContext;
	
	public CreateFileListAdater(Context context, int textViewResourceId,
			List<FileIcon> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		FileIcon fileIcon = getItem(position);
		View view = null;
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			view = mLayoutInflater.inflate(R.layout.create_file_list_item, parent,false);
			viewHolder.fileIcon = (ImageView)view.findViewById(R.id.icon);
			viewHolder.fileName = (TextView)view.findViewById(R.id.name);
			view.setTag(viewHolder);
		}else {
			view = convertView;
			viewHolder = (ViewHolder)view.getTag();
		}
		
		viewHolder.fileIcon.setImageResource(fileIcon.getImageId());
		viewHolder.fileName.setText(fileIcon.getName());
		return view;
	}
	
	
	class ViewHolder{
		ImageView fileIcon;
		TextView  fileName;
	}

}
