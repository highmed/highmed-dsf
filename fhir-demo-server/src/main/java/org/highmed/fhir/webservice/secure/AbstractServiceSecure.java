package org.highmed.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.webservice.specification.BasicService;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Parameters;

public class AbstractServiceSecure<R extends DomainResource, S extends BasicService<R>> implements BasicService<R>
{
	protected final S delegate;

	public AbstractServiceSecure(S delegate)
	{
		this.delegate = delegate;
	}

	public Response create(R resource, UriInfo uri, HttpHeaders headers)
	{
		return delegate.create(resource, uri, headers);
	}

	public Response read(String id, UriInfo uri, HttpHeaders headers)
	{
		return delegate.read(id, uri, headers);
	}

	public Response vread(String id, long version, UriInfo uri, HttpHeaders headers)
	{
		return delegate.vread(id, version, uri, headers);
	}

	public Response update(String id, R resource, UriInfo uri, HttpHeaders headers)
	{
		return delegate.update(id, resource, uri, headers);
	}

	public Response delete(String id, UriInfo uri, HttpHeaders headers)
	{
		return delegate.delete(id, uri, headers);
	}

	public Response search(UriInfo uri, HttpHeaders headers)
	{
		return delegate.search(uri, headers);
	}

	public Response postValidateNew(String validate, Parameters parameters, UriInfo uri, HttpHeaders headers)
	{
		return delegate.postValidateNew(validate, parameters, uri, headers);
	}

	@Override
	public Response getValidateNew(String validate, UriInfo uri, HttpHeaders headers)
	{
		return delegate.getValidateNew(validate, uri, headers);
	}

	public Response postValidateExisting(String validate, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers)
	{
		return delegate.postValidateExisting(validate, id, parameters, uri, headers);
	}

	@Override
	public Response getValidateExisting(String validate, String id, UriInfo uri, HttpHeaders headers)
	{
		return delegate.getValidateExisting(validate, id, uri, headers);
	}
}
