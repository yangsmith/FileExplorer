package com.yang.file_explorer.ui;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.MenuItem;
import com.yang.file_explorer.R;
import com.yang.file_explorer.apis.FileCategoryHelper.FileCategoryType;
import com.yang.file_explorer.slidingmenu.SlidingMenu;
import com.yang.file_explorer.ui.base.BaseSlidingFragmentActivity;
import com.yang.file_explorer.utils.LogUtils;
import com.yang.file_explorer.utils.MenuUtils.MenuItemType;
import com.yang.file_explorer.view.FileCategoryFragment;
import com.yang.file_explorer.view.FileViewFragment;
import com.yang.file_explorer.view.SlidingMenuFragment;
import com.yang.file_explorer.widget.CustomDialog;

import android.R.bool;
import android.R.string;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

;

public class MainActivity extends BaseSlidingFragmentActivity {

	private String LOG_TAG = "MainActivity";

	private SlidingMenu sm;

	private MenuItemType mCurrentmenuItemType;
	
	private TextView title;

	private TextView filenum;

	private boolean bmenuVisible;

	private FragmentTransaction mfragmentTransaction;

	private Fragment mcontentFragment;

	private FileViewFragment mFileViewFragment = null;
	
	private FileCategoryFragment mFileCategoryFragment = null;
	
	private SlidingMenuFragment mSlidingMenuFragment;

	private  Activity mActivity = null;

