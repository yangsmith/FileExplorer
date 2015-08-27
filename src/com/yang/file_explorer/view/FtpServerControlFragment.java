package com.yang.file_explorer.view;

import java.net.InetAddress;

import com.swiftp.FsService;
import com.swiftp.FsSettings;
import com.yang.file_explorer.R;
import com.yang.file_explorer.ui.MainActivity;
import com.yang.file_explorer.utils.ToastUtils;
import com.yang.file_explorer.utils.UiUpdateUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class FtpServerControlFragment extends Fragment {

	private final String LOG_TAG = "FtpServerControlFragment";

	private MainActivity mActivity;

	private View mRootView;

	private TextView mpreinstruction;

	private TextView mipText;

	private LinearLayout mftpaddressLinearLayout;

	private TextView mwifiState;

	private ImageView mwifiStateImage;

	private Switch mftpSwitch;

	private WifiManager wifiMgr;

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // We are being told to do a UI update
				// If more than one UI update is queued up, we only need to
				// do one.
				removeMessages(0);
				updateUI();
				break;
			case 1: // We are being told to display an error message
				removeMessages(1);
			}
		}
	};

	BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
		public void onReceive(Context ctx, Intent intent) {

			updateUI();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = (MainActivity) getActivity();
		mRootView = inflater.inflate(R.layout.ftp_server_control, container,
				false);

		mftpSwitch = (Switch) mRootView.findViewById(R.id.start_stop_switch);
		mpreinstruction = (TextView) mRootView
				.findViewById(R.id.instruction_pre);

		mipText = (TextView) mRootView.findViewById(R.id.ip_address);

		mftpaddressLinearLayout = (LinearLayout) mRootView
				.findViewById(R.id.ftp_address_linearlayout);
		mwifiState = (TextView) mRootView.findViewById(R.id.wifi_state);
		mwifiStateImage = (ImageView) mRootView
				.findViewById(R.id.wifi_state_image);

		wifiMgr = (WifiManager) mActivity
				.getSystemService(Context.WIFI_SERVICE);

		mftpSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					startServer();
				} else {
					stopServer();
				}
			}
		});

		mwifiStateImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(
						android.provider.Settings.ACTION_WIFI_SETTINGS);
				startActivity(intent);
			}
		});

		updateUI();
		UiUpdateUtil.registerClient(handler);

		return mRootView;
	}

	/*
	 * 更新wifi状态
	 */
	private void updateUI() {
		WifiInfo info = wifiMgr.getConnectionInfo();
		String wifiIdString = info == null ? null : info.getSSID();
		boolean isWifiReady = FsService.isConnectedUsingWifi();

		mwifiState.setText(isWifiReady ? wifiIdString
				: getString(R.string.no_wifi_hint));
		mwifiStateImage.setImageResource(isWifiReady ? R.drawable.wifi_state4
				: R.drawable.wifi_state0);

		boolean isRunning = FsService.isRunning();
		if (isRunning) {
			InetAddress address = FsService.getLocalInetAddress();
			if (address != null) {
				String port = ":" + FsSettings.getPortNumber();
				mipText.setText("ftp://" + address.getHostAddress()
						+ (FsSettings.getPortNumber() == 21 ? "" : port));
			} else {
				stopServer();
				mipText.setText("");
			}
		}

		mftpSwitch.setEnabled(isWifiReady);
		mftpaddressLinearLayout.setVisibility(isRunning ? View.VISIBLE
				: View.GONE);
		mpreinstruction.setVisibility(isRunning ? View.GONE : View.VISIBLE);

	}

	/*
	 * 开启ftp服务
	 */
	private void startServer() {
		Intent serverService = new Intent(mActivity, FsService.class);
		if (!FsService.isRunning()) {
			warnIfNoExternalStorage();
			mActivity.startService(serverService);
		}

	}

	/*
	 * 关闭ftp服务
	 */
	private void stopServer() {
		Intent serverService = new Intent(mActivity, FsService.class);
		mActivity.stopService(serverService);

	}

	private void warnIfNoExternalStorage() {
		String storageState = Environment.getExternalStorageState();
		if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
			Log.v(LOG_TAG, "Warning due to storage state " + storageState);
			Toast toast = Toast.makeText(mActivity, R.string.storage_warning,
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		UiUpdateUtil.registerClient(handler);
		updateUI();

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		UiUpdateUtil.registerClient(handler);
		updateUI();

		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mActivity.registerReceiver(wifiReceiver, filter);

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		UiUpdateUtil.unregisterClient(handler);
		mActivity.unregisterReceiver(wifiReceiver);
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		UiUpdateUtil.unregisterClient(handler);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		UiUpdateUtil.unregisterClient(handler);
	}

}
