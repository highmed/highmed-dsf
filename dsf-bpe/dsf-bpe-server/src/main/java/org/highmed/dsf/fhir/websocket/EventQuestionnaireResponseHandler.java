package org.highmed.dsf.fhir.websocket;

import org.highmed.dsf.fhir.questionnaire.QuestionnaireResponseHandler;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class EventQuestionnaireResponseHandler
{
	private static final Logger logger = LoggerFactory.getLogger(EventQuestionnaireResponseHandler.class);

	private final QuestionnaireResponseHandler questionnaireResponseHandler;
	private final LastEventTimeIo lastEventTimeIo;

	public EventQuestionnaireResponseHandler(LastEventTimeIo lastEventTimeIo,
			QuestionnaireResponseHandler questionnaireResponseHandler)
	{
		this.lastEventTimeIo = lastEventTimeIo;
		this.questionnaireResponseHandler = questionnaireResponseHandler;
	}

	public void onResource(DomainResource resource)
	{
		logger.trace("Resource of type {} received", resource.getClass().getAnnotation(ResourceDef.class).name());

		if (resource instanceof QuestionnaireResponse)
		{
			QuestionnaireResponse questionnaireResponse = (QuestionnaireResponse) resource;
			questionnaireResponseHandler.onResource(questionnaireResponse);
			lastEventTimeIo.writeLastEventTime(questionnaireResponse.getAuthored());
		}
		else
			logger.warn("Ignoring resource of type {}");
	}
}
