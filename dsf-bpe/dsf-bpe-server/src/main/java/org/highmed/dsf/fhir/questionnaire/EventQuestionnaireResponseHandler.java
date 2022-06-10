package org.highmed.dsf.fhir.questionnaire;

import java.util.Optional;

import org.highmed.dsf.fhir.subscription.AbstractEventResourceHandler;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;

public class EventQuestionnaireResponseHandler extends AbstractEventResourceHandler<QuestionnaireResponse>
{
	public EventQuestionnaireResponseHandler(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<QuestionnaireResponse> handler)
	{
		super(lastEventTimeIo, handler);
	}

	@Override
	protected Optional<QuestionnaireResponse> castResource(Resource resource)
	{
		if (resource instanceof QuestionnaireResponse)
			return Optional.of((QuestionnaireResponse) resource);
		else
			return Optional.empty();
	}
}
