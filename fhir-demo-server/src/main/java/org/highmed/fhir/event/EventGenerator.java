package org.highmed.fhir.event;

import org.hl7.fhir.r4.model.DomainResource;

public class EventGenerator
{
	public ResourceCreatedEvent newResourceCreatedEvent(DomainResource createdResource)
	{
		return new ResourceCreatedEvent(createdResource.getClass(), createdResource);
	}

	public ResourceUpdatedEvent newResourceUpdatedEvent(DomainResource updatedResource)
	{
		return new ResourceUpdatedEvent(updatedResource.getClass(), updatedResource);
	}

	public ResourceDeletedEvent newResourceDeletedEvent(Class<? extends DomainResource> resourceType, String id)
	{
		return new ResourceDeletedEvent(resourceType, id);
	}
}
