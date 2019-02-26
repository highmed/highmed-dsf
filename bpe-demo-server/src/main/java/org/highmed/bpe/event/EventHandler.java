package org.highmed.bpe.event;

import java.util.Objects;

import org.highmed.fhir.client.WebsocketClient;
import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class EventHandler implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);

	private final WebsocketClient fhirWebsocketClient;

	public EventHandler(WebsocketClient fhirWebsocketClient, FhirContext fhirContext, EventType eventType)
	{
		this.fhirWebsocketClient = fhirWebsocketClient;

		switch (eventType)
		{
			case XML:
				fhirWebsocketClient.setDomainResourceHandler(this::onResource, () -> fhirContext.newXmlParser());
				break;
			case JSON:
				fhirWebsocketClient.setDomainResourceHandler(this::onResource, () -> fhirContext.newJsonParser());
				break;
			case PING:
				fhirWebsocketClient.setPingHandler(this::onPing);
			default:
				break;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(fhirWebsocketClient, "fhirWebsocketClient");
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

	}

	private void onPing(String ping)
	{
		logger.trace("Ping for subscription {} received", ping);

	}
}
