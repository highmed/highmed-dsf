package org.highmed.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.authentication.UserProvider;
import org.highmed.fhir.webservice.specification.BasicService;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractServiceSecure<R extends DomainResource, S extends BasicService<R>> implements BasicService<R>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceSecure.class);

	protected final S delegate;

	protected UserProvider provider;

	public AbstractServiceSecure(S delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void setUserProvider(UserProvider provider)
	{
		delegate.setUserProvider(provider);

		this.provider = provider;
	}

	@Override
	public String getPath()
	{
		throw new UnsupportedOperationException("implemented by jaxrs service layer");
	}

	@Override
	public Response create(R resource, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.create(resource, uri, headers);
	}

	@Override
	public Response read(String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.read(id, uri, headers);
	}

	@Override
	public Response vread(String id, long version, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.vread(id, version, uri, headers);
	}

	@Override
	public Response update(String id, R resource, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.update(id, resource, uri, headers);
	}

	@Override
	public Response update(R resource, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.update(resource, uri, headers);
	}

	@Override
	public Response delete(String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.delete(id, uri, headers);
	}

	@Override
	public Response delete(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.delete(uri, headers);
	}

	@Override
	public Response search(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.search(uri, headers);
	}

	@Override
	public Response postValidateNew(String validate, Parameters parameters, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.postValidateNew(validate, parameters, uri, headers);
	}

	@Override
	public Response getValidateNew(String validate, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.getValidateNew(validate, uri, headers);
	}

	@Override
	public Response postValidateExisting(String validate, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.postValidateExisting(validate, id, parameters, uri, headers);
	}

	@Override
	public Response getValidateExisting(String validate, String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}'", provider.getCurrentUser().getName());

		return delegate.getValidateExisting(validate, id, uri, headers);
	}
}
