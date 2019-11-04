package org.highmed.dsf.fhir.client;

import org.highmed.fhir.client.WebsocketClient;

public interface FhirWebsocketClientProvider extends FhirWebserviceClientProvider
{
	WebsocketClient getLocalWebsocketClient(String subscriptionId);

	void disconnectAll();
}
