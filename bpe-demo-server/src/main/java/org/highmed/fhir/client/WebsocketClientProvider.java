package org.highmed.fhir.client;

public interface WebsocketClientProvider extends WebserviceClientProvider
{
	WebsocketClient getLocalWebsocketClient(String subscriptionId);

	void disconnectAll();
}
