package com.yang.file_explorer.apis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.menu;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.ActionMode;
import com.yang.file_explorer.adapter.CreateFileListAdater;
import com.yang.file_explorer.apis.FileListItem.ModeCallback;
import com.yang.file_explorer.apis.FileSortHelper.SortMethod;
import com.yang.file_explorer.R;
import com.yang.file_explorer.db.FavoriteDatabaseHelper;
import com.yang.file_explorer.entity.FileIcon;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.entity.GlobalConsts;
import com.yang.file_explorer.interfaces.IFileInteractionListener;
import com.yang.file_explorer.interfaces.IOperationProgressListener;
import com.yang.file_explorer.ui.MainActivity;
import com.yang.file_explorer.utils.FileUtil;
import com.yang.file_explorer.utils.LogUtils;
import com.yang.file_explorer.widget.CustomDialog;

public class FileInteractionHub implements IOperationProgressListener {

	private static final String LOG_TAG = "FileInteractionHub";

	private String createoutputImageName;

	private IFileInteractionListener mFileInteractionListener;

	private ArrayList<FileInfo> mCheckedFileNameList = new ArrayList<FileInfo>();

	private FileOperationHelper mFileOperationHelper;

	private FileSortHelper mFileSortHelper;

	private ProgressDialog progressDialog;

	private Context mContext;

	// File List view setup
	private ListView mFileListView;

	/*
	 * 当前文件路径
	 */
	private String mCurrentPath;

	/*
	 * 根目录文件路径
	 */
	private String mRootPath;

	public enum Mode {
		View, Pick
	};

	private Mode mcurrentMode;

	/*
	 * 
	 */
	public FileInteractionHub(IFileInteractionListener fileInteractionListener) {
		assert (fileInteractionListener != null);

		mFileInteractionListener = fileInteractionListener;
		mFileSortHelper = new FileSortHelper();
		mContext = mFileInteractionListener.getContext();
		mFileOperationHelper = new FileOperationHelper(this, mContext);
		setup();
	}

	private void setup() {
		setupFileListView();
	}

