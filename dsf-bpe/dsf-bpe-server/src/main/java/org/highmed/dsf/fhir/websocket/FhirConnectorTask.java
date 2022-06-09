package org.highmed.dsf.fhir.websocket;

import org.highmed.dsf.fhir.client.FhirWebsocketClientProvider;
import org.highmed.dsf.fhir.subscription.EventResourceHandler;
import org.highmed.dsf.fhir.subscription.ExistingResourceLoader;
import org.highmed.dsf.fhir.subscription.PingEventResourceHandler;
import org.highmed.dsf.fhir.task.EventTaskHandler;
import org.highmed.dsf.fhir.task.ExistingTaskLoader;
import org.highmed.dsf.fhir.task.TaskHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class FhirConnectorTask extends AbstractSubscriptionFhirConnector<Task>
{
	public FhirConnectorTask(FhirWebsocketClientProvider clientProvider, TaskHandler handler,
			LastEventTimeIo lastEventTimeIo, FhirContext fhirContext, String subscriptionSearchParameter,
			long retrySleepMillis, int maxRetries)
	{
		super("Task", clientProvider, handler, lastEventTimeIo, fhirContext, subscriptionSearchParameter,
				retrySleepMillis, maxRetries);
	}

	@Override
	public ExistingResourceLoader createExistingResourceLoader(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<Task> resourceHandler, FhirWebserviceClient client)
	{
		return new ExistingTaskLoader(lastEventTimeIo, resourceHandler, client);
	}

	@Override
	public EventResourceHandler createEventResourceHandler(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<Task> resourceHandler)
	{
		return new EventTaskHandler(lastEventTimeIo, resourceHandler);
	}

	@Override
	public PingEventResourceHandler createPingEventResourceHandler(ExistingResourceLoader existingResourceLoader)
	{
		return new PingEventResourceHandler(existingResourceLoader);
	}
}
