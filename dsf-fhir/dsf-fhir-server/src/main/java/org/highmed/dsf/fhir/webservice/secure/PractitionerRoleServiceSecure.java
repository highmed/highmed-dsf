package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.PractitionerRoleService;
import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleServiceSecure extends AbstractResourceServiceSecure<PractitionerRole, PractitionerRoleService>
		implements PractitionerRoleService
{
	public PractitionerRoleServiceSecure(PractitionerRoleService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
