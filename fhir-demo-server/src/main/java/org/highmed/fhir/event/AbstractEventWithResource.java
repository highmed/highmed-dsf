package org.highmed.fhir.event;

import java.util.Objects;

import org.hl7.fhir.r4.model.DomainResource;

public class AbstractEventWithResource extends AbstractEvent implements Event
{
	private final DomainResource resource;

	public AbstractEventWithResource(Class<? extends DomainResource> type, DomainResource resource)
	{
		super(type, Objects.requireNonNull(resource, "resource").getIdElement().getIdPart());
		this.resource = resource;
	}

	/**
	 * @return never <code>null</code>
	 */
	@Override
	public DomainResource getResource()
	{
		return resource;
	}
}
