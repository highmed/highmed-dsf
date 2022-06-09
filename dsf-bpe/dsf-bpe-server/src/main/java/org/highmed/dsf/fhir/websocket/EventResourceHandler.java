package org.highmed.dsf.fhir.websocket;

import org.hl7.fhir.r4.model.DomainResource;

public interface EventResourceHandler
{
	void onResource(DomainResource resource);
}
