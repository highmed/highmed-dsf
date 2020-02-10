package org.highmed.dsf.fhir.webservice.secure;

import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.RootService;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootServiceSecure extends AbstractServiceSecure<RootService> implements RootService
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourceServiceSecure.class);

	public RootServiceSecure(RootService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}

	@Override
	public Response root(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		// get root allowed for all authenticated users

		return delegate.root(uri, headers);
	}

	@Override
	public Response handleBundle(Bundle bundle, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return reasonHandleBundleNotAllowed(bundle).map(forbidden("POST"))
				.orElse(delegate.handleBundle(bundle, uri, headers));
	}

	private Optional<String> reasonHandleBundleNotAllowed(Bundle bundle)
	{
		/*
		 * TODO check if operation for each entry in transaction / batch bundle is allowed, batch can have not allowed
		 * operations and will return 403 for those, transaction can't and will return 403 for all
		 */

		if (!UserRole.LOCAL.equals(provider.getCurrentUser().getRole()))
			return Optional.of("Missing role 'LOCAL'");
		else
			return Optional.empty();
	}
}
