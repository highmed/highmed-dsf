package org.highmed.fhir.event;

public interface EventManager
{
	void handleEvent(Event<?> event);
}
