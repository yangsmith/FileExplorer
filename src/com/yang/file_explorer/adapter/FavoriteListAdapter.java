package com.yang.file_explorer.adapter;

import java.util.List;

import com.yang.file_explorer.R;
import com.yang.file_explorer.apis.FavoriteList;
import com.yang.file_explorer.apis.FileIconHelper;
import com.yang.file_explorer.apis.FileInteractionHub;
import com.yang.file_explorer.apis.FileListItem;
import com.yang.file_explorer.entity.FavoriteItem;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.utils.FileUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class FavoriteListAdapter extends ArrayAdapter<FavoriteItem> {

	private Context mContext;

	private LayoutInflater mInflater;

	private FileIconHelper mfileIconHelper;
	
	private OnClickListener mOnClickListener;
	
	private FileInteractionHub mFileInteractionHub;
	
	private FavoriteList mFavoriteList;

	public FavoriteListAdapter(Context context, int resource,
			List<FavoriteItem> objects,FileInteractionHub fHub, FileIconHelper fileIcon,FavoriteList favoriteList) {
		// TODO Auto-generated constructor stub
		super(context, resource, objects);
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mfileIconHelper = fileIcon;
		mFileInteractionHub = fHub;
		mFavoriteList = favoriteList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.favorite_item, parent, false);
		} else {
			view = convertView;
		}

		FavoriteItem item = getItem(position);
		FileInfo lFileInfo = item.fileInfo;

		FileUtil.setText(view, R.id.file_name, item.title != null ? item.title
				: lFileInfo.fileName);
		if (lFileInfo.ModifiedDate > 0) {
			FileUtil.setText(view, R.id.modified_time,
					FileUtil.formatDateString(mContext, lFileInfo.ModifiedDate));
			view.findViewById(R.id.modified_time).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.modified_time).setVisibility(View.GONE);
		}
		view.findViewById(R.id.modified_time).setVisibility(
				lFileInfo.ModifiedDate > 0 ? View.VISIBLE : View.GONE);
		if (lFileInfo.IsDir) {
			view.findViewById(R.id.file_size).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.file_size).setVisibility(View.VISIBLE);
			FileUtil.setText(view, R.id.file_size,
					FileUtil.convertStorage(lFileInfo.fileSize));
		}
		
		ImageView favoriteImageView = (ImageView) view
				.findViewById(R.id.favorite_img);
		favoriteImageView.setTag(Integer.valueOf(position));
		

		ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
		ImageView lFileImageFrame = (ImageView) view
				.findViewById(R.id.file_image_frame);
		

		if (lFileInfo.IsDir) {
			lFileImageFrame.setVisibility(View.GONE);
			lFileImage.setImageResource(R.drawable.folder_fav);
		} else {
			mfileIconHelper.setIcon(lFileInfo, lFileImage, lFileImageFrame);
		}
		
		mOnClickListener = new FavoriteList.FileItemOnClickListener(mContext,mFileInteractionHub,mFavoriteList);
        view.findViewById(R.id.favorite_area).setOnClickListener(mOnClickListener);

		return view;
	}

}
