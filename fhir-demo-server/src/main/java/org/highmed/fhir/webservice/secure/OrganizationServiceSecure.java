package org.highmed.fhir.webservice.secure;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.webservice.specification.OrganizationService;
import org.hl7.fhir.r4.model.Organization;

public class OrganizationServiceSecure extends AbstractServiceSecure<Organization, OrganizationService>
		implements OrganizationService
{
	public OrganizationServiceSecure(OrganizationService delegate)
	{
		super(delegate);
	}

	@Override
	public Response create(Organization resource, UriInfo uri)
	{
		// check organization not existing if contains identifier with identifier.system
		// http://highmed.org/fhir/NamingSystem/certificate-thumbprint-hexwith
		// and same identifier.value
		// no two organizations can have the same certificate thumb-print

		// TODO Auto-generated method stub
		return super.create(resource, uri);
	}

	@Override
	public Response update(String id, Organization resource, UriInfo uri)
	{
		// see create, no two organizations can have the same certificate thumb-print

		// TODO Auto-generated method stub
		return super.update(id, resource, uri);
	}
}
