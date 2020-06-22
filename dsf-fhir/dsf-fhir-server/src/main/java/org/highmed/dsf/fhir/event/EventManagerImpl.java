package org.highmed.dsf.fhir.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventManagerImpl implements EventManager
{
	private static final Logger logger = LoggerFactory.getLogger(EventManagerImpl.class);

	private final List<EventHandler> eventHandlers = Collections.synchronizedList(new ArrayList<>());

	public EventManagerImpl(Collection<? extends EventHandler> eventHandlers)
	{
		if (eventHandlers != null)
			this.eventHandlers.addAll(eventHandlers);
	}

	@Override
	public void handleEvent(Event event)
	{
		if (event != null)
			eventHandlers.forEach(doHandleEvent(event));
	}

	private Consumer<? super EventHandler> doHandleEvent(Event event)
	{
		return e ->
		{
			try
			{
				e.handleEvent(event);
			}
			catch (Exception ex)
			{
				logger.warn("Error while handling {} with {}", event.getClass().getSimpleName(),
						e.getClass().getName());
			}
		};
	}

	@Override
	public void handleEvents(List<Event> events)
	{
		if (events != null)
			eventHandlers.forEach(doHandleEvents(events));
	}

	private Consumer<? super EventHandler> doHandleEvents(List<Event> events)
	{
		return e ->
		{
			try
			{
				e.handleEvents(events);
			}
			catch (Exception ex)
			{
				logger.warn("Error while handling {} event{} with {}", events.size(), events.size() != 1 ? "s" : "",
						e.getClass().getName());
			}
		};
	}

	@Override
	public Runnable addHandler(EventHandler eventHandler)
	{
		eventHandlers.add(eventHandler);

		return () -> removeHandler(eventHandler);
	}

	@Override
	public void removeHandler(EventHandler eventHandler)
	{
		eventHandlers.remove(eventHandler);
	}
}
