package org.highmed.dsf.fhir.websocket;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.highmed.dsf.fhir.client.FhirWebsocketClientProvider;
import org.highmed.dsf.fhir.subscription.EventResourceHandler;
import org.highmed.dsf.fhir.subscription.ExistingResourceLoader;
import org.highmed.dsf.fhir.subscription.PingEventResourceHandler;
import org.highmed.dsf.fhir.subscription.SubscriptionHandlerFactory;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.WebsocketClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.Constants;

public class FhirConnectorImpl<R extends Resource> implements FhirConnector, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(FhirConnectorImpl.class);

	private final String resourcePath;
	private final FhirWebsocketClientProvider clientProvider;
	private final FhirContext fhirContext;
	private final SubscriptionHandlerFactory<R> subscriptionHandlerFactory;
	private final long retrySleepMillis;
	private final int maxRetries;
	private final Map<String, List<String>> subscriptionSearchParameter;

	public FhirConnectorImpl(String resourcePath, FhirWebsocketClientProvider clientProvider,
			SubscriptionHandlerFactory<R> subscriptionHandlerFactory, FhirContext fhirContext,
			String subscriptionSearchParameter, long retrySleepMillis, int maxRetries)
	{
		this.resourcePath = resourcePath;
		this.clientProvider = clientProvider;
		this.subscriptionHandlerFactory = subscriptionHandlerFactory;
		this.fhirContext = fhirContext;
		this.subscriptionSearchParameter = parse(subscriptionSearchParameter, null);
		this.retrySleepMillis = retrySleepMillis;
		this.maxRetries = maxRetries;
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
		Objects.requireNonNull(fhirContext, "fhirContext");
	}

	@Override
	public void connect()
	{
		logger.debug("Retrieving Subscription and connecting to websocket");

		CompletableFuture.supplyAsync(this::retrieveWebsocketSubscription, Executors.newSingleThreadExecutor())
				.thenApply(this::loadExistingResources).thenAccept(this::connectWebsocket).exceptionally(this::onError);
	}

	private Subscription retrieveWebsocketSubscription()
	{
		if (maxRetries >= 0)
			return retry(this::doRetrieveWebsocketSubscription);
		else
			return retryForever(this::doRetrieveWebsocketSubscription);
	}

	private Subscription retry(Supplier<Subscription> supplier)
	{
		RuntimeException lastException = null;
		for (int retryCounter = 0; retryCounter <= maxRetries; retryCounter++)
		{
			try
			{
				return supplier.get();
			}
			catch (RuntimeException e)
			{
				if (retryCounter < maxRetries)
				{
					logger.warn(
							"Error while retrieving websocket subscription ({}), trying again in {} ms (retry {} of {})",
							e.getMessage(), retrySleepMillis, retryCounter + 1, maxRetries);
					try
					{
						Thread.sleep(retrySleepMillis);
					}
					catch (InterruptedException e1)
					{
					}
				}

				lastException = e;
			}
		}

		logger.error("Error while retrieving websocket subscription ({}), giving up", lastException.getMessage());
		throw lastException;
	}

	private Subscription retryForever(Supplier<Subscription> supplier)
	{
		for (int retryCounter = 1; true; retryCounter++)
		{
			try
			{
				return supplier.get();
			}
			catch (RuntimeException e)
			{
				logger.warn("Error while retrieving websocket subscription ({}), trying again in {} ms (retry {})",
						e.getMessage(), retrySleepMillis, retryCounter);
				try
				{
					Thread.sleep(retrySleepMillis);
				}
				catch (InterruptedException e1)
				{
				}
			}
		}
	}

	private Subscription doRetrieveWebsocketSubscription()
	{
		logger.debug("Retrieving websocket subscription");

		Bundle bundle = clientProvider.getLocalWebserviceClient().searchWithStrictHandling(Subscription.class,
				subscriptionSearchParameter);

		if (!Bundle.BundleType.SEARCHSET.equals(bundle.getType()))
			throw new RuntimeException("Could not retrieve searchset for subscription search query "
					+ subscriptionSearchParameter + ", but got " + bundle.getType());
		if (bundle.getTotal() != 1)
			throw new RuntimeException("Could not retrieve exactly one result for subscription search query "
					+ subscriptionSearchParameter);
		if (!(bundle.getEntryFirstRep().getResource() instanceof Subscription))
			throw new RuntimeException("Could not retrieve exactly one Subscription for subscription search query "
					+ subscriptionSearchParameter + ", but got "
					+ bundle.getEntryFirstRep().getResource().getResourceType());

		Subscription subscription = (Subscription) bundle.getEntryFirstRep().getResource();
		logger.debug("Subscription with id {} found", subscription.getIdElement().getIdPart());

		return subscription;
	}

	private Subscription loadExistingResources(Subscription subscription)
	{
		logger.debug("Downloading existing resources");

		FhirWebserviceClient client = clientProvider.getLocalWebserviceClient();
		ExistingResourceLoader<R> existingResourceLoader = subscriptionHandlerFactory
				.createExistingResourceLoader(client);
		Map<String, List<String>> subscriptionCriteria = parse(subscription.getCriteria(), resourcePath);
		existingResourceLoader.readExistingResources(subscriptionCriteria);

		return subscription;
	}

	private void connectWebsocket(Subscription subscription)
	{
		logger.debug("Connecting to websocket");

		WebsocketClient client = clientProvider.getLocalWebsocketClient(() -> connect(),
				subscription.getIdElement().getIdPart());

		EventType eventType = toEventType(subscription.getChannel().getPayload());
		if (EventType.PING.equals(eventType))
		{
			Map<String, List<String>> subscriptionCriteria = parse(subscription.getCriteria(), resourcePath);
			setPingEventHandler(client, subscription.getIdElement().getIdPart(), subscriptionCriteria);
		}
		else
			setResourceEventHandler(client, eventType);

		try
		{
			logger.info("Connecting websocket to local FHIR server with subscription id {}",
					subscription.getIdElement().getIdPart());
			client.connect();
		}
		catch (Exception e)
		{
			logger.warn("Error while connecting websocket to local FHIR server", e);
			throw e;
		}
	}

	private Void onError(Throwable t)
	{
		logger.error("Error while connecting to websocket", t);
		return null;
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
		FhirWebserviceClient webserviceClient = clientProvider.getLocalWebserviceClient();
		ExistingResourceLoader<R> existingResourceLoader = subscriptionHandlerFactory
				.createExistingResourceLoader(webserviceClient);
		PingEventResourceHandler<R> pingHandler = subscriptionHandlerFactory
				.createPingEventResourceHandler(existingResourceLoader);
		client.setPingHandler(ping -> pingHandler.onPing(ping, subscriptionIdPart, searchCriteriaQueryParameters));
	}

	private void setResourceEventHandler(WebsocketClient client, EventType eventType)
	{
		EventResourceHandler<R> eventHandler = subscriptionHandlerFactory.createEventResourceHandler();
		client.setDomainResourceHandler(eventHandler::onResource, createParserFactory(eventType, fhirContext));
	}

	private Supplier<IParser> createParserFactory(EventType eventType, FhirContext fhirContext)
	{
		switch (eventType)
		{
			case XML:
				return () -> configureParser(fhirContext.newXmlParser());
			case JSON:
				return () -> configureParser(fhirContext.newJsonParser());
			default:
				throw new RuntimeException("EventType " + eventType + " not supported");
		}
	}

	private IParser configureParser(IParser p)
	{
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);
		return p;
	}
}
