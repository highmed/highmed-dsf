package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.webservice.specification.EndpointService;
import org.hl7.fhir.r4.model.Endpoint;

public class EndpointServiceSecure extends AbstractServiceSecure<Endpoint, EndpointService> implements EndpointService
{
	public EndpointServiceSecure(EndpointService delegate)
	{
		super(delegate);
	}
}
