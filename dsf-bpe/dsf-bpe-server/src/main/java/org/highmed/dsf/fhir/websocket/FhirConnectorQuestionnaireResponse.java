package org.highmed.dsf.fhir.websocket;

import org.highmed.dsf.fhir.client.FhirWebsocketClientProvider;
import org.highmed.dsf.fhir.questionnaire.EventQuestionnaireResponseHandler;
import org.highmed.dsf.fhir.questionnaire.ExistingQuestionnaireResponseLoader;
import org.highmed.dsf.fhir.questionnaire.QuestionnaireResponseHandler;
import org.highmed.dsf.fhir.subscription.EventResourceHandler;
import org.highmed.dsf.fhir.subscription.ExistingResourceLoader;
import org.highmed.dsf.fhir.subscription.PingEventResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

import ca.uhn.fhir.context.FhirContext;

public class FhirConnectorQuestionnaireResponse extends AbstractSubscriptionFhirConnector<QuestionnaireResponse>
{

	public FhirConnectorQuestionnaireResponse(FhirWebsocketClientProvider clientProvider,
			QuestionnaireResponseHandler handler, LastEventTimeIo lastEventTimeIo, FhirContext fhirContext,
			String subscriptionSearchParameter, long retrySleepMillis, int maxRetries)
	{
		super("QuestionnaireResponse", clientProvider, handler, lastEventTimeIo, fhirContext,
				subscriptionSearchParameter, retrySleepMillis, maxRetries);
	}

	@Override
	public ExistingResourceLoader createExistingResourceLoader(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<QuestionnaireResponse> resourceHandler, FhirWebserviceClient client)
	{
		return new ExistingQuestionnaireResponseLoader(lastEventTimeIo, resourceHandler, client);
	}

	@Override
	public EventResourceHandler createEventResourceHandler(LastEventTimeIo lastEventTimeIo,
			ResourceHandler<QuestionnaireResponse> resourceHandler)
	{
		return new EventQuestionnaireResponseHandler(lastEventTimeIo, resourceHandler);
	}

	@Override
	public PingEventResourceHandler createPingEventResourceHandler(ExistingResourceLoader existingResourceLoader)
	{
		return new PingEventResourceHandler(existingResourceLoader);
	}
}
