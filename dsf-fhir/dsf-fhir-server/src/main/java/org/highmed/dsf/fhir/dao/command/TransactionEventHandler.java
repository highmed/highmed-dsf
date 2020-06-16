package org.highmed.dsf.fhir.dao.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.highmed.dsf.fhir.event.Event;
import org.highmed.dsf.fhir.event.EventHandler;

public class TransactionEventHandler implements EventHandler
{
	private final List<Event> cachedEvents = new ArrayList<>();
	private final EventHandler commitDelegate;
	private final EventHandler delegate;

	public TransactionEventHandler(EventHandler commitDelegate, EventHandler delegate)
	{
		this.commitDelegate = Objects.requireNonNull(commitDelegate, "commitDelegate");
		this.delegate = delegate; // may be null
	}

	@Override
	public void handleEvent(Event event)
	{
		cachedEvents.add(event);

		if (delegate != null)
			delegate.handleEvent(event);
	}

	@Override
	public void handleEvents(List<Event> events)
	{
		cachedEvents.addAll(events);

		if (delegate != null)
			delegate.handleEvents(events);
	}

	public void commitEvents()
	{
		commitDelegate.handleEvents(cachedEvents);
	}
}
