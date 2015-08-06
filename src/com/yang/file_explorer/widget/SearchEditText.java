package com.yang.file_explorer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class SearchEditText extends EditText {

	public SearchEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SearchEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SearchEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		clearFocus();
		return super.onKeyPreIme(keyCode, event);
	}

}
