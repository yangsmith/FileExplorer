package com.yang.file_explorer.apis;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;

import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.interfaces.IOperationProgressListener;
import com.yang.file_explorer.utils.FileUtil;

public class FileOperationHelper {

	private static final String LOG_TAG = "FileOperation";

	private ArrayList<FileInfo> mCurFileNameList = new ArrayList<FileInfo>();

	private IOperationProgressListener moperationListener;

	private ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

	private FilenameFilter mFilter = null;

	private Context mContext;

	private boolean mMoving;

	public FileOperationHelper(IOperationProgressListener l, Context context) {
		moperationListener = l;
		mContext = context;
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

				try {
					mContext.getContentResolver().applyBatch(
							MediaStore.AUTHORITY, ops);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				if (moperationListener != null) {
					moperationListener.onFinish();
				}
				ops.clear();
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

		if (!f.IsDir) {
			ops.add(ContentProviderOperation
					.newDelete(FileUtil.getMediaUriFromFilename(f.fileName))
					.withSelection("_data = ?", new String[] { f.filePath })
					.build());
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
		String newPath = FileUtil.makePath(
				FileUtil.getPathFromFilepath(f.filePath), newName);
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

			FileInfo destFileInfo = FileUtil.GetFileInfo(destFile);
			ops.add(ContentProviderOperation
					.newInsert(
							FileUtil.getMediaUriFromFilename(destFileInfo.fileName))
					.withValue(FileColumns.TITLE, destFileInfo.fileName)
					.withValue(FileColumns.DATA, destFileInfo.filePath)
					.withValue(
							FileColumns.MIME_TYPE,
							FileUtil.getMimetypeFromFilename(destFileInfo.fileName))
					.withValue(FileColumns.DATE_MODIFIED,
							destFileInfo.ModifiedDate)
					.withValue(FileColumns.SIZE, destFileInfo.fileSize).build());
		}

	}

	private boolean MoveFile(FileInfo f, String dest) {

		if (f == null || dest == null) {
			return false;
		}

		File file = new File(f.filePath);
		String newPath = FileUtil.makePath(dest, f.fileName);
		try {

			File destFile = new File(newPath);

			if (file.renameTo(destFile)) {
				MoveFileToDB(destFile, file, destFile);
				return true;
			}

		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void MoveFileToDB(File destFile, File srcFile, File rootFile) {

		if (destFile.isDirectory()) {
			for (File child : destFile.listFiles(mFilter)) {
				if (!child.isHidden()
						&& FileUtil.isNormalFile(child.getAbsolutePath())) {
					MoveFileToDB(destFile, srcFile, rootFile);
				}
			}
		} else {
			int pos = -1;
			String destFilePath = destFile.getAbsolutePath();
			String srcFilePath = srcFile.getAbsolutePath();
			String rootFilePath = rootFile.getAbsolutePath();
			if (srcFile.isDirectory()
					&& (pos = destFilePath.indexOf(rootFilePath)) != -1) {
				srcFilePath = srcFilePath
						+ destFilePath.substring(rootFilePath.length(),
								destFilePath.length());
			}

			FileInfo desFileInfo = FileUtil.GetFileInfo(destFilePath);
			ops.add(ContentProviderOperation
					.newUpdate(
							FileUtil.getMediaUriFromFilename(desFileInfo.fileName))
					.withSelection("_data = ?",
							new String[] { srcFilePath })
					.withValue("_data", destFilePath).build());
		}

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
