package com.yang.file_explorer.apis;

import java.io.File;
import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.yang.file_explorer.R;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.entity.GlobalConsts;
import com.yang.file_explorer.interfaces.IFileInteractionListener;
import com.yang.file_explorer.interfaces.IOperationProgressListener;
import com.yang.file_explorer.ui.MainActivity;
import com.yang.file_explorer.utils.LogUtils;

public class FileInteractionHub implements IOperationProgressListener {

	private static final String LOG_TAG = "FileInteractionHub";

	private IFileInteractionListener mFileInteractionListener;

	private ArrayList<FileInfo> mCheckedFileNameList = new ArrayList<FileInfo>();

	private ArrayList<FileInfo> mStartFileNameList = new ArrayList<FileInfo>();

	private FileOperationHelper mFileOperationHelper;

	private FileSortHelper mFileSortHelper;

	private Context mContext;

	// File List view setup
	private ListView mFileListView;

	/*
	 * 当前文件路径
	 */
	private String mCurrentPath;

	/*
	 * 根目录文件路径
	 */
	private String mRootPath;

	public enum Mode {
		View, Pick
	};

	private Mode mcurrentMode;

	/*
	 * 
	 */
	public FileInteractionHub(IFileInteractionListener fileInteractionListener) {
		assert (fileInteractionListener != null);

		mFileInteractionListener = fileInteractionListener;
		mFileSortHelper = new FileSortHelper();
		mFileOperationHelper = new FileOperationHelper(this);
		mContext = mFileInteractionListener.getContext();
        setup();
	}

	private void setup() {
		setupFileListView();
	}

	private void setupFileListView() {
		mFileListView = (ListView) mFileInteractionListener
				.getViewById(R.id.file_path_list);

		mFileListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onListItemClick(parent, view, position, id);
			}
		});
	}

	/*
	 * ListView 刷新
	 */
	public void refreshFileList() {
		clearSelection();

		// onRefreshFileList returns true indicates list has changed
		mFileInteractionListener.onRefreshFileList(mCurrentPath, mFileSortHelper);

	}

	/*
	 * listView 点击事件
	 */
	public void onListItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		FileInfo lFileInfo = mFileInteractionListener.getItem(position);

		if (lFileInfo == null) {
			LogUtils.e(LOG_TAG, "file does not exist on position:" + position);
			return;
		}

		if (isInSelection()) {
			boolean selected = lFileInfo.Selected;
			ActionMode actionMode = ((MainActivity) mContext).getActionMode();
			ImageView checkBox = (ImageView) view
					.findViewById(R.id.file_checkbox);
			if (selected) {
				mCheckedFileNameList.remove(lFileInfo);
				checkBox.setImageResource(R.drawable.btn_check_on);
			} else {
				mCheckedFileNameList.add(lFileInfo);
				checkBox.setImageResource(R.drawable.btn_check_off);
			}
			if (actionMode != null) {
				if (mCheckedFileNameList.size() == 0)
					actionMode.finish();
				else
					actionMode.invalidate();
			}
			lFileInfo.Selected = !selected;

			return;
		}

		if (!lFileInfo.IsDir) {
			if (mcurrentMode == Mode.Pick) {
				mFileInteractionListener.onPick(lFileInfo);
			} else {
				viewFile(lFileInfo);
			}
			return;
		}

		mCurrentPath = getAbsoluteName(mCurrentPath, lFileInfo.fileName);
		ActionMode actionMode = ((MainActivity) mContext).getActionMode();
		if (actionMode != null) {
			actionMode.finish();
		}
		refreshFileList();
	}

	// check or uncheck
	public boolean onCheckItem(FileInfo f, View v) {
		switch (v.getId()) {
		case R.id.file_checkbox_area: {
			if (f.Selected) {
				mCheckedFileNameList.add(f);
			} else {
				mCheckedFileNameList.remove(f);
			}
		}
			break;
		case R.id.favorite_area: {
			if (f.Started) {
				mStartFileNameList.add(f);
			} else {
				mStartFileNameList.remove(f);
			}
		}
			break;
		default:
			break;
		}

		return true;
	}

	/*
	 * 清空选中列表
	 */
	public void clearSelection() {
		if (mCheckedFileNameList.size() > 0) {
			for (FileInfo f : mCheckedFileNameList) {
				if (f == null) {
					continue;
				}
				f.Selected = false;
			}
			mCheckedFileNameList.clear();
			mFileInteractionListener.onDataChanged();
		}
	}

	/*
	 * 浏览文件
	 */
	private void viewFile(FileInfo lFileInfo) {
		try {
			IntentBuilder.viewFile(mContext, lFileInfo.filePath);
		} catch (ActivityNotFoundException e) {
			LogUtils.e(LOG_TAG, "fail to view file: " + e.toString());
		}
	}

	/*
	 * 是否在文件选中状态
	 */
	public boolean isInSelection() {
		return mCheckedFileNameList.size() > 0;
	}

	/*
	 * 获取文件绝对路径
	 */
	private String getAbsoluteName(String path, String name) {
		return path.equals(GlobalConsts.ROOT_PATH) ? path + name : path
				+ File.separator + name;
	}

	public void setRootPath(String path) {
		mRootPath = path;
		mCurrentPath = path;
	}

	public String getRootPath() {
		return mRootPath;
	}

	public String getCurrentPath() {
		return mCurrentPath;
	}

	public void setCurrentPath(String path) {
		mCurrentPath = path;
	}

	public void setMode(Mode mode) {
		mcurrentMode = mode;
	}

	public Mode getMode() {
		return mcurrentMode;
	}

	public FileInfo getItem(int pos) {
		return mFileInteractionListener.getItem(pos);
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileChanged(String path) {
		// TODO Auto-generated method stub

	}
}
