package org.highmed.dsf.fhir.dao.command;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.RemoteEndpoint.Async;

import org.highmed.dsf.fhir.event.Event;
import org.highmed.dsf.fhir.event.EventManager;

public class TransactionEventManager implements EventManager
{
	private final List<Event> cachedEvents = new ArrayList<>();
	private final EventManager delegate;

	public TransactionEventManager(EventManager delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void handleEvent(Event event)
	{
		cachedEvents.add(event);
	}

	@Override
	public void handleEvents(List<Event> events)
	{
		this.cachedEvents.addAll(events);
	}

	public void commitEvents()
	{
		delegate.handleEvents(cachedEvents);
	}

	@Override
	public void bind(String sessionId, Async asyncRemote, String subscriptionIdPart)
	{
		delegate.bind(sessionId, asyncRemote, subscriptionIdPart);
	}

	@Override
	public void close(String sessionId)
	{
		delegate.close(sessionId);
	}
}
