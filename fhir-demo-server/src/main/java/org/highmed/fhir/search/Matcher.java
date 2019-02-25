package org.highmed.fhir.search;

import org.hl7.fhir.r4.model.DomainResource;

public interface Matcher
{
	boolean matches(DomainResource resource);

	Class<? extends DomainResource> getResourceType();
}
