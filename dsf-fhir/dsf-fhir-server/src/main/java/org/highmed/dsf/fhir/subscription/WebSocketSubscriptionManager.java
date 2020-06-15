package org.highmed.dsf.fhir.subscription;

import javax.websocket.RemoteEndpoint.Async;

import org.highmed.dsf.fhir.authentication.User;

public interface WebSocketSubscriptionManager
{
	void bind(User user, String sessionId, Async asyncRemote, String subscriptionIdPart);

	void close(String sessionId);
}
