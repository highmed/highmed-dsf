package org.highmed.bpe.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.highmed.fhir.client.WebserviceClient;
import org.highmed.fhir.client.WebsocketClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class PingEventHandler implements EventHandler, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(PingEventHandler.class);

	private static final String PARAM_LAST_UPDATE = "_lastUpdated";
	private static final String PARAM_COUNT = "_count";
	private static final String PARAM_PAGE = "page";
	private static final String PARAM_SORT = "_sort";
	private static final int RESULT_PAGE_COUNT = 20;

	private final WebsocketClient fhirWebsocketClient;
	private final LastEventTimeIo lastEventTimeIo;
	private final TaskHandler fhirTaskHandler;
	private final WebserviceClient fhirWebserviceClient;
	private final String subscriptionIdPart;
	private final String searchCriteria;
	private final String searchCriteriaPath;
	private final MultiValueMap<String, String> searchCriteriaQueryParameters;

	public PingEventHandler(WebsocketClient fhirWebsocketClient, LastEventTimeIo lastEventTimeIo,
			TaskHandler fhirTaskHandler, WebserviceClient fhirWebserviceClient, String subscriptionIdPart,
			String searchCriteria)
	{
		this.fhirWebsocketClient = fhirWebsocketClient;
		this.lastEventTimeIo = lastEventTimeIo;
		this.fhirTaskHandler = fhirTaskHandler;
		this.fhirWebserviceClient = fhirWebserviceClient;
		this.subscriptionIdPart = subscriptionIdPart;
		this.searchCriteria = searchCriteria;

		if (searchCriteria != null)
		{
			UriComponents componentes = UriComponentsBuilder.fromUriString(searchCriteria).build();
			searchCriteriaPath = componentes.getPath();
			searchCriteriaQueryParameters = componentes.getQueryParams();
		}
		else
		{
			searchCriteriaPath = null;
			searchCriteriaQueryParameters = null;
		}

		fhirWebsocketClient.setPingHandler(this::onPing);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(fhirWebsocketClient, "fhirWebsocketClient");
		Objects.requireNonNull(lastEventTimeIo, "lastEventTimeIo");
		Objects.requireNonNull(fhirTaskHandler, "fhirTaskHandler");
		Objects.requireNonNull(fhirWebserviceClient, "fhirWebserviceClient");
		Objects.requireNonNull(searchCriteria, "searchCriteria");

		if (!("Task".equals(searchCriteriaPath) || searchCriteriaPath.isEmpty()))
			logger.warn("Search criteria path for ping event handler not equal to 'Task'. '{}' will be ignored",
					searchCriteriaPath);
		if (searchCriteriaQueryParameters.containsKey(PARAM_LAST_UPDATE))
			logger.warn(
					"Search criteria query parameters for ping event handler contains parameter {}, parameter will be overridden",
					PARAM_LAST_UPDATE);
		if (searchCriteriaQueryParameters.containsKey(PARAM_COUNT))
			logger.warn(
					"Search criteria query parameters for ping event handler contains parameter {}, parameter will be overridden",
					PARAM_COUNT);
		if (searchCriteriaQueryParameters.containsKey(PARAM_PAGE))
			logger.warn(
					"Search criteria query parameters for ping event handler contains parameter {}, parameter will be overridden",
					PARAM_PAGE);
		if (searchCriteriaQueryParameters.containsKey(PARAM_SORT))
			logger.warn(
					"Search criteria query parameters for ping event handler contains parameter {}, parameter will be overridden",
					PARAM_SORT);

		logger.info("Ping event handler configured with subscription-id {} and search-criteria {}", subscriptionIdPart,
				searchCriteria);
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

	private void onPing(String ping)
	{
		logger.trace("Ping for subscription {} received", ping);
		if (!subscriptionIdPart.equals(ping))
		{
			logger.warn("Received ping for subscription {}, but expected subscription {}, ignoring ping", ping,
					subscriptionIdPart);
			return;
		}

		Optional<LocalDateTime> readLastEventTime = lastEventTimeIo.readLastEventTime();

		Map<String, List<String>> queryParams = new HashMap<>(searchCriteriaQueryParameters);
		if (readLastEventTime.isPresent())
		{
			queryParams.put(PARAM_LAST_UPDATE,
					Arrays.asList("gt" + readLastEventTime.get().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
			queryParams.put(PARAM_COUNT, Arrays.asList(String.valueOf(RESULT_PAGE_COUNT)));
		}
		else
		{
			queryParams.put(PARAM_COUNT, Arrays.asList(String.valueOf(Integer.MAX_VALUE)));
		}
		queryParams.put(PARAM_PAGE, Arrays.asList("1"));
		queryParams.put(PARAM_SORT, Arrays.asList(PARAM_LAST_UPDATE));

		Bundle bundle = fhirWebserviceClient.search(Task.class, queryParams);
		lastEventTimeIo.writeLastEventTime(LocalDateTime.now());

		if (bundle.getTotal() <= 0)
		{
			logger.warn("Result bundle.total <= 0, ignoring ping");
			return;
		}

		for (BundleEntryComponent entry : bundle.getEntry())
		{
			if (entry.getResource() instanceof Task)
				fhirTaskHandler.onTask((Task) entry.getResource());
			else
				logger.warn("Ignoring resource of type {}");
		}

		if (bundle.getTotal() > RESULT_PAGE_COUNT)
		{
			logger.warn("Result bundle.total > {}, calling onPing again", RESULT_PAGE_COUNT);
			onPing(ping);
		}
	}
}
