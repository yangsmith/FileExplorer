package com.yang.file_explorer.ui;



import com.yang.file_explorer.R;
import com.yang.file_explorer.widget.SearchEditText;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

                          /*ËÑË÷½çÃæ*/

public class SearchActivity extends Activity implements OnClickListener{

	private SearchEditText msearchEditText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_activity);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
