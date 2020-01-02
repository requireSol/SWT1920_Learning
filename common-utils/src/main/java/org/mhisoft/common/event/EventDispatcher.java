package org.mhisoft.common.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: EventDispatcher
 * Use it as a singleton.
 *
 * @author Tony Xue
 * @since May, 2016
 */
public class EventDispatcher {

	protected Map<EventType, List<EventListener>> listenerMap = new HashMap<>();

	public static final EventDispatcher instance = new EventDispatcher();

	private EventDispatcher() {
		//none
	}


	public void registerListener(EventType eventId, EventListener listener) {
		List<EventListener> listeners = listenerMap.get(eventId);
		if (listeners == null) {
			listeners = new ArrayList<EventListener>();
			listenerMap.put(eventId, listeners);

		}
		listeners.add(listener);

	}

	public void removeListener(EventType eventId, EventListener listener) {
		List<EventListener> listeners = listenerMap.get(eventId);
		if (listeners != null)
			listeners.remove(listener);

	}

	/**
	 * Dispatch the event to all the registered listeners.
	 *
	 * @param event
	 */
	public void dispatchEvent(MHIEvent event) {
		List<EventListener> listeners = listenerMap.get(event.getId());
		if (listeners != null) {
			for (EventListener listener : listeners) {
				listener.handleEvent(event);
			}
		}

	}


}
