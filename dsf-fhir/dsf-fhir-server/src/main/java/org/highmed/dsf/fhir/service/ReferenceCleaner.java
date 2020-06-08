package org.highmed.dsf.fhir.service;

import org.hl7.fhir.r4.model.Resource;

public interface ReferenceCleaner
{
	/**
	 * Removes literal references, if a conditional reference is also set
	 * 
	 * @param resource
	 * @return null if given resource is null, cleanup resource (same instance)
	 */
	<R extends Resource> R cleanupReferences(R resource);
}
