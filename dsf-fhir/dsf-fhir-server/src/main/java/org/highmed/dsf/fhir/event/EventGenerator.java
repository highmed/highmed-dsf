package org.highmed.dsf.fhir.event;

import org.hl7.fhir.r4.model.Resource;

public class EventGenerator
{
	public ResourceCreatedEvent newResourceCreatedEvent(Resource createdResource)
	{
		return new ResourceCreatedEvent(createdResource.getClass(), createdResource);
	}

	public ResourceUpdatedEvent newResourceUpdatedEvent(Resource updatedResource)
	{
		return new ResourceUpdatedEvent(updatedResource.getClass(), updatedResource);
	}

	public ResourceDeletedEvent newResourceDeletedEvent(Class<? extends Resource> resourceType, String id)
	{
		return new ResourceDeletedEvent(resourceType, id);
	}
}
