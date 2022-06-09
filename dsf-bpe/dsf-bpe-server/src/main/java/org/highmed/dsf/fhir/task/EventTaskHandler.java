package org.highmed.dsf.fhir.task;

import org.highmed.dsf.fhir.websocket.EventResourceHandler;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class EventTaskHandler implements EventResourceHandler
{
	private static final Logger logger = LoggerFactory.getLogger(EventTaskHandler.class);

	private final ResourceHandler<Task> handler;
	private final LastEventTimeIo lastEventTimeIo;

	public EventTaskHandler(LastEventTimeIo lastEventTimeIo, ResourceHandler<Task> handler)
	{
		this.lastEventTimeIo = lastEventTimeIo;
		this.handler = handler;
	}

	public void onResource(DomainResource resource)
	{
		logger.trace("Resource of type {} received", resource.getClass().getAnnotation(ResourceDef.class).name());

		if (resource instanceof Task)
		{
			Task task = (Task) resource;
			handler.onResource(task);
			lastEventTimeIo.writeLastEventTime(task.getAuthoredOn());
		}
		else
			logger.warn("Ignoring resource of type {}", resource.getClass().getAnnotation(ResourceDef.class).name());
	}
}
