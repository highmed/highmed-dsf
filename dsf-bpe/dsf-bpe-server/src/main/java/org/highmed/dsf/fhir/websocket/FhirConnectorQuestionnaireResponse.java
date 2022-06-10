package org.highmed.dsf.fhir.websocket;

import org.highmed.dsf.fhir.client.FhirWebsocketClientProvider;
import org.highmed.dsf.fhir.subscription.SubscriptionHandlerFactory;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

import ca.uhn.fhir.context.FhirContext;

public class FhirConnectorQuestionnaireResponse extends AbstractFhirConnector<QuestionnaireResponse>
{

	public FhirConnectorQuestionnaireResponse(FhirWebsocketClientProvider clientProvider,
			SubscriptionHandlerFactory<QuestionnaireResponse> subscriptionHandlerFactory, FhirContext fhirContext,
			String subscriptionSearchParameter, long retrySleepMillis, int maxRetries)
	{
		super("QuestionnaireResponse", clientProvider, subscriptionHandlerFactory, fhirContext,
				subscriptionSearchParameter, retrySleepMillis, maxRetries);
	}
}
