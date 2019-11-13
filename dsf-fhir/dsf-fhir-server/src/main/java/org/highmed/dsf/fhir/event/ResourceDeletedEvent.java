package org.highmed.dsf.fhir.event;

import org.hl7.fhir.r4.model.Resource;

public class ResourceDeletedEvent extends AbstractEvent implements Event
{
	public ResourceDeletedEvent(Class<? extends Resource> type, String id)
	{
		super(type, id);
	}
}
