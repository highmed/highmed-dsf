package org.highmed.dsf.fhir.subscription;

import java.util.Optional;

import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public abstract class AbstractEventResourceHandler<R extends Resource> implements EventResourceHandler<R>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractEventResourceHandler.class);

	private final ResourceHandler<R> handler;
	private final LastEventTimeIo lastEventTimeIo;

	public AbstractEventResourceHandler(LastEventTimeIo lastEventTimeIo, ResourceHandler<R> handler)
	{
		this.lastEventTimeIo = lastEventTimeIo;
		this.handler = handler;
	}

	public void onResource(Resource resource)
	{
		logger.trace("Resource of type {} received", resource.getClass().getAnnotation(ResourceDef.class).name());

		Optional<R> resourceOptional = castResource(resource);

		if (resourceOptional.isPresent())
		{
			R res = resourceOptional.get();
			handler.onResource(res);
			lastEventTimeIo.writeLastEventTime(res.getMeta().getLastUpdated());
		}
		else
		{
			logger.warn("Ignoring resource of type {}", resource.getClass().getAnnotation(ResourceDef.class).name());
		}
	}

	protected abstract Optional<R> castResource(Resource resource);
}
