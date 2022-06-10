package org.highmed.dsf.fhir.questionnaire;

import java.util.Objects;

import org.highmed.dsf.fhir.subscription.EventResourceHandler;
import org.highmed.dsf.fhir.subscription.ExistingResourceLoader;
import org.highmed.dsf.fhir.subscription.PingEventResourceHandler;
import org.highmed.dsf.fhir.subscription.SubscriptionHandlerFactory;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.springframework.beans.factory.InitializingBean;

public class QuestionnaireResponseSubscriptionHandlerFactory
		implements SubscriptionHandlerFactory<QuestionnaireResponse>, InitializingBean
{
	private final ResourceHandler<QuestionnaireResponse> resourceHandler;
	private final LastEventTimeIo lastEventTimeIo;

	public QuestionnaireResponseSubscriptionHandlerFactory(ResourceHandler<QuestionnaireResponse> resourceHandler,
			LastEventTimeIo lastEventTimeIo)
	{
		this.resourceHandler = resourceHandler;
		this.lastEventTimeIo = lastEventTimeIo;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(resourceHandler, "resourceHandler");
		Objects.requireNonNull(lastEventTimeIo, "lastEventTimeIo");
	}

	@Override
	public ExistingResourceLoader<QuestionnaireResponse> createExistingResourceLoader(FhirWebserviceClient client)
	{
		return new ExistingQuestionnaireResponseLoader(lastEventTimeIo, resourceHandler, client);
	}

	@Override
	public EventResourceHandler<QuestionnaireResponse> createEventResourceHandler()
	{
		return new EventQuestionnaireResponseHandler(lastEventTimeIo, resourceHandler);
	}

	@Override
	public PingEventResourceHandler createPingEventResourceHandler(
			ExistingResourceLoader<QuestionnaireResponse> existingResourceLoader)
	{
		return new PingEventResourceHandler(existingResourceLoader);
	}
}
