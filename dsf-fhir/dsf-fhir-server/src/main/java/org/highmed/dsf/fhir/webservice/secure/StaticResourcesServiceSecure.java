package org.highmed.dsf.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.StaticResourcesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticResourcesServiceSecure extends AbstractServiceSecure<StaticResourcesService>
		implements StaticResourcesService
{
	private static final Logger logger = LoggerFactory.getLogger(StaticResourcesServiceSecure.class);

	public StaticResourcesServiceSecure(StaticResourcesService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver);
	}

	@Override
	public Response getFile(String fileName, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		// get static files allowed for all authenticated users

		return delegate.getFile(fileName, uri, headers);
	}
}
