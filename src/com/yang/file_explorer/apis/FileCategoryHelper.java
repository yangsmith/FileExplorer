package com.yang.file_explorer.apis;

import java.io.FilenameFilter;
import java.util.HashMap;

import android.content.Context;

import com.yang.file_explorer.R;
import com.yang.file_explorer.entity.MediaFile;
import com.yang.file_explorer.entity.MediaFile.MediaFileType;
import com.yang.file_explorer.utils.FileUtil;

public class FileCategoryHelper {

	private FileCategoryType mCategoryType;

	private Context mContext;

	private static final String LOG_TAG = "FileCategoryHelper";

	public enum FileCategoryType {
		All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, Other, Favorite
	}

	private static String APK_EXT = "apk";
	private static String THEME_EXT = "mtz";
	private static String[] ZIP_EXTS = new String[] { "zip", "rar" };

	public static HashMap<FileCategoryType, FilenameExtFilter> filters = new HashMap<FileCategoryType, FilenameExtFilter>();

	public static HashMap<FileCategoryType, Integer> categoryNames = new HashMap<FileCategoryType, Integer>();
	
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
	        categoryNames.put(FileCategoryType.Favorite, R.string.category_favorite);
	    }

	/*
	 * ¹¹Ôìº¯Êý
	 */
	public FileCategoryHelper(Context context) {
		mContext = context;

		mCategoryType = FileCategoryType.All;
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
}
