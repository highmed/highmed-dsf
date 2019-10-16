package org.highmed.dsf.fhir.event;

import org.hl7.fhir.r4.model.Resource;

public class ResourceUpdatedEvent extends AbstractEventWithResource implements Event
{
	public ResourceUpdatedEvent(Class<? extends Resource> type, Resource resource)
	{
		super(type, resource);
	}
}
