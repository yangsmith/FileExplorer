package com.yang.file_explorer.adapter;

import java.util.List;

import com.yang.file_explorer.R;
import com.yang.file_explorer.apis.FileIconHelper;
import com.yang.file_explorer.apis.FileInteractionHub;
import com.yang.file_explorer.apis.FileListItem;
import com.yang.file_explorer.entity.FileInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class FileListAdater extends ArrayAdapter<FileInfo> {

	private LayoutInflater mInflater;

	private FileInteractionHub mFileInteractionHub;

	private FileIconHelper mFileIconHelper;

	private Context mContext;
	
	private OnClickListener mOnClickListener;

	public FileListAdater(Context context, int textViewResourceId,
			List<FileInfo> objects, FileInteractionHub fHub,
			FileIconHelper fileIconHelper) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mFileInteractionHub = fHub;
		mFileIconHelper = fileIconHelper;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = mInflater.inflate(R.layout.file_browser_item, parent, false);
		}

		FileInfo fileInfo = mFileInteractionHub.getItem(position);
		FileListItem.setupFileListItemInfo(mContext, view, fileInfo, mFileIconHelper,
				mFileInteractionHub);
		
		mOnClickListener = new FileListItem.FileItemOnClickListener(mContext,mFileInteractionHub);
        view.findViewById(R.id.file_checkbox_area).setOnClickListener(mOnClickListener);
        view.findViewById(R.id.favorite_area).setOnClickListener(mOnClickListener);
        
		return view;

	}

}
