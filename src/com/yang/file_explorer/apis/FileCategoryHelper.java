package com.yang.file_explorer.apis;

import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import com.yang.file_explorer.R;
import com.yang.file_explorer.apis.FileSortHelper.SortMethod;
import com.yang.file_explorer.entity.MediaFile;
import com.yang.file_explorer.entity.MediaFile.MediaFileType;
import com.yang.file_explorer.utils.FileUtil;
import com.yang.file_explorer.utils.LogUtils;

public class FileCategoryHelper {

	private FileCategoryType mCategoryType;

	private Context mContext;

	private static final String LOG_TAG = "FileCategoryHelper";

	public enum FileCategoryType {
		All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, Other, Favorite
	}

	public static final int COLUMN_ID = 0;

	public static final int COLUMN_PATH = 1;

	public static final int COLUMN_SIZE = 2;

	public static final int COLUMN_DATE = 3;

	private static String APK_EXT = "apk";
	private static String THEME_EXT = "mtz";
	private static String[] ZIP_EXTS = new String[] { "zip", "rar" };

	public static HashMap<FileCategoryType, FilenameExtFilter> filters = new HashMap<FileCategoryType, FilenameExtFilter>();

	public static HashMap<FileCategoryType, Integer> categoryNames = new HashMap<FileCategoryType, Integer>();
	
	public static HashMap<String, FileCategoryType> fileExtCategoryType = new HashMap<String, FileCategoryHelper.FileCategoryType>();

	static {
		categoryNames.put(FileCategoryType.All, R.string.category_all);
		categoryNames.put(FileCategoryType.Music, R.string.category_music);
		categoryNames.put(FileCategoryType.Video, R.string.category_video);
		categoryNames.put(FileCategoryType.Picture, R.string.category_picture);
		categoryNames.put(FileCategoryType.Theme, R.string.category_theme);
		categoryNames.put(FileCategoryType.Doc, R.string.category_document);
		categoryNames.put(FileCategoryType.Zip, R.string.category_zip);
		categoryNames.put(FileCategoryType.Apk, R.string.category_apk);
		categoryNames.put(FileCategoryType.Other, R.string.category_other);
		categoryNames
				.put(FileCategoryType.Favorite, R.string.category_favorite);
	}
	

	static {
		addItem(new String[] { "mp4", "wmv", "mpeg", "m4v", "3gp", "3gpp", "3g2", "3gpp2", "asf","rmvb","avi" }, FileCategoryType.Video);
		addItem(new String[] { "jpg", "jpeg", "gif", "png", "bmp", "wbmp" }, FileCategoryType.Picture);
		addItem(new String[] { "mp3","wma","wav","ogg" }, FileCategoryType.Music);
		
	}

	private static void addItem(String[] exts, FileCategoryType categoryType) {
		if (exts != null) {
			for (String ext : exts) {
				fileExtCategoryType.put(ext.toLowerCase(), categoryType);
			}
		}
	}
	

	public static FileCategoryType[] sCategories = new FileCategoryType[] {
			FileCategoryType.Music, FileCategoryType.Video,
			FileCategoryType.Picture, FileCategoryType.Theme,
			FileCategoryType.Doc, FileCategoryType.Zip, FileCategoryType.Apk,
			FileCategoryType.Other };

	/*
	 * 构造函数
	 */
	public FileCategoryHelper(Context context) {
		mContext = context;

		mCategoryType = FileCategoryType.All;
	}

	public FileCategoryType getCurCategoryType() {
		return mCategoryType;
	}

	public void setCurCategoryType(FileCategoryType c) {
		mCategoryType = c;
	}

	public int getCurCategoryNameResId() {
		return categoryNames.get(mCategoryType);
	}

	public static FileCategoryType getCategoryFromPath(String path) {
		MediaFileType type = MediaFile.getFileType(path);
		if (type != null) {
			if (MediaFile.isAudioFileType(type.fileType))
				return FileCategoryType.Music;
			if (MediaFile.isVideoFileType(type.fileType))
				return FileCategoryType.Video;
			if (MediaFile.isImageFileType(type.fileType))
				return FileCategoryType.Picture;
			if (FileUtil.sDocMimeTypesSet.contains(type.mimeType))
				return FileCategoryType.Doc;
		}

		int dotPosition = path.lastIndexOf('.');
		if (dotPosition < 0) {
			return FileCategoryType.Other;
		}

		String ext = path.substring(dotPosition + 1);
		if (ext.equalsIgnoreCase(APK_EXT)) {
			return FileCategoryType.Apk;
		}
		if (ext.equalsIgnoreCase(THEME_EXT)) {
			return FileCategoryType.Theme;
		}

		if (matchExts(ext, ZIP_EXTS)) {
			return FileCategoryType.Zip;
		}

		return FileCategoryType.Other;
	}

	private static boolean matchExts(String ext, String[] exts) {
		for (String ex : exts) {
			if (ex.equalsIgnoreCase(ext))
				return true;
		}
		return false;
	}

	public FilenameFilter getFilter() {
		return filters.get(mCategoryType);
	}

	private HashMap<FileCategoryType, CategoryInfo> mCategoryInfo = new HashMap<FileCategoryType, CategoryInfo>();

