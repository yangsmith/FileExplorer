package com.yang.file_explorer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;

import com.actionbarsherlock.view.ActionMode;
import com.yang.file_explorer.R;
import com.yang.file_explorer.apis.FileCategoryHelper;
import com.yang.file_explorer.apis.FileCategoryHelper.FileCategoryType;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.entity.Settings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

public class FileUtil {

	private static final String LOG_TAG = "Util";

	private static String ANDROID_SECURE = "/mnt/sdcard/.android_secure";

	public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
		{
			add("text/plain");
			add("text/html");
			add("application/vnd.ms-powerpoint");
			add("application/pdf");
			add("application/msword");
			add("application/vnd.ms-excel");
			add("application/vnd.ms-excel");
		}
	};
	
	public static String sZipFileMimeType = "application/zip";

	/*
	 * 获取外存储SD卡路径
	 */
	public static String getSdDirectory() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	/*
	 * Sd卡状态 true SD卡正常挂载
	 */
	public static boolean isSDCardReady() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static boolean setText(View view, int id, String text) {
		TextView textView = (TextView) view.findViewById(id);
		if (textView == null)
			return false;

		textView.setText(text);
		return true;
	}

	/*
	 * 是否常见文件
	 */
	public static boolean isNormalFile(String fullName) {
		return !fullName.equals(ANDROID_SECURE);
	}

	private static String[] SysFileDirs = new String[] { "miren_browser/imagecaches" };

	/*
	 * 是否可显示文件
	 */
	public static boolean shouldShowFile(String path) {
		return shouldShowFile(new File(path));
	}

	public static boolean shouldShowFile(File file) {
		boolean show = Settings.instance().getShowDotAndHiddenFiles();
		if (show)
			return true;

		if (file.isHidden())
			return false;

		if (file.getName().startsWith("."))
			return false;

		String sdFolder = getSdDirectory();
		for (String s : SysFileDirs) {
			if (file.getPath().startsWith(makePath(sdFolder, s)))
				return false;
		}

		return true;
	}

	public static String makePath(String path1, String path2) {
		if (path1.endsWith(File.separator))
			return path1 + path2;

		return path1 + File.separator + path2;
	}

	/*
	 * 时间格式化
	 */
	public static String formatDateString(Context context, long time) {
		DateFormat dateFormat = android.text.format.DateFormat
				.getDateFormat(context);
		DateFormat timeFormat = android.text.format.DateFormat
				.getTimeFormat(context);
		Date date = new Date(time);

		return dateFormat.format(date) + " " + timeFormat.format(date);
	}

	/*
	 * 计算文件大小 storage, G M K B
	 */
	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;

		if (size >= gb) {
			return String.format("%.1f GB", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}
	
	/*
	 *从文件名获取Mediauri 
	 */
	public static Uri getMediaUriFromFilename(String filename){
		String extString = getExtFromFilename(filename);
		String volumeName = "external";
        FileCategoryType fileCategoryType = FileCategoryHelper.fileExtCategoryType.get(extString);
		
        Uri uri = null;
        if (fileCategoryType == FileCategoryType.Music) {
            uri = Audio.Media.getContentUri(volumeName);
		}else if (fileCategoryType == FileCategoryType.Picture) {
			uri = Images.Media.getContentUri(volumeName);
		}else if(fileCategoryType == FileCategoryType.Video){
			uri = Video.Media.getContentUri(volumeName);
		}else {
			uri = Files.getContentUri(volumeName);
		}
		return uri;
	}
	 
	/*
	 * 从文件名获取mimetye
	 */
	public static String getMimetypeFromFilename(String filename){
		String mimetype = null;
		String extString = getExtFromFilename(filename);
		
		MimeTypeMap mineMap = MimeTypeMap.getSingleton();
		mimetype = mineMap.getMimeTypeFromExtension(extString);
		
		return mimetype;
	}
	

	/*
	 * 获取文件类型（后缀名）
	 */
	public static String getExtFromFilename(String filename) {
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(dotPosition + 1, filename.length());
		}
		return "";
	}

	/*
	 * 从文件名中获取后缀名
	 */
	public static String getNameFromFilename(String filename) {
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(0, dotPosition);
		}
		return "";
	}

	/*
	 * 从文件路径中获取文件名
	 */
	public static String getNameFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(pos + 1);
		}
		return "";
	}

	/*
	 * 获取文件路径
	 */

	public static String getPathFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(0, pos);
		}
		return "";
	}

	/*
	 * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过 appInfo.publicSourceDir =
	 * apkPath;来修正这个问题，详情参见:
	 * http://code.google.com/p/android/issues/detail?id=9151
	 */
	public static Drawable getApkIcon(Context context, String apkPath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir = apkPath;
			appInfo.publicSourceDir = apkPath;
			try {
				return appInfo.loadIcon(pm);
			} catch (OutOfMemoryError e) {
				LogUtils.e(LOG_TAG, e.toString());
			}
		}
		return null;
	}

	/*
	 * 计算文件夹大小
	 */

	public static long getTotalSizeOfFilesInDir(final File file) {
		if (file.isFile())
			return file.length();
		final File[] children = file.listFiles();
		long total = 0;
		if (children != null)
			for (final File child : children)
				total += getTotalSizeOfFilesInDir(child);
		return total;
	}

	/*
	 * 获取文件信息
	 */
	public static FileInfo GetFileInfo(File f, FilenameFilter filter,
			boolean showHidden) {
		FileInfo lFileInfo = new FileInfo();
		String filePath = f.getPath();
		File lFile = new File(filePath);
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = f.getName();
		lFileInfo.ModifiedDate = lFile.lastModified();
		lFileInfo.IsDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		if (lFileInfo.IsDir) {
			int lCount = 0;
			File[] files = lFile.listFiles(filter);

			// null means we cannot access this dir
			if (files == null) {
				return null;
			}

			for (File child : files) {
				if ((!child.isHidden() || showHidden)
						&& FileUtil.isNormalFile(child.getAbsolutePath())) {
					lCount++;
				}
			}
			lFileInfo.Count = lCount;
		} else {

			lFileInfo.fileSize = lFile.length();

		}
		return lFileInfo;
	}

	public static FileInfo GetFileInfo(String filePath) {
		File lFile = new File(filePath);
		if (!lFile.exists())
			return null;

		FileInfo lFileInfo = new FileInfo();
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = FileUtil.getNameFromFilepath(filePath);
		lFileInfo.ModifiedDate = lFile.lastModified();
		lFileInfo.IsDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		lFileInfo.fileSize = lFile.length();
		return lFileInfo;
	}

	/*
	 * ActionMode 显示选中的个数
	 */
	public static void updateActionModeTitle(ActionMode mode, Context context,
			int selectedNum) {
		if (mode != null) {
			View view = mode.getCustomView();
			Button btnTitle = (Button) view.findViewById(R.id.selection_menu);
			btnTitle.setText(context.getString(R.string.multi_select_title,
					selectedNum));
			if (selectedNum == 0) {
				mode.finish();
			}
		}
	}

	/*
	 * 文件复制 return new file path if successful, or return null
	 */
	public static String copyFile(String src, String dest) {
		File file = new File(src);
		if (!file.exists() || file.isDirectory()) {
			return null;
		}

		FileInputStream fi = null;
		FileOutputStream fo = null;
		try {
			fi = new FileInputStream(file);
			File destPlace = new File(dest);
			if (!destPlace.exists()) {
				if (!destPlace.mkdirs())
					return null;
			}

			String destPath = FileUtil.makePath(dest, file.getName());
			File destFile = new File(destPath);
			int i = 1;
			while (destFile.exists()) {
				String destName = FileUtil.getNameFromFilename(file.getName())
						+ " " + i++ + "."
						+ FileUtil.getExtFromFilename(file.getName());
				destPath = FileUtil.makePath(dest, destName);
				destFile = new File(destPath);
			}

			if (!destFile.createNewFile())
				return null;

			fo = new FileOutputStream(destFile);
			int count = 102400;
			byte[] buffer = new byte[count];
			int read = 0;
			while ((read = fi.read(buffer, 0, count)) != -1) {
				fo.write(buffer, 0, read);
			}

			// TODO: set access privilege

			return destPath;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			LogUtils.e(LOG_TAG, "copyFile: " + e.toString());
		} finally {
			try {
				if (fi != null)
					fi.close();
				if (fo != null)
					fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

}
