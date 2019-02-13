package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.OrganizationDao;
import org.hl7.fhir.r4.model.Organization;

@Path(OrganizationService.RESOURCE_TYPE_NAME)
public class OrganizationService extends AbstractService<OrganizationDao, Organization>
{
	public static final String RESOURCE_TYPE_NAME = "Organization";

	public OrganizationService(String serverBase, int defaultPageCount, OrganizationDao organizationDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, organizationDao, withSearchParameters());
	}
}
