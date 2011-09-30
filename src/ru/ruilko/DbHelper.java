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

	public void atomicallyUpdateItem(Item item, LogItem logitem) {
		Log.d(TAG, "Atomically updating item in db: " + item.toString());
		begin();
		try {
			updateItem(item, logitem);
			commit();
		} finally {
			end();
		}		
	}

	private void updateOnlyLog(LogItem log, SQLiteDatabase db) {
		ContentValues values = new ContentValues(3);
		if( log.getUpdated()!=LogItem.CURRENT_TIMESTAMP ) {
			// Update from server
			values.put("updated", log.getUpdated());
		} else {
			// Locally modified
			values.put("updated", System.currentTimeMillis()/1000);
		}
		values.put("uuid", log.getUuid());
		values.put("status", log.getStatus().ordinal());
		db.replace(LOGS_TABLE_NAME, null, values);
	}

	private void updateOnlyItem(Item item, SQLiteDatabase db, boolean shouldUpdate) {
		if( shouldUpdate ) {
			ContentValues values = new ContentValues(3);
			values.put("uuid", item.getUuid());
			values.put("title", item.getTitle());
			values.put("notes", item.getNotes());
			db.replace(ITEMS_TABLE_NAME, null, values);
		}
		else {
			db.delete(ITEMS_TABLE_NAME, "uuid=?", new String[]{item.getUuid()});
		}
	}
	
	public void begin() {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
	}

	public void commit() {
		SQLiteDatabase db = getWritableDatabase();
		db.setTransactionSuccessful();
	}

	public void end() {
		SQLiteDatabase db = getWritableDatabase();
		db.endTransaction();
	}

	public Item readItem(String uuid) throws Exception {
		SQLiteDatabase db = getWritableDatabase();
		Item result = null;
		
		Log.d(TAG, "Reading item from db: " + uuid);
		String[] columns = new String[2];
		columns[0] = "title";
		columns[1] = "notes";

		String selection = "uuid=?";

		Cursor cursor = db.query(ITEMS_TABLE_NAME, columns, selection, new String[]{uuid}, null, null, null);
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
		SQLiteDatabase db = getWritableDatabase();
		
		String[] columns = new String[1];
		columns[0] = "uuid";

		String selection = "uuid=? AND updated<=?";

		Cursor cursor = db.query(LOGS_TABLE_NAME, columns, selection,
				new String[]{logItem.getUuid(), Integer.toString(logItem.getUpdated())}, null, null, null);
		if( cursor.moveToFirst() ) {
			return false;
		}
		
		return true;
	}

	public void updateItem(Item item, LogItem logItem) {
		Log.d(TAG, "Updating item in db: " + item.toString());
		SQLiteDatabase db = getWritableDatabase();
		updateOnlyItem(item, db, logItem.getStatus()==LogItem.Status.UPDATED);
		updateOnlyLog(logItem, db);
	}
	
	public void close() {
		SQLiteDatabase db = getWritableDatabase();
		db.close();		
	}
}
