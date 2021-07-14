package org.highmed.dsf.fhir.service;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public interface ReferenceCleaner
{
	/**
	 * Removes literal references, if a conditional reference is also set
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            the resource to clean, may be <code>null</code>
	 * @return null if given resource is null, cleaned up resource (same instance)
	 */
	<R extends Resource> R cleanLiteralReferences(R resource);

	/**
	 * Removes embedded resources from references within {@link Bundle} entries
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            the resource to clean, may be <code>null</code>
	 * @return null if given resource is null, cleaned up resource (same instance)
	 */
	<R extends Resource> R cleanReferenceResourcesIfBundle(R resource);
}
