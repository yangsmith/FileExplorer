package com.yang.file_explorer.interfaces;

public interface IOperationProgressListener {

	 void onFinish();

     void onFileChanged(String path);
     
    

}
