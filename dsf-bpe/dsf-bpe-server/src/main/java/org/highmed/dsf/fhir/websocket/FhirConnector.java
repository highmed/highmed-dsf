package org.highmed.dsf.fhir.websocket;

import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.DomainResource;

public interface FhirConnector<R extends DomainResource>
{
	void connect();

	ExistingResourceLoader createExistingResourceLoader(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<R> resourceHandler, FhirWebserviceClient client);

	PingEventResourceHandler createPingEventResourceHandler(ExistingResourceLoader existingResourceLoader);

	EventResourceHandler createEventResourceHandler(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<R> resourceHandler);
}