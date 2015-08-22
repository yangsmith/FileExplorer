package com.yang.file_explorer.apis;

import java.io.File;
import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.yang.file_explorer.R;
import com.yang.file_explorer.adapter.FavoriteListAdapter;
import com.yang.file_explorer.entity.FavoriteItem;
import com.yang.file_explorer.interfaces.FavoriteDatabaseListener;
import com.yang.file_explorer.utils.FileUtil;
import com.yang.file_explorer.utils.LogUtils;

public class FavoriteList implements FavoriteDatabaseListener {

	private static final String LOG_TAG = "FavoriteList";

	private ArrayList<FavoriteItem> mFavoriteList = new ArrayList<FavoriteItem>();

	private ArrayAdapter<FavoriteItem> mFavoriteListAdapter;

	private ListView mListView;

	private FavoriteDatabaseListener mListener;
	
	private FileInteractionHub mFileInteractionHub;

	private Context mContext;

	public FavoriteList(Context context, ListView list,
			FavoriteDatabaseListener listener, FileInteractionHub fHub,FileIconHelper iconHelper) {
		mContext = context;

		mFavoriteListAdapter = new FavoriteListAdapter(context,
				R.layout.favorite_item, mFavoriteList, fHub,iconHelper);
		setupFavoriteListView(list);
		mListener = listener;
	}

	private void setupFavoriteListView(ListView list) {
		mListView = list;
		mListView.setAdapter(mFavoriteListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onFavoriteListItemClick(parent, view, position, id);
			}
		});
	}

	 public ArrayAdapter<FavoriteItem> getArrayAdapter() {
	        return mFavoriteListAdapter;
	    }
	
	/*
	 * 收藏文件列表点击事件
	 */
	public void onFavoriteListItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		FavoriteItem favorite = mFavoriteList.get(position);

		if (favorite.fileInfo.IsDir) {

		} else {
			try {
				IntentBuilder.viewFile(mContext, favorite.fileInfo.filePath);
			} catch (ActivityNotFoundException e) {
				LogUtils.e(LOG_TAG, "fail to view file: " + e.toString());
			}
		}
	}

	/*
	 * 初始化列表
	 */
	public void initList() {
		mFavoriteList.clear();
		update();
	}

	/*
	 * 列表刷新
	 */
	public void update() {
		mFavoriteList.clear();

		// Cursor c = mFavoriteDatabase.query();
		// if (c != null) {
		// while (c.moveToNext()) {
		// FavoriteItem item = new FavoriteItem(c.getLong(0), c.getString(1),
		// c.getString(2));
		// item.fileInfo = Util.GetFileInfo(item.location);
		// mFavoriteList.add(item);
		// }
		// c.close();
		// }

		// remove not existing items
		if (FileUtil.isSDCardReady()) {
			for (int i = mFavoriteList.size() - 1; i >= 0; i--) {
				File file = new File(mFavoriteList.get(i).location);
				if (file.exists())
					continue;

				FavoriteItem favorite = mFavoriteList.get(i);
				// mFavoriteDatabase.delete(favorite.id, false);
				mFavoriteList.remove(i);
			}
		}

		mFavoriteListAdapter.notifyDataSetChanged();
	}

	/*
	 * 是否显示列表
	 */
	public void show(boolean show) {
		mListView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	/*
	 * 列表文件数量
	 */
	public long getCount() {
		return mFavoriteList.size();
	}

	@Override
	public void onFavoriteDatabaseChanged() {
		// TODO Auto-generated method stub

	}

}
