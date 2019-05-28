package org.highmed.fhir.client;

import org.highmed.dsf.fhir.client.WebserviceClientProvider;

public interface WebsocketClientProvider extends WebserviceClientProvider
{
	WebsocketClient getLocalWebsocketClient(String subscriptionId);

	void disconnectAll();
}
