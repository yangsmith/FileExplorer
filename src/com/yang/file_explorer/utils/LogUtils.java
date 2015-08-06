package com.yang.file_explorer.utils;

import com.yang.file_explorer.entity.LogType;

import android.util.Log;

public class LogUtils {

	private LogType enumlogType = LogType.LOGTYPE_DEBUG;
	public void addLog(String tag,String msg){
		switch (enumlogType) {
		case LOGTYPE_VERBOSE:
			Log.v(tag, msg);
			break;
		case LOGTYPE_DEBUG:
			Log.d(tag, msg);
			break;
		case LOGTYPE_INFO:
			Log.i(tag, msg);
			break;
		case LOGTYPE_WARN:
			Log.w(tag, msg);
			break;
		case LOGTYPE_ERROR:
			Log.e(tag, msg);
			break;
		default:
			break;
		}
		
	}
	
	public void setLogType(LogType logType){
		enumlogType = logType;
	}
	
	public LogType getLogType(){
		return  enumlogType;
	}
}
