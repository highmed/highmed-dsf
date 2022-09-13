package org.highmed.dsf.fhir.subscription;

import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class EventResourceHandlerImpl<R extends Resource> implements EventResourceHandler<R>
{
	private static final Logger logger = LoggerFactory.getLogger(EventResourceHandlerImpl.class);

	private final LastEventTimeIo lastEventTimeIo;
	private final ResourceHandler<R> handler;
	private final Class<R> resourceClass;

	public EventResourceHandlerImpl(LastEventTimeIo lastEventTimeIo, ResourceHandler<R> handler, Class<R> resourceClass)
	{
		this.lastEventTimeIo = lastEventTimeIo;
		this.handler = handler;
		this.resourceClass = resourceClass;
	}

	public void onResource(Resource resource)
	{
		logger.trace("Resource of type {} received", resource.getClass().getAnnotation(ResourceDef.class).name());

		if (resourceClass.isInstance(resource))
		{
			R cast = (R) resource;
			handler.onResource(cast);
			lastEventTimeIo.writeLastEventTime(cast.getMeta().getLastUpdated());
		}
		else
		{
			logger.warn("Ignoring resource of type {}", resource.getClass().getAnnotation(ResourceDef.class).name());
		}

	}
}
