package com.yang.file_explorer.db;

import com.yang.file_explorer.interfaces.FavoriteDatabaseListener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoriteDatabaseHelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "file_explorer";

	private final static int DATABASE_VERSION = 1;

	private final static String TABLE_NAME = "favorite";

	public final static String FIELD_ID = "_id";

	public final static String FIELD_TITLE = "title";

	public final static String FIELD_LOCATION = "location";

	private FavoriteDatabaseListener mListener;

	private boolean firstCreate;

	private static FavoriteDatabaseHelper instance;

	public FavoriteDatabaseHelper(Context context,
			FavoriteDatabaseListener listener) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		instance = this;
		mListener = listener;
	}

	public static FavoriteDatabaseHelper getInstance() {
		return instance;
	}
	
	public boolean isFirstCreate() {
		return firstCreate;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "Create table " + TABLE_NAME + "(" + FIELD_ID
				+ " integer primary key autoincrement," + FIELD_TITLE + " text,"
				+ FIELD_LOCATION + " text);";
		db.execSQL(sql);
		firstCreate = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql = " DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}
	
	/*
	 * 是否收藏文件
	 */
	public boolean isFavorite(String filepath){
		String selection = FIELD_LOCATION + "=?";
		String[] selectionArgs = new String[]{filepath};
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
			if (cursor == null) {
				return false;
			}
			boolean ret = cursor.getCount() > 0;
			cursor.close();
			return ret;
			
		} catch (SQLiteException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	/*
	 * 查选所有收藏文件
	 */
	public Cursor query() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db
				.query(TABLE_NAME, null, null, null, null, null, null);
		return cursor;
	}

	/*
	 * 收藏文件
	 */
	public long insert(String title, String location){
		if (isFavorite(location)) {
			return -1;
		}
		
		try {
			SQLiteDatabase db = getWritableDatabase();
			long ret = db.insert(TABLE_NAME, null, createValues(title, location));
			mListener.onFavoriteDatabaseChanged();
			return ret;
			
		} catch (SQLiteException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return -1;
		
	}
	
	/*
	 * 取消收藏
	 */
	public void delete(long id,boolean notify){
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = { Long.toString(id) };
		db.delete(TABLE_NAME, where, whereValue);

		if (notify)
			mListener.onFavoriteDatabaseChanged();
	}
	
	public void delete(String location) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_LOCATION + "=?";
		String[] whereValue = { location };
		db.delete(TABLE_NAME, where, whereValue);
		mListener.onFavoriteDatabaseChanged();
	}

	
	public void update(int id, String title, String location) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = { Integer.toString(id) };
		db.update(TABLE_NAME, createValues(title, location), where, whereValue);
		mListener.onFavoriteDatabaseChanged();
	}
	

	private ContentValues createValues(String title, String location) {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_TITLE, title);
		cv.put(FIELD_LOCATION, location);
		return cv;
	}
}
