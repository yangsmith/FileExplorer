package com.yang.file_explorer.apis;

import com.yang.file_explorer.utils.FileUtil;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingHelper {

	private static String mRootPath;

	private static boolean mShowHideFile;

	private static Context mContext;
	
	private static SettingHelper mSettingHelper;
	
	
	public static SettingHelper getInstance(Context context){
		
		mContext = context;
		if (mSettingHelper == null) {
			mSettingHelper = new SettingHelper();
			SharedPreferences sharedPreferences = mContext.getSharedPreferences("Setting", Context.MODE_PRIVATE);
			mRootPath = sharedPreferences.getString("pref_key_default_path", FileUtil.getSdDirectory());
			mShowHideFile = sharedPreferences.getBoolean("pref_key_show_hide", false);
		}
		
		return mSettingHelper;
	}

	public static void setRootPath(String rootPath) {
		mContext.getSharedPreferences("Setting", Context.MODE_PRIVATE).edit()
				.putString("pref_key_default_path", rootPath).commit();
		mRootPath = rootPath;
	}

	public static void setShowHideFile(boolean showHideFile) {

		mContext.getSharedPreferences("Setting", Context.MODE_PRIVATE).edit()
				.putBoolean("pref_key_show_hide", showHideFile).commit();
		mShowHideFile = showHideFile;
	}

	public static boolean getShowHideFile() {
		return mShowHideFile;
	}

	public static String getRootPath() {
		return mRootPath;
	}
}
