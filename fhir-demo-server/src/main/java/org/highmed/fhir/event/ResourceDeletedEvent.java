package org.highmed.fhir.event;

import org.hl7.fhir.r4.model.DomainResource;

public class ResourceDeletedEvent<R extends DomainResource> extends AbstractEvent<R> implements Event<R>
{
	public ResourceDeletedEvent(Class<R> type, String id)
	{
		super(type, id);
	}
}
