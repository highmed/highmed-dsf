package org.highmed.dsf.fhir.websocket;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingEventQuestionnaireResponseHandler
{
	private static final Logger logger = LoggerFactory.getLogger(PingEventQuestionnaireResponseHandler.class);

	private final ExistingQuestionnaireResponseLoader loader;

	public PingEventQuestionnaireResponseHandler(ExistingQuestionnaireResponseLoader loader)
	{
		this.loader = loader;
	}

	public void onPing(String ping, String subscriptionIdPart, Map<String, List<String>> searchCriteriaQueryParameters)
	{
		logger.warn("Ping for subscription {} received", ping);
		if (!subscriptionIdPart.equals(ping))
		{
			logger.warn("Received ping for subscription {}, but expected subscription {}, ignoring ping", ping,
					subscriptionIdPart);
			return;
		}

		loader.readExistingResources(searchCriteriaQueryParameters);
	}
}
