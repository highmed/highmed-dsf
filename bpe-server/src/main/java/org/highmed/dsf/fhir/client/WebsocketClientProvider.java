package org.highmed.dsf.fhir.client;

import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.client.WebsocketClient;

public interface WebsocketClientProvider extends WebserviceClientProvider
{
	WebsocketClient getLocalWebsocketClient(String subscriptionId);

	void disconnectAll();
}
