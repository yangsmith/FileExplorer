package com.yang.file_explorer.view;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.yang.file_explorer.R;
import com.yang.file_explorer.ui.MainActivity;
import com.yang.file_explorer.utils.MenuUtils;
import com.yang.file_explorer.utils.ToastUtils;

public class FileViewFragment extends SherlockFragment{
	
	private MainActivity mActivity;
	private View mfileExplorerListView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 mActivity = (MainActivity)getActivity();
		 setHasOptionsMenu(true);
		 mfileExplorerListView = inflater.inflate(R.layout.file_explorer_list, container, false);
		 return mfileExplorerListView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		if (!mActivity.getSupportActionBar().isShowing()) {
			return;
		}
		
		MenuUtils.getInstance().addMenu(menu);
	    
		super.onCreateOptionsMenu(menu, inflater);
	}
}
