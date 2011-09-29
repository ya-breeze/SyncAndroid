package ru.ruilko;

public class LogItem {
	public enum Status {
		UPDATED,
		DELETED
	};
	private String uuid;
	private int updated;
	private Status status;
	
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
