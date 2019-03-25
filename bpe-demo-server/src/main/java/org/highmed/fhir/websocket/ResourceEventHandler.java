package org.highmed.fhir.websocket;

import java.util.Objects;

import org.highmed.fhir.client.WebsocketClient;
import org.highmed.fhir.task.TaskHandler;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class ResourceEventHandler implements EventHandler, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ResourceEventHandler.class);

	private final WebsocketClient fhirWebsocketClient;
	private final TaskHandler fhirTaskHandler;
	private final EventType eventType;

	public ResourceEventHandler(WebsocketClient fhirWebsocketClient, TaskHandler fhirTaskHandler, EventType eventType,
			FhirContext fhirContext)
	{
		this.fhirWebsocketClient = fhirWebsocketClient;
		this.fhirTaskHandler = fhirTaskHandler;
		this.eventType = eventType;

		Objects.requireNonNull(eventType, "eventType");
		Objects.requireNonNull(fhirContext, "fhirContext");

		switch (eventType)
		{
			case XML:
				fhirWebsocketClient.setDomainResourceHandler(this::onResource, () -> fhirContext.newXmlParser());
				break;
			case JSON:
				fhirWebsocketClient.setDomainResourceHandler(this::onResource, () -> fhirContext.newJsonParser());
				break;
			default:
				break;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(fhirWebsocketClient, "fhirWebsocketClient");
		Objects.requireNonNull(fhirTaskHandler, "fhirTaskHandler");

		logger.info("Resource event handler configured with event-type {}", eventType);
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
	{
		fhirWebsocketClient.connect();
	}

	@EventListener({ ContextClosedEvent.class })
	public void onContextClosedEvent(ContextClosedEvent event)
	{
		fhirWebsocketClient.disconnect();
	}

	private void onResource(DomainResource resource)
	{
		logger.trace("Resource of type {} received", resource.getClass().getAnnotation(ResourceDef.class).name());

		if (resource instanceof Task)
			fhirTaskHandler.onTask((Task) resource);
		else
			logger.warn("Ignoring resource of type {}");
	}
}
