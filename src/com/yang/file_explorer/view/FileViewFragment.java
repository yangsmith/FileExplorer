package com.yang.file_explorer.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.yang.file_explorer.R;
import com.yang.file_explorer.ui.MainActivity;

public class FileViewFragment extends SherlockFragment{

	private MainActivity mActivity;
	private View mfileExplorerListView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 mActivity = (MainActivity)getActivity();
		 mfileExplorerListView = inflater.inflate(R.layout.file_explorer_list, container, false);
		 return mfileExplorerListView;
	}
}
