package ru.ruilko;

import org.codehaus.jackson.JsonNode;

public class LogItem {
	public enum Status {
		UPDATED,
		DELETED
	};
	public static int CURRENT_TIMESTAMP = 0;
	private String uuid;
	private int updated;
	private Status status;
	
	public LogItem(JsonNode itemNode) throws Exception {
		setUuid(itemNode.path("uuid").getValueAsText());
		setStatus(itemNode.path("status").getValueAsText().toLowerCase()
				.equals("updated") ? Status.UPDATED : Status.DELETED);
		setUpdated(itemNode.path("updated").getValueAsInt(CURRENT_TIMESTAMP));
	}
	public LogItem(String uuid, Status status, int updated) throws Exception {
		setUuid(uuid);
		setStatus(status);
		setUpdated(updated);
	}
	public LogItem(Item item, Status status) throws Exception {
		setUuid(item.getUuid());
		setStatus(status);
		setUpdated(CURRENT_TIMESTAMP);
	}
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) throws Exception {
		if( uuid==null )
			throw new Exception("LogItem UUID should not be null");
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
