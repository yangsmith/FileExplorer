package com.yang.file_explorer.view;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.yang.file_explorer.R;
import com.yang.file_explorer.adapter.FileListAdater;
import com.yang.file_explorer.apis.FileCategoryHelper;
import com.yang.file_explorer.apis.FileIconHelper;
import com.yang.file_explorer.apis.FileInteractionHub;
import com.yang.file_explorer.apis.FileInteractionHub.Mode;
import com.yang.file_explorer.apis.FileSortHelper;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.entity.Settings;
import com.yang.file_explorer.interfaces.IFileInteractionListener;
import com.yang.file_explorer.ui.MainActivity;
import com.yang.file_explorer.utils.FileUtil;
import com.yang.file_explorer.utils.MenuUtils;
import com.yang.file_explorer.utils.ToastUtils;

public class FileViewFragment extends SherlockFragment implements
		IFileInteractionListener {

	private FileInteractionHub mFileInteractionHub;

	private FileIconHelper mFileIconHelper;

	private FileCategoryHelper mFileCategoryHelper;

	private MainActivity mActivity;

	private View mRootView;

	private ListView mfileListView;

	private View memptyView;

	private View mnoSdView;

	private ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();

	private ArrayAdapter<FileInfo> mAdapter;

	private static final String sdDir = FileUtil.getSdDirectory();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = (MainActivity) getActivity();
		mActivity.setFileViewFragment(this);
		
		setHasOptionsMenu(true);
		mRootView = inflater.inflate(R.layout.file_explorer_list, container,
				false);

		mFileInteractionHub = new FileInteractionHub(this);
		mFileCategoryHelper = new FileCategoryHelper(mActivity);

		memptyView = mRootView.findViewById(R.id.empty_view);
		mnoSdView = mRootView.findViewById(R.id.sd_not_available_page);
		// 文件列表
		mfileListView = (ListView) mRootView.findViewById(R.id.file_path_list);
		mFileIconHelper = new FileIconHelper(mActivity);
		mAdapter = new FileListAdater(mActivity, R.layout.file_browser_item,
				mFileNameList, mFileInteractionHub, mFileIconHelper);
		mfileListView.setAdapter(mAdapter);

		mFileInteractionHub.setMode(Mode.View);
		mFileInteractionHub.setRootPath(sdDir);
        
	
		updateUI();

		return mRootView;
	}

	// 更新UI
	private void updateUI() {
		boolean sdCardReady = FileUtil.isSDCardReady();
		mnoSdView.setVisibility(sdCardReady ? View.GONE : View.VISIBLE);

		mfileListView.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);

		if (sdCardReady) {
			mFileInteractionHub.refreshFileList();
		}
		
	}

	// 当文件夹无文件时显示空文件图标
	private void showEmptyView(boolean show) {
		if (memptyView != null)
			memptyView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public boolean onRefreshFileList(String path, FileSortHelper sort) {
		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return false;
		}

		ArrayList<FileInfo> fileList = mFileNameList;
		fileList.clear();

		File[] listFiles = file.listFiles(mFileCategoryHelper.getFilter());
		if (listFiles == null)
			return true;

		for (File child : listFiles) {

			String absolutePath = child.getAbsolutePath();
			if (FileUtil.isNormalFile(absolutePath)
					&& FileUtil.shouldShowFile(absolutePath)) {
				FileInfo lFileInfo = FileUtil.GetFileInfo(child,
						mFileCategoryHelper.getFilter(), Settings.instance()
								.getShowDotAndHiddenFiles());
				if (lFileInfo != null) {
					fileList.add(lFileInfo);
				}
			}
		}

		sortCurrentList(sort);
		showEmptyView(fileList.size() == 0);
//		mfileListView.post(new Runnable() {
//			@Override
//			public void run() {
//				mfileListView.setSelection(0);
//			}
//		});
		return true;
	}
	
	/*
	 * 返回
	 */
	 public boolean onBack() {
	        if ( !FileUtil.isSDCardReady() || mFileInteractionHub == null) {
	            return false;
	        }
	        return mFileInteractionHub.onBackPressed();
	    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		if (!mActivity.getSupportActionBar().isShowing()) {
			return;
		}

		MenuUtils.getInstance(mActivity,mFileInteractionHub).addMenu(menu);

		super.onCreateOptionsMenu(menu, inflater);
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
		if (pos < 0 || pos > mFileNameList.size() - 1) {
			return null;
		}
		return mFileNameList.get(pos);
	}

	@Override
	public void onPick(FileInfo f) {
		try {
			Intent intent = Intent.parseUri(Uri.fromFile(new File(f.filePath))
					.toString(), 0);
			mActivity.setResult(Activity.RESULT_OK, intent);
			mActivity.finish();
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDataChanged() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void runOnUiThread(Runnable r) {
		// TODO Auto-generated method stub
		mActivity.runOnUiThread(r);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sortCurrentList(FileSortHelper sort) {
		// TODO Auto-generated method stub
		Collections.sort(mFileNameList, sort.getComparator());
		onDataChanged();
	}

	@Override
	public ArrayList<FileInfo> getAllFiles() {
		// TODO Auto-generated method stub
		return mFileNameList;
	}

}
