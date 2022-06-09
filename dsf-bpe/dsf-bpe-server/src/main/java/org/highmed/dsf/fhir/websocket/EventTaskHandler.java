package org.highmed.dsf.fhir.websocket;

import org.highmed.dsf.fhir.task.TaskHandler;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class EventTaskHandler
{
	private static final Logger logger = LoggerFactory.getLogger(EventTaskHandler.class);

	private final TaskHandler taskHandler;
	private final LastEventTimeIo lastEventTimeIo;

	public EventTaskHandler(LastEventTimeIo lastEventTimeIo, TaskHandler taskHandler)
	{
		this.lastEventTimeIo = lastEventTimeIo;
		this.taskHandler = taskHandler;
	}

	public void onResource(DomainResource resource)
	{
		logger.trace("Resource of type {} received", resource.getClass().getAnnotation(ResourceDef.class).name());

		if (resource instanceof Task)
		{
			Task task = (Task) resource;
			taskHandler.onResource(task);
			lastEventTimeIo.writeLastEventTime(task.getAuthoredOn());
		}
		else
			logger.warn("Ignoring resource of type {}");
	}
}
