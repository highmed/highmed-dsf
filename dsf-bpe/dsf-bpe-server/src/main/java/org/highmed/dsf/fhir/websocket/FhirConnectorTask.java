package org.highmed.dsf.fhir.websocket;

import org.highmed.dsf.fhir.client.FhirWebsocketClientProvider;
import org.highmed.dsf.fhir.subscription.SubscriptionHandlerFactory;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class FhirConnectorTask extends AbstractFhirConnector<Task>
{
	public FhirConnectorTask(FhirWebsocketClientProvider clientProvider,
			SubscriptionHandlerFactory<Task> subscriptionHandlerFactory, FhirContext fhirContext,
			String subscriptionSearchParameter, long retrySleepMillis, int maxRetries)
	{
		super("Task", clientProvider, subscriptionHandlerFactory, fhirContext, subscriptionSearchParameter,
				retrySleepMillis, maxRetries);
	}
}
