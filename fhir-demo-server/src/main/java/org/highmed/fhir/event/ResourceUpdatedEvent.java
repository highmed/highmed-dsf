package org.highmed.fhir.event;

import org.hl7.fhir.r4.model.DomainResource;

public class ResourceUpdatedEvent extends AbstractEventWithResource implements Event
{
	public ResourceUpdatedEvent(Class<? extends DomainResource> type, DomainResource resource)
	{
		super(type, resource);
	}
}
