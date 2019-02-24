package org.highmed.fhir.event;

import javax.websocket.RemoteEndpoint.Async;

public interface EventManager
{
	void handleEvent(Event<?> event);

	void bind(String subscriptionIdPart, Async asyncRemote);

	void close(Async asyncRemote);
}
