package org.highmed.fhir.event;

import javax.websocket.RemoteEndpoint.Async;

public interface EventManager
{
	void handleEvent(Event event);

	void bind(String sessionId, Async asyncRemote, String subscriptionIdPart);

	void close(String sessionId);
}
