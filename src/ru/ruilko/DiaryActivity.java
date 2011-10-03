package ru.ruilko;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DiaryActivity extends Activity implements OnClickListener {
	public static final String SERVER_ADDRESS = "server_address";
	private static final String DEFAULT_SERVER_HOST = "10.0.2.2";
	private static final String TAG = "DiaryActivity";

	private Button btnSync;
	private Button btnWrite;
	private TextView textStatus;
	private DbHelper dbHelper;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnSync    = (Button) findViewById(R.id.btnSync);
        btnWrite   = (Button) findViewById(R.id.btnWrite);
        textStatus = (TextView) findViewById(R.id.viewStatus);
        
        btnWrite.setOnClickListener(this);
        btnSync.setOnClickListener(this);
        
        dbHelper = new DbHelper(this);
    }

	@Override
	public void onClick(View btn) {
		if( (Button)btn==btnWrite ) {
			textStatus.setText("Describing today...");
			Intent intent = new Intent(this, WriteActivity.class);
			startActivityForResult(intent, 0);
		} else if( (Button)btn==btnSync) {
			textStatus.setText("Syncing...");
			SharedPreferences prefs = getSharedPreferences("DIARY", MODE_PRIVATE);
			new SyncroTask(getServer(), prefs).execute(dbHelper);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO User real XML for menu
		menu.add("Set server...");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Switch for real menu items
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setText(getServer());
		
		new AlertDialog.Builder(this)
	    .setTitle("Select server")
	    .setView(input)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            Editable value = input.getText();
	            setServer(value.toString());
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
		
		return true;
	}

	private String getServer() {
		SharedPreferences prefs = getSharedPreferences("DIARY", MODE_PRIVATE);
		return prefs.getString(SERVER_ADDRESS, DEFAULT_SERVER_HOST);
	}
	
	private void setServer(String value) {
		Log.d(TAG, "Storing last server update time...");
		SharedPreferences prefs = getSharedPreferences("DIARY", MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SERVER_ADDRESS, value);
		editor.commit();
	}
}