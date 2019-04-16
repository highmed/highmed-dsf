package org.highmed.fhir.event;

import org.hl7.fhir.r4.model.DomainResource;

public interface Event
{
	/**
	 * @return never <code>null</code>
	 */
	Class<? extends DomainResource> getResourceType();

	/**
	 * @return never <code>null</code>
	 */
	String getId();

	/**
	 * @return might be <code>null</code>
	 */
	DomainResource getResource();
}
