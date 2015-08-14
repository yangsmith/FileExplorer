package com.yang.file_explorer.apis;

import java.util.Comparator;
import java.util.HashMap;

import com.yang.file_explorer.entity.FileInfo;
import com.yang.file_explorer.utils.FileUtil;

public class FileSortHelper {

	private boolean mFileFirst = false;

	public enum SortMethod {
		name, size, date, type
	}

	private SortMethod mSortMethod;

	private HashMap<SortMethod, Comparator<FileInfo>> mcomparatorList = new HashMap<SortMethod, Comparator<FileInfo>>();

	public FileSortHelper() {
		mSortMethod = SortMethod.name;
		mcomparatorList.put(SortMethod.name, cmpName);
		mcomparatorList.put(SortMethod.size, cmpSize);
		mcomparatorList.put(SortMethod.date, cmpDate);
		mcomparatorList.put(SortMethod.type, cmpType);
	}

	public void setSortMethog(SortMethod s) {
		mSortMethod = s;
	}

	public SortMethod getSortMethod() {
		return mSortMethod;
	}

	public void setFileFirst(boolean f) {
		mFileFirst = f;
	}

	public Comparator getComparator() {
		return mcomparatorList.get(mSortMethod);
	}

	private abstract class FileComparator implements Comparator<FileInfo> {

		@Override
		public int compare(FileInfo lhs, FileInfo rhs) {
			if (lhs.IsDir == rhs.IsDir) {
				return doCompare(lhs, rhs);
			}

			if (mFileFirst) {
				return (lhs.IsDir ? 1 : -1);
			} else {
				return (lhs.IsDir ? -1 : 1);
			}

		}

		protected abstract int doCompare(FileInfo object1, FileInfo object2);
	}

	private Comparator cmpName = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return object1.fileName.compareToIgnoreCase(object2.fileName);
		}
	};

	private Comparator cmpSize = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return longToCompareInt(object1.fileSize - object2.fileSize);
		}
	};

	private Comparator cmpDate = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return longToCompareInt(object2.ModifiedDate - object1.ModifiedDate);
		}
	};

	private int longToCompareInt(long result) {
		return result > 0 ? 1 : (result < 0 ? -1 : 0);
	}

	private Comparator cmpType = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			int result = FileUtil.getExtFromFilename(object1.fileName)
					.compareToIgnoreCase(
							FileUtil.getExtFromFilename(object2.fileName));
			if (result != 0)
				return result;

			return FileUtil.getNameFromFilename(object1.fileName)
					.compareToIgnoreCase(
							FileUtil.getNameFromFilename(object2.fileName));
		}
	};
}
