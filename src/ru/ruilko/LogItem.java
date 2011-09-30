package ru.ruilko;

import org.codehaus.jackson.JsonNode;

public class LogItem {
	public enum Status {
		UPDATED,
		DELETED
	};
	private String uuid;
	private int updated;
	private Status status;
	
	public LogItem(JsonNode itemNode) {
		setUuid(itemNode.get("uuid").getValueAsText());
		setUpdated(itemNode.get("updated").getValueAsInt(0));
		setStatus(itemNode.get("status").getValueAsInt(0)==Status.UPDATED.ordinal() ? Status.UPDATED : Status.DELETED);
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public int getUpdated() {
		return updated;
	}
	public void setUpdated(int updated) {
		this.updated = updated;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
}
