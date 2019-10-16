package org.highmed.dsf.fhir.event;

import java.util.List;

import javax.websocket.RemoteEndpoint.Async;

public interface EventManager
{
	void handleEvent(Event event);

	void handleEvents(List<Event> events);

	void bind(String sessionId, Async asyncRemote, String subscriptionIdPart);

	void close(String sessionId);
}
