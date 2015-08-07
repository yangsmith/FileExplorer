package com.yang.file_explorer.ui;

import java.util.ArrayList;

import com.yang.file_explorer.R;
import com.yang.file_explorer.adapter.SearchPopUpWindowAdapter;
import com.yang.file_explorer.widget.SearchEditText;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/*搜索界面*/

public class SearchActivity extends Activity implements OnClickListener,OnItemClickListener{

	private SearchEditText msearchEditText;
	private LinearLayout mbackLayout;
	private LinearLayout minputLayout;
	private LinearLayout mvoiceLayout;
	private LinearLayout mfilterLayout;
	private TextView mtextviewHint;
	private TextView mtextviewEmpty;
	private ImageButton mbtnClean;
	private ImageButton mbtnVoice;
	private ListView mlistviewSearch;
	private int VOICE_RECOGNITION_REQUEST_CODE = 10000;  
	private PopupWindow mpopupFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_activity);
		
		msearchEditText = (SearchEditText)findViewById(R.id.input);
		mbackLayout     = (LinearLayout)findViewById(R.id.back);
		minputLayout    = (LinearLayout)findViewById(R.id.input_layout);
		mvoiceLayout    = (LinearLayout)findViewById(R.id.voice_layout);
		mfilterLayout   = (LinearLayout)findViewById(R.id.filter);
		mtextviewHint   = (TextView)findViewById(R.id.hint);
		mtextviewEmpty  = (TextView)findViewById(R.id.empty_view);
		mbtnClean       = (ImageButton)findViewById(R.id.clean);
		mbtnVoice       = (ImageButton)findViewById(R.id.voice);
		mlistviewSearch = (ListView)findViewById(R.id.search_list);
		
		mbackLayout.setOnClickListener(this);
		mbtnClean.setOnClickListener(this);
		mbtnVoice.setOnClickListener(this);
		mfilterLayout.setOnClickListener(this);
		
		msearchEditText.addTextChangedListener(new SearchTextWatcher(this));
		msearchEditText.setOnFocusChangeListener(new SearchFocusChange(this));
		
		msearchEditText.postDelayed(new SearchAction(this), 100);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        switch (v.getId()) {
		case R.id.back:
			finish();
			overridePendingTransition(0, 0);
			break;
        case R.id.clean:
        	msearchEditText.setText("");
        	break;
        case R.id.voice:
        	try {
        	////通过Intent传递语音识别的模式,开启语音   
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);  
                //语言模式和自由形式的语音识别    
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);  
                  //提示语音开始  
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "开始语音,请讲话。。。");  
                //开始语音识别  
                startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);  
			} catch (ActivityNotFoundException e) {
				// TODO: handle exception
				 //找不到语音设备装置    
                Toast.makeText(this, "找不到语音设备装置  ", Toast.LENGTH_LONG).show();  
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://search?q=pname:com.google.android.voicesearch")));
			}
        	break;
        	
        case R.id.filter:
             if (mpopupFilter == null) {
				LinearLayout layout  = (LinearLayout)View.inflate(this, R.layout.dropdown, null);
				ListView popListView = (ListView)layout.findViewById(R.id.PopUpWindowlistView);
				popListView.setTag(Integer.valueOf(R.id.filter));
				popListView.setAdapter(new SearchPopUpWindowAdapter(getResources().getStringArray(R.array.filter_category),this));
				popListView.setOnKeyListener(new FileFilterKeyListener(this));
				popListView.setOnItemClickListener(this);
				mpopupFilter = new PopupWindow(layout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				mpopupFilter.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_dropdown_panel_holo_light));
				mpopupFilter.setFocusable(true);
				mpopupFilter.setOutsideTouchable(true);
				mpopupFilter.setTouchable(true);
			}
            mpopupFilter.showAsDropDown(mfilterLayout,0,getResources().getDimensionPixelSize(R.dimen.menu_y));
        	break;
		default:
			break;
		}
	}
	
	 //当语音结束时的回调函数onActivityResult    
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        // TODO Auto-generated method stub  
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {  
            // 取得语音的字符    
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);  
            if (results != null) {  
                //设置视图更新  
                String strText = results.get(0);  
                msearchEditText.setText(strText);
                msearchEditText.setSelection(strText.length());
            }  
        }  
        super.onActivityResult(requestCode, resultCode, data);  
    }  
	
	
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
    		long id) {
    	// TODO Auto-generated method stub
    	if (((Integer)parent.getTag()).intValue() == R.id.filter) {
			String[] arraryStrings = getResources().getStringArray(R.array.filter_category);
			((TextView)mfilterLayout.getChildAt(0)).setText(arraryStrings[position]);
			if (mpopupFilter != null) {
				mpopupFilter.dismiss();
			}
		}
    }
    
	//搜索编辑框文本监视器
	class SearchTextWatcher implements TextWatcher{

		public SearchTextWatcher(SearchActivity searchActivity){}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			if (s.toString().equals("")) {
				mtextviewHint.setVisibility(View.VISIBLE);
				mtextviewEmpty.setText(R.string.search_info);
				mbtnClean.setVisibility(View.GONE);
				mvoiceLayout.setVisibility(View.VISIBLE);
				mbtnVoice.setVisibility(View.VISIBLE);
			}else{
				
				mtextviewHint.setVisibility(View.GONE);
				mtextviewEmpty.setText(R.string.no_file);
				mbtnClean.setVisibility(View.VISIBLE);
				mvoiceLayout.setVisibility(View.GONE);
				mbtnVoice.setVisibility(View.GONE);
			}
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	//搜索编辑框焦点监视器
	class SearchFocusChange implements OnFocusChangeListener{

		public SearchFocusChange(SearchActivity searchActivity) {
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if (hasFocus ) {
				minputLayout.setBackgroundResource(R.drawable.textfield_search_selected);
				mvoiceLayout.setBackgroundResource(R.drawable.textfield_search_right_selected);
			}
			else{
				minputLayout.setBackgroundResource(R.drawable.textfield_search_default);
				mvoiceLayout.setBackgroundResource(R.drawable.textfield_search_right_default);
			}
		}
		
	}
	
	
	
	//软件盘显示
	class SearchAction implements Runnable{

	   public SearchAction(SearchActivity searchActivity){}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			  InputMethodManager  inputMethodManager = (InputMethodManager)msearchEditText.getContext().getSystemService(INPUT_METHOD_SERVICE); 
			  inputMethodManager.showSoftInput(msearchEditText, 0);
			  msearchEditText.setFocusableInTouchMode(true);
		}
		
	}
	
	//文件过滤按键监听
	class FileFilterKeyListener implements OnKeyListener{

		public FileFilterKeyListener(SearchActivity searchActivity) {
			// TODO Auto-generated constructor stub
		}
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_MENU) {
				mpopupFilter.dismiss();
			}
			return true;
		}
		
	}
}
