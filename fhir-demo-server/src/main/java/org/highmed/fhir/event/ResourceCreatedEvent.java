package org.highmed.fhir.event;

import org.hl7.fhir.r4.model.DomainResource;

public class ResourceCreatedEvent extends AbstractEventWithResource implements Event
{
	public ResourceCreatedEvent(Class<? extends DomainResource> type, DomainResource resource)
	{
		super(type, resource);
	}
}
