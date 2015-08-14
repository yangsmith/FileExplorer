package com.yang.file_explorer.apis;

import java.util.HashMap;

import android.R.integer;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.yang.file_explorer.R;
import com.yang.file_explorer.apis.FileCategoryHelper.FileCategoryType;
import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.interfaces.IconLoadFinishListener;
import com.yang.file_explorer.utils.FileUtil;

public class FileIconHelper implements IconLoadFinishListener {

	private static final String LOG_TAG = "FileIconHelper";

	private static HashMap<ImageView, ImageView> imageFrames = new HashMap<ImageView, ImageView>();

	private static HashMap<String, Integer> fileExtToIcons = new HashMap<String, Integer>();

	private FileIconLoader mIconLoader;

	static {
		addItem(new String[] { "doc" }, R.drawable.ic_document_filetype);
		addItem(new String[] { "ppt" }, R.drawable.ic_ppt_filetype);
		addItem(new String[] { "excel" }, R.drawable.ic_excel_filetype);
		addItem(new String[] { "txt" }, R.drawable.ic_txt_filetype);
		addItem(new String[] { "mp4", "wmv", "mpeg", "m4v", "3gp", "3gpp", "3g2", "3gpp2", "asf","rmvb","avi" }, R.drawable.ic_video_filetype);
		addItem(new String[] { "jpg", "jpeg", "gif", "png", "bmp", "wbmp" }, R.drawable.ic_picture_filetype);
		addItem(new String[] { "zip","rar","7z" }, R.drawable.ic_compress_filetype);
		addItem(new String[] { "mp3","wma","wav" }, R.drawable.ic_audio_filetype);
		addItem(new String[] { "pdf" }, R.drawable.ic_pdf_filetype);
	}

	// 构造函数
	public FileIconHelper(Context context) {
		mIconLoader = new FileIconLoader(context, this);
	}

	private static void addItem(String[] exts, int resId) {
		if (exts != null) {
			for (String ext : exts) {
				fileExtToIcons.put(ext.toLowerCase(), resId);
			}
		}
	}

	// 根据文件后缀获取标识图标
	public static int getFileIcon(String ext) {
		Integer i = fileExtToIcons.get(ext.toLowerCase());
		if (i != null) {
			return i.intValue();
		} else {
			return R.drawable.ic_other_filetype;
		}
	}
	
	 public void setIcon(FileInfo fileInfo, ImageView fileImage, ImageView fileImageFrame) {
	        String filePath = fileInfo.filePath;
	        long fileId = fileInfo.dbId;
	        String extFromFilename = FileUtil.getExtFromFilename(filePath);
	        FileCategoryType fc = FileCategoryHelper.getCategoryFromPath(filePath);
	        fileImageFrame.setVisibility(View.GONE);
	        boolean set = false;
	        int id = getFileIcon(extFromFilename);
	        fileImage.setImageResource(id);

	        mIconLoader.cancelRequest(fileImage);
	        switch (fc) {
	            case Apk:
	                set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
	                break;
	            case Picture:
	            case Video:
	                set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
	                if (set)
	                    fileImageFrame.setVisibility(View.VISIBLE);
	                else {
	                    fileImage.setImageResource(fc == FileCategoryType.Picture ? R.drawable.ic_picture_filetype
	                            : R.drawable.ic_video_filetype);
	                    imageFrames.put(fileImage, fileImageFrame);
	                    set = true;
	                }
	                break;
	            default:
	                set = true;
	                break;
	        }

	        if (!set)
	            fileImage.setImageResource(R.drawable.ic_other_filetype);
	    }


	@Override
	public void onIconLoadFinished(ImageView view) {
		// TODO Auto-generated method stub
		 ImageView frame = imageFrames.get(view);
	        if (frame != null) {
	            frame.setVisibility(View.VISIBLE);
	            imageFrames.remove(view);
	        }
	}

}
