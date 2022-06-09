package org.highmed.dsf.fhir.subscription;

import org.highmed.dsf.fhir.websocket.EventResourceHandler;
import org.highmed.dsf.fhir.websocket.ExistingResourceLoader;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.PingEventResourceHandler;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.DomainResource;

public interface SubscriptionHandler<R extends DomainResource>
{
	ExistingResourceLoader createExistingResourceLoader(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<R> resourceHandler, FhirWebserviceClient client);

	EventResourceHandler createEventResourceHandler(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<R> resourceHandler);

	PingEventResourceHandler createPingEventResourceHandler(ExistingResourceLoader existingResourceLoader);
}
