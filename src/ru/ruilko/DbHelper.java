package ru.ruilko;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private static final String LOGS_TABLE_NAME = "Logs";

	private static final String LOGS_TABLE_CREATE = "CREATE TABLE " + LOGS_TABLE_NAME
			+ "(uuid TEXT PRIMARY KEY, updated INTEGER DEFAULT CURRENT_TIMESTAMP, status INTEGER);";

	private static final String LOGS_INDEX_CREATE = "CREATE INDEX LogsIdx ON " + LOGS_TABLE_NAME
			+ "(updated);";

	private static final String ITEMS_TABLE_NAME = "Items";

	private static final String ITEMS_TABLE_CREATE = "CREATE TABLE " + ITEMS_TABLE_NAME
			+ "(uuid TEXT PRIMARY KEY, title TEXT, notes TEXT);";

	private static final String TAG = "DbHelper";

	DbHelper(Context context) {
		super(context, "diary", null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(LOGS_TABLE_CREATE);
		db.execSQL(LOGS_INDEX_CREATE);
		db.execSQL(ITEMS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

	public void atomicallySaveItem(Item item) {
		Log.d(TAG, "Saving item to db: " + item.toString() + " - " + item.getNotes());
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			// save item
			ContentValues values = new ContentValues(3);
			values.put("uuid", item.getUuid());
			values.put("title", item.getTitle());
			values.put("notes", item.getNotes());
			db.replace(ITEMS_TABLE_NAME, null, values);

			// update log
			values = new ContentValues(2);
			values.put("uuid", item.getUuid());
			values.put("status", LogItem.Status.UPDATED.ordinal());
			db.replace(LOGS_TABLE_NAME, null, values);
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}		
	}

	public Item readItem(String uuid) {
		SQLiteDatabase db = getWritableDatabase();
		Item result = null;
		
		Log.d(TAG, "Reading item from db: " + uuid);
		String[] columns = new String[2];
		columns[0] = "title";
		columns[1] = "notes";

//		String selection = "uuid='?'";
		String selection = "uuid='" + uuid + "'";

		String[] selectionArgs = new String[1];
		selectionArgs[0] = uuid;

//		Cursor cursor = db.query(ITEMS_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		Cursor cursor = db.query(ITEMS_TABLE_NAME, columns, selection, null, null, null, null);
		if( cursor.moveToFirst() ) {
			Log.d(TAG, "Columns - " + cursor.getColumnNames().toString());
			result = new Item(uuid, cursor.getString(0), cursor.getString(1));
		}
		
		return result;
	}

	public List<String> getLocalUpdates(int lastUpdate) {
		List<String> result = new LinkedList<String>();
		return result;
	}

	public boolean shouldUpdate(LogItem logItem) {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateItem(LogItem logItem, Item item) {
		// TODO Auto-generated method stub
	}
	
	public void close() {
		SQLiteDatabase db = getWritableDatabase();
		db.close();		
	}
}
