package com.yang.file_explorer.ui;

import com.actionbarsherlock.view.MenuItem;
import com.yang.file_explorer.R;


import com.yang.file_explorer.entity.MenuItemType;
import com.yang.file_explorer.slidingmenu.SlidingMenu;
import com.yang.file_explorer.ui.base.BaseSlidingFragmentActivity;








import com.yang.file_explorer.view.SlidingMenuFragment;

import android.R.bool;
import android.R.string;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
;

public class MainActivity extends BaseSlidingFragmentActivity{

	private String LOG_TAG = "MainActivity";
	private SlidingMenu sm;
	private MenuItemType currentmenuItemType;
	private TextView title;
	private TextView filenum;
	private boolean bmenuVisible;
	private FragmentTransaction mfragmentTransaction;
	private Fragment mcontentFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_frame);
		
		getWindow().setFormat(PixelFormat.RGBA_8888);
		initActionBar();
		initSlidingMenu();
		setShowSelFragments(MenuItemType.MENU_DEVICE);
	}
	
	// 初始化SlidingMenu
	private void initSlidingMenu(){
		
		sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);
		setBehindContentView(R.layout.main_slidingmenuframe);
		sm.setSlidingEnabled(true);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindWidthRes(R.dimen.slidingmenu_width);
		sm.setBehindScrollScale(0);
	
	}
	
	//初始化操作栏
	private void initActionBar(){
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.topbar_tile_bg));
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_logo_actionbar);
		
		RelativeLayout relativeLayout = (RelativeLayout)View.inflate(this, R.layout.file_num_layout, null);
		title   = (TextView)relativeLayout.findViewById(R.id.tilte);
		filenum = (TextView)relativeLayout.findViewById(R.id.file_num);
		getSupportActionBar().setCustomView(relativeLayout);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		
	}
	
	//显示选择的碎片
	public final void setShowSelFragments(MenuItemType menutype){
		currentmenuItemType= menutype;
		getSlidingMenu().showContent();;  //显示内容Fragment,隐藏Menu
		bmenuVisible = ((SlidingMenuFragment)getSupportFragmentManager().findFragmentById(R.id.menu_fragment)).SelMenu(menutype);
		if(menutype == MenuItemType.MENU_DEVICE){
			setTitle(R.string.my_device);
			mfragmentTransaction = getSupportFragmentManager().beginTransaction();
			mfragmentTransaction.show(getSupportFragmentManager().findFragmentById(R.id.file_fragment));
  		    mfragmentTransaction.hide(getSupportFragmentManager().findFragmentById(R.id.category_fragment));
			mfragmentTransaction.hide(getSupportFragmentManager().findFragmentById(R.id.ftp_fragment));
			mfragmentTransaction.commitAllowingStateLoss();			
			return;
		}
		
		if (menutype == MenuItemType.MENU_WIFI) {
			setTitle(R.string.wifi);
			mfragmentTransaction = getSupportFragmentManager().beginTransaction();
			mfragmentTransaction.hide(getSupportFragmentManager().findFragmentById(R.id.file_fragment));
			mfragmentTransaction.hide(getSupportFragmentManager().findFragmentById(R.id.category_fragment));
			mfragmentTransaction.show(getSupportFragmentManager().findFragmentById(R.id.ftp_fragment));
			mfragmentTransaction.commitAllowingStateLoss();
			return;
		}
		
		
		mfragmentTransaction = getSupportFragmentManager().beginTransaction();
		mfragmentTransaction.hide(getSupportFragmentManager().findFragmentById(R.id.file_fragment));
		mfragmentTransaction.show(getSupportFragmentManager().findFragmentById(R.id.category_fragment));
		mfragmentTransaction.hide(getSupportFragmentManager().findFragmentById(R.id.ftp_fragment));
		mfragmentTransaction.commit();
		
		switch(menutype){
		 case MENU_FAVORITE:
			 setTitle(R.string.star);
			 break;
		 case MENU_IMAGE:
			 setTitle(R.string.image);
			 break;
		 case MENU_VIDEO:
			 setTitle(R.string.video);
			 break;
		 case MENU_DOCUMENT:
			 setTitle(R.string.document);
			 break;
		 case MENU_ZIP:
			 setTitle(R.string.zip);
			 break;
		 case MENU_APK:
			 setTitle(R.string.apk);
			 break;
		 case MENU_MUSIC:
			 setTitle(R.string.music);
			 break;
		default:
			break;
		}
		
	}
	
	//设置标题
	public final void setTitle(int resid){
		title.setText(resid);
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
			bool = super.onOptionsItemSelected(item);;
			break;
		}
		return bool;
	}
	
}
