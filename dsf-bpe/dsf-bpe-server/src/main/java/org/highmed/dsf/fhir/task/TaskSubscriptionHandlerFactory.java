package org.highmed.dsf.fhir.task;

import java.util.Objects;

import org.highmed.dsf.bpe.dao.LastEventTimeDao;
import org.highmed.dsf.fhir.subscription.EventResourceHandler;
import org.highmed.dsf.fhir.subscription.EventResourceHandlerImpl;
import org.highmed.dsf.fhir.subscription.ExistingResourceLoader;
import org.highmed.dsf.fhir.subscription.ExistingResourceLoaderImpl;
import org.highmed.dsf.fhir.subscription.PingEventResourceHandler;
import org.highmed.dsf.fhir.subscription.SubscriptionHandlerFactory;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class TaskSubscriptionHandlerFactory implements SubscriptionHandlerFactory<Task>, InitializingBean
{
	private final ResourceHandler<Task> resourceHandler;
	private final LastEventTimeDao lastEventTimeDao;

	public TaskSubscriptionHandlerFactory(ResourceHandler<Task> resourceHandler, LastEventTimeDao lastEventTimeDao)
	{
		this.resourceHandler = resourceHandler;
		this.lastEventTimeDao = lastEventTimeDao;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(resourceHandler, "resourceHandler");
		Objects.requireNonNull(lastEventTimeDao, "lastEventTimeDao");
	}

	@Override
	public ExistingResourceLoader<Task> createExistingResourceLoader(FhirWebserviceClient client)
	{
		return new ExistingResourceLoaderImpl<>(lastEventTimeDao, resourceHandler, client, "Task", Task.class);
	}

	@Override
	public EventResourceHandler<Task> createEventResourceHandler()
	{
		return new EventResourceHandlerImpl<>(lastEventTimeDao, resourceHandler, Task.class);
	}

	@Override
	public PingEventResourceHandler<Task> createPingEventResourceHandler(
			ExistingResourceLoader<Task> existingResourceLoader)
	{
		return new PingEventResourceHandler<>(existingResourceLoader);
	}
}
