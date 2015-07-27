package com.yang.file_explorer.Activity;

import com.yang.file_explorer.R;
import com.yang.file_explorer.Fragments.ContentFragment;
import com.yang.file_explorer.Fragments.MenuFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SlidingPaneLayout.PanelSlideListener;
import android.view.View;
import android.view.Window;

public class MainActivity extends Activity{

	private SlidingPaneLayout spl = null;
	private MenuFragment    menuFragment;
	private ContentFragment contentFragment;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	//	requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		menuFragment     = new MenuFragment();
		contentFragment  = new ContentFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.menu_fragment, menuFragment);
		transaction.replace(R.id.content_fragment, contentFragment);
		transaction.commit();
		
		spl = (SlidingPaneLayout)findViewById(R.id.slidingpanellayout);
		spl.setPanelSlideListener(new PanelSlideListener() {
			
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPanelOpened(View panel) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPanelClosed(View panel) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		
	}
}
