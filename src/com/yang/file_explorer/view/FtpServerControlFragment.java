package com.yang.file_explorer.view;

import com.yang.file_explorer.R;
import com.yang.file_explorer.ui.MainActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


public class FtpServerControlFragment extends Fragment{

	private MainActivity mActivity;
	
	private View mRootView;
	
	private TextView mpreinstruction;
	
	private LinearLayout mftpaddressLinearLayout;
	
	private TextView mwifiState;
	
	private ImageView mwifiStateImage;
	
	private Switch mftpSwitch;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 mActivity = (MainActivity)getActivity();
		 mRootView = inflater.inflate(R.layout.ftp_server_control, container, false);
		 
		 mftpSwitch               = (Switch)mRootView.findViewById(R.id.start_stop_switch);
		 mpreinstruction          = (TextView)mRootView.findViewById(R.id.instruction_pre);
		 mftpaddressLinearLayout  = (LinearLayout)mRootView.findViewById(R.id.ftp_address_linearlayout);
		 mwifiState               = (TextView)mRootView.findViewById(R.id.wifi_state);
		 mwifiStateImage          = (ImageView)mRootView.findViewById(R.id.wifi_state_image);
		 
		 mftpSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					
				}else {
					
				}
			}
		});
		 
		 return mRootView;
	}
	
	
}
