package org.highmed.dsf.fhir.event;

import java.util.Objects;

import org.hl7.fhir.r4.model.Resource;

public class AbstractEventWithResource extends AbstractEvent implements Event
{
	private final Resource resource;

	public AbstractEventWithResource(Class<? extends Resource> type, Resource resource)
	{
		super(type, Objects.requireNonNull(resource, "resource").getIdElement().getIdPart());
		this.resource = resource;
	}

	/**
	 * @return never <code>null</code>
	 */
	@Override
	public Resource getResource()
	{
		return resource;
	}
}
