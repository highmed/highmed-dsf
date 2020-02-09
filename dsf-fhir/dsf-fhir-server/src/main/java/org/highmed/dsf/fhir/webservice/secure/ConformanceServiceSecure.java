package org.highmed.dsf.fhir.webservice.secure;

import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.ConformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConformanceServiceSecure extends AbstractServiceSecure<ConformanceService> implements ConformanceService
{
	private static final Logger logger = LoggerFactory.getLogger(ConformanceServiceSecure.class);
	
	public ConformanceServiceSecure(ConformanceService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}

	public Response getMetadata(String mode, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return reasonGetMetadataNotAllowed(mode).map(forbidden("read")).orElse(delegate.getMetadata(mode, uri, headers));
	}

	/**
	 * Override this method for non default behavior. Default: Always allowed.
	 * 
	 * @param mode
	 * @return {@link Optional#empty()} if read(id) allowed
	 */
	private Optional<String> reasonGetMetadataNotAllowed(String mode)
	{
		return Optional.empty();
	}
}
