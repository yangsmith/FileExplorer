package com.yang.file_explorer.view;

import com.yang.file_explorer.R;
import com.yang.file_explorer.ui.MainActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;


public class FtpServerControlFragment extends Fragment{

	private MainActivity mActivity;
	private View mftpServerControlView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 mActivity = (MainActivity)getActivity();
		 mftpServerControlView = inflater.inflate(R.layout.ftp_server_control, container, false);
		 return mftpServerControlView;
	}
	
	
}
