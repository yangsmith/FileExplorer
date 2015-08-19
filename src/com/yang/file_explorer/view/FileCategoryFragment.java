package com.yang.file_explorer.view;


import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.yang.file_explorer.R;
import com.yang.file_explorer.apis.FileInteractionHub;
import com.yang.file_explorer.apis.FileSortHelper;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.interfaces.IFileInteractionListener;
import com.yang.file_explorer.ui.MainActivity;
import com.yang.file_explorer.utils.MenuUtils;
import com.yang.file_explorer.utils.ToastUtils;

public class FileCategoryFragment extends SherlockFragment implements IFileInteractionListener{

	private MainActivity mActivity;
	private View mRootView;
	private FileInteractionHub mFileInteractionHub;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = (MainActivity)getActivity();
		setHasOptionsMenu(true);
		mRootView = inflater.inflate(R.layout.file_explorer_list, container, false);
		mFileInteractionHub = new FileInteractionHub(this);
		return mRootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		
		if (!mActivity.getSupportActionBar().isShowing()) {
			return;
		}
		
		MenuUtils.getInstance(mActivity,mFileInteractionHub).addMenu(menu);
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

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return mActivity;
	}

	@Override
	public View getViewById(int id) {
		// TODO Auto-generated method stub
		return mRootView.findViewById(id);
	}

	@Override
	public FileInfo getItem(int pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onPick(FileInfo f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runOnUiThread(Runnable r) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sortCurrentList(FileSortHelper sort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onRefreshFileList(String path, FileSortHelper sort) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<FileInfo> getAllFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSingleFile(FileInfo file) {
		// TODO Auto-generated method stub
		
	}
}
