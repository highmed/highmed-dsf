package org.highmed.dsf.fhir.subscription;

import java.sql.SQLException;
import java.util.Date;

import org.highmed.dsf.bpe.dao.LastEventTimeDao;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class EventResourceHandlerImpl<R extends Resource> implements EventResourceHandler<R>
{
	private static final Logger logger = LoggerFactory.getLogger(EventResourceHandlerImpl.class);

	private final LastEventTimeDao lastEventTimeDao;
	private final ResourceHandler<R> handler;
	private final Class<R> resourceClass;

	public EventResourceHandlerImpl(LastEventTimeDao lastEventTimeDao, ResourceHandler<R> handler,
			Class<R> resourceClass)
	{
		this.lastEventTimeDao = lastEventTimeDao;
		this.handler = handler;
		this.resourceClass = resourceClass;
	}

	public void onResource(Resource resource)
	{
		logger.trace("Resource of type {} received", resource.getClass().getAnnotation(ResourceDef.class).name());

		if (resourceClass.isInstance(resource))
		{
			@SuppressWarnings("unchecked")
			R cast = (R) resource;
			handler.onResource(cast);
			writeLastEventTime(cast.getMeta().getLastUpdated());
		}
		else
		{
			logger.warn("Ignoring resource of type {}", resource.getClass().getAnnotation(ResourceDef.class).name());
		}
	}

	private void writeLastEventTime(Date lastUpdated)
	{
		try
		{
			lastEventTimeDao.writeLastEventTime(lastUpdated);
		}
		catch (SQLException e)
		{
			logger.warn("Unable to write last event time to db: {} - {}", e.getClass().getName(), e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
