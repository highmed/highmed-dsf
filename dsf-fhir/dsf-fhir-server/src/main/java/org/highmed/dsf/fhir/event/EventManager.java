package org.highmed.dsf.fhir.event;

import java.util.List;

import javax.websocket.RemoteEndpoint.Async;

import org.highmed.dsf.fhir.authentication.User;

public interface EventManager
{
	void handleEvent(Event event);

	void handleEvents(List<Event> events);

	void bind(User user, String sessionId, Async asyncRemote, String subscriptionIdPart);

	void close(String sessionId);
}
