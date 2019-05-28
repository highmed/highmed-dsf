package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.webservice.specification.PractitionerRoleService;
import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleServiceSecure extends AbstractServiceSecure<PractitionerRole, PractitionerRoleService>
		implements PractitionerRoleService
{
	public PractitionerRoleServiceSecure(PractitionerRoleService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
