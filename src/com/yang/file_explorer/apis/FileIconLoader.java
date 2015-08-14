package com.yang.file_explorer.apis;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.yang.file_explorer.apis.FileCategoryHelper.FileCategoryType;
import com.yang.file_explorer.interfaces.IconLoadFinishListener;
import com.yang.file_explorer.utils.FileUtil;
import com.yang.file_explorer.utils.LogUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.Video;
import android.widget.ImageView;

/*
 * 文件图标加载类
 */

public class FileIconLoader implements Callback {

	private static final String LOADER_THREAD_NAME = "FileIconLoader";

	/**
	 * Type of message sent by the UI thread to itself to indicate that some
	 * photos need to be loaded.
	 */
	private static final int MESSAGE_REQUEST_LOADING = 1;

	/**
	 * Type of message sent by the loader thread to indicate that some photos
	 * have been loaded.
	 */
	private static final int MESSAGE_ICON_LOADED = 2;

	private static abstract class ImageHolder {
		public static final int NEEDED = 0;

		public static final int LOADING = 1;

		public static final int LOADED = 2;

		int state;

		public static ImageHolder create(FileCategoryType cate) {
			switch (cate) {
			case Apk:
				return new DrawableHolder();
			case Picture:
			case Video:
				return new BitmapHolder();
			}

			return null;
		};

		public abstract boolean setImageView(ImageView v);

		public abstract boolean isNull();

		public abstract void setImage(Object image);
	}

	private static class BitmapHolder extends ImageHolder {
		SoftReference<Bitmap> bitmapRef;

		@Override
		public boolean setImageView(ImageView v) {
			if (bitmapRef.get() == null)
				return false;
			v.setImageBitmap(bitmapRef.get());
			return true;
		}

		@Override
		public boolean isNull() {
			return bitmapRef == null;
		}

		@Override
		public void setImage(Object image) {
			bitmapRef = image == null ? null : new SoftReference<Bitmap>(
					(Bitmap) image);
		}
	}

	private static class DrawableHolder extends ImageHolder {
		SoftReference<Drawable> drawableRef;

		@Override
		public boolean setImageView(ImageView v) {
			if (drawableRef.get() == null)
				return false;

			v.setImageDrawable(drawableRef.get());
			return true;
		}

		@Override
		public boolean isNull() {
			return drawableRef == null;
		}

		@Override
		public void setImage(Object image) {
			drawableRef = image == null ? null : new SoftReference<Drawable>(
					(Drawable) image);
		}
	}

	public static class FileId {
		public String mPath;

		public long mId; // database id

		public FileCategoryType mCategoryType;

		public FileId(String path, long id, FileCategoryType cate) {
			mPath = path;
			mId = id;
			mCategoryType = cate;
		}
	}

	/**
	 * A soft cache for image thumbnails. the key is file path
	 */
	private final static ConcurrentHashMap<String, ImageHolder> mImageCache = new ConcurrentHashMap<String, ImageHolder>();

	/**
	 * A map from ImageView to the corresponding photo ID. Please note that this
	 * photo ID may change before the photo loading request is started.
	 */
	private final ConcurrentHashMap<ImageView, FileId> mPendingRequests = new ConcurrentHashMap<ImageView, FileId>();

	/**
	 * Handler for messages sent to the UI thread.
	 */
	private final Handler mMainThreadHandler = new Handler(this);

	/**
	 * Thread responsible for loading photos from the database. Created upon the
	 * first request.
	 */
	private LoaderThread mLoaderThread;

	/**
	 * A gate to make sure we only send one instance of MESSAGE_PHOTOS_NEEDED at
	 * a time.
	 */
	private boolean mLoadingRequested = false;

	/**
	 * Flag indicating if the image loading is paused.
	 */
	private boolean mPaused = false;

	private final Context mContext;

	private IconLoadFinishListener iconLoadListener;

	public FileIconLoader(Context context, IconLoadFinishListener l) {
		// TODO Auto-generated constructor stub
		mContext = context;
		iconLoadListener = l;
	}

	/*
	 * 主线线程
	 */
	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MESSAGE_REQUEST_LOADING: {
			mLoadingRequested = false;
			if (!mPaused) {
				if (mLoaderThread == null) {
					mLoaderThread = new LoaderThread();
					mLoaderThread.start();
				}

				mLoaderThread.requestLoading();
			}
			return true;
		}

