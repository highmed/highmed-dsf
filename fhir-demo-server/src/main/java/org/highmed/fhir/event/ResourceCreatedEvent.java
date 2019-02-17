package org.highmed.fhir.event;

import org.hl7.fhir.r4.model.DomainResource;

public class ResourceCreatedEvent<R extends DomainResource> extends AbstractEventWithResource<R> implements Event<R>
{
	public ResourceCreatedEvent(Class<R> type, R resource)
	{
		super(type, resource);
	}
}
