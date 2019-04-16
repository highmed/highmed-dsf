package org.highmed.fhir.event;

import org.hl7.fhir.r4.model.DomainResource;

public class ResourceDeletedEvent extends AbstractEvent implements Event
{
	public ResourceDeletedEvent(Class<? extends DomainResource> type, String id)
	{
		super(type, id);
	}
}
