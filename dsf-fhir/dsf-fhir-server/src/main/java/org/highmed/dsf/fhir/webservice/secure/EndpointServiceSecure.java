package org.highmed.dsf.fhir.webservice.secure;

import java.util.Optional;

import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.EndpointService;
import org.hl7.fhir.r4.model.Endpoint;

public class EndpointServiceSecure extends AbstractResourceServiceSecure<EndpointDao, Endpoint, EndpointService>
		implements EndpointService
{
	public EndpointServiceSecure(EndpointService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, EndpointDao endpointDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Endpoint.class, endpointDao, exceptionHandler,
				parameterConverter);
	}

	@Override
	protected Optional<String> reasonCreateAllowed(Endpoint resource)
	{
		// TODO validate unique on Endpoint.address

		return Optional.empty();
	}

	@Override
	protected Optional<String> reasonUpdateAllowed(Endpoint oldResource, Endpoint newResource)
	{
		// TODO validate unique on Endpoint.address

		return Optional.empty();
	}
}
