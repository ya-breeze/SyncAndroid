package ru.ruilko;

import java.util.UUID;

public class Item {
	private String uuid;
	private String title;
	private String notes;
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
