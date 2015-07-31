package com.yang.file_explorer.ui;

import com.yang.file_explorer.R;


import com.yang.file_explorer.entity.MenuItemType;
import com.yang.file_explorer.slidingmenu.SlidingMenu;
import com.yang.file_explorer.ui.base.BaseSlidingFragmentActivity;








import com.yang.file_explorer.view.SlidingMenuFragment;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
;

public class MainActivity extends BaseSlidingFragmentActivity{

	private SlidingMenu sm;
	private MenuItemType menuItemType;
	private TextView title;
	private TextView filenum;
	private boolean bmenuVisible;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		initActionBar();
	    initSlidingMenu();
	    setContentView(R.layout.content_frame);
	    SetShowSelFragments(MenuItemType.MENU_DEVICE);
	}
	
	// 初始化SlidingMenu
	private void initSlidingMenu(){
		setBehindContentView(R.layout.main_slidingmenuframe);
		sm = getSlidingMenu();
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
	public final void SetShowSelFragments(MenuItemType type){
		menuItemType = type;
		bmenuVisible = ((SlidingMenuFragment)getSupportFragmentManager().findFragmentById(R.id.slidingmenumain)).
		if(type == MenuItemType.MENU_DEVICE){
			return;
		}
		
		if (type == MenuItemType.MENU_WIFI) {
			return;
		}
		
		
		
	}
	
}
