package ru.ruilko;

import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WriteActivity extends Activity implements OnClickListener {
	private static final String TAG = "WriteActivity";
	private Button btnCancel;
	private Button btnSave;
	private DbHelper dbHelper;
	private SQLiteDatabase db;
	private Item item;
	private EditText editText;
	private TextView viewDate;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write);
        try {
	        btnSave   = (Button) findViewById(R.id.btnSave);
	        btnCancel = (Button) findViewById(R.id.btnCancel);
	        editText  = (EditText) findViewById(R.id.editText);
	        viewDate  = (TextView) findViewById(R.id.viewDate);
	
	        btnSave.setOnClickListener(this);
	        btnCancel.setOnClickListener(this);
	        
	        // TODO Get today record from DB and put it into edittext
            dbHelper = new DbHelper(this);
            db = dbHelper.getWritableDatabase();

            Date date = new Date();
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
            String uuid = String.format("%08d-%04d-%04d-0000-000000000000", date.getYear()+1900, date.getMonth(), date.getDay());
            item = dbHelper.readItem(db, uuid);
            if( item==null) {
            	Log.d(TAG, "There is no item in DB - will create new record");
	            item = new Item();
	            String dateStr = String.format("%d-%02d-%02d", date.getYear()+1900, date.getMonth(), date.getDay());
	            item.setTitle(dateStr);
	            item.setUuid( uuid );
            } else {
            	Log.d(TAG, "Fill record from DB");
            }

            viewDate.setText(item.getTitle());
            editText.setText(item.getNotes());            
        } catch (Exception e) {
        	Log.e(TAG, "Unable create activity: " + e.getMessage() + e.toString() + e.fillInStackTrace().toString());
		}
    }

	@Override
	public void onClick(View btn) {
		Intent resultIntent = new Intent();
		if( (Button)btn==btnSave ) {
			// TODO save into DB
			Log.d(TAG, "Save record into DB");
			item.setNotes(((TextView)(editText)).getText().toString());
			try{
				dbHelper.saveItem(db, item);
			}catch(Exception e) {
	        	Log.e(TAG, "Unable save item: " + e.getMessage() + e.toString() + e.fillInStackTrace().toString());
			}
		}
		setResult(Activity.RESULT_OK, resultIntent);
		if( db!=null )
			db.close();
		finish();
	}
}
