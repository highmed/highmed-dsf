package org.highmed.fhir.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class EventManagerImpl implements EventManager, InitializingBean, DisposableBean
{
	private static final Logger logger = LoggerFactory.getLogger(EventManagerImpl.class);

	private final ExecutorService executor = Executors.newCachedThreadPool();

	public EventManagerImpl()
	{
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		
		event.getClass();
		event.getResource();
	}
}
