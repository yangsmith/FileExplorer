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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.yang.file_explorer.R;
import com.yang.file_explorer.R.id;
import com.yang.file_explorer.adapter.FileListAdater;
import com.yang.file_explorer.apis.FileCategoryHelper;
import com.yang.file_explorer.apis.FileIconHelper;
import com.yang.file_explorer.apis.FileInteractionHub;
import com.yang.file_explorer.apis.FileInteractionHub.Mode;
import com.yang.file_explorer.apis.FileSortHelper;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.entity.GlobalConsts;
import com.yang.file_explorer.entity.Settings;
import com.yang.file_explorer.interfaces.IFileInteractionListener;
import com.yang.file_explorer.ui.MainActivity;
import com.yang.file_explorer.utils.FileUtil;
import com.yang.file_explorer.utils.MenuUtils;
import com.yang.file_explorer.utils.ToastUtils;

public class FileViewFragment extends SherlockFragment implements
		IFileInteractionListener, OnClickListener {

	private FileInteractionHub mFileInteractionHub;

	private FileIconHelper mFileIconHelper;

	private FileCategoryHelper mFileCategoryHelper;

	private MainActivity mActivity;

	private FileSortHelper mFileSortHelper;

	private View mRootView;

	private ListView mfileListView;

	private View memptyView;

	private View mnoSdView;

	private LinearLayout mMovingOperationBar;

	private ImageButton mButtonMovingConfirm;

	private TextView mTitleOperationBar;

	private Button mButtonMovingCancle;

	private LinearLayout mrefreshViewLinearLayout;

	private HorizontalScrollView mHorizontalScrollView;

	private LinearLayout mcurrentPathLinearLayout;

	private ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();

	private ArrayAdapter<FileInfo> mAdapter;

	private static final String sdDir = FileUtil.getSdDirectory();

	private refreshFileAsyncTask mrefreshFileAsyncTask;
	
	private MenuUtils mMenuUtils;

	private String mcurrentPath;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = (MainActivity) getActivity();
		mActivity.setFileViewFragment(this);

		setHasOptionsMenu(true);
		mRootView = inflater.inflate(R.layout.file_explorer_list, container,
				false);
		mcurrentPathLinearLayout = (LinearLayout) mRootView
				.findViewById(R.id.current_path);
		mHorizontalScrollView = (HorizontalScrollView) mRootView
				.findViewById(R.id.horizontalscrollview);
		mMovingOperationBar = (LinearLayout) mRootView
				.findViewById(R.id.moving_operation_bar);
		mTitleOperationBar = (TextView) mRootView.findViewById(R.id.title);
		mButtonMovingConfirm = (ImageButton) mRootView
				.findViewById(R.id.button_moving_confirm);
		mButtonMovingConfirm.setOnClickListener(this);
		mButtonMovingCancle = (Button) mRootView
				.findViewById(R.id.button_moving_cancel);
		mButtonMovingCancle.setOnClickListener(this);

		mrefreshViewLinearLayout = (LinearLayout) mRootView
				.findViewById(R.id.refresh_view);

		mFileInteractionHub = new FileInteractionHub(this);
		mFileCategoryHelper = new FileCategoryHelper(mActivity);
		mMenuUtils = new MenuUtils(mActivity, mFileInteractionHub);

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

	/*
	 * 当文件夹无文件时显示空文件图标
	 */
	private void showEmptyView(boolean show) {
		if (memptyView != null)
			memptyView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	/*
	 * 显示文件列表刷新进度条
	 */

	private void showProgressBar(boolean show) {
		if (show) {
			mrefreshViewLinearLayout.setVisibility(View.VISIBLE);
			mfileListView.setVisibility(View.GONE);
		} else {
			mrefreshViewLinearLayout.setVisibility(View.GONE);
			mfileListView.setVisibility(View.VISIBLE);
			sortCurrentList(mFileSortHelper);
			showEmptyView(mFileNameList.size() == 0);

			// mfileListView.post(new Runnable() {
			// @Override
			// public void run() {
			// mfileListView.setSelection(0);
			// }
			// });
		}
	}

	/*
	 * 显示复制、剪切的工具栏
	 */
	public void ShowMovingOperationBar(boolean isShow) {

		mMovingOperationBar.setVisibility(isShow ? View.VISIBLE : View.GONE);

		if (isShow) {

			mTitleOperationBar.setText(R.string.operation_paste);

			ActionMode mode = mActivity.getActionMode();
			if (mode != null) {
				mode.finish();
			}

			if (mActivity.getActionBar().isShowing()) {
				mActivity.getActionBar().hide();
			}
		} else {
			if (!mActivity.getActionBar().isShowing()) {
				mActivity.getActionBar().show();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yang.file_explorer.interfaces.IFileInteractionListener#onRefreshFileList
	 * (java.lang.String, com.yang.file_explorer.apis.FileSortHelper)
	 */
	public boolean onRefreshFileList(String path, FileSortHelper sort) {
		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return false;
		}

		mFileSortHelper = sort;
		
		mrefreshFileAsyncTask = new refreshFileAsyncTask();
		mrefreshFileAsyncTask.execute(file);
		mcurrentPath = path;
		
		showProgressBar(true);
		createPathNavigation(mcurrentPath);

		return true;
	}

	/*
	 * 返回
	 */
	public boolean onBack() {
		if (!FileUtil.isSDCardReady() || mFileInteractionHub == null) {
			return false;
		}
		return mFileInteractionHub.onBackPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(android
	 * .view.Menu, android.view.MenuInflater)
	 */
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
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GlobalConsts.TAKE_PHOTO
				&& resultCode == mActivity.RESULT_OK) {
			mFileInteractionHub.addTakePhotoFile();
		}

	}

	/*
	 * 创建文件路径导航
	 */

	public void createPathNavigation(String filePath) {
		mcurrentPathLinearLayout.removeAllViews();
		String[] fileNameStrings = filePath.split("/");
		int index = 0;
		for (String f : fileNameStrings) {
			if (!TextUtils.isEmpty(f)) {
				break;
			}

			index++;
		}

		String filename;
		TextView textView;
		if (fileNameStrings.length - index == 1) {
			filename = fileNameStrings[index];
			textView = new TextView(mActivity);
			textView.setText(filename);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setOnClickListener(this);
			textView.setBackgroundResource(R.drawable.bg_addressbar_right_0);

			mcurrentPathLinearLayout.addView(textView);

		} else {
			for (int i = index; i < fileNameStrings.length; i++) {
				filename = fileNameStrings[i];
				textView = new TextView(mActivity);
				textView.setText(filename);
				textView.setGravity(Gravity.CENTER_VERTICAL);
				textView.setOnClickListener(this);

				if (i == index) {
					textView.setBackgroundResource(R.drawable.bg_addressbar_left);
				} else if (fileNameStrings.length - i == 1) {
					textView.setBackgroundResource(R.drawable.bg_addressbar_right);
				} else {
					textView.setBackgroundResource(R.drawable.bg_addressbar_middle);
				}

				mcurrentPathLinearLayout.addView(textView);
			}

		}

		mHorizontalScrollView.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mHorizontalScrollView.fullScroll(ScrollView.FOCUS_RIGHT);
			}
		});

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_moving_confirm:
			mFileInteractionHub.onOperationButtonConfirm();
			break;

		case R.id.button_moving_cancel:
			mFileInteractionHub.onOperationButtonCancel();
			break;

		default:
			String fileIndex = (String) ((TextView) v).getText();
			int length = fileIndex.length();
			int index = mcurrentPath.indexOf(fileIndex);
			String path = mcurrentPath.substring(0, index + length);

			onRefreshFileList(path, mFileSortHelper);
			mFileInteractionHub.setCurrentPath(path);
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yang.file_explorer.interfaces.IFileInteractionListener#getContext()
	 */
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
		mActivity.setFileNum(mFileNameList.size());
	}

	@Override
	public ArrayList<FileInfo> getAllFiles() {
		// TODO Auto-generated method stub
		return mFileNameList;
	}

	@Override
	public void addSingleFile(FileInfo file) {
		mFileNameList.add(file);
		onDataChanged();
	}

	public class refreshFileAsyncTask extends AsyncTask<File, Void, Void> {

		@Override
		protected Void doInBackground(File... files) {
			// TODO Auto-generated method stub
			
			ArrayList<FileInfo> fileList = mFileNameList;
			fileList.clear();
			
			File[] listFiles = files[0].listFiles(mFileCategoryHelper.getFilter());
			if (listFiles == null)
				return null;
			
			for (File child : listFiles) {
				String absolutePath = child.getAbsolutePath();
				if (FileUtil.isNormalFile(absolutePath)
						&& FileUtil.shouldShowFile(absolutePath)) {
					FileInfo lFileInfo = FileUtil.GetFileInfo(child,
							mFileCategoryHelper.getFilter(), Settings
									.instance().getShowDotAndHiddenFiles());
					if (lFileInfo != null) {
						fileList.add(lFileInfo);
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			showProgressBar(false);
		}
	}

}
