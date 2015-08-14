package com.yang.file_explorer.utils;



import android.util.Log;

public final class LogUtils {

	private static final boolean LOGV = true;
	private static final boolean LOGD = true;
	private static final boolean LOGI = true;
	private static final boolean LOGW = true;
	private static final boolean LOGE = true;
	 
	public static void v(String tag, String mess) {
	    if (LOGV) { Log.v(tag, mess); }
	}
	public static void d(String tag, String mess) {
	    if (LOGD) { Log.d(tag, mess); }
	}
	public static void i(String tag, String mess) {
	    if (LOGI) { Log.i(tag, mess); }
	}
	public static void w(String tag, String mess) {
	    if (LOGW) { Log.w(tag, mess); }
	}
	public static void e(String tag, String mess) {
	    if (LOGE) { Log.e(tag, mess); }
	}
}
