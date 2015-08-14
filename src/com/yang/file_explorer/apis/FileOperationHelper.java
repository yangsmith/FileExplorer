package com.yang.file_explorer.apis;

import com.yang.file_explorer.interfaces.IOperationProgressListener;

public class FileOperationHelper {

	private IOperationProgressListener moperationListener;

	public FileOperationHelper(IOperationProgressListener l) {
		moperationListener = l;
	}
}
