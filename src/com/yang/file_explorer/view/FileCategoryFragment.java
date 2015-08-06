package com.yang.file_explorer.view;

import android.os.Bundle;
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

public class FileCategoryFragment extends SherlockFragment{

	private MainActivity mActivity;
	private View mfileExplorerCategoryView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = (MainActivity)getActivity();
		setHasOptionsMenu(true);
		mfileExplorerCategoryView = inflater.inflate(R.layout.file_explorer_category, container, false);
		return mfileExplorerCategoryView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		
		if (!mActivity.getSupportActionBar().isShowing()) {
			return;
		}
		
		MenuUtils.getInstance().addMenu(menu);
		ToastUtils.getInstance().showMask("FileCategoryFragment Createmenu", Toast.LENGTH_LONG);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.removeItem(2);
		ToastUtils.getInstance().showMask("FileCategoryFragment preCreatemenu", Toast.LENGTH_LONG);
		super.onPrepareOptionsMenu(menu);
	}
}