	public HashMap<FileCategoryType, CategoryInfo> getCategoryInfos() {
		return mCategoryInfo;
	}

	public CategoryInfo getCategoryInfo(FileCategoryType fc) {
		if (mCategoryInfo.containsKey(fc)) {
			return mCategoryInfo.get(fc);
		} else {
			CategoryInfo info = new CategoryInfo();
			mCategoryInfo.put(fc, info);
			return info;
		}
	}

	public class CategoryInfo {
		public long count;

		public long size;
	}

	private void setCategoryInfo(FileCategoryType fc, long count, long size) {
		CategoryInfo info = mCategoryInfo.get(fc);
		if (info == null) {
			info = new CategoryInfo();
			mCategoryInfo.put(fc, info);
		}
		info.count = count;
		info.size = size;
	}

	/*
	 * 获取媒体内容提供器中分类文件
	 */
	public Cursor query(FileCategoryType fc, SortMethod sort) {
		Uri uri = getContentUriByCategory(fc);
		String selection = buildSelectionByCategory(fc);
		String sortOrder = buildSortOrder(sort);

		if (uri == null) {
			LogUtils.e(LOG_TAG, "invalid uri, category:" + fc.name());
			return null;
		}

		String[] columns = new String[] { FileColumns._ID, FileColumns.DATA,
				FileColumns.SIZE, FileColumns.DATE_MODIFIED };

		return mContext.getContentResolver().query(uri, columns, selection,
				null, sortOrder);
	}

	/*
	 * 获取分类文件信息
	 */
	public void refreshCategoryInfo() {
		// clear
		for (FileCategoryType fc : sCategories) {
			setCategoryInfo(fc, 0, 0);
		}

		
		// query database
		String volumeName = "external";

		Uri uri = Audio.Media.getContentUri(volumeName);
		refreshMediaCategory(FileCategoryType.Music, uri);

		uri = Video.Media.getContentUri(volumeName);
		refreshMediaCategory(FileCategoryType.Video, uri);

		uri = Images.Media.getContentUri(volumeName);
		   
		refreshMediaCategory(FileCategoryType.Picture, uri);

		uri = Files.getContentUri(volumeName);
		refreshMediaCategory(FileCategoryType.Theme, uri);
		refreshMediaCategory(FileCategoryType.Doc, uri);
		refreshMediaCategory(FileCategoryType.Zip, uri);
		refreshMediaCategory(FileCategoryType.Apk, uri);
	}

	private boolean refreshMediaCategory(FileCategoryType fc, Uri uri) {
		String[] columns = new String[] { "COUNT(*)", "SUM(_size)" };
		Cursor c = mContext.getContentResolver().query(uri, columns,
				buildSelectionByCategory(fc), null, null);
		if (c == null) {
			LogUtils.e(LOG_TAG, "fail to query uri:" + uri);
			return false;
		}

		if (c.moveToNext()) {
			setCategoryInfo(fc, c.getLong(0), c.getLong(1));
			LogUtils.v(LOG_TAG, "Retrieved " + fc.name() + " info >>> count:"
					+ c.getLong(0) + " size:" + c.getLong(1));
			c.close();
			return true;
		}

		return false;
	}

	/*
	 * 根据文件类型获取Uri
	 */
	private Uri getContentUriByCategory(FileCategoryType cat) {
		Uri uri;
		String volumeName = "external";
		switch (cat) {
		case Theme:
		case Doc:
		case Zip:
		case Apk:
			uri = Files.getContentUri(volumeName);
			break;
		case Music:
			uri = Audio.Media.getContentUri(volumeName);
			break;
		case Video:
			uri = Video.Media.getContentUri(volumeName);
			break;
		case Picture:
			uri = Images.Media.getContentUri(volumeName);
			break;
		default:
			uri = null;
		}
		return uri;
	}

	/*
	 * 获取排序语法
	 */
	private String buildSortOrder(SortMethod sort) {
		String sortOrder = null;
		switch (sort) {
		case name:
			sortOrder = FileColumns.TITLE + " asc";
			break;
		case size:
			sortOrder = FileColumns.SIZE + " asc";
			break;
		case date:
			sortOrder = FileColumns.DATE_MODIFIED + " desc";
			break;
		case type:
			sortOrder = FileColumns.MIME_TYPE + " asc, " + FileColumns.TITLE
					+ " asc";
			break;
		}
		return sortOrder;
	}

	private String buildSelectionByCategory(FileCategoryType cat) {
		String selection = null;
		switch (cat) {
		case Theme:
			selection = FileColumns.DATA + " LIKE '%.mtz'";
			break;
		case Doc:
			selection = buildDocSelection();
			break;
		case Zip:
			selection = "(" + FileColumns.MIME_TYPE + " == '"
					+ FileUtil.sZipFileMimeType + "')";
			break;
		case Apk:
			selection = FileColumns.DATA + " LIKE '%.apk'";
			break;
		default:
			selection = null;
		}
		return selection;
	}

	private String buildDocSelection() {
		StringBuilder selection = new StringBuilder();
		Iterator<String> iter = FileUtil.sDocMimeTypesSet.iterator();
		while (iter.hasNext()) {
			selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next()
					+ "') OR ");
		}
		return selection.substring(0, selection.lastIndexOf(")") + 1);
	}
}
