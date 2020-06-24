package org.highmed.dsf.fhir.webservice.secure;

import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.RootService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootServiceSecure extends AbstractServiceSecure<RootService> implements RootService
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourceServiceSecure.class);

	private final AuthorizationRule<Resource> authorizationRule;

	public RootServiceSecure(RootService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, AuthorizationRule<Resource> authorizationRule)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver);

		this.authorizationRule = authorizationRule;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(authorizationRule, "authorizationRule");
	}

	@Override
	public Response root(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		// get root allowed for all authenticated users

		return delegate.root(uri, headers);
	}

	@Override
	public Response handleBundle(Bundle bundle, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Optional<String> reasonHandleBundleAllowed = reasonHandleBundleAllowed(bundle);

		if (reasonHandleBundleAllowed.isEmpty())
		{
			audit.info("Handling of transaction and batch bundles denied for user '{}'", getCurrentUser().getName());
			return forbidden("bundle");
		}
		else
		{
			audit.info("Handling of transaction or batch bundle allowed for user '{}': {}", getCurrentUser().getName(),
					reasonHandleBundleAllowed.get());
			return delegate.handleBundle(bundle, uri, headers);
		}
	}

	private Optional<String> reasonHandleBundleAllowed(Bundle bundle)
	{
		if (BundleType.BATCH.equals(bundle.getType()) || BundleType.TRANSACTION.equals(bundle.getType()))
		{
			logger.info(
					"Handling of batch or transaction bundles generaly allowed for all, entries will be individualy evaluated");
			return Optional.of("Allowed for all, entries individualy evaluated");
		}
		else
		{
			logger.warn("Handling bundle denied, not a batch or transaction bundle");
			return Optional.empty();
		}
	}

	@Override
	public Response history(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Optional<String> reasonHistoryAllowed = authorizationRule.reasonHistoryAllowed(getCurrentUser());
		if (reasonHistoryAllowed.isEmpty())
		{
			audit.info("Root History denied for user '{}'", getCurrentUser().getName());
			return forbidden("search");
		}
		else
		{
			audit.info("Root History allowed for user '{}': {}", getCurrentUser().getName(),
					reasonHistoryAllowed.get());
			return delegate.history(uri, headers);
		}
	}
}
