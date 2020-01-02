package org.mhisoft.common.event;

import java.io.Serializable;

/**
 * Description:
 *
 * @author Tony Xue
 * @since May, 2016
 */
public class MHIEvent implements Serializable{
	private static final long serialVersionUID = 1L;

	EventType id;
	String origin;
	Serializable payload;

	public MHIEvent(EventType id, String origin, Serializable payload) {
		this.id = id;
		this.origin = origin;
		this.payload = payload;
	}

	public EventType getId() {
		return id;
	}

	public void setId(EventType id) {
		this.id = id;
	}

	public Serializable getPayload() {
		return payload;
	}

	public void setPayload(Serializable payload) {
		this.payload = payload;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("MHIEvent{");
		sb.append("id=").append(id);
		sb.append(", origin='").append(origin).append('\'');
		sb.append(", payload=").append(payload);
		sb.append('}');
		return sb.toString();
	}
}
