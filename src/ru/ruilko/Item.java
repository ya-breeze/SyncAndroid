package ru.ruilko;

import java.util.UUID;

import org.codehaus.jackson.JsonNode;

public class Item {
	private String uuid;
	private String title;
	private String notes;
	public Item(JsonNode itemNode) {
		setUuid(itemNode.get("uuid").getValueAsText());
		setTitle(itemNode.get("title").getValueAsText());
		setNotes(itemNode.get("notes").getValueAsText());
	}
	public Item(String uuid, String title, String notes) {
		setUuid(uuid);
		setTitle(title);
		setNotes(notes);
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid.toString();
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return "{" + title + ":" + uuid + "}";
	}
}
