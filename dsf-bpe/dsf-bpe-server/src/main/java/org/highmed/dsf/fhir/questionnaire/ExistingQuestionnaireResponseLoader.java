package org.highmed.dsf.fhir.questionnaire;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.subscription.ExistingResourceLoader;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExistingQuestionnaireResponseLoader implements ExistingResourceLoader
{
	private static final Logger logger = LoggerFactory.getLogger(ExistingQuestionnaireResponseLoader.class);

	private static final String PARAM_LAST_UPDATED = "_lastUpdated";
	private static final String PARAM_COUNT = "_count";
	private static final String PARAM_PAGE = "_page";
	private static final String PARAM_SORT = "_sort";
	private static final int RESULT_PAGE_COUNT = 20;

	private final LastEventTimeIo lastEventTimeIo;
	private final FhirWebserviceClient webserviceClient;
	private final ResourceHandler<QuestionnaireResponse> handler;

	public ExistingQuestionnaireResponseLoader(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<QuestionnaireResponse> handler, FhirWebserviceClient webserviceClient)
	{
		this.lastEventTimeIo = lastEventTimeIo;
		this.handler = handler;
		this.webserviceClient = webserviceClient;
	}

	public void readExistingResources(Map<String, List<String>> searchCriteriaQueryParameters)
	{
		// executing search until call results in no more found questionnaire responses
		while (doReadExistingResources(searchCriteriaQueryParameters))
			;
	}

	private boolean doReadExistingResources(Map<String, List<String>> searchCriteriaQueryParameters)
	{
		Map<String, List<String>> queryParams = new HashMap<>(searchCriteriaQueryParameters);
		Optional<LocalDateTime> readLastEventTime = lastEventTimeIo.readLastEventTime();

		readLastEventTime.ifPresent(lastEventTime -> queryParams.put(PARAM_LAST_UPDATED,
				Collections.singletonList("gt" + lastEventTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

		queryParams.put(PARAM_COUNT, Collections.singletonList(String.valueOf(RESULT_PAGE_COUNT)));
		queryParams.put(PARAM_PAGE, Collections.singletonList("1"));
		queryParams.put(PARAM_SORT, Collections.singletonList(PARAM_LAST_UPDATED));

		UriBuilder builder = UriBuilder.fromPath("QuestionnaireResponse");
		queryParams.forEach((k, v) -> builder.replaceQueryParam(k, v.toArray()));

		logger.debug("Executing search {}", builder.toString());
		Bundle bundle = webserviceClient.searchWithStrictHandling(QuestionnaireResponse.class, queryParams);

		if (bundle.getTotal() <= 0)
		{
			logger.debug("Result bundle.total <= 0");
			return false;
		}

		for (BundleEntryComponent entry : bundle.getEntry())
		{
			if (entry.getResource() instanceof QuestionnaireResponse)
			{
				QuestionnaireResponse questionnaireResponse = (QuestionnaireResponse) entry.getResource();
				handler.onResource(questionnaireResponse);
				lastEventTimeIo.writeLastEventTime(questionnaireResponse.getAuthored());
			}
			else
				logger.warn("Ignoring resource of type {}", QuestionnaireResponse.class.getName());
		}

		return true;
	}
}
