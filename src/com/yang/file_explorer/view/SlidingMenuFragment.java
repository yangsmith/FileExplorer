package com.yang.file_explorer.view;

import com.yang.file_explorer.R;
import com.yang.file_explorer.entity.MenuItemType;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class SlidingMenuFragment extends Fragment implements OnClickListener{

	//设备
	private RelativeLayout device;
	
	//喜爱（标星）
	private RelativeLayout favorite;

	//wifi Ftp
	private RelativeLayout wifi;
	
	//音乐文件
	private RelativeLayout music;
		
	//视频文件
	private RelativeLayout video;
		
	//图像文件
	private RelativeLayout image;
		
	//文本文件
	private RelativeLayout document;
		
	//压缩包文件
	private RelativeLayout zip;
		
	//apk文件
	private RelativeLayout apk;
	
	//选择菜单
	private MenuItemType menuItemType = MenuItemType.MENU_DEVICE;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	View menu = inflater.inflate(R.layout.main_slidingmenu,container, false);
    	device   = (RelativeLayout)menu.findViewById(R.id.device);
    	favorite = (RelativeLayout)menu.findViewById(R.id.favorite);
    	wifi     = (RelativeLayout)menu.findViewById(R.id.wifi);
    	music    = (RelativeLayout)menu.findViewById(R.id.music);
    	image    = (RelativeLayout)menu.findViewById(R.id.image);
    	video    = (RelativeLayout)menu.findViewById(R.id.video);
    	document = (RelativeLayout)menu.findViewById(R.id.document);
    	zip      = (RelativeLayout)menu.findViewById(R.id.zip);
    	apk      = (RelativeLayout)menu.findViewById(R.id.apk);
    	
    	//监听点击事件
    	device.setOnClickListener(this);
    	favorite.setOnClickListener(this);
    	wifi.setOnClickListener(this);
    	music.setOnClickListener(this);
    	image.setOnClickListener(this);
    	video.setOnClickListener(this);
    	document.setOnClickListener(this);
    	zip.setOnClickListener(this);
    	apk.setOnClickListener(this);
    
    	
    	return menu;
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		device.setBackgroundResource(R.drawable.menu_item_selecter);
		device.getChildAt(0).setVisibility(View.GONE);
		
		favorite.setBackgroundResource(R.drawable.menu_item_selecter);
		favorite.getChildAt(0).setVisibility(View.GONE);
		
		wifi.setBackgroundResource(R.drawable.menu_item_selecter);
		wifi.getChildAt(0).setVisibility(View.GONE);
		
		image.setBackgroundResource(R.drawable.menu_item_selecter);
		image.getChildAt(0).setVisibility(View.GONE);
		
		music.setBackgroundResource(R.drawable.menu_item_selecter);
		music.getChildAt(0).setVisibility(View.GONE);
		
		video.setBackgroundResource(R.drawable.menu_item_selecter);
		video.getChildAt(0).setVisibility(View.GONE);
		
		document.setBackgroundResource(R.drawable.menu_item_selecter);
		document.getChildAt(0).setVisibility(View.GONE);
		
		zip.setBackgroundResource(R.drawable.menu_item_selecter);
		zip.getChildAt(0).setVisibility(View.GONE);
		
		apk.setBackgroundResource(R.drawable.menu_item_selecter);
		apk.getChildAt(0).setVisibility(View.GONE);
		
		switch (v.getId()) {
		case R.id.device:
			device.setBackgroundResource(R.drawable.menu_selected_tile_bg);
			device.getChildAt(0).setVisibility(View.VISIBLE);
			break;
			
		case R.id.favorite:
			favorite.setBackgroundResource(R.drawable.menu_selected_tile_bg);
			favorite.getChildAt(0).setVisibility(View.VISIBLE);
			break;
			
		case R.id.wifi:
			wifi.setBackgroundResource(R.drawable.menu_selected_tile_bg);
			wifi.getChildAt(0).setVisibility(View.VISIBLE);
			break;
			
		case R.id.video:
			video.setBackgroundResource(R.drawable.menu_selected_tile_bg);
			video.getChildAt(0).setVisibility(View.VISIBLE);
			break;
			
		case R.id.music:
			music.setBackgroundResource(R.drawable.menu_selected_tile_bg);
			music.getChildAt(0).setVisibility(View.VISIBLE);
			break;
			
			
		case R.id.document:
		    document.setBackgroundResource(R.drawable.menu_selected_tile_bg);
			document.getChildAt(0).setVisibility(View.VISIBLE);
			break;
			
		case R.id.zip:
			zip.setBackgroundResource(R.drawable.menu_selected_tile_bg);
			zip.getChildAt(0).setVisibility(View.VISIBLE);
			break;
			
		case R.id.apk:
			apk.setBackgroundResource(R.drawable.menu_selected_tile_bg);
			apk.getChildAt(0).setVisibility(View.VISIBLE);
			break;
			
		case R.id.image:
			image.setBackgroundResource(R.drawable.menu_selected_tile_bg);
			image.getChildAt(0).setVisibility(View.VISIBLE);
			break;
			
		default:
			break;
		}
	}
	

	
}
