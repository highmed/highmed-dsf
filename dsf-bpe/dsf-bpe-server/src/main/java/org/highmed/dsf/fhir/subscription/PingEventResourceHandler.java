package org.highmed.dsf.fhir.subscription;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingEventResourceHandler<R extends Resource>
{
	private static final Logger logger = LoggerFactory.getLogger(PingEventResourceHandler.class);

	private final ExistingResourceLoader<R> loader;

	public PingEventResourceHandler(ExistingResourceLoader<R> loader)
	{
		this.loader = loader;
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

		loader.readExistingResources(searchCriteriaQueryParameters);
	}
}
