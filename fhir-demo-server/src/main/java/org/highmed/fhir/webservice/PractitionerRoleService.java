package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.PractitionerRoleDao;
import org.hl7.fhir.r4.model.PractitionerRole;

@Path(PractitionerRoleService.RESOURCE_TYPE_NAME)
public class PractitionerRoleService extends AbstractService<PractitionerRoleDao, PractitionerRole>
{
	public static final String RESOURCE_TYPE_NAME = "PractitionerRole";

	public PractitionerRoleService(String serverBase, PractitionerRoleDao practitionerRoleDao)
	{
		super(serverBase, RESOURCE_TYPE_NAME, practitionerRoleDao);
	}
}
