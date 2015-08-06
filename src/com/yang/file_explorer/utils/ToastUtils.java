package com.yang.file_explorer.utils;

import com.yang.file_explorer.ui.MainActivity;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {
  
	private Context mContext = null;
	private static ToastUtils mToastUtils = null;
	
	private ToastUtils(Context context) {
		mContext = context;
	}
	public static ToastUtils getInstance(){
		if (mToastUtils == null) {
			mToastUtils = new ToastUtils(MainActivity.getActivity());
		}
		return mToastUtils;
	}
	
   public void showMask(CharSequence text, int duration){
	   Toast toast =  Toast.makeText(mContext, text, duration);
	   toast.setGravity(Gravity.CENTER, 0, 0);
	   toast.show();
   }
}
