package org.highmed.fhir.event;

import java.util.Objects;

import org.hl7.fhir.r4.model.DomainResource;

public class AbstractEvent<R extends DomainResource> implements Event<R>
{
	private final Class<R> type;
	private final String id;

	/**
	 * @param type
	 *            not <code>null</code>
	 * @param id
	 *            not <code>null</code>
	 */
	public AbstractEvent(Class<R> type, String id)
	{
		this.type = Objects.requireNonNull(type, "type");
		this.id = Objects.requireNonNull(id, "id");
	}

	@Override
	public Class<R> getType()
	{
		return type;
	}

	@Override
	public String getId()
	{
		return id;
	}

	/**
	 * @return always <code>null</code>
	 */
	@Override
	public R getResource()
	{
		return null;
	}
}
