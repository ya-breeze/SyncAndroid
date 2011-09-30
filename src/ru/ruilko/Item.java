package ru.ruilko;

import java.util.UUID;

import org.codehaus.jackson.JsonNode;

public class Item {
	private String uuid;
	private String title;
	private String notes;
	
	public Item(JsonNode itemNode) throws Exception {
		setUuid(itemNode.path("uuid").getValueAsText());
		setTitle(itemNode.path("title").getValueAsText());
		setNotes(itemNode.path("notes").getValueAsText());
	}
	public Item(String uuid, String title, String notes) throws Exception {
		setUuid(uuid);
		setTitle(title);
		setNotes(notes);
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) throws Exception {
		if( uuid==null )
			throw new Exception("Item UUID should not be null");
		this.uuid = uuid.toString();
	}
	public void setUuid(String uuid) throws Exception {
		if( uuid==null )
			throw new Exception("Item UUID should not be null");
		this.uuid = uuid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		if( title!=null )
			this.title = title;
		else
			this.title = "";
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		if( notes!=null )
			this.notes = notes;
		else
			this.notes = "";
	}

	@Override
	public String toString() {
		return "{" + title + ":" + uuid + "}";
	}
}
