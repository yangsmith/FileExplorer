package com.yang.file_explorer.apis;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.yang.file_explorer.R;
import com.yang.file_explorer.adapter.SearchPopUpWindowAdapter;
import com.yang.file_explorer.apis.FileInteractionHub.Mode;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.ui.MainActivity;
import com.yang.file_explorer.utils.FileUtil;

public class FileListItem {

	private String LOG_TAG = "FileListItem";

	public static void setupFileListItemInfo(Context context, View view,
			FileInfo fileInfo, FileIconHelper fileIcon,
			FileInteractionHub fileInteractionHub) {

		ImageView checkboxImageView = (ImageView) view
				.findViewById(R.id.file_checkbox);
		ImageView favoriteImageView = (ImageView) view
				.findViewById(R.id.favorite_img);
		if (fileInteractionHub.getMode() == Mode.Pick) {
			checkboxImageView.setVisibility(View.GONE);
		} else {
			checkboxImageView
					.setImageResource(fileInfo.Selected ? R.drawable.btn_check_on
							: R.drawable.btn_check_off);
			favoriteImageView
					.setImageResource(fileInfo.Started ? R.drawable.ic_star_filetype
							: R.drawable.ic_graystar_filetype);

			checkboxImageView.setTag(fileInfo);
			view.setSelected(fileInfo.Selected);
		}

		// 显示文件名、大小、修改时间、文件夹中文件数量
		FileUtil.setText(view, R.id.file_name, fileInfo.fileName);
		FileUtil.setText(view, R.id.file_count, fileInfo.IsDir ? "("
				+ fileInfo.Count + ")" : "");
		FileUtil.setText(view, R.id.modified_time,
				FileUtil.formatDateString(context, fileInfo.ModifiedDate));
		FileUtil.setText(
				view,
				R.id.file_size,
				(fileInfo.IsDir ? "" : FileUtil
						.convertStorage(fileInfo.fileSize)));

		ImageView fileiconImageView = (ImageView) view
				.findViewById(R.id.file_image);
		ImageView fileiconframeImageView = (ImageView) view
				.findViewById(R.id.file_image_frame);
		if (fileInfo.IsDir) {
			fileiconframeImageView.setVisibility(View.GONE);
			fileiconImageView.setImageResource(R.drawable.ic_folder_filetype);
		} else {
			fileIcon.setIcon(fileInfo, fileiconImageView,
					fileiconframeImageView);
		}

	}

	public static class FileItemOnClickListener implements OnClickListener {

		private Context mContext;
		private FileInteractionHub mfileInteractionHub;

		public FileItemOnClickListener(Context context, FileInteractionHub hub) {
			mContext = context;
			mfileInteractionHub = hub;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.file_checkbox_area: // 选择按钮点击事件
			{
				ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
				assert (img != null && img.getTag() != null);

				FileInfo fileInfo = (FileInfo) img.getTag();
				fileInfo.Selected = !fileInfo.Selected;
				ActionMode actionMode = ((MainActivity) mContext)
						.getActionMode();
				if (actionMode == null) {
					actionMode = ((MainActivity) mContext)
							.startActionMode(new ModeCallback(mContext,
									mfileInteractionHub));
					((MainActivity) mContext).setActionMode(actionMode);
				} else {
					actionMode.invalidate();
				}

				if (mfileInteractionHub.onCheckItem(fileInfo, v)) {
					img.setImageResource(fileInfo.Selected ? R.drawable.btn_check_on
							: R.drawable.btn_check_off);
				} else {
					fileInfo.Selected = !fileInfo.Selected;
				}

				FileUtil.updateActionModeTitle(actionMode, mContext,
						mfileInteractionHub.getSelectedFileList().size());

			}
				break;
			case R.id.favorite_area: // 加星按钮点击事件
			{

			}
				break;
			default:
				break;
			}
		}
	}

	public static class ModeCallback implements ActionMode.Callback,
			OnClickListener, OnItemClickListener {

		private Menu mMenu;
		private Button btnTitle;
		private Context mContext;
		private FileInteractionHub mfInteractionHub;

		public ModeCallback(Context context,
				FileInteractionHub fileInteractionHub) {
			mContext = context;
			mfInteractionHub = fileInteractionHub;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			MenuInflater menuInflater = ((MainActivity) mContext)
					.getSupportMenuInflater();
			mMenu = menu;
			menuInflater.inflate(R.menu.action_mode_menu, mMenu);
			View titleView = View.inflate(mContext, R.layout.action_mode, null);
			btnTitle = (Button) titleView.findViewById(R.id.selection_menu);
			btnTitle.setOnClickListener(this);
			mode.setCustomView(titleView);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case R.id.delete: // 删除操作

				break;

			case R.id.copy: // 复制操作
				break;

			case R.id.cut: // 剪切操作
				break;

			case R.id.share: // 分享操作
				break;

			case R.id.favorite: // 收藏操作
				break;

			case R.id.rename: // 重命名操作
				break;

			case R.id.detail: // 详情操作
				break;

			case R.id.compress: // 压缩操作
				break;

			default:
				break;
			}

			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			mfInteractionHub.clearSelection();
			((MainActivity) mContext).setActionMode(null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 * 选中事件相应
		 */
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
	
			//if (mfInteractionHub.isAllSelection()) {
				// 全选时，创建取消菜单
				LinearLayout layout = (LinearLayout) View.inflate(mContext,
						R.layout.dropdown, null);
				ListView popListView = (ListView) layout
						.findViewById(R.id.PopUpWindowlistView);
				popListView.setAdapter(new SearchPopUpWindowAdapter(
						new String[] { "全选" }, mContext));
				popListView.setOnItemClickListener(this);
				PopupWindow mpopupFilter = new PopupWindow(layout,
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				mpopupFilter
						.setBackgroundDrawable(mContext
								.getResources()
								.getDrawable(
										R.drawable.menu_item_selecter));
				mpopupFilter.setFocusable(true);
				mpopupFilter.setOutsideTouchable(true);
				mpopupFilter.setTouchable(true);

				mpopupFilter.showAsDropDown(v, 0, mContext.getResources()
						.getDimensionPixelSize(R.dimen.menu_y));
			//} else {

			//}
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub

		}

	}

}
