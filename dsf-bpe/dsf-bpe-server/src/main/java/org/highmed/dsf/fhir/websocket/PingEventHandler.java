package org.highmed.dsf.fhir.websocket;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingEventHandler
{
	private static final Logger logger = LoggerFactory.getLogger(PingEventHandler.class);

	private final ExistingTaskLoader taskLoader;

	public PingEventHandler(ExistingTaskLoader taskLoader)
	{
		this.taskLoader = taskLoader;
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

		taskLoader.readExistingTasks(searchCriteriaQueryParameters);
	}
}
