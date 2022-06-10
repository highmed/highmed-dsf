package org.highmed.dsf.fhir.questionnaire;

import java.util.Optional;

import org.highmed.dsf.fhir.subscription.AbstractExistingResourceLoader;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;

public class ExistingQuestionnaireResponseLoader extends AbstractExistingResourceLoader<QuestionnaireResponse>
{
	public ExistingQuestionnaireResponseLoader(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<QuestionnaireResponse> handler, FhirWebserviceClient webserviceClient)
	{
		super(lastEventTimeIo, handler, webserviceClient);
	}

	@Override
	protected Optional<QuestionnaireResponse> castExistingResource(Resource resource)
	{
		if (resource instanceof QuestionnaireResponse)
			return Optional.of((QuestionnaireResponse) resource);
		else
			return Optional.empty();
	}
}
