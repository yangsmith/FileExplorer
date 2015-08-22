package com.yang.file_explorer.apis;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.interfaces.IOperationProgressListener;
import com.yang.file_explorer.utils.FileUtil;

public class FileOperationHelper {

	private static final String LOG_TAG = "FileOperation";

	private ArrayList<FileInfo> mCurFileNameList = new ArrayList<FileInfo>();

	private IOperationProgressListener moperationListener;

	private FilenameFilter mFilter = null;

	private boolean mMoving;

	public FileOperationHelper(IOperationProgressListener l) {
		moperationListener = l;
	}

	public void setFilenameFilter(FilenameFilter f) {
		mFilter = f;
	}

	public void Copy(ArrayList<FileInfo> files) {
		copyFileList(files);
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
	 * 创建文件夹
	 */

	public boolean CreateFolder(String path, String name) {
		File f = new File(FileUtil.makePath(path, name));
		if (f.exists())
			return false;

		return f.mkdir();
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

	/*
	 * 文件重命名
	 */
	public boolean Rename(FileInfo f, String newName) {
		if (f == null || newName == null) {
			return false;
		}

		File file = new File(f.filePath);
		String newPath = FileUtil.makePath(FileUtil.getPathFromFilepath(f.filePath),
				newName);
		final boolean needScan = file.isFile();
		try {
			boolean ret = file.renameTo(new File(newPath));
			if (ret) {
				if (needScan) {
					moperationListener.onFileChanged(f.filePath);
				}
				moperationListener.onFileChanged(newPath);
			}
			return ret;
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * 开始剪切
	 */
	public void StartMove(ArrayList<FileInfo> files) {
		if (mMoving)
			return;

		mMoving = true;
		copyFileList(files);
	}

	public boolean isMoveState() {
		return mMoving;
	}

	/*
	 * 移动文件
	 */
	public boolean EndMove(String path) {
		if (!mMoving)
			return false;
		mMoving = false;

		if (TextUtils.isEmpty(path))
			return false;

		final String _path = path;
		asnycExecute(new Runnable() {
			@Override
			public void run() {
				for (FileInfo f : mCurFileNameList) {
					MoveFile(f, _path);
				}

				moperationListener.onFileChanged(FileUtil.getSdDirectory());

				clear();
			}
		});

		return true;
	}

	/*
	 * 粘贴文件
	 */
	public boolean Paste(String path) {
		if (mCurFileNameList.size() == 0)
			return false;

		final String _path = path;
		asnycExecute(new Runnable() {
			@Override
			public void run() {
				for (FileInfo f : mCurFileNameList) {
					CopyFile(f, _path);
				}

				moperationListener.onFileChanged(FileUtil.getSdDirectory());

				clear();
			}
		});

		return true;
	}

	public boolean canPaste() {
		return mCurFileNameList.size() != 0;
	}

	private void CopyFile(FileInfo f, String dest) {
		if (f == null || dest == null) {
			return;
		}

		File file = new File(f.filePath);
		if (file.isDirectory()) {

			// directory exists in destination, rename it
			String destPath = FileUtil.makePath(dest, f.fileName);
			File destFile = new File(destPath);
			int i = 1;
			while (destFile.exists()) {
				destPath = FileUtil.makePath(dest, f.fileName + " " + i++);
				destFile = new File(destPath);
			}

			for (File child : file.listFiles(mFilter)) {
				if (!child.isHidden()
						&& FileUtil.isNormalFile(child.getAbsolutePath())) {
					CopyFile(FileUtil.GetFileInfo(child, mFilter, false),
							destPath);
				}
			}
		} else {
			String destFile = FileUtil.copyFile(f.filePath, dest);
		}

	}

	private boolean MoveFile(FileInfo f, String dest) {

		if (f == null || dest == null) {
			return false;
		}

		File file = new File(f.filePath);
		String newPath = FileUtil.makePath(dest, f.fileName);
		try {
			return file.renameTo(new File(newPath));
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void copyFileList(ArrayList<FileInfo> files) {
		synchronized (mCurFileNameList) {
			mCurFileNameList.clear();
			for (FileInfo f : files) {
				mCurFileNameList.add(f);
			}
		}
	}

}
