package com.yang.file_explorer.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.yang.file_explorer.R;
import com.yang.file_explorer.adapter.FileListCursorAdapter;
import com.yang.file_explorer.adapter.SearchFileCursorAdaper;
import com.yang.file_explorer.adapter.SearchPopUpWindowAdapter;
import com.yang.file_explorer.apis.FileCategoryHelper;
import com.yang.file_explorer.apis.FileIconHelper;
import com.yang.file_explorer.apis.FileInteractionHub;
import com.yang.file_explorer.apis.IntentBuilder;
import com.yang.file_explorer.apis.FileCategoryHelper.FileCategoryType;
import com.yang.file_explorer.apis.FileSortHelper;
import com.yang.file_explorer.apis.FileSortHelper.SortMethod;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.interfaces.IFileInteractionListener;
import com.yang.file_explorer.utils.LogUtils;
import com.yang.file_explorer.widget.SearchEditText;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
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

public class SearchActivity extends Activity implements OnClickListener,
		OnItemClickListener, IFileInteractionListener {
    private String LOG_TAG = "SearchActivity";
	private SearchEditText msearchEditText;
	private LinearLayout mbackLayout;
	private LinearLayout minputLayout;
	private LinearLayout mvoiceLayout;
	private LinearLayout mfilterLayout;
	private LinearLayout mprogressview;
	private TextView mtextviewHint;
	private TextView mtextviewEmpty;
	private ImageButton mbtnClean;
	private ImageButton mbtnVoice;
	private ListView mlistviewSearch;
	private FileCategoryHelper mFileCagetoryHelper;
	private FileIconHelper mFileIconHelper;
	private SearchFileCursorAdaper mAdapter;
	private int VOICE_RECOGNITION_REQUEST_CODE = 10000;
	private PopupWindow mpopupFilter;
	private SearchFileTask mSearchFileTask;
	private static String[] arraryFilterStrings;
	private String mCurrentFilter;
	private String mSearchFileName;

	private HashMap<String, FileCategoryType> filterTypeMap = new HashMap<String, FileCategoryHelper.FileCategoryType>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_activity);

		msearchEditText = (SearchEditText) findViewById(R.id.input);
		mbackLayout = (LinearLayout) findViewById(R.id.back);
		minputLayout = (LinearLayout) findViewById(R.id.input_layout);
		mvoiceLayout = (LinearLayout) findViewById(R.id.voice_layout);
		mfilterLayout = (LinearLayout) findViewById(R.id.filter);
		mtextviewHint = (TextView) findViewById(R.id.hint);
		mtextviewEmpty = (TextView) findViewById(R.id.empty_view);
		mbtnClean = (ImageButton) findViewById(R.id.clean);
		mbtnVoice = (ImageButton) findViewById(R.id.voice);

		mlistviewSearch = (ListView) findViewById(R.id.search_list);
		mprogressview = (LinearLayout) findViewById(R.id.progress_view);

		mFileCagetoryHelper = new FileCategoryHelper(this);
		mFileIconHelper = new FileIconHelper(this);

		mAdapter = new SearchFileCursorAdaper(this, null,mFileIconHelper);
		mlistviewSearch.setAdapter(mAdapter);
		mlistviewSearch.setOnItemClickListener(this);

		arraryFilterStrings = getResources().getStringArray(
				R.array.filter_category);

		filterTypeMap.put(arraryFilterStrings[0], FileCategoryType.All);
		filterTypeMap.put(arraryFilterStrings[1], FileCategoryType.Music);
		filterTypeMap.put(arraryFilterStrings[2], FileCategoryType.Video);
		filterTypeMap.put(arraryFilterStrings[3], FileCategoryType.Picture);
		filterTypeMap.put(arraryFilterStrings[4], FileCategoryType.Doc);
		filterTypeMap.put(arraryFilterStrings[5], FileCategoryType.Zip);
		filterTypeMap.put(arraryFilterStrings[6], FileCategoryType.Apk);
		mCurrentFilter = arraryFilterStrings[0];

		mbackLayout.setOnClickListener(this);
		mbtnClean.setOnClickListener(this);
		mbtnVoice.setOnClickListener(this);
		mfilterLayout.setOnClickListener(this);

		msearchEditText.addTextChangedListener(new SearchTextWatcher(this));
		msearchEditText.setOnFocusChangeListener(new SearchFocusChange(this));

		msearchEditText.postDelayed(new SearchAction(this), 100);
		
		showEmptyView(true);
	}

	/*
	 * 空白文件
	 */
	private void showEmptyView(boolean show) {
		if (mtextviewEmpty != null){
			mtextviewEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
			mlistviewSearch.setVisibility(show ? View.GONE : View.VISIBLE);
		}
			
	}

	/*
	 * 开始搜索文件
	 */
	private void onStartSearchFile(String filename) {

		mSearchFileName = filename;
		if (TextUtils.isEmpty(mSearchFileName)) {
			mAdapter.changeCursor(null);
			return;
		}

		if (mSearchFileTask != null
				&& mSearchFileTask.getStatus() != AsyncTask.Status.FINISHED) {
			mSearchFileTask.cancel(true);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		mSearchFileTask = new SearchFileTask();
		mSearchFileTask.execute(filterTypeMap.get(mCurrentFilter));
	}

	/*
	 * 结束搜索文件
	 */
	private void onEndSearchFile(Cursor c) {
		mprogressview.setVisibility(View.GONE);
		mAdapter.changeCursor(c);
		showEmptyView(c == null || c.getCount() == 0);
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
				// //通过Intent传递语音识别的模式,开启语音
				Intent intent = new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				// 语言模式和自由形式的语音识别
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				// 提示语音开始
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "开始语音,请讲话。。。");
				// 开始语音识别
				startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			} catch (ActivityNotFoundException e) {
				// TODO: handle exception
				// 找不到语音设备装置
				Toast.makeText(this, "找不到语音设备装置  ", Toast.LENGTH_LONG).show();
				startActivity(new Intent(
						"android.intent.action.VIEW",
						Uri.parse("market://search?q=pname:com.google.android.voicesearch")));
			}
			break;

		case R.id.filter:
			if (mpopupFilter == null) {
				LinearLayout layout = (LinearLayout) View.inflate(this,
						R.layout.dropdown, null);
				ListView popListView = (ListView) layout
						.findViewById(R.id.PopUpWindowlistView);
				popListView.setTag(Integer.valueOf(R.id.filter));
				popListView.setAdapter(new SearchPopUpWindowAdapter(
						getResources().getStringArray(R.array.filter_category),
						this));
				popListView.setOnKeyListener(new FileFilterKeyListener(this));
				popListView.setOnItemClickListener(this);
				mpopupFilter = new PopupWindow(layout,
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				mpopupFilter.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.menu_dropdown_panel_holo_light));
				mpopupFilter.setFocusable(true);
				mpopupFilter.setOutsideTouchable(true);
				mpopupFilter.setTouchable(true);
			}
			mpopupFilter.showAsDropDown(mfilterLayout, 0, getResources()
					.getDimensionPixelSize(R.dimen.menu_y));
			break;
		default:
			break;
		}
	}

	// 当语音结束时的回调函数onActivityResult
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// 取得语音的字符
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (results != null) {
				// 设置视图更新
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
		if(parent.getAdapter() == mAdapter){
			//浏览文件
			FileInfo fileInfo = getItem(position);
			if (!fileInfo.IsDir) {
				try {
					IntentBuilder.viewFile(this, fileInfo.filePath);
				} catch (ActivityNotFoundException e) {
					LogUtils.e(LOG_TAG, "fail to view file: " + e.toString());
				}
			}
		}
		else {
			if (((Integer) parent.getTag()).intValue() == R.id.filter) {
				mCurrentFilter = arraryFilterStrings[position];
				
				if (!TextUtils.isEmpty(mSearchFileName)) {
					onStartSearchFile(mSearchFileName);
				}
				
				((TextView) mfilterLayout.getChildAt(0)).setText(mCurrentFilter);
				if (mpopupFilter != null) {
					mpopupFilter.dismiss();
				}
			}
		}
		
	}

	// 搜索编辑框文本监视器
	class SearchTextWatcher implements TextWatcher {

		private Context mContext;

		public SearchTextWatcher(SearchActivity searchActivity) {
			mContext = searchActivity;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

			msearchEditText.setSelection(s.toString().length());
			
			if (s.toString().equals("")) {
				mtextviewHint.setVisibility(View.VISIBLE);
				mtextviewEmpty.setText(R.string.search_info);
				showEmptyView(true);
				mbtnClean.setVisibility(View.GONE);
				mvoiceLayout.setVisibility(View.VISIBLE);
				mbtnVoice.setVisibility(View.VISIBLE);

			} else {

				mtextviewHint.setVisibility(View.GONE);
				mtextviewEmpty.setText(R.string.no_file);
				mbtnClean.setVisibility(View.VISIBLE);
				mvoiceLayout.setVisibility(View.GONE);
				mbtnVoice.setVisibility(View.GONE);

			}

			//
			if (!s.equals(mSearchFileName)) {
				onStartSearchFile(s.toString());
			}

		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}

	}

	// 搜索编辑框焦点监视器
	class SearchFocusChange implements OnFocusChangeListener {

		public SearchFocusChange(SearchActivity searchActivity) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if (hasFocus) {
				minputLayout
						.setBackgroundResource(R.drawable.textfield_search_selected);
				mvoiceLayout
						.setBackgroundResource(R.drawable.textfield_search_right_selected);
			} else {
				minputLayout
						.setBackgroundResource(R.drawable.textfield_search_default);
				mvoiceLayout
						.setBackgroundResource(R.drawable.textfield_search_right_default);
			}
		}

	}

	// 软件盘显示
	class SearchAction implements Runnable {

		public SearchAction(SearchActivity searchActivity) {
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			InputMethodManager inputMethodManager = (InputMethodManager) msearchEditText
					.getContext().getSystemService(INPUT_METHOD_SERVICE);
			inputMethodManager.showSoftInput(msearchEditText, 0);
			msearchEditText.setFocusableInTouchMode(true);
		    
		}

	}

	// 文件过滤按键监听
	class FileFilterKeyListener implements OnKeyListener {

		public FileFilterKeyListener(SearchActivity searchActivity) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& keyCode == KeyEvent.KEYCODE_MENU) {
				mpopupFilter.dismiss();
			}
			return true;
		}

	}

	/*
	 * 文件搜索线程
	 */
	private class SearchFileTask extends
			AsyncTask<FileCategoryType, Void, Cursor> {

		@Override
		protected Cursor doInBackground(FileCategoryType... params) {
			// TODO Auto-generated method stub
			Cursor cursor = null;
			if (!isCancelled())
				cursor = mFileCagetoryHelper.query(params[0], mSearchFileName,SortMethod.name);
			return cursor;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			// TODO Auto-generated method stub
			if (!isCancelled())
				onEndSearchFile(result);
		}

	}

	@Override
	public View getViewById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public FileInfo getItem(int pos) {
		// TODO Auto-generated method stub
		return mAdapter.getFileItem(pos);
	}

	@Override
	public void onPick(FileInfo f) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sortCurrentList(FileSortHelper sort) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onRefreshFileList(String path, FileSortHelper sort) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<FileInfo> getAllFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSingleFile(FileInfo file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ShowMovingOperationBar(boolean isShow) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateMediaData() {
		// TODO Auto-generated method stub

	}

}
