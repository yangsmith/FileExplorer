package com.yang.file_explorer.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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

	private String LOG_TAG = "FileCategoryFragment";
	
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
	
	private ScannerReceiver mScannerReceiver;
	
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
		
		setHasOptionsMenu(true);
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
		registerScannerReceiver();

		return mRootView;
	}
	
	
	   private void registerScannerReceiver() {
	        mScannerReceiver = new ScannerReceiver();
	        IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
	        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
	        intentFilter.addDataScheme("file");
	        mActivity.registerReceiver(mScannerReceiver, intentFilter);
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
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mActivity != null) {
			mActivity.unregisterReceiver(mScannerReceiver);
		}
	}
	
	private class ScannerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(LOG_TAG, "received broadcast: " + action.toString());
            // handle intents related to external storage
            if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED) || action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                notifyFileChanged();
            }
        }
    }

    private void updateUI() {
      
       mFileInteractionHub.refreshFileList();       
    }

    // process file changed notification, using a timer to avoid frequent
    // refreshing due to batch changing on file system
    synchronized public void notifyFileChanged() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                timer = null;
                Message message = new Message();
                message.what = MSG_FILE_CHANGED_TIMER;
                handler.sendMessage(message);
            }

        }, 1000);
    }

    private static final int MSG_FILE_CHANGED_TIMER = 100;

    private Timer timer;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FILE_CHANGED_TIMER:
                    updateUI();
                    break;
            }
            super.handleMessage(msg);
        }

    };

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

	/*
	 * (non-Javadoc)
	 * @see com.yang.file_explorer.interfaces.IFileInteractionListener#updateMediaData()
	 * 更新媒体库后的刷新
	 */
	@Override
	public void updateMediaData() {
		// TODO Auto-generated method stub
		ToastUtils.getInstance(mActivity).showMask("FileCategory  updateMediaData", Toast.LENGTH_SHORT);
	}
}