		case MESSAGE_ICON_LOADED: {
			if (!mPaused) {
				processLoadedIcons();
			}
			return true;
		}
		}
		return false;
	}

	private void processLoadedIcons() {
		Iterator<ImageView> iterator = mPendingRequests.keySet().iterator();
		while (iterator.hasNext()) {
			ImageView view = iterator.next();
			FileId fileId = mPendingRequests.get(view);
			boolean loaded = loadCachedIcon(view, fileId.mPath,
					fileId.mCategoryType);
			if (loaded) {
				iterator.remove();
				iconLoadListener.onIconLoadFinished(view);
			}
		}

		if (!mPendingRequests.isEmpty()) {
			requestLoading();
		}
	}

	public void cancelRequest(ImageView view) {
		mPendingRequests.remove(view);
	}

	private boolean loadCachedIcon(ImageView view, String path,
			FileCategoryType cate) {
		ImageHolder holder = mImageCache.get(path);

		if (holder == null) {
			holder = ImageHolder.create(cate);
			if (holder == null)
				return false;

			mImageCache.put(path, holder);
		} else if (holder.state == ImageHolder.LOADED) {
			if (holder.isNull()) {
				return true;
			}

			// failing to set imageview means that the soft reference was
			// released by the GC, we need to reload the photo.
			if (holder.setImageView(view)) {
				return true;
			}
		}

		holder.state = ImageHolder.NEEDED;
		return false;
	}

	/*
	 * 加载图片到imageview控件，如果图片已经缓存立即加载，否则从媒体内容提供器获取
	 */
	public boolean loadIcon(ImageView view, String path, long id,
			FileCategoryType cate) {
		boolean loaded = loadCachedIcon(view, path, cate);
		if (loaded) {
			mPendingRequests.remove(view);
		} else {
			FileId p = new FileId(path, id, cate);
			mPendingRequests.put(view, p);
			if (!mPaused) {
				// Send a request to start loading photos
				requestLoading();
			}
		}
		return loaded;
	}

	private void requestLoading() {
		if (!mLoadingRequested) {
			mLoadingRequested = true;
			mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
		}
	}

	public long getDbId(String path, boolean isVideo) {
		String volumeName = "external";
		Uri uri = isVideo ? Video.Media.getContentUri(volumeName)
				: Images.Media.getContentUri(volumeName);
		String selection = FileColumns.DATA + "=?";
		;
		String[] selectionArgs = new String[] { path };

		String[] columns = new String[] { FileColumns._ID, FileColumns.DATA };

		Cursor c = mContext.getContentResolver().query(uri, columns, selection,
				selectionArgs, null);
		if (c == null) {
			return 0;
		}
		long id = 0;
		if (c.moveToFirst()) {
			id = c.getLong(0);
		}
		c.close();
		return id;
	}

	/**
	 * 加载图片(从媒体提供器获取)、app缩略图线程
	 */
	private class LoaderThread extends HandlerThread implements Callback {

		private Handler mLoaderThreadHandler;

		public LoaderThread() {
			super(LOADER_THREAD_NAME);
			// TODO Auto-generated constructor stub
		}

		public void requestLoading() {
			if (mLoaderThreadHandler == null) {
				mLoaderThreadHandler = new Handler(getLooper(), this);
			}
			mLoaderThreadHandler.sendEmptyMessage(0);
		}

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Iterator<FileId> iterator = mPendingRequests.values().iterator();
			while (iterator.hasNext()) {
				FileId id = iterator.next();
				ImageHolder holder = mImageCache.get(id.mPath);
				if (holder != null && holder.state == ImageHolder.NEEDED) {
					// Assuming atomic behavior
					holder.state = ImageHolder.LOADING;
					switch (id.mCategoryType) {
					case Apk:
						Drawable icon = FileUtil.getApkIcon(mContext, id.mPath);
						holder.setImage(icon);
						break;
					case Picture:
					case Video:
						boolean isVideo = id.mCategoryType == FileCategoryType.Video;
						if (id.mId == 0)
							id.mId = getDbId(id.mPath, isVideo);
						if (id.mId == 0) {
							LogUtils.e("FileIconLoader",
									"Fail to get dababase id for:" + id.mPath);
						}
						holder.setImage(isVideo ? getVideoThumbnail(id.mId)
								: getImageThumbnail(id.mId));
						break;
					default:
						break;
					}

					holder.state = BitmapHolder.LOADED;
					mImageCache.put(id.mPath, holder);
				}
			}

			mMainThreadHandler.sendEmptyMessage(MESSAGE_ICON_LOADED);
			return true;
		}

		private Bitmap getImageThumbnail(long id) {
			return Images.Thumbnails.getThumbnail(
					mContext.getContentResolver(), id, Thumbnails.MICRO_KIND,
					null);
		}

		private Bitmap getVideoThumbnail(long id) {
			return Video.Thumbnails.getThumbnail(mContext.getContentResolver(),
					id, Thumbnails.MICRO_KIND, null);
		}

	}

}
