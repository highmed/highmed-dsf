package org.highmed.dsf.fhir.subscription;

import javax.websocket.Session;

import org.highmed.dsf.fhir.authentication.User;

public interface WebSocketSubscriptionManager
{
	void bind(User user, Session session, String subscriptionIdPart);

	void close(String sessionId);
}
