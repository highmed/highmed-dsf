package org.highmed.dsf.fhir.questionnaire;

import org.highmed.dsf.fhir.websocket.EventResourceHandler;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class EventQuestionnaireResponseHandler implements EventResourceHandler
{
	private static final Logger logger = LoggerFactory.getLogger(EventQuestionnaireResponseHandler.class);

	private final ResourceHandler<QuestionnaireResponse> handler;
	private final LastEventTimeIo lastEventTimeIo;

	public EventQuestionnaireResponseHandler(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<QuestionnaireResponse> handler)
	{
		this.lastEventTimeIo = lastEventTimeIo;
		this.handler = handler;
	}

	public void onResource(DomainResource resource)
	{
		logger.trace("Resource of type {} received", resource.getClass().getAnnotation(ResourceDef.class).name());

		if (resource instanceof QuestionnaireResponse)
		{
			QuestionnaireResponse questionnaireResponse = (QuestionnaireResponse) resource;
			handler.onResource(questionnaireResponse);
			lastEventTimeIo.writeLastEventTime(questionnaireResponse.getAuthored());
		}
		else
			logger.warn("Ignoring resource of type {}", resource.getClass().getAnnotation(ResourceDef.class).name());
	}
}
