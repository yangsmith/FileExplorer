package com.yang.file_explorer.apis;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.os.AsyncTask;

import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.interfaces.IOperationProgressListener;
import com.yang.file_explorer.utils.FileUtil;

public class FileOperationHelper {

	private static final String LOG_TAG = "FileOperation";

	private ArrayList<FileInfo> mCurFileNameList = new ArrayList<FileInfo>();

	private IOperationProgressListener moperationListener;
	
	private FilenameFilter mFilter = null;

	public FileOperationHelper(IOperationProgressListener l) {
		moperationListener = l;
	}
	
	  public void setFilenameFilter(FilenameFilter f) {
	        mFilter = f;
	    }

	private void copyFileList(ArrayList<FileInfo> files) {
		synchronized (mCurFileNameList) {
			mCurFileNameList.clear();
			for (FileInfo f : files) {
				mCurFileNameList.add(f);
			}
		}
	}

	public void clear() {
		synchronized (mCurFileNameList) {
			mCurFileNameList.clear();
		}
	}

	/*
	 * 创建异步线程
	 */
	private void asnycExecute(Runnable r) {
		final Runnable _r = r;
		new AsyncTask() {
			@Override
			protected Object doInBackground(Object... params) {
				synchronized (mCurFileNameList) {
					_r.run();
				}
				if (moperationListener != null) {
					moperationListener.onFinish();
				}

				return null;
			}
		}.execute();
	}

	/*
	 * 删除文件
	 */
	public boolean Delete(ArrayList<FileInfo> files) {
		copyFileList(files);
		asnycExecute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (FileInfo f : mCurFileNameList) {
					DeleteFile(f);
				}

				moperationListener.onFileChanged(FileUtil.getSdDirectory());
				clear();
			}
		});
		return true;
	}

	protected void DeleteFile(FileInfo f) {
		if (f == null) 
			  return;

		File file = new File(f.filePath);
		boolean directory = file.isDirectory();
		if (directory) {
			for (File child : file.listFiles(mFilter)) {
				if (FileUtil.isNormalFile(child.getAbsolutePath())) {
					DeleteFile(FileUtil.GetFileInfo(child, mFilter, true));
				}
			}
		}

		file.delete();
	}
}
