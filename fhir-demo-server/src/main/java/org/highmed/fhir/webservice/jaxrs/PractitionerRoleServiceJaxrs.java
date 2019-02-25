package org.highmed.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.fhir.webservice.specification.PractitionerRoleService;
import org.hl7.fhir.r4.model.PractitionerRole;

@Path(PractitionerRoleServiceJaxrs.PATH)
public class PractitionerRoleServiceJaxrs extends AbstractServiceJaxrs<PractitionerRole, PractitionerRoleService>
		implements PractitionerRoleService
{
	public static final String PATH = "PractitionerRole";

	public PractitionerRoleServiceJaxrs(PractitionerRoleService delegate)
	{
		super(delegate);
	}

	@Override
	public String getPath()
	{
		return PATH;
	}
}
