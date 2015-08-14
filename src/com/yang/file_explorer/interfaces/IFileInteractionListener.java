package com.yang.file_explorer.interfaces;

import com.yang.file_explorer.apis.FileSortHelper;
import com.yang.file_explorer.entity.FileInfo;

import android.content.Context;
import android.view.View;

public interface IFileInteractionListener {

	public View getViewById(int id);

	public Context getContext();

	public FileInfo getItem(int pos);

	public void onPick(FileInfo f);

	public void onDataChanged();

	public void runOnUiThread(Runnable r);

	public void sortCurrentList(FileSortHelper sort);

	public boolean onRefreshFileList(String path, FileSortHelper sort);

}