	/*
	 * listview 事件
	 */
	private void setupFileListView() {
		mFileListView = (ListView) mFileInteractionListener
				.getViewById(R.id.file_path_list);

		mFileListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onListItemClick(parent, view, position, id);
			}
		});
	}

	private void showProgress(String msg) {
		progressDialog = new ProgressDialog(mContext);
		// dialog.setIcon(R.drawable.icon);
		progressDialog.setMessage(msg);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	/*
	 * ListView 刷新
	 */
	public void refreshFileList() {
		clearSelection();

		// onRefreshFileList returns true indicates list has changed
		mFileInteractionListener.onRefreshFileList(mCurrentPath,
				mFileSortHelper);

	}

	/*
	 * listView 点击事件
	 */
	public void onListItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		FileInfo lFileInfo = mFileInteractionListener.getItem(position);

		if (lFileInfo == null) {
			LogUtils.e(LOG_TAG, "file does not exist on position:" + position);
			return;
		}

		if (isInSelection()) {
			boolean selected = lFileInfo.Selected;
			ActionMode actionMode = ((MainActivity) mContext).getActionMode();
			ImageView checkBox = (ImageView) view
					.findViewById(R.id.file_checkbox);
			if (selected) {
				mCheckedFileNameList.remove(lFileInfo);
				checkBox.setImageResource(R.drawable.btn_check_off);
			} else {
				mCheckedFileNameList.add(lFileInfo);
				checkBox.setImageResource(R.drawable.btn_check_on);
			}
			if (actionMode != null) {
				if (mCheckedFileNameList.size() == 0)
					actionMode.finish();
				else
					actionMode.invalidate();
			}
			lFileInfo.Selected = !selected;

			FileUtil.updateActionModeTitle(actionMode, mContext,
					getSelectedFileList().size());
			return;
		}

		if (!lFileInfo.IsDir) {
			if (mcurrentMode == Mode.Pick) {
				mFileInteractionListener.onPick(lFileInfo);
			} else {
				viewFile(lFileInfo);
			}
			return;
		}

		mCurrentPath = getAbsoluteName(mCurrentPath, lFileInfo.fileName);
		ActionMode actionMode = ((MainActivity) mContext).getActionMode();
		if (actionMode != null) {
			actionMode.finish();
		}
		refreshFileList();
	}

	// check or uncheck
	public boolean onCheckItem(FileInfo f, View v) {
		switch (v.getId()) {
		case R.id.category_file_checkbox_area:
		case R.id.file_checkbox_area: {
			if (f.Selected) {
				mCheckedFileNameList.add(f);
			} else {
				mCheckedFileNameList.remove(f);
			}
		}
			break;

		default:
			break;
		}

		return true;
	}

	/*
	 * 浏览文件
	 */
	private void viewFile(FileInfo lFileInfo) {
		try {
			IntentBuilder.viewFile(mContext, lFileInfo.filePath);
		} catch (ActivityNotFoundException e) {
			LogUtils.e(LOG_TAG, "fail to view file: " + e.toString());
		}
	}

	/*
	 * 是否全部选中
	 */

	public boolean isAllSelection() {
		return mCheckedFileNameList.size() == mFileInteractionListener
				.getAllFiles().size();
	}

	/*
	 * 是否在文件选中状态
	 */
	public boolean isInSelection() {
		return mCheckedFileNameList.size() > 0;
	}

	/*
	 * 获取文件绝对路径
	 */
	private String getAbsoluteName(String path, String name) {
		return path.equals(GlobalConsts.ROOT_PATH) ? path + name : path
				+ File.separator + name;
	}

	/*
	 * 选中文件数量
	 */
	public ArrayList<FileInfo> getSelectedFileList() {
		return mCheckedFileNameList;
	}

	public void setRootPath(String path) {
		mRootPath = path;
		mCurrentPath = path;
	}

	public String getRootPath() {
		return mRootPath;
	}

	public String getCurrentPath() {
		return mCurrentPath;
	}

	public void setCurrentPath(String path) {
		mCurrentPath = path;
	}

	public void setMode(Mode mode) {
		mcurrentMode = mode;
	}

	public Mode getMode() {
		return mcurrentMode;
	}

	public SortMethod getSortMethod() {
		return mFileSortHelper.getSortMethod();
	}

	public FileInfo getItem(int pos) {
		return mFileInteractionListener.getItem(pos);
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}

		mFileInteractionListener.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				clearSelection();
				refreshFileList();
			}
		});
	}

	@Override
	public void onFileChanged(String path) {
		// TODO Auto-generated method stub
		notifyFileSystemChanged(path);
	}

	/*
	 * 通知扫描Sd卡
	 */
	private void notifyFileSystemChanged(String path) {
		if (path == null)
			return;

		// MediaScannerConnection.scanFile(mContext, new String[] { path },
		// null, new OnScanCompletedListener() {
		//
		// @Override
		// public void onScanCompleted(String path, Uri uri) {
		// // TODO Auto-generated method stub
		// //mFileInteractionListener.updateMediaData();
		// LogUtils.d(LOG_TAG, "notifyFileSystemChanged");
		// }
		// });

		final File f = new File(path);
		final Intent intent;
		if (f.isDirectory()) {
			intent = new Intent(GlobalConsts.FILEUPDATEBROADCAST);

		} else {
			intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			intent.setData(Uri.fromFile(new File(path)));

		}
		mContext.sendBroadcast(intent);
	}

	/*
	 * 返回到文件上一层目录
	 */
	public boolean onOperationUpLevel() {

		if (!mRootPath.equals(mCurrentPath)
				&& !mRootPath.contains(mCurrentPath)) {
			mCurrentPath = new File(mCurrentPath).getParent();
			refreshFileList();
			return true;
		}

		return false;
	}

	/*
	 * 后退键事件
	 */
	public boolean onBackPressed() {
		if (isInSelection()) {
			clearSelection();
		} else if (!onOperationUpLevel()) {
			return false;
		}
		return true;
	}

	// /////////////////////////////////////////////文件操作函数////////////////////////////////////////////////////////////////////////////

	public void onOperationButtonConfirm() {
		if (isInSelection()) {
			clearSelection();
		} else if (mFileOperationHelper.isMoveState()) {
			if (mFileOperationHelper.EndMove(mCurrentPath)) {
				showProgress(mContext.getString(R.string.operation_moving));
			}
		} else {
			if (mFileOperationHelper.Paste(mCurrentPath)) {
				showProgress(mContext.getString(R.string.operation_pasting));
			}
		}

		mFileInteractionListener.ShowMovingOperationBar(false);
	}

	public void onOperationButtonCancel() {
		mFileOperationHelper.clear();

		if (isInSelection()) {
			clearSelection();
		} else if (mFileOperationHelper.isMoveState()) {
			// refresh to show previously selected hidden files
			mFileOperationHelper.EndMove(null);
			refreshFileList();
		} else {
			refreshFileList();
		}

		mFileInteractionListener.ShowMovingOperationBar(false);
	}

	/*
	 * 全选
	 */
	public void onOperationSelectAll() {
		mCheckedFileNameList.clear();
		for (FileInfo f : mFileInteractionListener.getAllFiles()) {
			f.Selected = true;
			mCheckedFileNameList.add(f);
		}

		MainActivity mainActivity = (MainActivity) mContext;
		ActionMode mode = mainActivity.getActionMode();
		if (mode == null) {
			mode = mainActivity
					.startActionMode(new ModeCallback(mContext, this));
			mainActivity.setActionMode(mode);
			FileUtil.updateActionModeTitle(mode, mContext,
					mCheckedFileNameList.size());
		}

		mFileInteractionListener.onDataChanged();
	}

	/*
	 * 取消选中的所有列表
	 */

	public void clearSelection() {
		if (mCheckedFileNameList.size() > 0) {
			for (FileInfo f : mCheckedFileNameList) {
				if (f == null) {
					continue;
				}
				f.Selected = false;
			}
			mCheckedFileNameList.clear();
			mFileInteractionListener.onDataChanged();
		}
	}

	/*
	 * 删除文件
	 */
	public void onOperationDelete() {
		doOperationDelete(getSelectedFileList());
	}

	private void doOperationDelete(final ArrayList<FileInfo> selectedFileList) {
		final ArrayList<FileInfo> selectedFiles = new ArrayList<FileInfo>(
				selectedFileList);

		Dialog dialog = new CustomDialog.Builder(mContext)
				.setTitle(
						mContext.getString(R.string.operation_delete_confirm_message))

				.setPositiveButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						clearSelection();
					}
				}).setNegativeButton(R.string.confirm, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						if (mFileOperationHelper.Delete(selectedFiles)) {
							showProgress(mContext
									.getString(R.string.operation_delete));
						}
						clearSelection();
					}
				}).create();

		dialog.show();
	}

	/*
	 * 复制文件
	 */
	public void onOperationCopy(ArrayList<FileInfo> files) {
		mFileOperationHelper.Copy(files);
		mFileInteractionListener.ShowMovingOperationBar(true);
		clearSelection();
	}

	/*
	 * 剪切文件
	 */
	public void onOperationMove(ArrayList<FileInfo> files) {
		mFileOperationHelper.StartMove(files);
		mFileInteractionListener.ShowMovingOperationBar(true);
		// refresh to hide selected files
		refreshFileList();
	}

	/*
	 * 分享文件
	 */
	public void onOperationShare() {
		ArrayList<FileInfo> selectedFileLists = getSelectedFileList();
		for (FileInfo f : selectedFileLists) {
			if (f.IsDir) {
				AlertDialog dialog = new AlertDialog.Builder(mContext)
						.setTitle(R.string.error_info_cant_send_folder)
						.setPositiveButton(R.string.confirm, null).create();
				dialog.show();
				return;
			}
		}

		Intent intent = IntentBuilder.buildSendFile(selectedFileLists);
		if (intent != null) {
			try {
				mFileInteractionListener.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			clearSelection();
		}
	}

	/*
	 * 文件重命名
	 */

	public void onOperationRename() {

		if (getSelectedFileList().size() == 0)
			return;

		final FileInfo f = getSelectedFileList().get(0);
		clearSelection();

		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		View view = layoutInflater.inflate(R.layout.dialog_input_layout, null);
		final EditText editText = (EditText) view.findViewById(R.id.edit_text);
		editText.setText(R.string.operation_rename_message);

		Dialog dialog = new CustomDialog.Builder(mContext)
				.setTitle(R.string.operation_rename).setContentView(view)
				.setPositiveButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (editText != null) {
							String textString = editText.getText().toString();
							dialog.dismiss();
							doRename(f, textString);
						} else {
							dialog.dismiss();
						}

					}
				}).create();
		dialog.show();

	}

	private boolean doRename(final FileInfo f, String text) {
		if (TextUtils.isEmpty(text))
			return false;

		if (mFileOperationHelper.Rename(f, text)) {
			f.fileName = text;
			mFileInteractionListener.onDataChanged();
		} else {
			new AlertDialog.Builder(mContext)
					.setMessage(mContext.getString(R.string.fail_to_rename))
					.setPositiveButton(R.string.confirm, null).create().show();
			return false;
		}

		return true;
	}

	/*
	 * 收藏
	 */
	public void onOperationFavorite(String path) {
		FavoriteDatabaseHelper databaseHelper = FavoriteDatabaseHelper
				.getInstance();
		if (databaseHelper != null) {

			if (databaseHelper.isFavorite(path)) {
				databaseHelper.delete(path);
			} else {
				databaseHelper.insert(FileUtil.getNameFromFilepath(path), path);
			}

		}
	}

	/*
	 * 文件详情
	 */

	public void onOperationdetail() {
		if (getSelectedFileList().size() == 0)
			return;

		final FileInfo f = getSelectedFileList().get(0);
		clearSelection();

		int icon = 0;

		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		View view = layoutInflater.inflate(R.layout.information_dialog, null);
		((TextView) view.findViewById(R.id.information_location))
				.setText(f.filePath);
		;
		((TextView) view.findViewById(R.id.information_modified))
				.setText(FileUtil.formatDateString(mContext, f.ModifiedDate));
		((TextView) view.findViewById(R.id.information_canwrite))
				.setText(f.canWrite ? "是" : "否");
		((TextView) view.findViewById(R.id.information_canread))
				.setText(f.canRead ? "是" : "否");
		((TextView) view.findViewById(R.id.information_ishidden))
				.setText(f.isHidden ? "是" : "否");

		final TextView filesizeviewTextView = (TextView) view
				.findViewById(R.id.information_size);

		if (f.IsDir) {
			icon = R.drawable.ic_folder_filetype;
			File file = new File(f.filePath);

			new AsyncTask<File, Long, Void>() {

				@Override
				protected Void doInBackground(File... params) {
					// TODO Auto-generated method stub

					final File[] children = params[0].listFiles();
					long total = 0;
					if (children != null)
						for (final File child : children) {
							total += FileUtil.getTotalSizeOfFilesInDir(child);
							publishProgress(total);
						}

					return null;
				}

				@Override
				protected void onProgressUpdate(Long... values) {
					filesizeviewTextView.setText(FileUtil
							.convertStorage(values[0])
							+ " ("
							+ Long.valueOf(values[0]) + "字节)");
				};

			}.execute(file);

		} else {
			String ext = FileUtil.getExtFromFilename(f.fileName);
			icon = FileIconHelper.getFileIcon(ext);
			filesizeviewTextView.setText(FileUtil.convertStorage(f.fileSize)
					+ " (" + Long.valueOf(f.fileSize) + "字节)");
		}

		Dialog dialog = new CustomDialog.Builder(mContext).setIcon(icon)
				.setTitle(f.fileName).setContentView(view)
				.setPositiveButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).create();

		dialog.show();
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// ///////////////////////////////////////////////Menu操作///////////////////////////////////////////////////////////////////////////
	public void onMenuOperation(int itemId) {
		switch (itemId) {
		case GlobalConsts.MENU_NEW_FOLDER: // 新建文件
			onOperationNewFile();
			break;
		case GlobalConsts.MENU_SORT_DATE:
			onSortChanged(SortMethod.date);
			break;

		case GlobalConsts.MENU_SORT_NAME:
			onSortChanged(SortMethod.name);
			break;

		case GlobalConsts.MENU_SORT_TYPE:
			onSortChanged(SortMethod.type);
			break;

		case GlobalConsts.MENU_SORT_SIZE:
			onSortChanged(SortMethod.size);
			break;

		case GlobalConsts.MENU_REFRESH:
			refreshFileList();
			break;
			
		case GlobalConsts.MENU_SETTING:
			mContext.startActivity(new Intent("com.yang.file_explorer.ui.SettingActivity"));;
			break;
			
		case GlobalConsts.MENU_ABOUT:
			onAbout();
			break;

		case GlobalConsts.MENU_EXIT:
			((MainActivity) mContext).exit();
			break;
		default:
			break;
		}
	}

	/*
	 * 文件排序
	 */
	public void onSortChanged(SortMethod s) {
		if (mFileSortHelper.getSortMethod() != s) {
			mFileSortHelper.setSortMethog(s);
			sortCurrentList();
		}
	}

	public void sortCurrentList() {
		mFileInteractionListener.sortCurrentList(mFileSortHelper);
	}

	/*
	 * 新建文件
	 */
	public void onOperationNewFile() {

		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		View view = layoutInflater.inflate(R.layout.dialog_list_layout, null);
		ListView fileListView = (ListView) view.findViewById(R.id.listView);

		List<FileIcon> fileIconList = new ArrayList<FileIcon>();
		FileIcon fileIcon1 = new FileIcon("文件夹", R.drawable.ic_folder_new);
		fileIconList.add(fileIcon1);
		FileIcon fileIcon2 = new FileIcon("拍照", R.drawable.ic_takephoto_new);
		fileIconList.add(fileIcon2);

		ArrayAdapter<FileIcon> fileAdapter = new CreateFileListAdater(mContext,
				R.layout.create_file_list_item, fileIconList);
		fileListView.setAdapter(fileAdapter);

		final Dialog dialog = new CustomDialog.Builder(mContext)
				.setTitle(R.string.create).setContentView(view)
				.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).create();
		dialog.show();

		fileListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				if (position == 0) {
					// 创建文件夹
					createFloder();
				} else if (position == 1) {
					// 拍照
					createTakePhoto();
				}
			}
		});

	}

	/*
	 * 关于
	 */

	public void onAbout() {
		Dialog dialog = new AlertDialog.Builder(mContext)
				.setIcon(R.drawable.ic_logo_actionbar)
				.setTitle(R.string.about_dlg_title)
				.setMessage(R.string.about_dlg_message).setPositiveButton(R.string.confirm, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).create();
		
		dialog.show();
	}

	/*
	 * 创建文件夹
	 */
	private void createFloder() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		View view = layoutInflater.inflate(R.layout.dialog_input_layout, null);
		final EditText editText = (EditText) view.findViewById(R.id.edit_text);
		editText.setText("新建文件夹");

		Dialog dialog = new CustomDialog.Builder(mContext)
				.setTitle(R.string.create_folder_name).setContentView(view)
				.setPositiveButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (editText != null) {
							String textString = editText.getText().toString();
							dialog.dismiss();
							doCreateFolder(textString);
						} else {
							dialog.dismiss();
						}

					}
				}).create();
		dialog.show();
	}

	private boolean doCreateFolder(String text) {
		if (TextUtils.isEmpty(text))
			return false;

		if (mFileOperationHelper.CreateFolder(mCurrentPath, text)) {
			mFileInteractionListener.addSingleFile(FileUtil
					.GetFileInfo(FileUtil.makePath(mCurrentPath, text)));
			mFileListView.setSelection(mFileListView.getCount() - 1);
		} else {
			new AlertDialog.Builder(mContext)
					.setMessage(
							mContext.getString(R.string.fail_to_create_folder))
					.setPositiveButton(R.string.confirm, null).create().show();
			return false;
		}

		return true;
	}

	/*
	 * 拍照
	 */
	private void createTakePhoto() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		View view = layoutInflater.inflate(R.layout.dialog_input_layout, null);
		final EditText editText = (EditText) view.findViewById(R.id.edit_text);
		editText.setText("照片");

		Dialog dialog = new CustomDialog.Builder(mContext)
				.setTitle(R.string.create_photo_name).setContentView(view)
				.setPositiveButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						if (editText != null) {
							String textString = editText.getText().toString();
							dialog.dismiss();
							doTakePhoto(textString);
						} else {
							dialog.dismiss();
						}

					}
				}).create();
		dialog.show();
	}

	private boolean doTakePhoto(String text) {
		if (TextUtils.isEmpty(text))
			return false;

		createoutputImageName = text + ".jpg";
		File outputImage = new File(mCurrentPath, createoutputImageName);
		try {
			if (outputImage.exists()) {
				outputImage.delete();
			}
			outputImage.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 将File对象转换为Uri并启动照相程序
		Uri imageUri = Uri.fromFile(outputImage);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 照相
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // 指定图片输出地址
		mFileInteractionListener.startActivityForResult(intent,
				GlobalConsts.TAKE_PHOTO); // 启动照相

		return true;
	}

	public void addTakePhotoFile() {
		String filePath = FileUtil
				.makePath(mCurrentPath, createoutputImageName);
		mFileInteractionListener.addSingleFile(FileUtil.GetFileInfo(filePath));
		mFileListView.setSelection(mFileListView.getCount() - 1);
		onFileChanged(filePath);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
