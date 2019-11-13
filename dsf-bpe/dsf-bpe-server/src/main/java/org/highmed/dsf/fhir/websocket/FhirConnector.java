package org.highmed.dsf.fhir.websocket;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.highmed.dsf.fhir.client.FhirWebsocketClientProvider;
import org.highmed.dsf.fhir.task.TaskHandler;
import org.highmed.fhir.client.WebsocketClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.Constants;

public class FhirConnector implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(FhirConnector.class);

	private final FhirWebsocketClientProvider clientProvider;
	private final TaskHandler taskHandler;
	private final LastEventTimeIo lastEventTimeIo;
	private final FhirContext fhirContext;

	private final Map<String, List<String>> subscriptionSearchParameter;

	public FhirConnector(FhirWebsocketClientProvider clientProvider, TaskHandler taskHandler,
			LastEventTimeIo lastEventTimeIo, FhirContext fhirContext, String subscriptionSearchParameter)
	{
		this.clientProvider = clientProvider;
		this.taskHandler = taskHandler;
		this.lastEventTimeIo = lastEventTimeIo;
		this.fhirContext = fhirContext;
		this.subscriptionSearchParameter = parse(subscriptionSearchParameter, null);
	}

	private static Map<String, List<String>> parse(String queryParameters, String expectedPath)
	{
		if (expectedPath != null && !expectedPath.isBlank())
		{
			UriComponents components = UriComponentsBuilder.fromUriString(queryParameters).build();
			if (!expectedPath.equals(components.getPath()))
				throw new RuntimeException("Unexpected query parameters format '" + queryParameters + "'");
			else
				return components.getQueryParams();
		}
		else
		{
			UriComponents componentes = UriComponentsBuilder
					.fromUriString(queryParameters.startsWith("?") ? queryParameters : "?" + queryParameters).build();

			return componentes.getQueryParams();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(taskHandler, "taskHandler");
		Objects.requireNonNull(lastEventTimeIo, "lastEventTimeIo");
		Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
	{
		Subscription subscription = retrieveWebsocketSubscription();

		WebsocketClient client = clientProvider.getLocalWebsocketClient(subscription.getIdElement().getIdPart());

		EventType eventType = toEventType(subscription.getChannel().getPayload());
		if (EventType.PING.equals(eventType))
		{
			Map<String, List<String>> subscriptionCriteria = parse(subscription.getCriteria(), "Task");
			setPingEventHandler(client, subscription.getIdElement().getIdPart(), subscriptionCriteria);
		}
		else
			setResourceEventHandler(client, eventType);

		try
		{
			logger.info("Connecting websocket to loal FHIR server with subscription id {} ...",
					subscription.getIdElement().getIdPart());
			client.connect();
		}
		catch (Exception e)
		{
			logger.warn("Error while connecting websocket to local FHIR server", e);
			throw e;
		}
	}

	private Subscription retrieveWebsocketSubscription()
	{
		try
		{
			Bundle bundle = clientProvider.getLocalWebserviceClient().search(Subscription.class,
					subscriptionSearchParameter);

			if (!BundleType.SEARCHSET.equals(bundle.getType()))
				throw new RuntimeException("Could not retrieve searchset for subscription search query "
						+ subscriptionSearchParameter + ", but got " + bundle.getType());
			if (bundle.getTotal() != 1)
				throw new RuntimeException("Could not retrieve exactly one result for subscription search query "
						+ subscriptionSearchParameter);
			if (!(bundle.getEntryFirstRep().getResource() instanceof Subscription))
				throw new RuntimeException("Could not retrieve exactly one Subscription for subscription search query "
						+ subscriptionSearchParameter + ", but got "
						+ bundle.getEntryFirstRep().getResource().getResourceType());

			return (Subscription) bundle.getEntryFirstRep().getResource();
		}
		catch (Exception e)
		{
			logger.warn("Error while retrieving websocket subscription from local FHIR server", e);
			throw e;
		}
	}

	private EventType toEventType(String payload)
	{
		if (payload == null)
			return EventType.PING;

		switch (payload)
		{
			case Constants.CT_FHIR_JSON:
			case Constants.CT_FHIR_JSON_NEW:
				return EventType.JSON;
			case Constants.CT_FHIR_XML:
			case Constants.CT_FHIR_XML_NEW:
				return EventType.XML;
			default:
				throw new RuntimeException("Unsupportet subscription.payload " + payload);
		}
	}

	@EventListener({ ContextClosedEvent.class })
	public void onContextClosedEvent(ContextClosedEvent event)
	{
		clientProvider.disconnectAll();
	}

	private void setPingEventHandler(WebsocketClient client, String subscriptionIdPart,
			Map<String, List<String>> searchCriteriaQueryParameters)
	{
		PingEventHandler handler = new PingEventHandler(lastEventTimeIo, taskHandler,
				clientProvider.getLocalWebserviceClient());
		client.setPingHandler(ping -> handler.onPing(ping, subscriptionIdPart, searchCriteriaQueryParameters));
	}

	private void setResourceEventHandler(WebsocketClient client, EventType eventType)
	{
		ResourceEventHandler handler = new ResourceEventHandler(taskHandler);
		client.setDomainResourceHandler(handler::onResource, createParserFactory(eventType, fhirContext));
	}

	private Supplier<IParser> createParserFactory(EventType eventType, FhirContext fhirContext)
	{
		switch (eventType)
		{
			case XML:
				return () -> fhirContext.newXmlParser();
			case JSON:
				return () -> fhirContext.newJsonParser();
			default:
				throw new RuntimeException("EventType " + eventType + " not supported");
		}
	}
}
