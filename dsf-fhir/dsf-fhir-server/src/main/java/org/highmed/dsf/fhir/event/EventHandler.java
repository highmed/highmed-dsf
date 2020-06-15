package org.highmed.dsf.fhir.event;

import java.util.List;

public interface EventHandler
{
	void handleEvent(Event event);

	default void handleEvents(List<Event> events)
	{
		events.stream().forEach(this::handleEvent);
	}
}
