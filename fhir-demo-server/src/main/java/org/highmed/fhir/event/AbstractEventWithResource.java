package org.highmed.fhir.event;

import java.util.Objects;

import org.hl7.fhir.r4.model.DomainResource;

public class AbstractEventWithResource<R extends DomainResource> extends AbstractEvent<R> implements Event<R>
{
	private final R resource;

	public AbstractEventWithResource(Class<R> type, R resource)
	{
		super(type, Objects.requireNonNull(resource, "resource").getIdElement().getIdPart());
		this.resource = resource;
	}

	/**
	 * @return never <code>null</code>
	 */
	@Override
	public R getResource()
	{
		return resource;
	}
}
