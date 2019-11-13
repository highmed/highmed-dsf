package org.highmed.dsf.fhir.webservice.secure;

import java.util.Optional;
import java.util.function.Function;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.BasicService;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractServiceSecure<R extends Resource, S extends BasicService<R>> implements BasicService<R>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceSecure.class);

	protected final S delegate;
	protected final ResponseGenerator responseGenerator;

	protected UserProvider provider;

	public AbstractServiceSecure(S delegate, ResponseGenerator responseGenerator)
	{
		this.delegate = delegate;
		this.responseGenerator = responseGenerator;
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
		return delegate.getPath();
	}

	private Function<String, Response> forbidden(String operation)
	{
		return reason -> responseGenerator.forbiddenNotAllowed(operation, provider.getCurrentUser(), reason);
	}

	@Override
	public Response create(R resource, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return reasonCreateNotAllowed(resource).map(forbidden("create"))
				.orElse(delegate.create(resource, uri, headers));
	}

	/**
	 * Override this method for non default behavior. Default: Not allowed if user role not {@link UserRole#LOCAL}.
	 * 
	 * @param resource
	 * @return {@link Optional#empty()} if create(resource) allowed
	 */
	protected Optional<String> reasonCreateNotAllowed(R resource)
	{
		if (!UserRole.LOCAL.equals(provider.getCurrentUser().getRole()))
			return Optional.of("Missing role 'LOCAL'");
		else
			return Optional.empty();
	}

	@Override
	public Response read(String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return reasonReadNotAllowed(id).map(forbidden("read")).orElse(delegate.read(id, uri, headers));
	}

	/**
	 * Override this method for non default behavior. Default: Always allowed.
	 * 
	 * @param id
	 * @return {@link Optional#empty()} if read(id) allowed
	 */
	protected Optional<String> reasonReadNotAllowed(String id)
	{
		return Optional.empty();
	}

	@Override
	public Response vread(String id, long version, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return reasonReadNotAllowed(id, version).map(forbidden("read"))
				.orElse(delegate.vread(id, version, uri, headers));
	}

	/**
	 * Override this method for non default behavior. Default: Always allowed.
	 * 
	 * @param id
	 * @param version
	 * @return {@link Optional#empty()} if read(id, version) allowed
	 */
	protected Optional<String> reasonReadNotAllowed(String id, long version)
	{
		return Optional.empty();
	}

	@Override
	public Response update(String id, R resource, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return reasonUpdateNotAllowed(id, resource).map(forbidden("update"))
				.orElse(delegate.update(id, resource, uri, headers));
	}

	/**
	 * Override this method for non default behavior. Default: Not allowed if user role not {@link UserRole#LOCAL}.
	 * 
	 * @param id
	 * @param resource
	 * @return {@link Optional#empty()} if update(id, resource) allowed
	 */
	protected Optional<String> reasonUpdateNotAllowed(String id, R resource)
	{
		if (!UserRole.LOCAL.equals(provider.getCurrentUser().getRole()))
			return Optional.of("Missing role 'LOCAL'");
		else
			return Optional.empty();
	}

	@Override
	public Response update(R resource, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return reasonUpdateNotAllowed(resource, uri).map(forbidden("update"))
				.orElse(delegate.update(resource, uri, headers));
	}

	/**
	 * Override this method for non default behavior. Default: Not allowed if user role not {@link UserRole#LOCAL}.
	 * 
	 * @param resource
	 * @param uri
	 * @return {@link Optional#empty()} if update(resource, uri) allowed
	 */
	protected Optional<String> reasonUpdateNotAllowed(R resource, UriInfo uri)
	{
		if (!UserRole.LOCAL.equals(provider.getCurrentUser().getRole()))
			return Optional.of("Missing role 'LOCAL'");
		else
			return Optional.empty();
	}

	@Override
	public Response delete(String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return reasonDeleteNotAllowed(id).map(forbidden("delete")).orElse(delegate.delete(id, uri, headers));
	}

	/**
	 * Override this method for non default behavior. Default: Not allowed if user role not {@link UserRole#LOCAL}.
	 * 
	 * @param id
	 * @return {@link Optional#empty()} if delete(id) allowed
	 */
	protected Optional<String> reasonDeleteNotAllowed(String id)
	{
		if (!UserRole.LOCAL.equals(provider.getCurrentUser().getRole()))
			return Optional.of("Missing role 'LOCAL'");
		else
			return Optional.empty();
	}

	@Override
	public Response delete(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return reasonDeleteNotAllowed(uri).map(forbidden("delete")).orElse(delegate.delete(uri, headers));
	}

	/**
	 * Override this method for non default behavior. Default: Not allowed if user role not {@link UserRole#LOCAL}.
	 * 
	 * @param uri
	 * @return {@link Optional#empty()} if delete(uri) allowed
	 */
	protected Optional<String> reasonDeleteNotAllowed(UriInfo uri)
	{
		if (!UserRole.LOCAL.equals(provider.getCurrentUser().getRole()))
			return Optional.of("Missing role 'LOCAL'");
		else
			return Optional.empty();
	}

	@Override
	public Response search(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.search(uri, headers);
	}

	@Override
	public Response postValidateNew(String validate, Parameters parameters, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.postValidateNew(validate, parameters, uri, headers);
	}

	@Override
	public Response getValidateNew(String validate, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.getValidateNew(validate, uri, headers);
	}

	@Override
	public Response postValidateExisting(String validate, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.postValidateExisting(validate, id, parameters, uri, headers);
	}

	@Override
	public Response getValidateExisting(String validate, String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.getValidateExisting(validate, id, uri, headers);
	}
}
