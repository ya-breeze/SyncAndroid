package ru.ruilko;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import ru.ruilko.util.Utils;

import android.os.AsyncTask;
import android.util.Log;

public class SyncroTask extends AsyncTask<DbHelper, Integer, Void> {
	private static final String TAG = "SyncroTask";
	private static final String HOST = "http://10.0.2.2/sync";
	private static final String UPLOAD = HOST + "/upload.php";
	private static final String FETCH = HOST + "/fetch.php";
	private DbHelper dbHelper = null;

	@Override
	protected Void doInBackground(DbHelper... dbHelpers) {
		dbHelper  = dbHelpers[0];
		dbHelper.begin();
		try{
			// TODO Get last update time from saved place
			// It's "updated" time of the most recent item from last syncronization
			int lastUpdate = 0;
			// TODO Get list of locally modified items
			List<String> localItems = dbHelper.getLocalUpdates(lastUpdate);
			// TODO Download from server
			fetchUpdates(lastUpdate);
			// TODO Upload to server only items from list
			uploadUpdates(localItems);
			// TODO Save current last updates
			storeLastUpdate();
			// TODO if last update time is stored NOT in db, this commit
			// should be _before_ saving last update time
			dbHelper.commit();
		}catch (Exception e) {
			Log.e(TAG, "Error while syncronizing: " + Utils.getDescription(e));
		} finally {
			dbHelper.end();
		}
		
		return null;
	}

	private void storeLastUpdate() {
		Log.d(TAG, "Storing last update time...");
	}

	private void uploadUpdates(List<String> localItems) {
		Log.d(TAG, "Uploading updates to server...");
	}

	private void fetchUpdates(int lastUpdate) throws Exception {
		Log.d(TAG, "Fetching updates from server...");
		JsonNode items = fetchJsonUpdates(lastUpdate);
		for (JsonNode itemNode : items) {
			Log.d(TAG, "Handle item: " + itemNode.toString());
			LogItem logItem = new LogItem(itemNode);
			if( dbHelper.shouldUpdate(logItem) ) {
				Log.d(TAG, "Will update item: " + logItem.getUuid() + ":" + logItem.getStatus());
				dbHelper.updateItem(new Item(itemNode), logItem);				
			} else {
				Log.d(TAG, "Will NOT update item, it's too old: " + logItem.getUuid() + ":" + logItem.getStatus());
			}
		}
	}

	private JsonNode fetchJsonUpdates(int lastUpdate) throws IOException, ServerException {
		URL url = new URL(FETCH + "?lastUpdate=" + lastUpdate);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		JsonNode items = null;
		try {
			if( urlConnection.getResponseCode()/100!=2 )
				throw new ServerException("Error while reading server response: " + urlConnection.getResponseMessage());

			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			ObjectMapper mapper = new ObjectMapper();
			JsonParser jp = mapper.getJsonFactory().createJsonParser(in);
			JsonNode rootNode = mapper.readTree(jp);
			items  = rootNode.get("items");
			if( items==null )
				throw new ServerException("There is no items in server response");
		} finally {
			urlConnection.disconnect();
		}
		return items;
	}
}