	ActionMode mActionMode;
	
	


	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_frame);

		getWindow().setFormat(PixelFormat.RGBA_8888);
		initActionBar();
		initSlidingMenu();

		mActivity = this;
		setShowSelFragments(MenuItemType.MENU_DEVICE);
	}

	// 初始化SlidingMenu
	private void initSlidingMenu() {

		sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);
		setBehindContentView(R.layout.main_slidingmenuframe);
		sm.setSlidingEnabled(true);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindWidthRes(R.dimen.slidingmenu_width);
		sm.setBehindScrollScale(0);

	}

	// 初始化操作栏
	private void initActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.topbar_tile_bg));
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_logo_actionbar);

		RelativeLayout relativeLayout = (RelativeLayout) View.inflate(this,
				R.layout.file_num_layout, null);
		title = (TextView) relativeLayout.findViewById(R.id.tilte);
		filenum = (TextView) relativeLayout.findViewById(R.id.file_num);
		getSupportActionBar().setCustomView(relativeLayout);
		getSupportActionBar().setDisplayShowCustomEnabled(true);

	}

	// 显示选择的碎片
	public final void setShowSelFragments(MenuItemType menutype) {
		mCurrentmenuItemType = menutype;
		getSlidingMenu().showContent();
		// 显示内容Fragment,隐藏Menu
		bmenuVisible = mSlidingMenuFragment.SelMenu(menutype);
		
		if (menutype == MenuItemType.MENU_DEVICE) {
			setTitle(R.string.my_device);
			setFileNum(mFileViewFragment.getAllFiles().size(),mCurrentmenuItemType);
			
			mfragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			mfragmentTransaction.show(getSupportFragmentManager()
					.findFragmentById(R.id.file_fragment));
			mfragmentTransaction.hide(getSupportFragmentManager()
					.findFragmentById(R.id.category_fragment));
			mfragmentTransaction.hide(getSupportFragmentManager()
					.findFragmentById(R.id.ftp_fragment));
			mfragmentTransaction.commitAllowingStateLoss();
			return;
		}

		if (menutype == MenuItemType.MENU_WIFI) {
			setTitle(R.string.wifi);
			mfragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			mfragmentTransaction.hide(getSupportFragmentManager()
					.findFragmentById(R.id.file_fragment));
			mfragmentTransaction.hide(getSupportFragmentManager()
					.findFragmentById(R.id.category_fragment));
			mfragmentTransaction.show(getSupportFragmentManager()
					.findFragmentById(R.id.ftp_fragment));
			mfragmentTransaction.commitAllowingStateLoss();
			return;
		}

		mfragmentTransaction = getSupportFragmentManager().beginTransaction();
		mfragmentTransaction.hide(getSupportFragmentManager().findFragmentById(
				R.id.file_fragment));
		mfragmentTransaction.show(getSupportFragmentManager().findFragmentById(
				R.id.category_fragment));
		mfragmentTransaction.hide(getSupportFragmentManager().findFragmentById(
				R.id.ftp_fragment));
		mfragmentTransaction.commitAllowingStateLoss();

		switch (menutype) {
		case MENU_FAVORITE:
			setTitle(R.string.star);
			mFileCategoryFragment.onCategorySelected(FileCategoryType.Favorite);
			
			break;
		case MENU_IMAGE:
			setTitle(R.string.image);
			setFileNum((int)mSlidingMenuFragment.getFilenum(FileCategoryType.Picture),mCurrentmenuItemType);
			mFileCategoryFragment.onCategorySelected(FileCategoryType.Picture);
			break;
		case MENU_VIDEO:
			setTitle(R.string.video);
			setFileNum((int)mSlidingMenuFragment.getFilenum(FileCategoryType.Video),mCurrentmenuItemType);
			mFileCategoryFragment.onCategorySelected(FileCategoryType.Video);
			break;
		case MENU_DOCUMENT:
			setTitle(R.string.document);
			setFileNum((int)mSlidingMenuFragment.getFilenum(FileCategoryType.Doc),mCurrentmenuItemType);
			mFileCategoryFragment.onCategorySelected(FileCategoryType.Doc);
			break;
		case MENU_ZIP:
			setTitle(R.string.zip);
			setFileNum((int)mSlidingMenuFragment.getFilenum(FileCategoryType.Zip),mCurrentmenuItemType);
			mFileCategoryFragment.onCategorySelected(FileCategoryType.Zip);
			break;
		case MENU_APK:
			setTitle(R.string.apk);
			setFileNum((int)mSlidingMenuFragment.getFilenum(FileCategoryType.Apk),mCurrentmenuItemType);
			mFileCategoryFragment.onCategorySelected(FileCategoryType.Apk);
			break;
		case MENU_MUSIC:
			setTitle(R.string.music);
			setFileNum((int)mSlidingMenuFragment.getFilenum(FileCategoryType.Music),mCurrentmenuItemType);
			mFileCategoryFragment.onCategorySelected(FileCategoryType.Music);
			break;
		default:
			break;
		}

	}
	
	public MenuItemType getCurrentMenuItemType(){
		return mCurrentmenuItemType;
	}
	
	public void setSlidingMenuFragment(SlidingMenuFragment slidingMenuFragment){
		 mSlidingMenuFragment = slidingMenuFragment;
	}

	
	public Fragment getSlidingMenuFragment(){
		return mSlidingMenuFragment;
	}

	public void setFileViewFragment(FileViewFragment FileViewFragment) {
		this.mFileViewFragment = FileViewFragment;
	}

	public Fragment getFileViewFragment() {
		return mFileViewFragment;
	}
	
	public void setFileCategoryFragment(FileCategoryFragment fileCategoryFragment){
		mFileCategoryFragment = fileCategoryFragment;
	}
	
	public Fragment getFileCategoryFragment(){
		return mFileCategoryFragment;
	}

	public void setActionMode(ActionMode actionMode) {
		mActionMode = actionMode;
	}

	public ActionMode getActionMode() {
		return mActionMode;
	}

	// 设置标题
	public final void setTitle(int resid) {
		title.setText(resid);
	}

	// 设置文件数量
	public final void setFileNum(int num,MenuItemType menuItemType) {
		if (filenum != null && menuItemType == mCurrentmenuItemType) {
			filenum.setText(Integer.valueOf(num).toString());
		}

	}

	public  Activity getActivity() {
		return mActivity;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		boolean bool = false;
		switch (item.getItemId()) {
		case android.R.id.home:
			sm.toggle();

			return true;

		default:
			bool = super.onOptionsItemSelected(item);

			break;
		}
		return bool;
	}

	/*
	 * 退出
	 */
	public void exit() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!isFinishing()) {
					Dialog dialog = new CustomDialog.Builder(mActivity)
							.setTitle("确定退出程序?")
							.setPositiveButton(R.string.cancel,
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
										}
									})
							.setNegativeButton(R.string.confirm,
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
											finish();
											System.exit(0);;
										}
									}).create();

					dialog.show();
				}
			}
		});

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (mFileViewFragment == null)
			finish();

		if (sm.isMenuShowing()) {
			sm.showContent();
		} else if (!mFileViewFragment.onBack()) {
			exit();
		}

	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		LogUtils.d(LOG_TAG, "Mainactivity onpause");
	}

}
