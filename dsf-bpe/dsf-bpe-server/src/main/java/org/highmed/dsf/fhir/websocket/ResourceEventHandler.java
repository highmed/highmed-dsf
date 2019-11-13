package org.highmed.dsf.fhir.websocket;

import java.util.Objects;

import org.highmed.dsf.fhir.task.TaskHandler;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class ResourceEventHandler implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ResourceEventHandler.class);

	private final TaskHandler taskHandler;

	public ResourceEventHandler(TaskHandler taskHandler)
	{
		this.taskHandler = taskHandler;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(taskHandler, "taskHandler");
	}

	public void onResource(DomainResource resource)
	{
		logger.trace("Resource of type {} received", resource.getClass().getAnnotation(ResourceDef.class).name());

		if (resource instanceof Task)
			taskHandler.onTask((Task) resource);
		else
			logger.warn("Ignoring resource of type {}");
	}
}
