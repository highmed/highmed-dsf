package org.highmed.dsf.fhir.websocket;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.highmed.dsf.fhir.task.TaskHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class PingEventHandler implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(PingEventHandler.class);

	private static final String PARAM_LAST_UPDATE = "_lastUpdated";
	private static final String PARAM_COUNT = "_count";
	private static final String PARAM_PAGE = "_page";
	private static final String PARAM_SORT = "_sort";
	private static final int RESULT_PAGE_COUNT = 20;

	private final LastEventTimeIo lastEventTimeIo;
	private final TaskHandler taskHandler;
	private final FhirWebserviceClient webserviceClient;

	public PingEventHandler(LastEventTimeIo lastEventTimeIo, TaskHandler taskHandler, FhirWebserviceClient webserviceClient)
	{
		this.lastEventTimeIo = lastEventTimeIo;
		this.taskHandler = taskHandler;
		this.webserviceClient = webserviceClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(lastEventTimeIo, "lastEventTimeIo");
		Objects.requireNonNull(taskHandler, "taskHandler");
		Objects.requireNonNull(webserviceClient, "webserviceClient");
	}

	public void onPing(String ping, String subscriptionIdPart, Map<String, List<String>> searchCriteriaQueryParameters)
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

		Bundle bundle = webserviceClient.search(Task.class, queryParams);
		lastEventTimeIo.writeLastEventTime(LocalDateTime.now());

		if (bundle.getTotal() <= 0)
		{
			logger.warn("Result bundle.total <= 0, ignoring ping");
			return;
		}

		for (BundleEntryComponent entry : bundle.getEntry())
		{
			if (entry.getResource() instanceof Task)
			{
				Task task = (Task) entry.getResource();
				taskHandler.onTask(task);

				lastEventTimeIo.writeLastEventTime(LocalDateTime.ofInstant(task.getMeta().getLastUpdated().toInstant(), ZoneId.systemDefault()));
			}
			else
				logger.warn("Ignoring resource of type {}");
		}

		if (bundle.getTotal() > RESULT_PAGE_COUNT)
		{
			logger.warn("Result bundle.total > {}, calling onPing again", RESULT_PAGE_COUNT);
			onPing(ping, subscriptionIdPart, searchCriteriaQueryParameters);
		}
	}
}
