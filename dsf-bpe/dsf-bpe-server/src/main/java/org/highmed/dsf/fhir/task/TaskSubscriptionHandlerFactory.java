package org.highmed.dsf.fhir.task;

import java.util.Objects;

import org.highmed.dsf.fhir.subscription.EventResourceHandler;
import org.highmed.dsf.fhir.subscription.ExistingResourceLoader;
import org.highmed.dsf.fhir.subscription.PingEventResourceHandler;
import org.highmed.dsf.fhir.subscription.SubscriptionHandlerFactory;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class TaskSubscriptionHandlerFactory implements SubscriptionHandlerFactory<Task>, InitializingBean
{
	private final ResourceHandler<Task> resourceHandler;
	private final LastEventTimeIo lastEventTimeIo;

	public TaskSubscriptionHandlerFactory(ResourceHandler<Task> resourceHandler, LastEventTimeIo lastEventTimeIo)
	{
		this.resourceHandler = resourceHandler;
		this.lastEventTimeIo = lastEventTimeIo;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(resourceHandler, "resourceHandler");
		Objects.requireNonNull(lastEventTimeIo, "lastEventTimeIo");
	}

	@Override
	public ExistingResourceLoader<Task> createExistingResourceLoader(FhirWebserviceClient client)
	{
		return new ExistingTaskLoader(lastEventTimeIo, resourceHandler, client);
	}

	@Override
	public EventResourceHandler<Task> createEventResourceHandler()
	{
		return new EventTaskHandler(lastEventTimeIo, resourceHandler);
	}

	@Override
	public PingEventResourceHandler createPingEventResourceHandler(ExistingResourceLoader<Task> existingResourceLoader)
	{
		return new PingEventResourceHandler(existingResourceLoader);
	}
}
