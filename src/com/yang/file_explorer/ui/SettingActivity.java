package com.yang.file_explorer.ui;

import android.content.Intent;
import android.drm.DrmStore.Action;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.yang.file_explorer.R;
import com.yang.file_explorer.apis.SettingHelper;

public class SettingActivity extends SherlockActivity implements
		OnClickListener {

	private LinearLayout mdefaultPathLine;

	private TextView mdefaultPath;

	private CheckBox mhideFile;

	private TextView mrate;

	private TextView mshare;

	private TextView memail;

	private TextView mabout;

	private SettingHelper msettingHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		msettingHelper = SettingHelper.getInstance(this);

		mdefaultPathLine = (LinearLayout) findViewById(R.id.default_path_line);
		mdefaultPath = (TextView) findViewById(R.id.default_path);
		mhideFile = (CheckBox) findViewById(R.id.checkBox_showhide);
		mrate = (TextView) findViewById(R.id.rate);
		mshare = (TextView) findViewById(R.id.share);
		memail = (TextView) findViewById(R.id.email);
		mabout = (TextView) findViewById(R.id.about);

		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.topbar_tile_bg));
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_logo_actionbar);

		mdefaultPath.setText(msettingHelper.getRootPath());
		mhideFile.setChecked(msettingHelper.getShowHideFile());

		mdefaultPathLine.setOnClickListener(this);
		mrate.setOnClickListener(this);
		mshare.setOnClickListener(this);
		memail.setOnClickListener(this);
		mabout.setOnClickListener(this);

		mhideFile.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				msettingHelper.setShowHideFile(isChecked);
			}
		});

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.default_path_line:

			break;
		case R.id.rate:
			startActivity(new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://market.android.com/details?id=com.youba.FileExplorer")));
			break;
		case R.id.share:
			
			break;
		case R.id.email:
			break;
		case R.id.about:
			break;
		default:
			break;
		}
	}
}
