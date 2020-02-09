package org.highmed.dsf.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.StaticResourcesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticResourcesServiceSecure extends AbstractServiceSecure<StaticResourcesService> implements StaticResourcesService
{
	private static final Logger logger = LoggerFactory.getLogger(StaticResourcesServiceSecure.class);

	public StaticResourcesServiceSecure(StaticResourcesService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}

	@Override
	public Response getFile(String fileName, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.getFile(fileName, uri, headers);
	}
}
