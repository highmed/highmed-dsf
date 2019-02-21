package org.highmed.fhir.event;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.highmed.fhir.dao.SubscriptionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class EventManagerImpl implements EventManager, InitializingBean, DisposableBean
{
	private static final Logger logger = LoggerFactory.getLogger(EventManagerImpl.class);

	private final ExecutorService executor = Executors.newCachedThreadPool();

	private final SubscriptionDao subscriptionDao;

	public EventManagerImpl(SubscriptionDao subscriptionDao)
	{
		this.subscriptionDao = subscriptionDao;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(subscriptionDao, "subscriptionDao");
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
	{
		logger.info("onContextRefreshedEvent");
	}

	@Override
	public void destroy() throws Exception
	{
		executor.shutdown();
		try
		{
			if (!executor.awaitTermination(60, TimeUnit.SECONDS))
			{
				executor.shutdownNow();
				if (!executor.awaitTermination(60, TimeUnit.SECONDS))
					logger.warn("EventManager executor did not terminate");
			}
		}
		catch (InterruptedException ie)
		{
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void handleEvent(Event<?> event)
	{
		executor.execute(() -> handleEventAsync(event));
	}

	private void handleEventAsync(Event<?> event)
	{
		logger.debug("handling event {} for resource of type {} with id {}", event.getClass().getName(),
				event.getType().getAnnotation(ResourceDef.class).name(), event.getId());

		// TODO get subscriptions from db
		// TODO figure out what connected event handlers to call, and call them
	}
}
