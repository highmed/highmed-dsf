package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.PractitionerRoleService;
import org.hl7.fhir.r4.model.PractitionerRole;

@Path(PractitionerRoleServiceJaxrs.PATH)
public class PractitionerRoleServiceJaxrs extends
		AbstractResourceServiceJaxrs<PractitionerRole, PractitionerRoleService> implements PractitionerRoleService
{
	public static final String PATH = "PractitionerRole";

	public PractitionerRoleServiceJaxrs(PractitionerRoleService delegate)
	{
		super(delegate);
	}
}
