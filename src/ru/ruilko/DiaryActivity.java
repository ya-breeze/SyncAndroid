package ru.ruilko;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DiaryActivity extends Activity implements OnClickListener {
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
			new SyncroTask().execute(dbHelper);
		}
	}
}