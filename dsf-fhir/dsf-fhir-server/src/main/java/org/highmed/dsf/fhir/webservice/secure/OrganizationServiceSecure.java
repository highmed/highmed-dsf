package org.highmed.dsf.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.OrganizationService;
import org.hl7.fhir.r4.model.Organization;

public class OrganizationServiceSecure extends AbstractServiceSecure<Organization, OrganizationService>
		implements OrganizationService
{
	public OrganizationServiceSecure(OrganizationService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}

	@Override
	public Response create(Organization resource, UriInfo uri, HttpHeaders headers)
	{
		// check organization not existing if contains identifier with identifier.system (or extension)
		// http://highmed.org/fhir/NamingSystem/certificate-thumbprint-hex with same identifier.value
		// no two organizations can have the same certificate thumb-print

		// TODO Auto-generated method stub
		return super.create(resource, uri, headers);
	}

	@Override
	public Response update(String id, Organization resource, UriInfo uri, HttpHeaders headers)
	{
		// see create, no two organizations can have the same certificate thumb-print

		// TODO Auto-generated method stub
		return super.update(id, resource, uri, headers);
	}

	@Override
	public Response update(Organization resource, UriInfo uri, HttpHeaders headers)
	{
		// see create, no two organizations can have the same certificate thumb-print

		// TODO Auto-generated method stub
		return super.update(resource, uri, headers);
	}
}
