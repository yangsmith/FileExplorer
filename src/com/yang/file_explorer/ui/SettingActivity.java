package com.yang.file_explorer.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.drm.DrmStore.Action;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.yang.file_explorer.R;
import com.yang.file_explorer.apis.SettingHelper;
import com.yang.file_explorer.utils.ToastUtils;
import com.yang.file_explorer.widget.CustomDialog;

public class SettingActivity extends SherlockActivity implements
		OnClickListener {

	private LinearLayout mdefaultPathLine;
	
	private RelativeLayout mshowhideline;
	
	private TextView mdefaultPath;

	private CheckBox mchkhideFile;

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
		mshowhideline = (RelativeLayout)findViewById(R.id.showhide_line);
		mchkhideFile = (CheckBox) findViewById(R.id.checkBox_showhide);
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
		mchkhideFile.setChecked(msettingHelper.getShowHideFile());

		mdefaultPathLine.setOnClickListener(this);
		mshowhideline.setOnClickListener(this);
		mrate.setOnClickListener(this);
		mshare.setOnClickListener(this);
		memail.setOnClickListener(this);
		mabout.setOnClickListener(this);

		mchkhideFile.setOnCheckedChangeListener(new OnCheckedChangeListener() {

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
		Intent intent;
		switch (v.getId()) {
		case R.id.default_path_line:
			LayoutInflater layoutInflater = LayoutInflater.from(this);
			View view = layoutInflater.inflate(R.layout.dialog_input_layout,
					null);
			final EditText editText = (EditText) view
					.findViewById(R.id.edit_text);
			editText.setText(mdefaultPath.getText());

			Dialog dialog = new CustomDialog.Builder(this)
					.setTitle(R.string.operation_rename)
					.setContentView(view)
					.setPositiveButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							})
					.setNegativeButton(R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									if (editText != null) {
										String textString = editText.getText()
												.toString();
										mdefaultPath.setText(textString);
										msettingHelper.setRootPath(textString);
									}

									dialog.dismiss();

								}

							}).create();
			dialog.show();
			break;
		case R.id.showhide_line:

				mchkhideFile.setChecked(!mchkhideFile.isChecked());
			
			break;
		case R.id.rate:
			startActivity(new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://market.android.com/details?id=com.yang.file_explorer")));
			break;
		case R.id.share:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT,
					getString(R.string.share_subject));
			intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
			try {
				startActivity(Intent.createChooser(intent,
						getString(R.string.share_title)));
			} catch (ActivityNotFoundException e) {
				// TODO: handle exception
				Toast.makeText(this, R.string.no_way_to_share,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.email:
			try {
				intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
						+ getString(R.string.my_email)));
				intent.putExtra(Intent.EXTRA_SUBJECT,
						getString(R.string.feekback_subject));
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// TODO: handle exception
				Toast.makeText(this, getString(R.string.no_way_to_email),
						Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.about:
			Dialog dialogAbout = new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_logo_actionbar)
					.setTitle(R.string.about_dlg_title)
					.setMessage(R.string.about_dlg_message)
					.setPositiveButton(R.string.confirm, null).create();

			dialogAbout.show();

			break;
		case R.id.homeAsUp:
			finish();
			overridePendingTransition(0, 0);
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
}
