package ru.ruilko;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import ru.ruilko.util.Utils;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class SyncroTask extends AsyncTask<DbHelper, Integer, Void> {
	private static final String TAG = "SyncroTask";

	private static final String HOST = "http://10.0.2.2/sync";

	private static final String UPLOAD = HOST + "/upload.php";

	private static final String FETCH = HOST + "/fetch.php";

	private static final String LAST_SERVER_UPDATE = "last_server_update";

	private static final String LAST_LOCAL_UPDATE = "last_local_update";

	private DbHelper dbHelper = null;

	private SharedPreferences prefs;
	
	public SyncroTask(SharedPreferences prefs) {
		super();
		this.prefs = prefs;
	}

	@Override
	protected Void doInBackground(DbHelper... dbHelpers) {
		dbHelper = dbHelpers[0];
		dbHelper.begin();
		try {
			// Get last update time from saved place
			int lastServerUpdate = getLastServerUpdate();
			int lastLocalUpdate  = getLastLocalUpdate();
			// Get list of locally modified items
			List<LogItem> localItems = dbHelper.getLocalUpdates(lastLocalUpdate);
			// Download from server
			lastServerUpdate = fetchUpdates(lastServerUpdate);
			// Upload to server only items from list
			lastLocalUpdate = uploadUpdates(lastLocalUpdate, localItems);
			dbHelper.commit();

			// Save current last updates
			storeLastServerUpdate(lastServerUpdate);
			storeLastLocalUpdate(lastLocalUpdate);
		} catch (Exception e) {
			Log.e(TAG, "Error while syncronizing: " + Utils.getDescription(e));
		} finally {
			dbHelper.end();
		}

		return null;
	}

	private int getLastServerUpdate() {
		return prefs.getInt(LAST_SERVER_UPDATE, 0);
	}
	private int getLastLocalUpdate() {
		return prefs.getInt(LAST_LOCAL_UPDATE, 0);
	}

	private void storeLastServerUpdate(int lastUpdate) {
		Log.d(TAG, "Storing last server update time...");
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(LAST_SERVER_UPDATE, lastUpdate);
		editor.commit();
	}
	private void storeLastLocalUpdate(int lastUpdate) {
		Log.d(TAG, "Storing last local update time...");
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(LAST_LOCAL_UPDATE, lastUpdate);
		editor.commit();
	}

	private int uploadUpdates(int lastUpdate, List<LogItem> localItems) throws Exception {
		Log.d(TAG, "Uploading updates to server...");
		JsonNode items = prepareJsonUpdates(localItems);
		uploadJsonUpdates(items);

		// Find latest one - not optimal, just my laziness )
		int maxLocalUpdate = lastUpdate;
		for (LogItem logItem : localItems) {
			if( logItem.getLocalUpdated()>maxLocalUpdate )
				maxLocalUpdate = logItem.getLocalUpdated();
		}
		
		return maxLocalUpdate;
	}

	private void uploadJsonUpdates(JsonNode items) throws Exception {
		URL url = new URL(UPLOAD);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		try {
			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);

			OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
			ObjectMapper mapper = new ObjectMapper();
			JsonGenerator writer = mapper.getJsonFactory().createJsonGenerator(out);
			writer.writeTree(items);

			if (urlConnection.getResponseCode() / 100 != 2) throw new ServerException(
					"Error while uploading to server: " + urlConnection.getResponseMessage());
		} finally {
			urlConnection.disconnect();
		}
	}

	private JsonNode prepareJsonUpdates(List<LogItem> localItems) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ArrayNode itemsNode = mapper.createArrayNode();
		rootNode.put("items", itemsNode);
		for (LogItem logItem : localItems) {
			Item item = dbHelper.readItem(logItem.getUuid());

			ObjectNode itemNode = mapper.createObjectNode();
			itemsNode.add(itemNode);
			itemNode.put("uuid", logItem.getUuid());
			itemNode.put("status", logItem.getStatus().toString());
			itemNode.put("updated", logItem.getUpdated());
			// Will be null for deleted items
			if (item != null) {
				itemNode.put("title", item.getTitle());
				itemNode.put("notes", item.getNotes());
			}
		}
		//		Log.d(TAG, "!!! Generate:" + rootNode.toString());

		return rootNode;
	}

	private int fetchUpdates(int lastUpdate) throws Exception {
		Log.d(TAG, "Fetching updates from server...");
		int maxLastUpdate = lastUpdate;
		JsonNode items = fetchJsonUpdates(lastUpdate);
		for (JsonNode itemNode : items) {
			Log.d(TAG, "Handle item: " + itemNode.toString());
			LogItem logItem = new LogItem(itemNode);
			if (dbHelper.shouldUpdate(logItem)) {
				Log.d(TAG, "Will update item: " + logItem.getUuid() + ":" + logItem.getStatus());
				dbHelper.updateItem(new Item(itemNode), logItem);
			} else {
				Log.d(TAG, "Will NOT update item, it's too old: " + logItem.getUuid() + ":"
						+ logItem.getStatus());
			}
			if( logItem.getUpdated()>maxLastUpdate )
				maxLastUpdate = logItem.getUpdated();
		}
		
		return maxLastUpdate;
	}

	private JsonNode fetchJsonUpdates(int lastUpdate) throws IOException, ServerException {
		URL url = new URL(FETCH + "?lastUpdate=" + lastUpdate);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		JsonNode items = null;
		try {
			if (urlConnection.getResponseCode() / 100 != 2) throw new ServerException(
					"Error while reading server response: " + urlConnection.getResponseMessage());

			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			ObjectMapper mapper = new ObjectMapper();
			JsonParser jp = mapper.getJsonFactory().createJsonParser(in);
			JsonNode rootNode = mapper.readTree(jp);
			items = rootNode.get("items");
			if (items == null) throw new ServerException("There is no items in server response");
		} finally {
			urlConnection.disconnect();
		}
		return items;
	}
}
