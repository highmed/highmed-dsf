package org.highmed.dsf.fhir.questionnaire;

import java.util.Objects;

import org.highmed.dsf.bpe.dao.LastEventTimeDao;
import org.highmed.dsf.fhir.subscription.EventResourceHandler;
import org.highmed.dsf.fhir.subscription.EventResourceHandlerImpl;
import org.highmed.dsf.fhir.subscription.ExistingResourceLoader;
import org.highmed.dsf.fhir.subscription.ExistingResourceLoaderImpl;
import org.highmed.dsf.fhir.subscription.PingEventResourceHandler;
import org.highmed.dsf.fhir.subscription.SubscriptionHandlerFactory;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.springframework.beans.factory.InitializingBean;

public class QuestionnaireResponseSubscriptionHandlerFactory
		implements SubscriptionHandlerFactory<QuestionnaireResponse>, InitializingBean
{
	private final ResourceHandler<QuestionnaireResponse> resourceHandler;
	private final LastEventTimeDao lastEventTimeDao;

	public QuestionnaireResponseSubscriptionHandlerFactory(ResourceHandler<QuestionnaireResponse> resourceHandler,
			LastEventTimeDao lastEventTimeDao)
	{
		this.resourceHandler = resourceHandler;
		this.lastEventTimeDao = lastEventTimeDao;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(resourceHandler, "resourceHandler");
		Objects.requireNonNull(lastEventTimeDao, "lastEventTimeDao");
	}

	@Override
	public ExistingResourceLoader<QuestionnaireResponse> createExistingResourceLoader(FhirWebserviceClient client)
	{
		return new ExistingResourceLoaderImpl<>(lastEventTimeDao, resourceHandler, client, "QuestionnaireResponse",
				QuestionnaireResponse.class);
	}

	@Override
	public EventResourceHandler<QuestionnaireResponse> createEventResourceHandler()
	{
		return new EventResourceHandlerImpl<>(lastEventTimeDao, resourceHandler, QuestionnaireResponse.class);
	}

	@Override
	public PingEventResourceHandler<QuestionnaireResponse> createPingEventResourceHandler(
			ExistingResourceLoader<QuestionnaireResponse> existingResourceLoader)
	{
		return new PingEventResourceHandler<>(existingResourceLoader);
	}
}
