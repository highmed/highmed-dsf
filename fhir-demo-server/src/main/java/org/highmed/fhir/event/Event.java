package org.highmed.fhir.event;

import org.hl7.fhir.r4.model.DomainResource;

public interface Event<R extends DomainResource>
{
	/**
	 * @return never <code>null</code>
	 */
	Class<R> getType();

	/**
	 * @return never <code>null</code>
	 */
	String getId();

	/**
	 * @return might be <code>null</code>
	 */
	R getResource();
}
