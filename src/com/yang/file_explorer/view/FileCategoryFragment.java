package com.yang.file_explorer.view;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.yang.file_explorer.R;
import com.yang.file_explorer.adapter.FileListCursorAdapter;
import com.yang.file_explorer.apis.FavoriteList;
import com.yang.file_explorer.apis.FileCategoryHelper;
import com.yang.file_explorer.apis.FileCategoryHelper.FileCategoryType;
import com.yang.file_explorer.apis.FileIconHelper;
import com.yang.file_explorer.apis.FileInteractionHub;
import com.yang.file_explorer.apis.FileInteractionHub.Mode;
import com.yang.file_explorer.apis.FileSortHelper;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.interfaces.FavoriteDatabaseListener;
import com.yang.file_explorer.interfaces.IFileInteractionListener;
import com.yang.file_explorer.ui.MainActivity;
import com.yang.file_explorer.utils.MenuUtils;
import com.yang.file_explorer.utils.ToastUtils;

public class FileCategoryFragment extends SherlockFragment implements
		IFileInteractionListener, FavoriteDatabaseListener {

	private MainActivity mActivity;

	private View mRootView;

	private FileInteractionHub mFileInteractionHub;

	private FileIconHelper mFileIconHelper;

	private FileViewFragment mFileViewFragment;

	private ListView mFilePathListView;

	private FileListCursorAdapter mAdapter;

	private FavoriteList mFavoriteList;

	private LinearLayout mEmptyView;

	private LinearLayout mSDNotAvailable;

	private FileCategoryHelper mFileCagetoryHelper;
	
	private MenuUtils mMenuUtils;

	private ViewPage curViewPage = ViewPage.Invalid;

	public enum ViewPage {
		Favorite, Category, Invalid
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = (MainActivity) getActivity();
		mActivity.setFileCategoryFragment(this);
		
		//setHasOptionsMenu(true);
		mRootView = inflater.inflate(R.layout.file_explorer_category,
				container, false);

		mFilePathListView = (ListView) mRootView
				.findViewById(R.id.file_path_list);

		mEmptyView = (LinearLayout) mRootView.findViewById(R.id.empty_view);

		mFileInteractionHub = new FileInteractionHub(this);
		mFileInteractionHub.setMode(Mode.View);
		mFileInteractionHub.setRootPath("/");
		
		mMenuUtils = new MenuUtils(mActivity, mFileInteractionHub);

		mFileCagetoryHelper = new FileCategoryHelper(mActivity);
		mFileIconHelper = new FileIconHelper(mActivity);

		mFavoriteList = new FavoriteList(mActivity,
				(ListView) mRootView.findViewById(R.id.favorite_list), this,
				mFileInteractionHub, mFileIconHelper);
		mFavoriteList.initList();

		mAdapter = new FileListCursorAdapter(mActivity, null,
				mFileInteractionHub, mFileIconHelper);
		mFilePathListView.setAdapter(mAdapter);

		return mRootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub

		if (!mActivity.getSupportActionBar().isShowing()) {
			return;
		}

		mMenuUtils.addMenu(menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	/*
	 * 选中某分类文件
	 */
	public  void onCategorySelected(FileCategoryType f) {
		if (mFileCagetoryHelper.getCurCategoryType() != f) {
			mFileCagetoryHelper.setCurCategoryType(f);
			;
			mFileInteractionHub.setCurrentPath(mFileInteractionHub
					.getRootPath()
					+ getString(mFileCagetoryHelper.getCurCategoryNameResId()));
			mFileInteractionHub.refreshFileList();
		}

		if (f == FileCategoryType.Favorite) {
			showPage(ViewPage.Favorite);
		} else {
			showPage(ViewPage.Category);
		}
	}

	private void showPage(ViewPage p) {
		if (curViewPage == p)
			return;

		curViewPage = p;

		showView(R.id.file_path_list, false);
		mFavoriteList.show(false);
		showEmptyView(false);

		switch (p) {
		case Favorite:
			mFavoriteList.show(true);
			showEmptyView(mFavoriteList.getCount() == 0);
			break;
		case Category:
			showView(R.id.file_path_list, true);
			showEmptyView(mAdapter.getCount() == 0);
			break;

		}
	}

	private void showView(int id, boolean show) {
		View view = mRootView.findViewById(id);
		if (view != null) {
			view.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	/*
	 * 空白文件
	 */
	private void showEmptyView(boolean show) {
		if (mEmptyView != null)
			mEmptyView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.removeItem(2);

		super.onPrepareOptionsMenu(menu);
	}

	/*
	 * 列表刷新
	 */
	@Override
	public boolean onRefreshFileList(String path, FileSortHelper sort) {
		// TODO Auto-generated method stub
		FileCategoryType curCategoryType = mFileCagetoryHelper.getCurCategoryType();
		if (curCategoryType == FileCategoryType.Favorite
				|| curCategoryType == FileCategoryType.All)
			return false;

		Cursor c = mFileCagetoryHelper.query(curCategoryType, sort.getSortMethod());
		showEmptyView(c == null || c.getCount() == 0);
		mAdapter.changeCursor(c);

		return true;
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return mActivity;
	}

	@Override
	public View getViewById(int id) {
		// TODO Auto-generated method stub
		return mRootView.findViewById(id);
	}

	@Override
	public FileInfo getItem(int pos) {
		// TODO Auto-generated method stub
		return mAdapter.getFileItem(pos);
	}

	@Override
	public void onPick(FileInfo f) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataChanged() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mAdapter.notifyDataSetChanged();
				mFavoriteList.getArrayAdapter().notifyDataSetChanged();
				showEmptyView(mAdapter.getCount() == 0);
			}

		});
	}

	@Override
	public void runOnUiThread(Runnable r) {
		// TODO Auto-generated method stub
		mActivity.runOnUiThread(r);
	}

	@Override
	public void sortCurrentList(FileSortHelper sort) {
		// TODO Auto-generated method stub
		mFileInteractionHub.refreshFileList();
	}

	@Override
	public Collection<FileInfo> getAllFiles() {
		// TODO Auto-generated method stub
		return mAdapter.getAllFiles();
	}

	@Override
	public void addSingleFile(FileInfo file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ShowMovingOperationBar(boolean isShow) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFavoriteDatabaseChanged() {
		// TODO Auto-generated method stub

	}
}
