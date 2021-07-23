package org.highmed.dsf.fhir.event;

import org.hl7.fhir.r4.model.Resource;

public class ResourceExpungeEvent extends AbstractEvent implements Event
{
	public ResourceExpungeEvent(Class<? extends Resource> type, String id)
	{
		super(type, id);
	}
}
