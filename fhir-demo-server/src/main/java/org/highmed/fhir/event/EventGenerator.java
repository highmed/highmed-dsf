package org.highmed.fhir.event;

import org.hl7.fhir.r4.model.DomainResource;

public class EventGenerator<R extends DomainResource>
{
	private final Class<R> resourceType;

	public EventGenerator(Class<R> resourceType)
	{
		this.resourceType = resourceType;
	}

	public ResourceCreatedEvent<R> newResourceCreatedEvent(R createdResource)
	{
		return new ResourceCreatedEvent<R>(resourceType, createdResource);
	}

	public ResourceUpdatedEvent<R> newResourceUpdatedEvent(R updatedResource)
	{
		return new ResourceUpdatedEvent<R>(resourceType, updatedResource);
	}

	public ResourceDeletedEvent<R> newResourceDeletedEvent(String id)
	{
		return new ResourceDeletedEvent<R>(resourceType, id);
	}
}
