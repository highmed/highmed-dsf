package org.highmed.dsf.fhir.event;

import org.hl7.fhir.r4.model.Resource;

public interface Event
{
	/**
	 * @return never <code>null</code>
	 */
	Class<? extends Resource> getResourceType();

	/**
	 * @return never <code>null</code>
	 */
	String getId();

	/**
	 * @return might be <code>null</code>
	 */
	Resource getResource();
}
