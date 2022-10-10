package org.highmed.dsf.fhir.subscription;

import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Resource;

public interface SubscriptionHandlerFactory<R extends Resource>
{
	ExistingResourceLoader<R> createExistingResourceLoader(FhirWebserviceClient client);

	EventResourceHandler<R> createEventResourceHandler();

	PingEventResourceHandler<R> createPingEventResourceHandler(ExistingResourceLoader<R> existingResourceLoader);
}
